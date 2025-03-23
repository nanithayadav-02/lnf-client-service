/*
 *
 *  * Copyright © 2025 Lever And Fulcrum Solutions (hereinafter referred to as "LNF").
 *  * All rights reserved.
 *  *
 *  * This source code is the proprietary property of LNF
 *  *
 *  * Unauthorized copying, redistribution, or modification of this code,
 *  * via any medium, is strictly prohibited unless expressly authorized
 *  * in writing by LNF.
 *  *
 *  * This code is confidential and intended solely for the use of LNF
 *  * and its authorized personnel.
 *
 */

package com.lnf.client.config;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.Statement;
import org.openrewrite.java.tree.TypeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class EnhancedLoggingRecipe extends Recipe {
    @Override
    public String getDisplayName() {
        return "Enhanced SLF4J logging";
    }

    @Override
    public String getDescription() {
        return "Standardizes SLF4J logging by ensuring proper parameterized logging and adds method entry/exit logging.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new StandardizeLoggingVisitor().andThen(new InsertLoggingVisitor());
    }

    private static class StandardizeLoggingVisitor extends JavaIsoVisitor<ExecutionContext> {
        // Pattern to detect SLF4J logger types
        private final Pattern loggerTypePattern = Pattern.compile("(Logger|Log)$");

        // Pattern to count existing placeholders
        private final Pattern placeholderPattern = Pattern.compile("\\{\\}");

        @Override
        public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
            J.MethodInvocation m = super.visitMethodInvocation(method, ctx);

            // Check if this is a logger method call
            if (isLoggerCall(m)) {
                // First check if there's an exception parameter that needs special handling
                m = handleExceptionParameter(m);

                // Convert string concatenation to parameterized logging
                return fixStringConcatenation(m);
            }

            return m;
        }

        private boolean isLoggerCall(J.MethodInvocation method) {
            if (method.getSelect() == null) {
                return false;
            }

            // Check if the type of the select expression is a Logger
            if (method.getSelect().getType() != null) {
                String typeName = method.getSelect().getType().toString();
                Matcher matcher = loggerTypePattern.matcher(typeName);
                if (matcher.find()) {
                    String methodName = method.getSimpleName();
                    return methodName.equals("trace") ||
                            methodName.equals("debug") ||
                            methodName.equals("info") ||
                            methodName.equals("warn") ||
                            methodName.equals("error");
                }
            }

            // Fallback to the original name-based check
            String selectString = method.getSelect().toString();
            if (!selectString.contains("log") && !selectString.contains("logger")) {
                return false;
            }

            String methodName = method.getSimpleName();
            return methodName.equals("trace") ||
                    methodName.equals("debug") ||
                    methodName.equals("info") ||
                    methodName.equals("warn") ||
                    methodName.equals("error");
        }

        private J.MethodInvocation handleExceptionParameter(J.MethodInvocation method) {
            List<Expression> args = method.getArguments();
            if (args.size() <= 1) {
                return method;
            }

            // Check if the last argument is a Throwable
            Expression lastArg = args.get(args.size() - 1);
            if (lastArg.getType() != null && TypeUtils.isAssignableTo("java.lang.Throwable", lastArg.getType())) {
                // Move the exception to the end if it's not already there
                if (args.size() > 2) {
                    List<Expression> newArgs = new ArrayList<>(args);
                    newArgs.remove(args.size() - 1);

                    // If the first argument is a format string and has placeholders,
                    // make sure we have the right number of arguments
                    if (args.get(0) instanceof J.Literal &&
                            TypeUtils.isString(((J.Literal) args.get(0)).getType())) {
                        String formatStr = ((J.Literal) args.get(0)).getValue().toString();
                        Matcher matcher = placeholderPattern.matcher(formatStr);
                        int placeholderCount = 0;
                        while (matcher.find()) {
                            placeholderCount++;
                        }

                        // If we have too many arguments (including the exception), fix it
                        if (newArgs.size() - 1 > placeholderCount) {
                            // We have extra arguments that need to be part of the message
                            List<Expression> extraArgs = new ArrayList<>(newArgs.subList(placeholderCount + 1, newArgs.size()));
                            newArgs = new ArrayList<>(newArgs.subList(0, placeholderCount + 1));

                            // Add remaining arguments as concatenation to the message
                            StringBuilder newFormat = new StringBuilder(formatStr);
                            for (Expression extra : extraArgs) {
                                newFormat.append(" {}");
                                newArgs.add(extra);
                            }

                            newArgs.set(0, J.Literal.buildString(newFormat.toString()));
                        }
                    }

                    // Add the exception at the end
                    newArgs.add(lastArg);
                    return method.withArguments(newArgs);
                }
            }

            return method;
        }

        private J.MethodInvocation fixStringConcatenation(J.MethodInvocation method) {
            // Only process if we have arguments
            if (method.getArguments().isEmpty()) {
                return method;
            }

            // Check if the first argument contains string concatenation
            Expression firstArg = method.getArguments().get(0);
            if (!(firstArg instanceof J.Binary)) {
                return method;
            }

            J.Binary binary = (J.Binary) firstArg;
            if (binary.getOperator() != J.Binary.Type.Addition) {
                return method;
            }

            // This is a string concatenation in logging - let's fix it
            // Extract the string parts and variables
            StringBuilder formatBuilder = new StringBuilder();
            List<Expression> params = new ArrayList<>();

            extractStringAndParams(binary, formatBuilder, params);

            // If we found any parameters, create a new method invocation
            if (!params.isEmpty()) {
                // Create the format string, preserving existing placeholders
                String formatString = formatBuilder.toString();

                // Build new arguments list
                List<Expression> newArgs = new ArrayList<>();
                newArgs.add(J.Literal.buildString(formatString));
                newArgs.addAll(params);

                // Preserve any exception parameter
                List<Expression> originalArgs = method.getArguments();
                if (originalArgs.size() > 1) {
                    Expression lastArg = originalArgs.get(originalArgs.size() - 1);
                    if (lastArg.getType() != null &&
                            TypeUtils.isAssignableTo("java.lang.Throwable", lastArg.getType())) {
                        newArgs.add(lastArg);
                    }
                }

                // Create the new method invocation
                return method.withArguments(newArgs);
            }

            return method;
        }

        private void extractStringAndParams(Expression expr, StringBuilder formatBuilder, List<Expression> params) {
            if (expr instanceof J.Literal && TypeUtils.isString(((J.Literal) expr).getType())) {
                // It's a string literal
                String value = ((J.Literal) expr).getValue().toString();
                formatBuilder.append(value);
            } else if (expr instanceof J.Binary && ((J.Binary) expr).getOperator() == J.Binary.Type.Addition) {
                // It's a binary addition
                J.Binary binary = (J.Binary) expr;
                extractStringAndParams(binary.getLeft(), formatBuilder, params);
                extractStringAndParams(binary.getRight(), formatBuilder, params);
            } else {
                // It's a variable or expression to be parameterized
                formatBuilder.append("{}");
                params.add(expr);
            }
        }
    }

    private static class InsertLoggingVisitor extends JavaIsoVisitor<ExecutionContext> {
        private static final String LOGGER_FIELD = "private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(#{}#.class);";

        @Override
        public J.ClassDeclaration visitClassDeclaration(J.ClassDeclaration classDecl, ExecutionContext ctx) {
            J.ClassDeclaration cd = super.visitClassDeclaration(classDecl, ctx);

            // Skip if the class already has a logger field
            if (hasLoggerField(cd)) {
                return cd;
            }

            // Add logger field to the class
            JavaTemplate template = JavaTemplate.builder(LOGGER_FIELD)
                    .contextSensitive()
                    .build();

            return template.apply(
                    updateCursor(cd),
                    cd.getSimpleName());
        }

        @Override
        public J.MethodDeclaration visitMethodDeclaration(J.MethodDeclaration method, ExecutionContext ctx) {
            J.MethodDeclaration md = super.visitMethodDeclaration(method, ctx);

            // Skip if method is private, a constructor, or already has logging
            if (md.hasModifier(J.Modifier.Type.PRIVATE) ||
                    md.getReturnTypeExpression() == null ||
                    hasLoggingStatements(md)) {
                return md;
            }

            // For methods with a body
            if (md.getBody() != null) {
                return addLoggingToMethod(md);
            }

            return md;
        }

        private boolean hasLoggerField(J.ClassDeclaration classDecl) {
            return classDecl.getBody().getStatements().stream()
                    .filter(statement -> statement instanceof J.VariableDeclarations)
                    .map(statement -> (J.VariableDeclarations) statement)
                    .anyMatch(varDecl ->
                            varDecl.getVariables().stream()
                                    .anyMatch(var ->
                                            var.getSimpleName().contains("log") ||
                                                    var.getSimpleName().contains("logger")));
        }

        private boolean hasLoggingStatements(J.MethodDeclaration method) {
            if (method.getBody() == null) {
                return false;
            }

            // Check if the method body already contains log statements
            return method.getBody().getStatements().stream()
                    .anyMatch(statement -> containsLoggerCall(statement));
        }

        private boolean containsLoggerCall(Statement statement) {
            if (statement instanceof J.MethodInvocation) {
                J.MethodInvocation methodInvocation = (J.MethodInvocation) statement;
                if (methodInvocation.getSelect() != null) {
                    String select = methodInvocation.getSelect().toString();
                    if (select.contains("log") || select.contains("logger")) {
                        return true;
                    }
                }
            } else if (statement instanceof J.Try) {
                J.Try tryStatement = (J.Try) statement;
                return tryStatement.getBody().getStatements().stream().anyMatch(this::containsLoggerCall);
            } else if (statement instanceof J.If) {
                J.If ifStatement = (J.If) statement;
                boolean thenHasLogging = ifStatement.getThenPart().getStatements().stream().anyMatch(this::containsLoggerCall);
                boolean elseHasLogging = ifStatement.getElsePart() != null &&
                        ifStatement.getElsePart().getStatements().stream().anyMatch(this::containsLoggerCall);
                return thenHasLogging || elseHasLogging;
            } else if (statement instanceof J.Block) {
                J.Block block = (J.Block) statement;
                return block.getStatements().stream().anyMatch(this::containsLoggerCall);
            }

            return false;
        }

        private J.MethodDeclaration addLoggingToMethod(J.MethodDeclaration method) {
            J.Block body = method.getBody();
            List<Statement> statements = new ArrayList<>(body.getStatements());

            // Get method name and parameter details for logging
            String methodName = method.getSimpleName();

            // Build entry log statement
            String entryLogTemplate = "log.debug(\"Entering method: " + methodName;
            if (!method.getParameters().isEmpty()) {
                entryLogTemplate += " with parameters: ";
                List<String> paramNames = method.getParameters().stream()
                        .filter(param -> param instanceof J.VariableDeclarations)
                        .map(param -> ((J.VariableDeclarations) param).getVariables().get(0).getSimpleName())
                        .collect(Collectors.toList());

                for (int i = 0; i < paramNames.size(); i++) {
                    entryLogTemplate += paramNames.get(i) + "={}";
                    if (i < paramNames.size() - 1) {
                        entryLogTemplate += ", ";
                    }
                }
            }
            entryLogTemplate += "\")";

            if (!method.getParameters().isEmpty()) {
                List<String> paramRefs = method.getParameters().stream()
                        .filter(param -> param instanceof J.VariableDeclarations)
                        .map(param -> ((J.VariableDeclarations) param).getVariables().get(0).getSimpleName())
                        .collect(Collectors.toList());

                for (String param : paramRefs) {
                    entryLogTemplate += ", " + param;
                }
            }
            entryLogTemplate += ";";

            // Create entry log statement
            JavaTemplate entryTemplate = JavaTemplate.builder(entryLogTemplate)
                    .contextSensitive()
                    .build();

            J.Block newBody = body;
            newBody = entryTemplate.apply(updateCursor(newBody), newBody.getCoordinates().firstStatement());

            // If method returns void, add simple exit log
            if (TypeUtils.isOfClassType(method.getReturnTypeExpression().getType(), "void")) {
                String exitLogTemplate = "log.debug(\"Exiting method: " + methodName + "\");";
                JavaTemplate exitTemplate = JavaTemplate.builder(exitLogTemplate)
                        .contextSensitive()
                        .build();

                newBody = exitTemplate.apply(updateCursor(newBody), newBody.getCoordinates().lastStatement());
            } else {
                // For non-void methods, we need to add try-finally or modify the return statements
                // This example uses a simpler approach of just adding a log before the last statement if it's a return
                List<Statement> bodyStatements = new ArrayList<>(newBody.getStatements());

                if (!bodyStatements.isEmpty() && bodyStatements.get(bodyStatements.size() - 1) instanceof J.Return) {
                    String exitLogTemplate = "log.debug(\"Exiting method: " + methodName + " with return value: {}\", ";
                    J.Return returnStmt = (J.Return) bodyStatements.get(bodyStatements.size() - 1);

                    if (returnStmt.getExpression() != null) {
                        // If the return expression is simple, log it directly
                        if (returnStmt.getExpression() instanceof J.Identifier ||
                                returnStmt.getExpression() instanceof J.Literal) {
                            exitLogTemplate += returnStmt.getExpression().toString() + ");";
                            JavaTemplate exitTemplate = JavaTemplate.builder(exitLogTemplate)
                                    .contextSensitive()
                                    .build();

                            newBody = exitTemplate.apply(
                                    updateCursor(newBody),
                                    newBody.getCoordinates().lastStatement()
                            );
                        } else {
                            // For complex expressions, just add a generic exit log
                            exitLogTemplate = "log.debug(\"Exiting method: " + methodName + " with return value\");";
                            JavaTemplate exitTemplate = JavaTemplate.builder(exitLogTemplate)
                                    .contextSensitive()
                                    .build();

                            newBody = exitTemplate.apply(
                                    updateCursor(newBody),
                                    newBody.getCoordinates().lastStatement()
                            );
                        }
                    }
                }
            }

            return method.withBody(newBody);
        }
    }
}