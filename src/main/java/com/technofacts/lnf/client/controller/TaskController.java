package com.technofacts.lnf.client.controller;

import java.util.List;
import java.util.UUID;

import com.technofacts.lnf.client.dto.TaskDto;
import com.technofacts.lnf.client.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/lnf")
public class TaskController {

    private final TaskService service;

    @GetMapping(value = "/projects/{projectId}/task")
    public List<TaskDto> findByProjectId
            (@PathVariable("projectId") final UUID projectId) {
        return service.findByProjectId(projectId);
    }

    @GetMapping(value = "/projects/{projectId}/task/{taskId}")
    public TaskDto findById(@PathVariable("projectId") final UUID projectId, @PathVariable("taskId") final UUID taskId) {
        return service.findById(projectId, taskId);
    }

    @PostMapping(value = "/projects/{projectId}/task")
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@PathVariable("projectId") final UUID projectId, @RequestBody final TaskDto resource) {
        service.create(projectId, resource);
    }

    @PutMapping(value = "/projects/{projectId}/task/{taskId}")
    @ResponseStatus(HttpStatus.OK)
    public void update(@PathVariable("projectId") final UUID projectId, @PathVariable("taskId") final UUID taskId, @RequestBody final TaskDto resource) {
        service.update(projectId, taskId, resource);
    }

    @DeleteMapping(value = "/projects/{projectId}/task")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("projectId") final UUID projectId) {
        service.deleteByProjectId(projectId);
    }

    @DeleteMapping(value = "/projects/{projectId}/task/{taskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("projectId") final UUID projectId, @PathVariable("taskId") final UUID taskId) {
        service.deleteById(projectId, taskId);
    }
}

