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

import com.lnf.client.model.ClientDirectory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClientDirectoryRepository extends JpaRepository<ClientDirectory, UUID>, JpaSpecificationExecutor<ClientDirectory> {

    @Query("select cd from ClientDirectory cd where cd.client.id = :clientId")
    List<ClientDirectory> findDirectoriesByClientId(@Param("clientId") UUID clientId);

    @Query("select c from ClientDirectory c where c.email = :email")
    List<ClientDirectory> findByEmail(@Param("email") String email);

    @Query("SELECT cd FROM ClientDirectory cd WHERE cd.client.id = :clientId AND cd.email = :email")
    List<ClientDirectory> findByClientIdAndEmail(@Param("clientId") UUID clientId, @Param("email") String email);

    @Query("SELECT cd FROM ClientDirectory cd WHERE cd.client.id = :clientId")
    List<ClientDirectory> findClientDirectoryByClientId(@Param("clientId") UUID clientId);

    @Query("SELECT cd FROM ClientDirectory cd WHERE cd.client.id = :clientId")
    Page<ClientDirectory> findClientDirectoryByClientId(@Param("clientId") UUID clientId, Pageable pageable);

    @Query("select cd from ClientDirectory cd where cd.email = :email")
    Optional<ClientDirectory> findByEmailId(@Param("email") String email);

}
