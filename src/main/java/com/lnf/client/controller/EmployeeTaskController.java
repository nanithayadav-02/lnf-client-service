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

import com.lnf.client.service.EmployeeTaskService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/lnf")
@Slf4j
public class EmployeeTaskController {

    private final EmployeeTaskService service;
    private static final String CLIENT_SERVICE = "clientService";

    /**
     * Get tasks associated to the employee
     *
     * @param employeeId EmployeeId
     * @return EmployeeProjectTasksDto
     */
    @CircuitBreaker(name = CLIENT_SERVICE, fallbackMethod = "fallbackFindTasksByEmployeeId")
    @Retry(name = CLIENT_SERVICE)
    @GetMapping(value = "/tasks")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Object> findTasksByEmployeeId(
            @RequestParam final String employeeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return service.findTasksByEmployeeId(employeeId, page, size);
    }

    public Map<String, Object> fallbackFindTasksByEmployeeId(String employeeId, int page, int size, Throwable throwable) {
        return Collections.singletonMap("message", "Service is temporarily unavailable. Please try again later.");
    }
}
