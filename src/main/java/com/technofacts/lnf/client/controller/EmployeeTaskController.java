package com.technofacts.lnf.client.controller;

import com.technofacts.lnf.client.service.EmployeeTaskService;
import com.technofacts.lnf.dto.client.EmployeeProjectTaskDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/lnf")
public class EmployeeTaskController {

    private final EmployeeTaskService service;

    /**
     * Get projects associated to the employee
     *
     * @param employeeId Employee Id
     * @return ProjectEmployeeDto
     */
    @GetMapping(value = "/employees/{employeeId}/tasks")
    @ResponseStatus(HttpStatus.OK)
    public List<EmployeeProjectTaskDto> findProjectsByEmployeeId(@PathVariable("employeeId") final String employeeId) {
        return service.findEmployeeTasks(employeeId);
    }
}
