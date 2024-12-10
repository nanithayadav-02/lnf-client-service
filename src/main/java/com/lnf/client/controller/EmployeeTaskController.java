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
import com.lnf.dto.client.EmployeeProjectTasksDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/lnf")
public class EmployeeTaskController {

    private final EmployeeTaskService service;

    /**
     * Get tasks associated to the employee
     *
     * @param employeeId EmployeeId
     * @return EmployeeProjectTasksDto
     */
    @GetMapping(value = "/tasks")
    @ResponseStatus(HttpStatus.OK)
    public EmployeeProjectTasksDto findTasksByEmployeeId(@RequestParam final String employeeId) {
        return service.findTasksByEmployeeId(employeeId);
    }

}
