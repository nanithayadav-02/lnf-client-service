package com.technofacts.lnf.client.controller;

import java.util.List;
import java.util.UUID;

import com.technofacts.lnf.client.service.ClientService;
import com.technofacts.lnf.client.service.DashboardService;
import com.technofacts.lnf.client.service.ProjectService;
import com.technofacts.lnf.dto.client.ClientDto;
import com.technofacts.lnf.dto.client.ProjectDto;
import com.technofacts.lnf.dto.common.DashboardDto;
import com.technofacts.lnf.util.QueryConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


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
}
