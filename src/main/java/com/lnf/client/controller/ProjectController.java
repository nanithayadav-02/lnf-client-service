/*
 *
 *  * Copyright © 2024 Lever And Fulcrum Solutions (hereinafter referred to as "LNF").
 *  * All rights reserved.
 *  *
 *  * This source code is the proprietary property of LNF
 *  *
 *  * Unauthorized copying, redistribution, or modification of this code,
 *  * via any medium, is strictly prohibited unless expressly authorized
 *  * in writing by LNF.
 *  *
 *  * This code is confidential and intended solely for the use of LNF
 *  * and its authorized personnel.
 *
 */

package com.lnf.client.controller;

import com.lnf.client.service.DataExportService;
import com.lnf.client.service.ProjectService;
import com.lnf.dto.client.ProjectDto;
import com.lnf.dto.common.PageRequestDto;
import com.lnf.service.common.page.PageableAsQueryParam;
import com.lnf.service.common.page.PaginationAndSortingHandler;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/lnf")
public class ProjectController {

    private final ProjectService service;
    private final PaginationAndSortingHandler paginationAndSortingHandler;
    private final DataExportService dataExportService;
    private static final String CLIENT_SERVICE = "clientService";

    /**
     * Return requested page with list of ProjectDto objects with requested sortBy and sortOrder and size and page.  Raises LnFEntityNotFoundException
     * if the requested page is more than the total number of pages.
     */
    @GetMapping(value = "/projects")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> findAll(@PageableAsQueryParam PageRequestDto pageRequest) {
        return paginationAndSortingHandler.handleFindAllRequest(pageRequest, service);
    }

    /**
     * Return list of all ProjectDto objects matching the search query.
     *
     * @return List of all ProjectDto objects.
     */
    @GetMapping(value = "/projects", params = {"search"})
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> search(@RequestParam(required = false) String search,
                                    @PageableAsQueryParam PageRequestDto pageRequest) {
        if (search != null && !search.isEmpty()) {
            if (pageRequest != null && pageRequest.getPage() != null) {
                return ResponseEntity.ok(service.findingAllWithPagination(search, pageRequest));
            } else {
                return ResponseEntity.ok(service.findAll(search));
            }
        } else {
            return paginationAndSortingHandler.handleFindAllRequest(pageRequest, service);
        }
    }

    /**
     * Returns projectDto from the projectId. Raises LnFEntityNotFoundException
     * if there is no project with the input projectId
     *
     * @param projectId Project Id
     * @return ProjectDto object
     */
    @GetMapping(value = "/projects/{projectId}")
    @ResponseStatus(HttpStatus.OK)
    public ProjectDto findByProjectId(@PathVariable final UUID projectId) {
        return service.findByProjectId(projectId);
    }

    /**
     * Creates the project
     *
     * @param resource projectDto object
     */
    @PostMapping(value = "/projects")
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@RequestBody final ProjectDto resource) {
        service.create(resource);
    }

    @PostMapping(value = "/projects/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadFile(@RequestParam final UUID clientId,
                                             @RequestParam MultipartFile file) throws IOException {
        dataExportService.uploadFile(file, ProjectDto.class, clientId);
        return ResponseEntity.ok("File uploaded successfully.");
    }

    /**
     * Updates the project
     *
     * @param projectId Project Id
     * @param resource  ProjectDto
     */
    @PutMapping(value = "/projects/{projectId}")
    @ResponseStatus(HttpStatus.OK)
    public void update(@PathVariable final UUID projectId, @RequestBody final ProjectDto resource) {
        service.update(projectId, resource);
    }

    /**
     * Deletes the project
     *
     * @param projectId Project Id
     */
    @DeleteMapping(value = "/projects/{projectId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable final UUID projectId) {
        service.delete(projectId);
    }

    @PostMapping("/projects/refresh")
    @ResponseStatus(HttpStatus.CREATED)
    public void clearCaches() {
        service.clearProjectsCache();
    }

    @CircuitBreaker(name = CLIENT_SERVICE, fallbackMethod = "fallbackGetTimeSheetsByClientId")
    @Retry(name = CLIENT_SERVICE)
    @GetMapping("/projects/clients/{clientId}")
    @ResponseStatus(HttpStatus.OK)
    public List<Map<String, Object>> getTimeSheetsByClientId(@PathVariable UUID clientId,
                                                             @RequestParam(required = false) UUID projectId,
                                                             @RequestParam(required = false) Optional<Integer> month,
                                                             @RequestParam(required = false) Optional<Integer> year,
                                                             @RequestParam(required = false) String status) {
        return service.getTimeSheetsByClientId(clientId, projectId, month, year, status);
    }

    public List<Map<String, Object>> fallbackGetTimeSheetsByClientId(UUID clientId, UUID projectId,
                                                                     Optional<Integer> month, Optional<Integer> year,
                                                                     String status, Throwable throwable) {
        return List.of(Map.of(
                "message", "Service is temporarily unavailable. Please try again later.",
                "clientId", clientId
        ));
    }

    @PostMapping(value = "/projects/retrieve-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> retrieveFile(@RequestParam MultipartFile file, @RequestParam final UUID clientId) throws IOException {
        return ResponseEntity.ok(dataExportService.retrieveProjectFile(file, clientId));
    }

    @DeleteMapping("/projects/last-upload")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLastUpload() {
        service.deleteLastUploadFile();
    }

    @DeleteMapping("/projects")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProjects(@RequestBody List<UUID> projectIds) {
        service.deleteProjectList(projectIds);
    }

    @GetMapping("/projects/last-upload")
    @ResponseStatus(HttpStatus.OK)
    public Page<ProjectDto> getLastUpload(@PageableAsQueryParam PageRequestDto pageRequest) {
        return service.getLastUploadData(pageRequest);
    }

    @PostMapping(value = "projects/data-upload")
    @ResponseStatus(HttpStatus.CREATED)
    public List<ProjectDto> create(@RequestBody final List<ProjectDto> resources, @RequestParam final UUID clientId) {
        return service.create(resources, clientId);
    }

}
