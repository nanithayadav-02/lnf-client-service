package com.technofacts.lnf.client.repository;

import java.util.List;
import java.util.UUID;

import com.technofacts.lnf.client.model.Gst;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GstRepository extends JpaRepository<Gst, UUID> {

    @Query("SELECT g FROM Gst g WHERE g.client.id = :client_id")
    List<Gst> findByClientId(@Param("client_id") UUID client_id);

}
