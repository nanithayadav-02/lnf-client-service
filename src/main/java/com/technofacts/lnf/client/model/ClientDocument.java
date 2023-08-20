package com.technofacts.lnf.client.model;

import com.technofacts.lnf.client.model.enums.DocumentType;
import com.technofacts.lnf.model.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

@ToString
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "client_document", uniqueConstraints = @UniqueConstraint(columnNames = {"client_id", "type"}))
public class ClientDocument extends AuditableEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(nullable = false, name = "type")
    @Enumerated(EnumType.STRING)
    private DocumentType type;

    @Column(name = "size", nullable = false)
    private Long size;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Lob
    @Column(name = "content", nullable = false)
    private byte[] content;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", referencedColumnName="id", nullable = false)
    private Client client;

}
