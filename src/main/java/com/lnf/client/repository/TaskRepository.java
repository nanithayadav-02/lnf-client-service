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
import java.util.UUID;

import com.lnf.client.model.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;


public interface TaskRepository extends JpaRepository<Task, UUID>, JpaSpecificationExecutor<Task> {


    List<Task> findByProjectId(UUID project_id);

    List<Task> findByProjectId(UUID project_id, Sort sort);

    Page<Task> findByProjectId(UUID projectId, Pageable pageable);

    @Query(value = "SELECT EXTRACT(YEAR FROM t.start_date) AS year, " +
            " TO_CHAR(t.start_date, 'MON') AS month, status, COUNT(t.id) AS count " +
            " FROM task AS t GROUP BY year, month, status ORDER BY year, status DESC", nativeQuery = true)
    List<StatisticsSummary> tasksByYearAndStatus();

}
