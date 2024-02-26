package com.technofacts.lnf.client.controller;

import com.technofacts.lnf.client.service.ClientService;
import com.technofacts.lnf.client.service.ProjectService;
import com.technofacts.lnf.dto.client.ClientDto;
import com.technofacts.lnf.dto.client.ProjectDto;
import com.technofacts.lnf.dto.common.PageRequestDto;
import com.technofacts.lnf.dto.employee.EmployeeDto;
import com.technofacts.lnf.service.common.page.PageableAsQueryParam;
import com.technofacts.lnf.service.common.page.PaginationAndSortingHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequiredArgsConstructor
@RequestMapping("/lnf")
public class ClientController {

    private final ClientService service;
    private final ProjectService projectService;
    private final PaginationAndSortingHandler paginationAndSortingHandler;

    /**
     * Return requested page with list of ClientDto objects with requested sortBy and sortOrder and size and page.Raises LnFEntityNotFoundException
     * if the requested page is more than the total number of pages.
     *
     * @return A Page object with clientDtos
     */
    @GetMapping(value = "/clients")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> findAll(@PageableAsQueryParam PageRequestDto pageRequest) {
        return paginationAndSortingHandler.handleFindAllRequest(pageRequest, service);
    }

    /**
     * Return list of all ClientDto objects matching the search query.
     *
     * @return List of all ClientDto objects.
     */
    @GetMapping(value = "/clients", params = {"search"})
    @ResponseStatus(HttpStatus.OK)
    public List<ClientDto> search(@RequestParam(value = "search") String search) {
        return service.findAll(search);
    }

    /**
     * Returns clientDto from the clientId. Raises LnFEntityNotFoundException
     * if there is no client with the input clientId
     *
     * @param clientId Client Id
     * @return ClientDto object
     */
    @GetMapping(value = "/clients/{clientId}")
    @ResponseStatus(HttpStatus.OK)
    public ClientDto findByClientId(@PathVariable("clientId") final UUID clientId) {
        return service.findByClientId(clientId);
    }

    /**
     * Returns List of projectsDto from the clientId.
     *
     * @param clientId Client Id
     * @return List of all projectDto objects
     */
    @GetMapping(value = "/clients/{clientId}/projects")
    @ResponseStatus(HttpStatus.OK)
    public List<ProjectDto> findProjectsByClientId(@PathVariable("clientId") final UUID clientId) {
        return projectService.findProjectsByClientId(clientId);
    }

    /**
     * Returns List of employeeDto associated with the given clientId.
     * Raises LnFEntityNotFoundException if there is no client with the input clientId
     * @param clientId Client Id
     * @return List of all employeeDto objects.
     */
    @GetMapping(value = "/clients/{clientId}/employees")
    @ResponseStatus(HttpStatus.OK)
    public List<EmployeeDto> findEmployeesByClientId(@PathVariable("clientId") final UUID clientId) {
        return projectService.findEmployeesByClientId(clientId);
    }

    /**
     * Creates the client
     *
     * @param resource clientDto object
     */
    @PostMapping(value = "/clients")
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@RequestBody final ClientDto resource) {
        service.create(resource);
    }

    /**
     * Updates the client
     *
     * @param clientId Client Id
     * @param resource ClientDto
     */
    @PutMapping(value = "/clients/{clientId}")
    @ResponseStatus(HttpStatus.OK)
    public void update(@PathVariable("clientId") final UUID clientId, @RequestBody final ClientDto resource) {
        service.update(clientId, resource);
    }

    /**
     * Deletes the client
     *
     * @param clientId Client Id
     */
    @DeleteMapping(value = "/clients/{clientId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("clientId") final UUID clientId) {
        service.delete(clientId);
    }

}
