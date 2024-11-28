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
import com.lnf.client.model.ProjectTaskEmployee;
import com.lnf.client.model.Task;
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

    List<ProjectTaskEmployee> findByProjectId(UUID projectId);

}
