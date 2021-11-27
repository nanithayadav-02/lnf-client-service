package com.technofacts.lnf.client.repository;

import java.util.List;
import java.util.UUID;

import com.technofacts.lnf.client.model.Project;
import com.technofacts.lnf.client.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface TaskRepository extends JpaRepository<Task, UUID>, JpaSpecificationExecutor<Task> {

    @Query("SELECT t FROM Task t WHERE t.project.id = :project_id")
    List<Task> findByProjectId(@Param("project_id") UUID project_id);

}
