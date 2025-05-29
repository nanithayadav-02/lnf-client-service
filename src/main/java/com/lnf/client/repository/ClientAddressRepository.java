/*
 *
 *  * Copyright © 2024 Lever And Fulcrum Solutions (hereinafter referred to as "LNF").
 *  * All rights reserved.
 *  *
 *  * This source code is the proprietary property of LNF
 *  *
 *  * Unauthorized copying, redistribution, or modification of this code,
 *  * via any medium, is strictly prohibited unless expressly authorized
 *  * in writing by LNF.
 *  *
 *  * This code is confidential and intended solely for the use of LNF
 *  * and its authorized personnel.
 *
 */

package com.lnf.client.repository;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.lnf.client.model.ClientAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClientAddressRepository extends JpaRepository<ClientAddress, UUID> {

    @Query("SELECT ad FROM ClientAddress ad WHERE ad.client.id = :client_id")
    Optional<ClientAddress> findByClientId(@Param("client_id") UUID clientId);

    @Query("SELECT ad FROM ClientAddress ad WHERE ad.client.id = :client_id")
    List<ClientAddress> findAddressByClientId(@Param("client_id") UUID clientId);

}