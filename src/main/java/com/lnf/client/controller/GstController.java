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

import java.util.List;
import java.util.UUID;

import com.lnf.client.service.GstService;
import com.lnf.dto.client.GstDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/lnf")
public class GstController {

    private final GstService service;

    /**
     * Returns GstDto for the client gst by clientId
     *
     * @param clientId Client Id
     * @return GstDto of the client gst
     */
    @GetMapping(value = "/clients/{clientId}/gst")
    @ResponseStatus(HttpStatus.OK)
    public List<GstDto> findByClientId(@PathVariable("clientId") final UUID clientId) {
        return service.findByClientId(clientId);
    }

    /**
     * Returns GstDto of the Client by clientId and gstId
     *
     * @param clientId Client Id
     * @param gstId    GST ID
     * @return GstDto of the client gst
     */
    @GetMapping(value = "/clients/{clientId}/gst/{gstId}")
    @ResponseStatus(HttpStatus.OK)
    public GstDto findById(@PathVariable("clientId") final UUID clientId, @PathVariable("gstId") final UUID gstId) {
        return service.findById(clientId, gstId);
    }

    /**
     * Creates the gst for the client
     *
     * @param clientId Client Id
     * @param resource GstDto
     */
    @PostMapping(value = "/clients/{clientId}/gst")
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@PathVariable("clientId") final UUID clientId, @RequestBody final GstDto resource) {
        service.create(clientId, resource);
    }

    /**
     * Updates the gst for the client
     *
     * @param clientId Client Id
     * @param gstId    GST ID
     * @param resource GstDto
     */
    @PutMapping(value = "/clients/{clientId}/gst/{gstId}")
    @ResponseStatus(HttpStatus.OK)
    public void update(@PathVariable("clientId") final UUID clientId, @PathVariable("gstId") final UUID gstId, @RequestBody final GstDto resource) {
        service.update(clientId, gstId, resource);
    }

    /**
     * Deletes the client gst by clientId
     *
     * @param clientId Client Id
     */
    @DeleteMapping(value = "/clients/{clientId}/gst")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("clientId") final UUID clientId) {
        service.deleteByClientId(clientId);
    }

    /**
     * Deletes the client gst by clientId and gstId
     *
     * @param clientId Client Id
     * @param gstId    GST Id
     */
    @DeleteMapping(value = "/clients/{clientId}/gst/{gstId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("clientId") final UUID clientId, @PathVariable("gstId") final UUID gstId) {
        service.deleteById(clientId, gstId);
    }
}

