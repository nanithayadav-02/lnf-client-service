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

import com.lnf.client.service.ProjectEmployeeService;
import com.lnf.dto.client.ProjectEmployeeDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * This class is a REST controller that handles CRUD operations related to project employees.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/lnf")
public class ProjectEmployeeController {

    private final ProjectEmployeeService service;
    private static final String CLIENT_SERVICE = "clientService";

    /**
     * Find employees assigned to a project.
     *
     * @param projectId The ID of the project.
     * @param page      The page number for pagination (optional).
     * @param size      The number of employees per page for pagination (optional).
     * @return ResponseEntity representing the result of the operation.
     */
    @CircuitBreaker(name = CLIENT_SERVICE)
    @GetMapping(value = "/projects/{projectId}/employees")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> findEmployees(
            @PathVariable final UUID projectId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {

        if (page != null && size != null) {
            // Pagination parameters are provided, return paginated result of assigned employees
            Map<String, Object> result = service.findAllAssignedEmployees(projectId, page, size);
            return ResponseEntity.ok(result);
        } else {
            // No pagination parameters provided, return all employees assigned to the project
            ProjectEmployeeDto projectEmployeeDto = service.findEmployeesByProjectId(projectId);
            return ResponseEntity.ok(projectEmployeeDto);
        }
    }

    private ResponseEntity<?> fallbackFindEmployees(UUID projectId, Integer page, Integer size, Throwable t) {
        //       log.error("Fallback triggered for projectId: {} with exception: {}", projectId, t.getMessage());

        String errorMessage = "Unable to fetch employees for project " + projectId;
        if (page != null && size != null) {
            // If pagination parameters are present, return an empty page response
            return ResponseEntity.ok(Map.of("content", Collections.emptyList(), "totalElements", 0));
        } else {
            // If no pagination, return an empty list response
            return ResponseEntity.ok(new ProjectEmployeeDto());
        }
    }

    /**
     * Add employees to a project.
     *
     * @param projectId   The ID of the project.
     * @param type        The type of employee addition. Default is "specific".
     * @param identifiers The list of identifiers for the employees to be added. If 'type' is "active",
     *                    treat the identifiers as statuses and add all active employees based on these
     *                    statuses. If 'type' is not "active", treat the identifiers as employee IDs and add them to the project.
     * @return A ResponseEntity representing the result of the operation.
     */
    @PostMapping(value = "/projects/{projectId}/employees")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> addEmployeesToProject(
            @PathVariable final String projectId,
            @RequestParam(required = false, defaultValue = "specific") String type,
            @RequestBody List<String> identifiers) {

        UUID projectUUID = UUID.fromString(projectId);

        if ("active".equals(type)) {
            // If 'type' is 'active', treat identifiers as statuses and add all active employees based on these statuses
            service.addAllActiveEmployeeToProject(projectUUID, identifiers);
        } else {
            // Default behavior: treat identifiers as employeeIds and add them to the project
            service.addEmployeeToProject(projectUUID, identifiers);
        }

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    /**
     * Remove employees to the project
     *
     * @param projectId   Project Id
     * @param employeeIds List of Strings
     */
    @DeleteMapping(value = "/projects/{projectId}/employees")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeEmployeesFromProject(@PathVariable final UUID projectId, @RequestBody List<String> employeeIds) {
        service.removeEmployeeFromProject(projectId, employeeIds);
    }

    @PostMapping("/projectEmployees/refresh")
    @ResponseStatus(HttpStatus.CREATED)
    public void clearCaches() {
        service.clearProjectEmployeesCache();
    }

}
