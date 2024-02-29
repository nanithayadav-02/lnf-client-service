package com.technofacts.lnf.client.model;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

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
@Table(name = "client")
public class Client extends AuditableEntity {

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "pan")
    private String pan;

    @Column(name = "tan")
    private String tan;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "working_from", nullable = false)
    private LocalDate workingFrom;

    @Column(name = "agreement_expiry_date")
    private LocalDate agreementExpiryDate;

    @Column(name = "service_type")
    private String serviceType;

    @Column(name = "client_details", nullable = false)
    private String clientDetails;

    @ToString.Exclude
    @OneToOne(mappedBy = "client", cascade = CascadeType.ALL)
    private ClientAddress clientAddress;

    @ToString.Exclude
    @Builder.Default
    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL)
    private Set<ClientContact> clientContacts = new HashSet<>();

    @ToString.Exclude
    @Builder.Default
    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL)
    private Set<Escalation> escalations = new HashSet<>();

    @ToString.Exclude
    @Builder.Default
    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL)
    private Set<Gst> gst = new HashSet<>();

    @ToString.Exclude
    @Builder.Default
    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL)
    private Set<ClientDocument> files = new HashSet<>();

    @ToString.Exclude
    @Builder.Default
    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL)
    private Set<Project> projects = new HashSet<>();

    @ToString.Exclude
    @Builder.Default
    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL)
    private Set<ClientNotes> notes = new HashSet<>();

}