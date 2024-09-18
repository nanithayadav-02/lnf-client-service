package com.technofacts.lnf.client.repository;

import com.technofacts.lnf.client.model.ClientDirectory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ClientDirectoryRepository extends JpaRepository<ClientDirectory, UUID>, JpaSpecificationExecutor<ClientDirectory> {

    @Query("select cd from ClientDirectory cd where cd.client.id = :clientId")
    List<ClientDirectory> findDirectoriesByClientId(@Param("clientId") UUID clientId);

    @Query("select c from ClientDirectory c where c.email = :email")
    List<ClientDirectory> findByEmail(@Param("email") String email);

    @Query("SELECT cd FROM ClientDirectory cd WHERE cd.client.id = :clientId AND cd.email = :email")
    List<ClientDirectory> findByClientIdAndEmail(@Param("clientId") UUID clientId, @Param("email") String email);

}
