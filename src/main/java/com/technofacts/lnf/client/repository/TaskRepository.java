package com.technofacts.lnf.client.repository;

import java.util.List;
import java.util.UUID;

import com.technofacts.lnf.client.model.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface TaskRepository extends JpaRepository<Task, UUID>, JpaSpecificationExecutor<Task> {


    List<Task> findByProjectId(UUID project_id);

    List<Task> findByProjectId(UUID project_id, Sort sort);

    Page<Task> findByProjectId(UUID projectId, Pageable pageable);

    @Query(value = "SELECT EXTRACT(YEAR FROM t.start_date) AS year, " +
            " TO_CHAR(t.start_date, 'MON') AS month, status, COUNT(t.id) AS count " +
            " FROM task AS t GROUP BY year, month, status ORDER BY year, status DESC", nativeQuery = true)
    List<StatisticsSummary> tasksByYearAndStatus();

}
