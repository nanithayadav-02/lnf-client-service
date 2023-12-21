package com.technofacts.lnf.client.repository.specification.client;


import com.technofacts.lnf.client.model.Client;
import com.technofacts.lnf.util.SpecSearchCriteria;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

public class ClientSpecification implements Specification<Client> {

    private SpecSearchCriteria criteria;

    public ClientSpecification(final SpecSearchCriteria criteria) {
        super();
        this.criteria = criteria;
    }

    public SpecSearchCriteria getCriteria() {
        return criteria;
    }

    @Override
    public Predicate toPredicate(Root<Client> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder builder) {
        switch (criteria.getOperation()) {
            case EQUALITY:
                return builder.equal(
                        builder.lower(root.get(criteria.getKey())),
                        criteria.getValue().toString().trim().toLowerCase()
                );
            case NEGATION:
                return builder.notEqual(
                        builder.lower(root.get(criteria.getKey())),
                        criteria.getValue().toString().trim().toLowerCase()
                );
            case GREATER_THAN:
                return builder.greaterThan(root.get(criteria.getKey()), criteria.getValue().toString().trim());
            case LESS_THAN:
                return builder.lessThan(root.get(criteria.getKey()), criteria.getValue().toString().trim());
            case LIKE, CONTAINS:
                return builder.like(
                        builder.lower(root.get(criteria.getKey())),
                        "%" + criteria.getValue().toString().trim().toLowerCase() + "%"
                );
            case STARTS_WITH:
                return builder.like(
                        builder.lower(root.get(criteria.getKey())),
                        criteria.getValue().toString().trim().toLowerCase() + "%"
                );
            case ENDS_WITH:
                return builder.like(
                        builder.lower(root.get(criteria.getKey())),
                        "%" + criteria.getValue().toString().trim().toLowerCase()
                );
            default:
                return null;
        }
    }

}
