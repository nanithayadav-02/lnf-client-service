package com.technofacts.lnf.client.model;

import com.technofacts.lnf.client.model.enums.Status;
import com.technofacts.lnf.model.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@ToString
@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "statement_of_work")
public class StatementOfWork extends AuditableEntity {

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="project_id", referencedColumnName="id",  nullable = false)
    private Project project;

}
