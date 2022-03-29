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
@Table(name = "escalation")
public class Escalation extends AuditableEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Email
    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "mobile_number", nullable = false)
    private String mobileNumber;

    @ToString.Exclude
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="client_id", referencedColumnName="id", nullable = false)
    private Client client;

}
