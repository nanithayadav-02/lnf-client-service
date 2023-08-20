package com.technofacts.lnf.client.repository;

import java.util.List;
import java.util.UUID;

import com.technofacts.lnf.client.model.ClientNotes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface ClientNotesRepository extends JpaRepository<ClientNotes, UUID> {

    @Query("select n from ClientNotes n where n.client.id = :id")
    List<ClientNotes> findByClientId(@Param("id") UUID id);

}