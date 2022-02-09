package com.technofacts.lnf.client.model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import com.technofacts.lnf.model.AuditableEntity;

import lombok.*;

@ToString
@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "project")
public class Project extends AuditableEntity {

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "purchase_order", nullable = false)
    private String purchaseOrder;

    @Column(name = "budget_terms")
    private String budgetTerms;

    @Column(name = "status")
    private String status;

    @Column(name = "currency", nullable = false)
    private String currency;

    @Column(name = "budget")
    private BigDecimal budget;

    @Column(name = "hours_per_day")
    private Integer hoursPerDay;

    @Column(name = "billing_term", nullable = false)
    private String billingTerm;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", referencedColumnName="id", nullable = false)
    private Client client;

    @ToString.Exclude
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    private Set<Task> tasks = new HashSet<>();

}
