package com.technofacts.lnf.client.controller;

import com.technofacts.lnf.client.service.EmployeeProjectService;
import com.technofacts.lnf.dto.client.EmployeeProjectDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/lnf")
public class EmployeeProjectController {

    private final EmployeeProjectService service;

    /**
     * Get projects associated to the employee
     *
     * @param employeeId EmployeeId
     * @return ProjectEmployeeDto
     */
    @GetMapping(value = "/projects")
    @ResponseStatus(HttpStatus.OK)
    public EmployeeProjectDto findProjectsByEmployeeId(@RequestParam("employeeId") final String employeeId) {
        return service.findProjectsByEmployeeId(employeeId);
    }

}
