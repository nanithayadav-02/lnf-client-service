package com.technofacts.lnf.client.model;

import javax.persistence.*;

import com.technofacts.lnf.model.AuditableEntity;
import lombok.*;

@ToString
@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "project_task_employee", uniqueConstraints = @UniqueConstraint(columnNames = {"project_id", "task_id", "employee_id"}))
public class ProjectTaskEmployee extends AuditableEntity {

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", referencedColumnName="id", nullable = false)
    private Project project;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", referencedColumnName="id", nullable = false)
    private Task task;

    @Column(name = "employee_id", nullable = false)
    private String employeeId;
}
