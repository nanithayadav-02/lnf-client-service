package com.technofacts.lnf.client.repository;

import com.technofacts.lnf.client.model.StatementOfWork;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface StatementOfWorkRepository extends JpaRepository<StatementOfWork, UUID> {

    @Query("SELECT sw FROM StatementOfWork sw WHERE sw.fileName = :fileName")
    Optional<StatementOfWork> findByFileName(@Param("fileName") String fileName);

}
