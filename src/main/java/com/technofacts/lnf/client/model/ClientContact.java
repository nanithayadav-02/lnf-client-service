package com.technofacts.lnf.client.model;

import javax.persistence.*;
import javax.validation.constraints.Email;

import com.technofacts.lnf.model.AuditableEntity;
import lombok.*;

@ToString
@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "client_contact", uniqueConstraints = @UniqueConstraint(columnNames = {"client_id", "name", "department"}))
public class ClientContact extends AuditableEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Email
    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "designation", nullable = false)
    private String designation;

    @Column(name = "department", nullable = false)
    private String department;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="client_id", referencedColumnName="id", nullable = false)
    private Client client;

}
