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

package com.lnf.client.controller;

import com.lnf.client.service.ProjectTaskEmployeeService;
import com.lnf.dto.client.ProjectTaskEmployeeDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/lnf")
@Slf4j
public class ProjectTaskEmployeeController {

    private final ProjectTaskEmployeeService service;
    private static final String CLIENT_SERVICE = "clientService";

    /**
     * Get employees associated to the Task
     *
     * @param projectId Project Id
     * @param taskId    Task Id
     * @return ProjectTaskDto
     */
    @CircuitBreaker(name = CLIENT_SERVICE, fallbackMethod = "fallbackFindEmployees")
    @Retry(name = CLIENT_SERVICE)
    @GetMapping(value = "/projects/{projectId}/tasks/{taskId}/employees")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> findEmployees(@PathVariable final UUID projectId,
                                           @PathVariable final UUID taskId,
                                           @RequestParam(required = false) Integer page,
                                           @RequestParam(required = false) Integer size) {
        if (page != null && size != null) {
            // Pagination parameters are provided, return paginated result of assigned employees
            Map<String, Object> result = service.findAllAssignedEmployees(projectId, taskId, page, size);
            return ResponseEntity.ok(result);
        } else {
            // No pagination parameters provided, return all employees assigned to the project and task
            ProjectTaskEmployeeDto projectTaskEmployeeDto = service.findEmployeesByProjectIdAndTaskId(projectId, taskId);
            return ResponseEntity.ok(projectTaskEmployeeDto);
        }
    }

    public ResponseEntity<?> fallbackFindEmployees(UUID projectId, UUID taskId, Integer page, Integer size, Throwable throwable) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Collections.singletonMap("message", "Service is temporarily unavailable. Please try again later."));
    }

    /**
     * Add employees to the task
     *
     * @param projectId   Project Id
     * @param taskId      Task Id
     * @param employeeIds List of Strings
     */
    @PostMapping(value = "/projects/{projectId}/tasks/{taskId}/employees")
    @ResponseStatus(HttpStatus.CREATED)
    @Retry(name = CLIENT_SERVICE, fallbackMethod = "fallbackAddEmployeesToProjectAndTask")
    public void addEmployeesToProjectAndTask(@PathVariable final UUID projectId, @PathVariable final UUID taskId, @RequestBody List<String> employeeIds) {
        service.addEmployeesToProjectAndTask(projectId, taskId, employeeIds);
    }

    public void fallbackAddEmployeesToProjectAndTask(UUID projectId, UUID taskId, List<String> employeeIds, Throwable throwable) {
        log.error("Circuit breaker triggered while adding employees to projectId: {}, taskId: {}. Reason: {}",
                projectId, taskId, throwable.getMessage());
    }

    /**
     * Remove employees from the task
     *
     * @param projectId   Project Id
     * @param employeeIds List of Strings
     */
    @DeleteMapping(value = "/projects/{projectId}/tasks/{taskId}/employees")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeEmployeesFromProjectAndTask(@PathVariable final UUID projectId, @PathVariable final UUID taskId, @RequestBody List<String> employeeIds) {
        service.removeEmployeesFromProjectAndTask(projectId, taskId, employeeIds);
    }

    @PostMapping("/projectTaskEmployees/refresh")
    @ResponseStatus(HttpStatus.CREATED)
    public void clearCaches() {
        service.clearProjectTaskEmployeesCache();
    }

    @PostMapping(value = "/projects/{projectId}/tasks/employees")
    @ResponseStatus(HttpStatus.CREATED)
    public void addAllTasksToEmployees(@PathVariable final UUID projectId, @RequestBody List<String> employeeIds) {
        service.addAllTasksToEmployee(projectId, employeeIds);
    }

    @PostMapping(value = "/projects/{projectId}/employee/{employeeId}")
    @ResponseStatus(HttpStatus.CREATED)
    public void addAllTasksToEmployee(@PathVariable final UUID projectId, @PathVariable final String employeeId) {
        service.addAllTasksToEmployee(projectId, employeeId);
    }

}
