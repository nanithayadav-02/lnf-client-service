package com.technofacts.lnf.client.repository;

import java.util.Optional;
import java.util.UUID;

import com.technofacts.lnf.client.model.Project;
import com.technofacts.lnf.client.model.ProjectEmployee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProjectEmployeeRepository extends JpaRepository<ProjectEmployee, UUID>, JpaSpecificationExecutor<ProjectEmployee> {

    Optional<ProjectEmployee> findByProjectCodeAndEmployeeId(UUID projectId, String employeeId);

}
