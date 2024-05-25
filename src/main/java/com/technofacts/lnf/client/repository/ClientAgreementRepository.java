package com.technofacts.lnf.client.repository;

import com.technofacts.lnf.client.model.Agreement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ClientAgreementRepository extends JpaRepository<Agreement, UUID> {

    @Query("SELECT a FROM Agreement a WHERE a.fileName = :fileName")
    Optional<Agreement> findByFileName(@Param("fileName") String fileName);
}
