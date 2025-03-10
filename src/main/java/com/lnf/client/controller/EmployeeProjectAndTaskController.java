package com.lnf.client.controller;

import com.lnf.client.service.EmployeeProjectAndTaskService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
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
    private static final String CLIENT_SERVICE = "clientService";

    @PostMapping(value = "/project/{projectId}/employees")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> addEmployeesToProject(@PathVariable final String projectId) {
        UUID projectUUID = UUID.fromString(projectId);
        service.addAllActiveEmployeeToProject(projectUUID);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping(value = "/project/{projectId}/employees")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeEmployeesFromProject(@PathVariable final UUID projectId) {
        service.removeEmployeeFromProject(projectId);
    }

    @CircuitBreaker(name = "CLIENT_SERVICE", fallbackMethod = "fallbackAddAllEmployeesToProjectAndTask")
    @Retry(name = CLIENT_SERVICE)
    @PostMapping(value = "/projects/{projectId}/tasks/{taskId}/project-employees")
    @ResponseStatus(HttpStatus.CREATED)
    public void addAllEmployeesToProjectAndTask(@PathVariable final UUID projectId, @PathVariable final UUID taskId) {
        service.addAllEmployeesToProjectAndTask(projectId, taskId);
    }

    public void fallbackAddAllEmployeesToProjectAndTask(UUID projectId, UUID taskId, Throwable throwable) {
        log.error("Circuit breaker triggered for projectId: {}, taskId: {}. Reason: {}",
                projectId, taskId, throwable.getMessage());
    }

    @DeleteMapping(value = "/project/{projectId}/task/{taskId}/employees")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeEmployeesFromProjectAndTask(@PathVariable final UUID projectId, @PathVariable final UUID taskId) {
        service.removeEmployeesFromProjectAndTask(projectId, taskId);
    }

    @PostMapping(value = "/project/{projectId}/tasks/employees")
    @ResponseStatus(HttpStatus.CREATED)
    public void addAllTasksToEmployees(@PathVariable final UUID projectId) {
        service.addAllTasksToEmployee(projectId);
    }

    @DeleteMapping(value = "/project/{projectId}/tasks/employees")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeAllTasksToEmployees(@PathVariable final UUID projectId) {
        service.removeAllTasksToEmployee(projectId);
    }

}
