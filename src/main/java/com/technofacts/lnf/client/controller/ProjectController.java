package com.technofacts.lnf.client.controller;

import java.util.List;
import java.util.UUID;

import com.technofacts.lnf.client.service.ProjectService;
import com.technofacts.lnf.dto.client.ProjectDto;
import com.technofacts.lnf.util.QueryConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/lnf")
public class ProjectController {

    private final ProjectService service;

    /**
     * Return requested page with list of ProjectDto objects with requested size.  Raises LnFEntityNotFoundException
     * if the requested page is more than the total number of pages.
     *
     * @param page Requested Page Number
     * @param size Requested size in the page
     * @return A Page object with projectDtos
     */
    @GetMapping(value = "/projects", params = {QueryConstants.PAGE, QueryConstants.SIZE})
    @ResponseStatus(HttpStatus.OK)
    public Page<ProjectDto> findPaginated(@RequestParam(value = QueryConstants.PAGE) final int page,
                                          @RequestParam(value = QueryConstants.SIZE) final int size) {
        return service.findPaginated(page, size);
    }

    /**
     * Return requested page with sorted list of ProjectDto objects with requested size. Raises LnFEntityNotFoundException
     * if the requested page is more than the total number of pages.
     *
     * @param page      Requested Page Number
     * @param size      Requested size in the page
     * @param sortBy    sorting parameter
     * @param sortOrder sort order ASC or DESC
     * @return A Page object with sorted projectDtos
     */
    @GetMapping(value = "/projects", params = {QueryConstants.PAGE, QueryConstants.SIZE, QueryConstants.SORT_BY})
    @ResponseStatus(HttpStatus.OK)
    public Page<ProjectDto> findPaginatedAndSorted(@RequestParam(value = QueryConstants.PAGE) final int page,
                                                  @RequestParam(value = QueryConstants.SIZE) final int size,
                                                  @RequestParam(value = QueryConstants.SORT_BY) final String sortBy,
                                                  @RequestParam(value = QueryConstants.SORT_ORDER) final String sortOrder) {
        return service.findPaginatedAndSorted(page, size, sortBy, sortOrder);
    }

    /**
     * Return sorted list of all ProjectDto objects
     *
     * @param sortBy    sorting parameter
     * @param sortOrder sort order ASC or DESC
     * @return Sorted list of all ProjectDto objects.
     */
    @GetMapping(value = "/projects", params = {QueryConstants.SORT_BY, QueryConstants.SORT_ORDER})
    @ResponseStatus(HttpStatus.OK)
    public List<ProjectDto> findAllSorted(@RequestParam(value = QueryConstants.SORT_BY) final String sortBy,
                                         @RequestParam(value = QueryConstants.SORT_ORDER) final String sortOrder) {
        return service.findAllSorted(sortBy, sortOrder);
    }

    /**
     * Return list of all ProjectDto objects
     *
     * @return List of all ProjectDto objects.
     */
    @GetMapping(value = "/projects")
    @ResponseStatus(HttpStatus.OK)
    public List<ProjectDto> findAll() {
        return service.findAll();
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

