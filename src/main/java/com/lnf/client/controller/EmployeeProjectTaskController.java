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

import java.util.UUID;

import com.lnf.client.service.EmployeeProjectTaskService;
import com.lnf.dto.client.EmployeeProjectTaskDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/lnf")
public class EmployeeProjectTaskController {

    private final EmployeeProjectTaskService service;

    /**
     * Get projects associated to the employee
     *
     * @param employeeId EmployeeId
     * @return ProjectEmployeeDto
     */
    @GetMapping(value = "/projects/{projectId}/task")
    @ResponseStatus(HttpStatus.OK)
    public EmployeeProjectTaskDto findTasksByEmployeeIdAndProjectId(@RequestParam("employeeId") final String employeeId, @PathVariable("projectId") final UUID projectId) {
        return service.findTasksByEmployeeIdAndProjectId(employeeId, projectId);
    }

}
