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
@Table(name = "client_notes")
public class ClientNotes extends AuditableEntity {

    @Column(name = "description", nullable = false)
    private String description;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="client_id", referencedColumnName="id",  nullable = false)
    private Client client;

}
