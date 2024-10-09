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
import com.lnf.client.service.TaskService;
import com.lnf.dto.client.TaskDto;
import com.lnf.util.QueryConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/lnf")
public class TaskController {

    private final TaskService service;
    private final DataExportService dataExportService;
    /**
     * Return requested page with list of TaskDto objects with requested size assigned to the project.
     * Raises LnFEntityNotFoundException if the requested page is more than the total number of pages or the
     * project is not found.
     *
     * @param projectId Project Id
     * @param page      Requested Page Number
     * @param size      Requested size in the page
     * @return A Page object with taskDtos
     */
    @GetMapping(value = "/projects/{projectId}/tasks", params = {QueryConstants.PAGE, QueryConstants.SIZE})
    @ResponseStatus(HttpStatus.OK)
    public Page<TaskDto> findPaginatedByProjectId(@PathVariable("projectId") final UUID projectId,
                                                  @RequestParam(value = QueryConstants.PAGE) final int page,
                                                  @RequestParam(value = QueryConstants.SIZE) final int size) {
        return service.findPaginatedByProjectId(projectId, page, size);
    }

    /**
     * Return requested page with sorted list of TaskDto objects with requested size assigned to the project.
     * Raises LnFEntityNotFoundException if the requested page is more than the total number of pages or the
     * project is not found.
     *
     * @param projectId Project Id
     * @param page      Requested Page Number
     * @param size      Requested size in the page
     * @param sortBy    sorting parameter
     * @param sortOrder sort order ASC or DESC
     * @return A Page object with sorted taskDtos
     */
    @GetMapping(value = "/projects/{projectId}/tasks", params = {QueryConstants.PAGE, QueryConstants.SIZE, QueryConstants.SORT_BY})
    @ResponseStatus(HttpStatus.OK)
    public Page<TaskDto> findPaginatedAndSortedByProjectId(@PathVariable("projectId") final UUID projectId,
                                                @RequestParam(value = QueryConstants.PAGE) final int page,
                                                @RequestParam(value = QueryConstants.SIZE) final int size,
                                                @RequestParam(value = QueryConstants.SORT_BY) final String sortBy,
                                                @RequestParam(value = QueryConstants.SORT_ORDER) final String sortOrder) {
        return service.findPaginatedAndSortedByProjectId(projectId, page, size, sortBy, sortOrder);
    }

    /**
     * Return sorted list of all TaskDto objects assigned to a project
     *
     * @param projectId Project Id
     * @param sortBy    sorting parameter
     * @param sortOrder sort order ASC or DESC
     * @return Sorted list of all ProjectDto objects.
     */
    @GetMapping(value = "/projects/{projectId}/tasks", params = {QueryConstants.SORT_BY, QueryConstants.SORT_ORDER})
    @ResponseStatus(HttpStatus.OK)
    public List<TaskDto> findAllSortedByProjectId(@PathVariable("projectId") final UUID projectId,
                                       @RequestParam(value = QueryConstants.SORT_BY) final String sortBy,
                                       @RequestParam(value = QueryConstants.SORT_ORDER) final String sortOrder) {
        return service.findAllSortedByProjectId(projectId, sortBy, sortOrder);
    }

    /**
     * Return list of all TaskDto objects assigned to a project
     *
     * @param projectId Project Id
     * @return List of all ProjectDto objects.
     */
    @GetMapping(value = "/projects/{projectId}/tasks")
    @ResponseStatus(HttpStatus.OK)
    public List<TaskDto> findAllByProjectId(@PathVariable("projectId") final UUID projectId) {
        return service.findAllByProjectId(projectId);
    }

    /**
     * Returns taskDto from the projectId and taskId. Raises LnFEntityNotFoundException
     * if there is no project or task.
     *
     * @param projectId Project Id
     * @param taskId Task Id
     * @return TaskDto object
     */
    @GetMapping(value = "/projects/{projectId}/tasks/{taskId}")
    @ResponseStatus(HttpStatus.OK)
    public TaskDto findByProjectIdAndTaskId(@PathVariable("projectId") final UUID projectId, @PathVariable("taskId") final UUID taskId) {
        return service.findByProjectIdAndTaskId(projectId, taskId);
    }

    /**
     * Returns taskDto from the taskId. Raises LnFEntityNotFoundException
     * if there is no task.
     *
     * @param taskId Task Id
     * @return TaskDto object
     */
    @GetMapping(value = "/tasks/{taskId}")
    @ResponseStatus(HttpStatus.OK)
    public TaskDto findByTaskId(@PathVariable("taskId") final UUID taskId) {
        return service.findByTaskId(taskId);
    }

    /**
     * Creates the project
     *
     * @param projectId Project Id
     * @param resource TaskDto object
     */
    @PostMapping(value = "/projects/{projectId}/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@PathVariable("projectId") final UUID projectId, @RequestBody final TaskDto resource) {
        service.create(projectId, resource);
    }

    @PostMapping(value = "/tasks/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadProjectTasksFile(@RequestParam("projectId") final UUID projectId,
                                             @RequestParam("file") MultipartFile file) throws IOException {
        dataExportService.uploadFile(file, TaskDto.class, projectId);
        return ResponseEntity.ok("File uploaded successfully.");
    }

    /**
     * Updates the project
     *
     * @param projectId Project Id
     * @param taskId Task Id
     * @param resource TaskDto
     */
    @PutMapping(value = "/projects/{projectId}/tasks/{taskId}")
    @ResponseStatus(HttpStatus.OK)
    public void update(@PathVariable("projectId") final UUID projectId, @PathVariable("taskId") final UUID taskId, @RequestBody final TaskDto resource) {
        service.update(projectId, taskId, resource);
    }

    /**
     * Deletes the tasks by projectId
     *
     * @param projectId Project Id
     */
    @DeleteMapping(value = "/projects/{projectId}/tasks")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteByProjectId(@PathVariable("projectId") final UUID projectId) {
        service.deleteByProjectId(projectId);
    }

    /**
     * Deletes the task by projectId and taskId
     *
     * @param projectId Project Id
     * @param taskId Task Id
     */
    @DeleteMapping(value = "/projects/{projectId}/tasks/{taskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteByProjectIdAndTaskId(@PathVariable("projectId") final UUID projectId, @PathVariable("taskId") final UUID taskId) {
        service.deleteByProjectIdAndTaskId(projectId, taskId);
    }

}

