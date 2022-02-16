package com.technofacts.lnf.client.controller;

import java.util.List;
import java.util.UUID;

import com.technofacts.lnf.client.service.ProjectEmployeeService;
import com.technofacts.lnf.dto.client.ProjectDto;
import com.technofacts.lnf.dto.client.ProjectEmployeeDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/lnf")
public class ProjectEmployeeController {

    private final ProjectEmployeeService service;

    /**
     * Get employees associated to the project
     *
     * @param projectId Project Id
     * @return ProjectEmployeeDto
     */
    @GetMapping(value = "/projects/{projectId}/employees")
    @ResponseStatus(HttpStatus.OK)
    public ProjectEmployeeDto findEmployeesByProjectId(@PathVariable("projectId") final UUID projectId) {
        return service.findEmployeesByProjectId(projectId);
    }

    /**
     * Add employees to the project
     *
     * @param projectId Project Id
     * @param employeeIds List of Strings
     */
    @PostMapping(value = "/projects/{projectId}/employees")
    @ResponseStatus(HttpStatus.CREATED)
    public void addEmployeesToProject(@PathVariable("projectId") final UUID projectId, @RequestBody List<String> employeeIds) {
        service.addEmployeeToProject(projectId, employeeIds);
    }

    /**
     * Remove employees to the project
     *
     * @param projectId Project Id
     * @param employeeIds List of Strings
     */
    @DeleteMapping(value = "/projects/{projectId}/employees")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeEmployeesFromProject(@PathVariable("projectId") final UUID projectId, @RequestBody List<String> employeeIds) {
        service.removeEmployeeFromProject(projectId, employeeIds);
    }

}
