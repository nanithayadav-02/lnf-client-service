package com.technofacts.lnf.client.controller;

import java.util.List;
import java.util.UUID;

import com.technofacts.lnf.client.dto.ProjectDto;
import com.technofacts.lnf.client.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/lnf")
public class ProjectController {

    private final ProjectService service;

    @GetMapping(value = "/clients/{clientId}/project")
    public List<ProjectDto> findByClientId
            (@PathVariable("clientId") final UUID clientId) {
        return service.findByClientId(clientId);
    }

    @GetMapping(value = "/clients/{clientId}/project/{projectId}")
    public ProjectDto findById(@PathVariable("clientId") final UUID clientId, @PathVariable("projectId") final UUID projectId) {
        return service.findById(clientId, projectId);
    }

    @PostMapping(value = "/clients/{clientId}/project")
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@PathVariable("clientId") final UUID clientId, @RequestBody final ProjectDto resource) {
        service.create(clientId, resource);
    }

    @PutMapping(value = "/clients/{clientId}/project/{projectId}")
    @ResponseStatus(HttpStatus.OK)
    public void update(@PathVariable("clientId") final UUID clientId, @PathVariable("projectId") final UUID projectId, @RequestBody final ProjectDto resource) {
        service.update(clientId, projectId, resource);
    }

    @DeleteMapping(value = "/clients/{clientId}/project")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("clientId") final UUID clientId) {
        service.deleteByClientId(clientId);
    }

    @DeleteMapping(value = "/clients/{clientId}/project/{projectId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("clientId") final UUID clientId, @PathVariable("projectId") final UUID projectId) {
        service.deleteById(clientId, projectId);
    }
}

