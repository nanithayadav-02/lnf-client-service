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

package com.lnf.client.service;

import com.lnf.dto.client.EmployeeProjectDto;
import com.lnf.dto.client.EmployeeProjectTaskDto;
import com.lnf.dto.client.ProjectTasksDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class EmployeeTaskService {

    private final EmployeeProjectService employeeProjectService;
    private final EmployeeProjectTaskService employeeProjectTaskService;
    private static final String ACTIVE = "Active";

    public Map<String, Object> findTasksByEmployeeId(final String employeeId, int page, int size) {

        EmployeeProjectDto employeeProjectDto = employeeProjectService.findProjectsByEmployeeId(employeeId);
        List<ProjectTasksDto> projectTaskDtos = employeeProjectDto.getProjects().stream()
                .filter(projectDto -> ACTIVE.equalsIgnoreCase(projectDto.getStatus()))
                .map(projectDto -> {
                    ProjectTasksDto projectTaskDto = new ProjectTasksDto();
                    projectTaskDto.setProjectId(projectDto.getId());
                    projectTaskDto.setProjectCode(projectDto.getCode());
                    projectTaskDto.setProjectName(projectDto.getName());
                    projectTaskDto.setProjectType(projectDto.getType());

                    EmployeeProjectTaskDto employeeProjectTaskDto =
                            employeeProjectTaskService.findTasksByEmployeeIdAndProjectId(employeeId, projectDto.getId());

                    if (!CollectionUtils.isEmpty(employeeProjectTaskDto.getTasks())) {
                        projectTaskDto.setTasks(employeeProjectTaskDto.getTasks());
                    }
                    return projectTaskDto;
                })
                .toList();

        Map<String, Object> paginatedResult = applyPagination(projectTaskDtos, page, size);

        Map<String, Object> result = new HashMap<>();
        result.put("employeeId", employeeId);
        result.put("projectTasks", paginatedResult.get("data"));
        result.put("totalPages", paginatedResult.get("totalPages"));
        result.put("totalElements", paginatedResult.get("totalElements"));
        return result;
    }

    private Map<String, Object> applyPagination(List<ProjectTasksDto> projectTaskDtos, int page, int size) {
        int totalElements = projectTaskDtos.size();
        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, totalElements);

        List<ProjectTasksDto> paginatedTasks = projectTaskDtos.subList(
                Math.min(startIndex, totalElements),
                endIndex);

        Map<String, Object> result = new HashMap<>();
        result.put("data", paginatedTasks);
        result.put("totalElements", totalElements);
        result.put("totalPages", (int) Math.ceil((double) totalElements / size));
        return result;
    }


}
