package com.technofacts.lnf.client.controller;

import java.util.List;
import java.util.UUID;

import com.technofacts.lnf.client.service.ProjectTaskEmployeeService;
import com.technofacts.lnf.dto.client.TaskEmployeeDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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
    public TaskEmployeeDto findEmployeesByProjectIdAndTaskId(@PathVariable("projectId") final UUID projectId, @PathVariable("taskId") final UUID taskId) {
        return service.findEmployeesByProjectIdAndTaskId(projectId, taskId);
    }

    /**
     * Add employees to the project
     *
     * @param projectId   Project Id
     * @param taskId    Task Id
     * @param employeeIds List of Strings
     */
    @PostMapping(value = "/projects/{projectId}/tasks/{taskId}/employees")
    @ResponseStatus(HttpStatus.CREATED)
    public void addEmployeesToProjectAndTask(@PathVariable("projectId") final UUID projectId, @PathVariable("taskId") final UUID taskId, @RequestBody List<String> employeeIds) {
        service.addEmployeesToProjectAndTask(projectId, taskId, employeeIds);
    }

    /**
     * Remove employees to the project
     *
     * @param projectId   Project Id
     * @param employeeIds List of Strings
     */
    @DeleteMapping(value = "/projects/{projectId}/tasks/{taskId}/employees")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeEmployeesFromProjectAndTask(@PathVariable("projectId") final UUID projectId, @PathVariable("taskId") final UUID taskId, @RequestBody List<String> employeeIds) {
        service.removeEmployeesFromProjectAndTask(projectId, taskId, employeeIds);
    }

}
