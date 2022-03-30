package com.technofacts.lnf.client.controller;

import com.technofacts.lnf.client.service.EmployeeTaskService;
import com.technofacts.lnf.dto.client.EmployeeTaskDto;
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
     * @param employeeId Employee Id
     * @return EmployeeTaskDto
     */
    @GetMapping(value = "/employees/{employeeId}/tasks")
    @ResponseStatus(HttpStatus.OK)
    public EmployeeTaskDto findTasksByEmployeeId(@PathVariable("employeeId") final String employeeId) {
        return service.findEmployeeTasks(employeeId);
    }
}
