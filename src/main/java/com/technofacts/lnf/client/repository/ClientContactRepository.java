package com.technofacts.lnf.client.repository;


import java.util.List;
import java.util.UUID;

import com.technofacts.lnf.client.model.ClientContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClientContactRepository extends JpaRepository<ClientContact, UUID> {

    @Query("SELECT cc FROM ClientContact cc WHERE cc.client.id = :client_id")
    List<ClientContact> findByClientId(@Param("client_id") UUID client_id);

}