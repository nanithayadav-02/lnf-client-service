package com.technofacts.lnf.client.controller;

import com.technofacts.lnf.client.service.DataExportService;
import com.technofacts.lnf.client.service.ProjectService;
import com.technofacts.lnf.dto.client.ProjectDto;
import com.technofacts.lnf.dto.common.PageRequestDto;
import com.technofacts.lnf.service.common.page.PageableAsQueryParam;
import com.technofacts.lnf.service.common.page.PaginationAndSortingHandler;
import lombok.RequiredArgsConstructor;
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
public class ProjectController {

    private final ProjectService service;
    private final PaginationAndSortingHandler paginationAndSortingHandler;
    private final DataExportService dataExportService;

    /**
     * Return requested page with list of ProjectDto objects with requested sortBy and sortOrder and size and page.  Raises LnFEntityNotFoundException
     * if the requested page is more than the total number of pages.
     *
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
    public List<ProjectDto> search(@RequestParam(value = "search") String search) {
        return service.findAll(search);
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
    public ProjectDto findByProjectId(@PathVariable("projectId") final UUID projectId) {
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

    @PostMapping(value = "/projects/{clientId}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadFile(@PathVariable("clientId") final UUID clientId, @RequestParam("file") MultipartFile file) throws IOException {
        dataExportService.uploadFile(file,ProjectDto.class,clientId);
        return ResponseEntity.ok("File uploaded successfully.");
    }

    /**
     * Updates the project
     *
     * @param projectId Project Id
     * @param resource ProjectDto
     */
    @PutMapping(value = "/projects/{projectId}")
    @ResponseStatus(HttpStatus.OK)
    public void update(@PathVariable("projectId") final UUID projectId, @RequestBody final ProjectDto resource) {
        service.update(projectId, resource);
    }

    /**
     * Deletes the project
     *
     * @param projectId Project Id
     */
    @DeleteMapping(value = "/projects/{projectId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("projectId") final UUID projectId) {
        service.delete(projectId);
    }

}

