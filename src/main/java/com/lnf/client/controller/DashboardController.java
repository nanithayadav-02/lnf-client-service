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

import com.lnf.client.service.DashboardService;
import com.lnf.dto.client.ClientDto;
import com.lnf.dto.common.DashboardDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/lnf")
public class DashboardController {

    private final DashboardService service;

    /**
     * Return client dashboard statistics
     *
     * @return DashboardDto
     */
    @GetMapping(value = "/dashboard/client")
    @ResponseStatus(HttpStatus.OK)
    public DashboardDto getClientDashboardStatistics() {
        return service.getClientDashboardStatistics();
    }

    /**
     * Return project dashboard statistics
     *
     * @return DashboardDto
     */
    @GetMapping(value = "/dashboard/project")
    @ResponseStatus(HttpStatus.OK)
    public DashboardDto getProjectDashboardStatistics() {
        return service.getProjectDashboardStatistics();
    }

    /**
     * Return task dashboard statistics
     *
     * @return DashboardDto
     */
    @GetMapping(value = "/dashboard/task")
    @ResponseStatus(HttpStatus.OK)
    public DashboardDto getTaskDashboardStatistics() {
        return service.getTaskDashboardStatistics();
    }

    @GetMapping(value = "/dashboard/clients", params = {"addedSince"})
    public List<ClientDto> findNewlyAddedClients(@RequestParam(value = "addedSince") int daysAgo) {
        return service.findNewlyAddedClients(daysAgo);
    }

}
