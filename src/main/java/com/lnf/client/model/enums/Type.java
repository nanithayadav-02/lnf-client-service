package com.lnf.client.model.enums;

public enum Type {

    REVENUE("Revenue"),
    EXPENSE("Expense"),
    ASSET("Asset"),
    LIABILITY("Liability"),
    EQUITY("Equity"),
    OTHER_INCOME("Other Income");

    private final String label;

    Type(String label) {
        this.label = label;
    }

    public static Type valueOfLabel(String label) {
        for (Type at : values()) {
            if (at.label.equals(label)) {
                return at;
            }
        }
        return null;
    }

}
