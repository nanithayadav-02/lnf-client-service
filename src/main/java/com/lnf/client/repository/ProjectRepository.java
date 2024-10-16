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

import com.lnf.client.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface ProjectRepository extends JpaRepository<Project, UUID>, JpaSpecificationExecutor<Project> {

    @Query("SELECT p FROM Project p WHERE p.client.id= :clientId ORDER BY p.startDate ASC")
    List<Project> findByClientId(@Param("clientId") UUID clientId);

    @Query(value = "select extract(year from p.start_date) as year, " +
            " to_char(p.start_date, 'MON') as month, count(p.id) as count "
            + " from project as p group by year, month order by year desc", nativeQuery = true)
    List<StatisticsSummary> projectsByYearAndMonth();

    @Query(value = "SELECT pe.employeeId FROM ProjectEmployee pe JOIN pe.project p WHERE p.client.id = :clientId")
    List<String> findEmployeeIdsByClientId(@Param("clientId") UUID clientId);

    @Query("SELECT pe.project.id FROM ProjectEmployee pe JOIN pe.project p WHERE p.client.id = :clientId")
    List<UUID> findProjectIdsByClientId(@Param("clientId") UUID clientId);

    @Query(value = "select p from Project p where p.id = :id And p.client.id = :clientId")
    Optional<Project> findByProjectIdAndClientId(@Param("id") UUID id, @Param("clientId") UUID clientId);

}
