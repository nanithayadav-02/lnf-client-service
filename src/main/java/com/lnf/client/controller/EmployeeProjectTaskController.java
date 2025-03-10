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

import com.lnf.client.service.EmployeeProjectTaskService;
import com.lnf.dto.client.EmployeeProjectTaskDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/lnf")
@Slf4j
public class EmployeeProjectTaskController {

    private final EmployeeProjectTaskService service;
    private static final String CLIENT_SERVICE = "clientService";

    /**
     * Get projects associated to the employee
     *
     * @param employeeId EmployeeId
     * @return ProjectEmployeeDto
     */
    @CircuitBreaker(name = CLIENT_SERVICE, fallbackMethod = "fallbackFindTasksByEmployeeIdAndProjectId")
    @Retry(name = CLIENT_SERVICE)
    @GetMapping(value = "/projects/{projectId}/task")
    @ResponseStatus(HttpStatus.OK)
    public EmployeeProjectTaskDto findTasksByEmployeeIdAndProjectId(@RequestParam final String employeeId, @PathVariable final UUID projectId) {
        return service.findTasksByEmployeeIdAndProjectId(employeeId, projectId);
    }

    public EmployeeProjectTaskDto fallbackFindTasksByEmployeeIdAndProjectId(String employeeId, UUID projectId, Throwable throwable) {
        return new EmployeeProjectTaskDto();
    }

}
