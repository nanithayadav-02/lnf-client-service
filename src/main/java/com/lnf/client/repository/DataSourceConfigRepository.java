package com.lnf.client.repository;

import com.lnf.tenant.core.model.DataSourceConfig;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface DataSourceConfigRepository extends JpaRepository<DataSourceConfig, UUID> {

    @Query("select ds from DataSourceConfig ds where ds.name = :tenantId")
    DataSourceConfig findByTenantId(@Param("tenantId") String tenantId);
}
