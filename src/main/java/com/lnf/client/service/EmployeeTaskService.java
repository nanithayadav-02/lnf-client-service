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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

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

        List<Map<String, Object>> allTasks = employeeProjectDto.getProjects().stream()
                .filter(projectDto -> ACTIVE.equalsIgnoreCase(projectDto.getStatus()))
                .flatMap(projectDto -> {
                    EmployeeProjectTaskDto employeeProjectTaskDto =
                            employeeProjectTaskService.findTasksByEmployeeIdAndProjectId(employeeId, projectDto.getId());

                    if (!CollectionUtils.isEmpty(employeeProjectTaskDto.getTasks())) {
                        return employeeProjectTaskDto.getTasks().stream()
                                .map(task -> {
                                    Map<String, Object> taskMap = new HashMap<>();
                                    taskMap.put("id", task.getId());
                                    taskMap.put("name", task.getName());
                                    taskMap.put("type", task.getType());
                                    taskMap.put("status", task.getStatus());
                                    taskMap.put("description", task.getDescription());
                                    taskMap.put("startDate", task.getStartDate());
                                    taskMap.put("endDate", task.getEndDate());
                                    taskMap.put("projectId", projectDto.getId());
                                    taskMap.put("projectCode", projectDto.getCode());
                                    taskMap.put("projectName", projectDto.getName());
                                    taskMap.put("projectType", projectDto.getType());
                                    return taskMap;
                                });
                    }
                    return Stream.empty();
                })
                .toList();
        Map<String, Object> paginatedResult = applyPagination(allTasks, page, size);
        Map<String, Object> result = new HashMap<>();
        result.put("employeeId", employeeId);
        result.put("tasks", paginatedResult.get("data"));
        result.put("totalPages", paginatedResult.get("totalPages"));
        result.put("totalElements", paginatedResult.get("totalElements"));
        return result;
    }

    private Map<String, Object> applyPagination(List<Map<String, Object>> tasks, int page, int size) {
        int totalElements = tasks.size();
        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, totalElements);

        List<Map<String, Object>> paginatedTasks = tasks.subList(
                Math.min(startIndex, totalElements),
                endIndex);

        Map<String, Object> result = new HashMap<>();
        result.put("data", paginatedTasks);
        result.put("totalElements", totalElements);
        result.put("totalPages", (int) Math.ceil((double) totalElements / size));
        return result;
    }

}
