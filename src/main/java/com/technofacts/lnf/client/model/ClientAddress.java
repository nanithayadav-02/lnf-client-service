package com.technofacts.lnf.client.model;

import jakarta.persistence.*;
import lombok.*;

@ToString
@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "client_address")
public class ClientAddress extends Address {

    @ToString.Exclude
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="client_id", referencedColumnName="id", nullable = false)
    private Client client;
}
