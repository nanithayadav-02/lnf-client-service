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
import com.lnf.client.model.ProjectEmployee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectEmployeeRepository extends JpaRepository<ProjectEmployee, UUID>, JpaSpecificationExecutor<ProjectEmployee> {

    Optional<ProjectEmployee> findByProjectIdAndEmployeeId(UUID projectId, String employeeId);

    List<ProjectEmployee> findByProject(Project project);

    List<ProjectEmployee> findByEmployeeId(String employeeId);

    List<ProjectEmployee> findAllByProjectId(UUID projectId);

    boolean existsByProjectId(UUID projectId);
}
