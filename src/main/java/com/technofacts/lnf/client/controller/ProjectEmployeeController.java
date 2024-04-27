package com.technofacts.lnf.client.controller;

import com.technofacts.lnf.client.service.ProjectEmployeeService;
import com.technofacts.lnf.dto.client.ProjectEmployeeDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/lnf")
public class ProjectEmployeeController {

    private final ProjectEmployeeService service;

    /**
     * Find employees assigned to a project.
     *
     * @param projectId The ID of the project.
     * @param page      The page number for pagination (optional).
     * @param size      The number of employees per page for pagination (optional).
     * @return ResponseEntity representing the result of the operation.
     */
    @GetMapping(value = "/projects/{projectId}/employees")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> findEmployees(
            @PathVariable("projectId") final UUID projectId,
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "size", required = false) Integer size) {

        if (page != null && size != null) {
            // Pagination parameters are provided, return paginated result of assigned employees
            Map<String, Object> result = service.findAllAssignedEmployees(projectId, page, size);
            return ResponseEntity.ok(result);
        } else {
            // No pagination parameters provided, return all employees assigned to the project
            ProjectEmployeeDto projectEmployeeDto = service.findEmployeesByProjectId(projectId);
            return ResponseEntity.ok(projectEmployeeDto);
        }
    }

    @PostMapping(value = "/projects/{projectId}/employees")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> addEmployeesToProject(
            @PathVariable("projectId") final String projectId,
            @RequestParam(name = "type", required = false, defaultValue = "specific") String type,
            @RequestBody List<String> identifiers) {

        UUID projectUUID = UUID.fromString(projectId);

        if ("active".equals(type)) {
            // If 'type' is 'active', treat identifiers as statuses and add all active employees based on these statuses
            service.addAllActiveEmployeeToProject(projectUUID, identifiers);
        } else {
            // Default behavior: treat identifiers as employeeIds and add them to the project
            service.addEmployeeToProject(projectUUID, identifiers);
        }

        return ResponseEntity.status(HttpStatus.CREATED).build();
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
