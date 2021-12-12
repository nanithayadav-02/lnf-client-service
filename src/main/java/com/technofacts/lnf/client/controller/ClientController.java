package com.technofacts.lnf.client.controller;

import java.util.List;
import java.util.UUID;

import com.technofacts.lnf.client.dto.ClientDto;
import com.technofacts.lnf.client.dto.DashboardDto;
import com.technofacts.lnf.client.service.ClientService;
import com.technofacts.lnf.client.util.QueryConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/lnf")
public class ClientController {

    private final ClientService service;

    @GetMapping(value = "/clients", params = {QueryConstants.PAGE, QueryConstants.SIZE, QueryConstants.SORT_BY})
    public List<ClientDto> findAllPaginatedAndSorted(@RequestParam(value = QueryConstants.PAGE) final int page,
                                                     @RequestParam(value = QueryConstants.SIZE) final int size,
                                                     @RequestParam(value = QueryConstants.SORT_BY) final String sortBy,
                                                     @RequestParam(value = QueryConstants.SORT_ORDER) final String sortOrder) {
        return service.findPaginatedAndSorted(page, size, sortBy, sortOrder);
    }

    @GetMapping(value = "/clients", params = {QueryConstants.PAGE, QueryConstants.SIZE})
    public List<ClientDto> findAllPaginated(@RequestParam(value = QueryConstants.PAGE) final int page,
                                            @RequestParam(value = QueryConstants.SIZE) final int size) {
        return service.findPaginated(page, size);
    }

    @GetMapping(value = "/clients", params = {QueryConstants.SORT_BY})
    public List<ClientDto> findAllSorted(@RequestParam(value = QueryConstants.SORT_BY) final String sortBy,
                                         @RequestParam(value = QueryConstants.SORT_ORDER) final String sortOrder) {
        return service.findAllSorted(sortBy, sortOrder);
    }

    @GetMapping(value = "/clients/dashboard")
    public DashboardDto dashboard() {
        return service.dashboard();
    }

    @GetMapping(value = "/clients", params = {"search"})
    public List<ClientDto> search(@RequestParam(value = "search") String search) {
        return service.findAll(search);
    }

    @GetMapping(value = "/clients/{clientId}")
    public ClientDto findOne(@PathVariable("clientId") final UUID clientId) {
        return service.findByClientId(clientId);
    }

    @GetMapping(value = "/clients")
    public List<ClientDto> findAll() {
        return service.findAll();
    }

    @PostMapping(value = "/clients")
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@RequestBody final ClientDto resource) {
        service.create(resource);
    }

    @PutMapping(value = "/clients/{clientId}")
    @ResponseStatus(HttpStatus.OK)
    public void update(@PathVariable("clientId") final UUID clientId, @RequestBody final ClientDto resource) {
        service.update(clientId, resource);
    }

    // delete
    @DeleteMapping(value = "/clients/{clientId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("clientId") final UUID clientId) {
        service.delete(clientId);
    }
}
