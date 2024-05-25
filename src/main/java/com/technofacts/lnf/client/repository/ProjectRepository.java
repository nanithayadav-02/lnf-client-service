package com.technofacts.lnf.client.repository;

import com.technofacts.lnf.client.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface ProjectRepository extends JpaRepository<Project, UUID>, JpaSpecificationExecutor<Project> {

    @Query("SELECT p FROM Project p WHERE p.client.id= :client_id ORDER BY p.startDate ASC")
    List<Project> findByClientId(@Param("client_id") UUID client_id);

    @Query(value = "select extract(year from p.start_date) as year, " +
            " to_char(p.start_date, 'MON') as month, count(p.id) as count "
            + " from project as p group by year, month order by year desc", nativeQuery = true)
    List<StatisticsSummary> projectsByYearAndMonth();

    @Query(value = "SELECT pe.employee_id FROM project_employee pe JOIN project p ON pe.project_id = p.id" +
            " WHERE p.client_id = :clientId", nativeQuery = true)
    List<String> findEmployeeIdsByClientId(@Param("clientId") UUID clientId);

    @Query(value = "SELECT pe.project_id FROM project_employee pe JOIN project p ON pe.project_id = p.id" +
            " WHERE p.client_id = :clientId", nativeQuery = true)
    List<UUID> findProjectIdsByClientId(@Param("clientId") UUID clientId);

    @Query(value = "select p from Project p where p.id = :id And p.client.id = :clientId")
    Optional<Project> findByProjectIdAndClientId(@Param("id") UUID id, @Param("clientId") UUID clientId);

}
