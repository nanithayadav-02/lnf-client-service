package com.technofacts.lnf.client.controller;

import com.technofacts.lnf.client.service.DashboardService;
import com.technofacts.lnf.dto.client.ClientDto;
import com.technofacts.lnf.dto.common.DashboardDto;
import com.technofacts.lnf.dto.employee.EmployeeDto;
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
