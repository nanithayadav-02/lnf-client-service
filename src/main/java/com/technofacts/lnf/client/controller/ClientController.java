package com.technofacts.lnf.client.controller;

import java.util.List;
import java.util.UUID;

import com.technofacts.lnf.client.service.ClientService;
import com.technofacts.lnf.client.service.ProjectService;
import com.technofacts.lnf.dto.client.ClientDto;
import com.technofacts.lnf.dto.client.ProjectDto;
import com.technofacts.lnf.util.QueryConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/lnf")
public class ClientController {

    private final ClientService service;
    private final ProjectService projectService;

    /**
     * Return requested page with list of ClientDto objects with requested size.  Raises LnFEntityNotFoundException
     * if the requested page is more than the total number of pages.
     *
     * @param page Requested Page Number
     * @param size Requested size in the page
     * @return A Page object with clientDtos
     */
    @GetMapping(value = "/clients", params = {QueryConstants.PAGE, QueryConstants.SIZE})
    @ResponseStatus(HttpStatus.OK)
    public Page<ClientDto> findPaginated(@RequestParam(value = QueryConstants.PAGE) final int page,
                                         @RequestParam(value = QueryConstants.SIZE) final int size) {
        return service.findPaginated(page, size);
    }

    /**
     * Return requested page with sorted list of ClientDto objects with requested size. Raises LnFEntityNotFoundException
     * if the requested page is more than the total number of pages.
     *
     * @param page      Requested Page Number
     * @param size      Requested size in the page
     * @param sortBy    sorting parameter
     * @param sortOrder sort order ASC or DESC
     * @return A Page object with sorted clientDtos
     */
    @GetMapping(value = "/clients", params = {QueryConstants.PAGE, QueryConstants.SIZE, QueryConstants.SORT_BY})
    @ResponseStatus(HttpStatus.OK)
    public Page<ClientDto> findPaginatedAndSorted(@RequestParam(value = QueryConstants.PAGE) final int page,
                                                  @RequestParam(value = QueryConstants.SIZE) final int size,
                                                  @RequestParam(value = QueryConstants.SORT_BY) final String sortBy,
                                                  @RequestParam(value = QueryConstants.SORT_ORDER) final String sortOrder) {
        return service.findPaginatedAndSorted(page, size, sortBy, sortOrder);
    }

    /**
     * Return sorted list of all ClientDto objects
     *
     * @param sortBy    sorting parameter
     * @param sortOrder sort order ASC or DESC
     * @return Sorted list of all ClientDto objects.
     */
    @GetMapping(value = "/clients", params = {QueryConstants.SORT_BY, QueryConstants.SORT_ORDER})
    @ResponseStatus(HttpStatus.OK)
    public List<ClientDto> findAllSorted(@RequestParam(value = QueryConstants.SORT_BY) final String sortBy,
                                         @RequestParam(value = QueryConstants.SORT_ORDER) final String sortOrder) {
        return service.findAllSorted(sortBy, sortOrder);
    }

    /**
     * Return list of all ClientDto objects
     *
     * @return List of all ClientDto objects.
     */
    @GetMapping(value = "/clients")
    @ResponseStatus(HttpStatus.OK)
    public List<ClientDto> findAll() {
        return service.findAll();
    }

    /**
     * Return list of all ClientDto objects matching the search query.
     *
     * @return List of all ClientDto objects.
     */
    @GetMapping(value = "/clients", params = {QueryConstants.SEARCH})
    @ResponseStatus(HttpStatus.OK)
    public List<ClientDto> search(@RequestParam(value = QueryConstants.SEARCH) String search) {
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
     * @return ClientDto object
     */
    @GetMapping(value = "/clients/{clientId}/projects")
    @ResponseStatus(HttpStatus.OK)
    public List<ProjectDto> findProjectsByClientId(@PathVariable("clientId") final UUID clientId) {
        return projectService.findProjectsByClientId(clientId);
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
