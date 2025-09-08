/*
 *
 *  * Copyright © 2024 Lever And Fulcrum Solutions (hereinafter referred to as "LNF").
 *  * All rights reserved.
 *  *
 *  * This source code is the proprietary property of LNF
 *  *
 *  * Unauthorized copying, redistribution, or modification of this code,
 *  * via any medium, is strictly prohibited unless expressly authorized
 *  * in writing by LNF.
 *  *
 *  * This code is confidential and intended solely for the use of LNF
 *  * and its authorized personnel.
 *
 */

package com.lnf.client.model;

import com.lnf.model.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

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

    @Column(name = "opening_balance")
    private BigDecimal openingBalance;

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

    @Column(name = "upload_time")
    private LocalDateTime uploadTime;

    @Column(name = "category")
    private String category;

    @Column(name = "sub_category")
    private String subCategory;

    @Column(name = "type")
    private String type;

    @ToString.Exclude
    @Builder.Default
    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL)
    private Set<ClientAddress> clientAddresses = new HashSet<>();

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

    @ToString.Exclude
    @Builder.Default
    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL)
    private Set<Agreement> agreements = new HashSet<>();

    @ToString.Exclude
    @Builder.Default
    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL)
    private Set<ClientDirectory> clientDirectories = new HashSet<>();

}