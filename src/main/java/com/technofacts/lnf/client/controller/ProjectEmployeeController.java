package com.technofacts.lnf.client.controller;

import com.technofacts.lnf.client.service.ProjectEmployeeService;
import com.technofacts.lnf.dto.client.ProjectEmployeeDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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
    public ProjectEmployeeDto findEmployeesByProjectId(
            @PathVariable("projectId") final UUID projectId,
            @RequestParam(name = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(name = "size", required = false) Integer size) {

        return service.findEmployeesByProjectIdWithPagination(projectId, page, size);
    }

    /**
     * Add employees to the project
     *
     * @param projectId Project Id
     * @param employeeIds List of Strings
     */
    @PostMapping(value = "/projects/{projectId}/employees")
    @ResponseStatus(HttpStatus.CREATED)
    public void addEmployeesToProject(@PathVariable("projectId") final String projectId, @RequestBody List<String> employeeIds) {
        service.addEmployeeToProject(UUID.fromString(projectId), employeeIds);
    }

    @PostMapping(value = "/projects/{projectId}/active-employees")
    @ResponseStatus(HttpStatus.CREATED)
    public void addAllActiveEmployeesToProject(@PathVariable("projectId") final String projectId,@RequestBody List<String> statuses) {
        service.addAllActiveEmployeeToProject(UUID.fromString(projectId),statuses);
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
