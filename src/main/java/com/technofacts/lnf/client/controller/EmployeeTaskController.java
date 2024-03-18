package com.technofacts.lnf.client.controller;

import com.technofacts.lnf.client.service.EmployeeTaskService;
import com.technofacts.lnf.dto.client.EmployeeProjectTasksDto;
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
    public EmployeeProjectTasksDto findTasksByEmployeeId(@RequestParam("employeeId") final String employeeId) {
        return service.findTasksByEmployeeId(employeeId);
    }

}
