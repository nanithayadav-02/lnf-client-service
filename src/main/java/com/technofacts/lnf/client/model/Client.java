package com.technofacts.lnf.client.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

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

    @Column(name = "client_id", nullable = false, unique = true)
    private String clientId;
}