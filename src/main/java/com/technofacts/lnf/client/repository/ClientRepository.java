package com.technofacts.lnf.client.repository;

import java.util.Optional;
import java.util.UUID;

import com.technofacts.lnf.client.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface ClientRepository extends JpaRepository<Client, UUID>, JpaSpecificationExecutor<Client> {

    @Query("select e from Client e where e.clientId = :client_id")
    Optional<Client> findByClientId(@Param("client_id") String client_id);

}
