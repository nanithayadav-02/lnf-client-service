package com.technofacts.lnf.client.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.technofacts.lnf.client.model.Client;
import com.technofacts.lnf.client.model.Gst;
import com.technofacts.lnf.client.model.Project;
import com.technofacts.lnf.client.model.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface ProjectRepository extends JpaRepository<Project, UUID>, JpaSpecificationExecutor<Project> {

    @Query("SELECT p FROM Project p WHERE p.client.id= :client_id ORDER BY p.startDate ASC")
    List<Project> findByClientId(@Param("client_id") UUID client_id);

    @Query(value = "select extract(year from p.start_date) as year, " +
            " to_char(p.start_date, 'MON') as month, count(p.id) as count "
            + " from project as p group by year, month order by year desc", nativeQuery = true)
    List<StatisticsSummary> projectsByYearAndMonth();

}
