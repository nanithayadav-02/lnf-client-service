package com.technofacts.lnf.client.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.technofacts.lnf.client.model.Project;
import com.technofacts.lnf.client.model.ProjectEmployee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProjectEmployeeRepository extends JpaRepository<ProjectEmployee, UUID>, JpaSpecificationExecutor<ProjectEmployee> {

    Optional<ProjectEmployee> findByProjectIdAndEmployeeId(UUID projectId, String employeeId);

    List<ProjectEmployee> findByProject(Project project);

    List<ProjectEmployee> findByEmployeeId(String employeeId);

}
