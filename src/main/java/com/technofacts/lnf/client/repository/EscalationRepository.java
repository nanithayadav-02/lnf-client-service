package com.technofacts.lnf.client.repository;


import com.technofacts.lnf.client.model.Escalation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface EscalationRepository extends JpaRepository<Escalation, UUID> {

    @Query("SELECT es FROM Escalation es WHERE es.client.id = :client_id")
    List<Escalation> findByClientId(@Param("client_id") UUID client_id);

}