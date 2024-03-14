package com.technofacts.lnf.client.repository;

import com.technofacts.lnf.client.model.Project;
import com.technofacts.lnf.client.model.ProjectTaskEmployee;
import com.technofacts.lnf.client.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectTaskEmployeeRepository extends JpaRepository<ProjectTaskEmployee, UUID>, JpaSpecificationExecutor<ProjectTaskEmployee> {

    Optional<ProjectTaskEmployee> findByProjectAndTaskAndEmployeeId(Project project, Task task, String employeeId);

    List<ProjectTaskEmployee> findByProjectAndTask(Project project, Task task);

    List<ProjectTaskEmployee> findByProjectAndEmployeeId(Project project, String employeeId);

    List<ProjectTaskEmployee> findByTask(Task task);

}
