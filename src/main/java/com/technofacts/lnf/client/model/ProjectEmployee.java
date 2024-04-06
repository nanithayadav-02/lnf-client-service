package com.technofacts.lnf.client.model;

import com.technofacts.lnf.model.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

@ToString
@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "project_employee", uniqueConstraints = @UniqueConstraint(columnNames = {"project_id", "employee_id"}))
public class ProjectEmployee extends AuditableEntity {

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", referencedColumnName="id", nullable = false)
    private Project project;

    @Column(name = "employee_id", nullable = false)
    private String employeeId;
}
