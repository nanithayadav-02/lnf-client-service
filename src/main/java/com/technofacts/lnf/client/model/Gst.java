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
@Table(name = "gst", uniqueConstraints = @UniqueConstraint(columnNames = {"client_id", "number"}))
public class Gst extends AuditableEntity {

    @Column(name = "location", nullable = false)
    private String location;

    @Column(name = "number", nullable = false)
    private String number;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="client_id", referencedColumnName="id", nullable = false)
    private Client client;

}
