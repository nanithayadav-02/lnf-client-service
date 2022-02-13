package com.technofacts.lnf.client.repository.specification.project;

import java.util.ArrayList;
import java.util.List;

import com.technofacts.lnf.client.model.Project;
import com.technofacts.lnf.util.SearchOperation;
import com.technofacts.lnf.util.SpecSearchCriteria;
import org.springframework.data.jpa.domain.Specification;

public class ProjectSpecificationBuilder {

    private final List<SpecSearchCriteria> params;

    public ProjectSpecificationBuilder() {
        this.params = new ArrayList<>();
    }

    public final ProjectSpecificationBuilder with(final String key, final String operation, final Object value) {
        return with(null, key, operation, value, null, null);
    }

    public final ProjectSpecificationBuilder with(final String orPredicate, final String key, final String operation,
                                                  final Object value, final String prefix, final String suffix) {
        SearchOperation op = SearchOperation.getSimpleOperation(operation.charAt(0));
        if (op != null) {
            if (op == SearchOperation.EQUALITY) {
                final boolean startWithAsterisk = prefix != null && prefix.contains(SearchOperation.ZERO_OR_MORE_REGEX);
                final boolean endWithAsterisk = suffix != null && suffix.contains(SearchOperation.ZERO_OR_MORE_REGEX);

                if (startWithAsterisk && endWithAsterisk) {
                    op = SearchOperation.CONTAINS;
                } else if (startWithAsterisk) {
                    op = SearchOperation.ENDS_WITH;
                } else if (endWithAsterisk) {
                    op = SearchOperation.STARTS_WITH;
                }
            }
            params.add(new SpecSearchCriteria(orPredicate, key, op, value));
        }
        return this;
    }

    public final ProjectSpecificationBuilder with(ProjectSpecification spec) {
        params.add(spec.getCriteria());
        return this;
    }

    public final ProjectSpecificationBuilder with(SpecSearchCriteria criteria) {
        params.add(criteria);
        return this;
    }

    public Specification<Project> build() {
        if (params.size() == 0)
            return null;

        Specification<Project> result = new ProjectSpecification(params.get(0));

        for (int i = 1; i < params.size(); i++) {
            result = params.get(i).isOrPredicate()
                    ? Specification.where(result).or(new ProjectSpecification(params.get(i)))
                    : Specification.where(result).and(new ProjectSpecification(params.get(i)));
        }

        return result;
    }

}