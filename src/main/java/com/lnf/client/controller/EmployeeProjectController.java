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

import com.lnf.client.service.EmployeeProjectService;
import com.lnf.dto.client.EmployeeProjectDto;
import com.lnf.dto.client.ProjectDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/lnf")
@Slf4j
public class EmployeeProjectController {

    private final EmployeeProjectService service;
    private static final String CLIENT_SERVICE = "clientService";

    /**
     * Get projects associated to the employee
     *
     * @param employeeId EmployeeId
     * @return ProjectEmployeeDto
     */
    @CircuitBreaker(name = CLIENT_SERVICE, fallbackMethod = "fallbackFindProjectsByEmployeeId")
    @Retry(name = CLIENT_SERVICE)
    @GetMapping(value = "/project")
    @ResponseStatus(HttpStatus.OK)
    public EmployeeProjectDto findProjectsByEmployeeId(@RequestParam final String employeeId) {
        return service.findProjectsByEmployeeId(employeeId);
    }

    private EmployeeProjectDto fallbackFindProjectsByEmployeeId(final String employeeId, Throwable t) {
        log.error("Fallback triggered for employeeId: {} due to exception: {}", employeeId, t.getMessage());
        EmployeeProjectDto fallbackDto = new EmployeeProjectDto();
        fallbackDto.setEmployeeId(employeeId);
        fallbackDto.setProjects(List.of(new ProjectDto()));
        return fallbackDto;
    }

}
