package com.technofacts.lnf.client.repository;


import java.util.Optional;
import java.util.UUID;

import com.technofacts.lnf.client.model.Escalation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EscalationRepository extends JpaRepository<Escalation, UUID> {

    @Query("SELECT es FROM Escalation es WHERE es.client.id = :client_id")
    Optional<Escalation> findByClientId(@Param("client_id") UUID client_id);

}