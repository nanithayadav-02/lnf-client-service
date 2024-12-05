package com.lnf.client.controller;

import com.lnf.client.service.EmployeeProjectAndTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/lnf")
@Slf4j
public class EmployeeProjectAndTaskController {

    private final EmployeeProjectAndTaskService service;

    @PostMapping(value = "/project/{projectId}/employees")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> addEmployeesToProject(@PathVariable("projectId") final String projectId) {
        UUID projectUUID = UUID.fromString(projectId);
        service.addAllActiveEmployeeToProject(projectUUID);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping(value = "/project/{projectId}/employees")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeEmployeesFromProject(@PathVariable("projectId") final UUID projectId) {
        service.removeEmployeeFromProject(projectId);
    }

    @PostMapping(value = "/projects/{projectId}/tasks/{taskId}/project-employees")
    @ResponseStatus(HttpStatus.CREATED)
    public void addAllEmployeesToProjectAndTask(@PathVariable("projectId") final UUID projectId, @PathVariable("taskId") final UUID taskId) {
        service.addAllEmployeesToProjectAndTask(projectId, taskId);
    }

    @DeleteMapping(value = "/project/{projectId}/task/{taskId}/employees")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeEmployeesFromProjectAndTask(@PathVariable("projectId") final UUID projectId, @PathVariable("taskId") final UUID taskId) {
        service.removeEmployeesFromProjectAndTask(projectId, taskId);
    }

    @PostMapping(value = "/project/{projectId}/tasks/employees")
    @ResponseStatus(HttpStatus.CREATED)
    public void addAllTasksToEmployees(@PathVariable("projectId") final UUID projectId) {
        service.addAllTasksToEmployee(projectId);
    }

    @DeleteMapping(value = "/project/{projectId}/tasks/employees")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeAllTasksToEmployees(@PathVariable("projectId") final UUID projectId) {
        service.removeAllTasksToEmployee(projectId);
    }

}
