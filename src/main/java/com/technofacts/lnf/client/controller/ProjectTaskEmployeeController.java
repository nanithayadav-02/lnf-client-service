package com.technofacts.lnf.client.controller;

import com.technofacts.lnf.client.service.ProjectTaskEmployeeService;
import com.technofacts.lnf.dto.client.ProjectTaskEmployeeDto;
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
public class ProjectTaskEmployeeController {

    private final ProjectTaskEmployeeService service;

    /**
     * Get employees associated to the Task
     *
     * @param projectId Project Id
     * @param taskId    Task Id
     * @return ProjectTaskDto
     */
    @GetMapping(value = "/projects/{projectId}/tasks/{taskId}/employees")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> findEmployees(@PathVariable("projectId") final UUID projectId,
                                           @PathVariable("taskId") final UUID taskId,
                                           @RequestParam(name = "page", required = false) Integer page,
                                           @RequestParam(name = "size", required = false) Integer size) {
        if (page != null && size != null) {
            // Pagination parameters are provided, return paginated result of assigned employees
            Map<String, Object> result = service.findAllAssignedEmployees(projectId, taskId, page, size);
            return ResponseEntity.ok(result);
        } else {
            // No pagination parameters provided, return all employees assigned to the project and task
            ProjectTaskEmployeeDto projectTaskEmployeeDto = service.findEmployeesByProjectIdAndTaskId(projectId, taskId);
            return ResponseEntity.ok(projectTaskEmployeeDto);
        }
    }

    /**
     * Add employees to the task
     *
     * @param projectId   Project Id
     * @param taskId      Task Id
     * @param employeeIds List of Strings
     */
    @PostMapping(value = "/projects/{projectId}/tasks/{taskId}/employees")
    @ResponseStatus(HttpStatus.CREATED)
    public void addEmployeesToProjectAndTask(@PathVariable("projectId") final UUID projectId, @PathVariable("taskId") final UUID taskId, @RequestBody List<String> employeeIds) {
        service.addEmployeesToProjectAndTask(projectId, taskId, employeeIds);
    }

    @PostMapping(value = "/projects/{projectId}/tasks/{taskId}/project-employees")
    @ResponseStatus(HttpStatus.CREATED)
    public void addAllEmployeesToProjectAndTask(@PathVariable("projectId") final UUID projectId, @PathVariable("taskId") final UUID taskId) {
        service.addAllEmployeesToProjectAndTask(projectId, taskId);
    }

    /**
     * Remove employees from the task
     *
     * @param projectId   Project Id
     * @param employeeIds List of Strings
     */
    @DeleteMapping(value = "/projects/{projectId}/tasks/{taskId}/employees")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeEmployeesFromProjectAndTask(@PathVariable("projectId") final UUID projectId, @PathVariable("taskId") final UUID taskId, @RequestBody List<String> employeeIds) {
        service.removeEmployeesFromProjectAndTask(projectId, taskId, employeeIds);
    }

    @PostMapping("/projectTaskEmployees/refresh")
    @ResponseStatus(HttpStatus.CREATED)
    public void clearCaches() {
        service.clearProjectTaskEmployeesCache();
    }

    @PostMapping(value = "/projects/{projectId}/employee/{employeeId}")
    @ResponseStatus(HttpStatus.CREATED)
    public void addAllTasksToEmployee(@PathVariable("projectId") final UUID projectId, @PathVariable("employeeId") final String employeeId) {
        service.addAllTasksToEmployee(projectId, employeeId);
    }

}
