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

import com.lnf.client.service.ClientService;
import com.lnf.client.service.DataExportService;
import com.lnf.client.service.ProjectService;
import com.lnf.dto.client.ClientDto;
import com.lnf.dto.client.ClientEmployeeDto;
import com.lnf.dto.client.ProjectOverviewDto;
import com.lnf.dto.common.PageRequestDto;
import com.lnf.service.common.page.PageableAsQueryParam;
import com.lnf.service.common.page.PaginationAndSortingHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class ClientController {

    private final ClientService service;
    private final ProjectService projectService;
    private final PaginationAndSortingHandler paginationAndSortingHandler;
    private final DataExportService dataExportService;

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

    @GetMapping(value = "/clients/health")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> health() {
        log.info("Client service is healthy");
        return ResponseEntity.ok("Client SVC healthy!");
    }

    /**
     * Return list of all ClientDto objects matching the search query.
     *
     * @return List of all ClientDto objects.
     */
    @GetMapping(value = "/clients", params = {"search"})
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
     * Returns clientDto from the clientId. Raises LnFEntityNotFoundException
     * if there is no client with the input clientId
     *
     * @param clientId Client Id
     * @return ClientDto object
     */
    @GetMapping(value = "/clients/{clientId}")
    @ResponseStatus(HttpStatus.OK)
    public ClientDto findByClientId(@PathVariable final UUID clientId) {
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
    public List<ProjectOverviewDto> findProjectsByClientId(@PathVariable final UUID clientId) {
        return projectService.findProjectsByClientId(clientId);
    }

    /**
     * Returns List of employeeDto associated with the given clientId.
     * Raises LnFEntityNotFoundException if there is no client with the input clientId
     *
     * @param clientId Client Id
     * @return List of all employeeDto objects.
     */
    @GetMapping(value = "/clients/{clientId}/employees")
    @ResponseStatus(HttpStatus.OK)
    public List<ClientEmployeeDto> findEmployeesByClientId(@PathVariable final UUID clientId) {
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

    @PostMapping(value = "/clients/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadFile(@RequestParam MultipartFile file) throws IOException {
        dataExportService.uploadFile(file, ClientDto.class, null);
        return ResponseEntity.ok("File uploaded successfully.");
    }

    /**
     * Updates the client
     *
     * @param clientId Client Id
     * @param resource ClientDto
     */
    @PutMapping(value = "/clients/{clientId}")
    @ResponseStatus(HttpStatus.OK)
    public void update(@PathVariable final UUID clientId, @RequestBody final ClientDto resource) {
        service.update(clientId, resource);
    }

    /**
     * Deletes the client
     *
     * @param clientId Client Id
     */
    @DeleteMapping(value = "/clients/{clientId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable final UUID clientId) {
        service.delete(clientId);
    }

    @PostMapping("/clients/refresh")
    @ResponseStatus(HttpStatus.CREATED)
    public void clearCaches() {
        service.clearClientsCache();
    }

    @GetMapping(value = "/clients/retrieve-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> retrieveFile(@RequestParam MultipartFile file) throws IOException {
        return ResponseEntity.ok(dataExportService.retrieveClientFile(file));
    }

    @DeleteMapping("/clients/last-upload")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLastUpload() {
        service.deleteLastUploadFile();
    }

    @DeleteMapping("/clients")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteClients(@RequestBody List<UUID> clientIds) {
        service.deleteClientList(clientIds);
    }

}
