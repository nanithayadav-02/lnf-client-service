package com.technofacts.lnf.client.repository;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.technofacts.lnf.client.model.ClientAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClientAddressRepository extends JpaRepository<ClientAddress, UUID> {

    @Query("SELECT ad FROM ClientAddress ad WHERE ad.client.id = :client_id")
    Optional<ClientAddress> findByClientId(@Param("client_id") UUID client_id);

    @Query("SELECT ad FROM ClientAddress ad WHERE ad.client.id = :client_id")
    List<ClientAddress> findAddressByClientId(@Param("client_id") UUID client_id);

}