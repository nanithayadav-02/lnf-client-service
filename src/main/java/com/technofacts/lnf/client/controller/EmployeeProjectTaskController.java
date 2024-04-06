package com.technofacts.lnf.client.controller;

import java.util.UUID;

import com.technofacts.lnf.client.service.EmployeeProjectTaskService;
import com.technofacts.lnf.dto.client.EmployeeProjectTaskDto;
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
