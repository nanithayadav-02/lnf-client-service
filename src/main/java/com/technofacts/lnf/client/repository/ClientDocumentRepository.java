package com.technofacts.lnf.client.repository;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.technofacts.lnf.client.model.ClientDocument;
import com.technofacts.lnf.client.model.enums.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClientDocumentRepository extends JpaRepository<ClientDocument, UUID> {

    @Query("SELECT cd FROM ClientDocument cd WHERE cd.client.id = :client_id AND cd.type = :type")
    Optional<ClientDocument> findByClientIdAndType(@Param("client_id") UUID client_id, @Param("type") DocumentType type);

    @Query("SELECT cd FROM ClientDocument cd WHERE cd.client.id = :client_id")
    List<ClientDocument> findByClientId(@Param("client_id") UUID client_id);

}