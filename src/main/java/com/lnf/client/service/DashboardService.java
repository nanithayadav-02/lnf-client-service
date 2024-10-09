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

package com.lnf.client.service;

import com.lnf.client.converter.ClientConverter;
import com.lnf.client.repository.ClientRepository;
import com.lnf.client.repository.ProjectRepository;
import com.lnf.client.repository.StatisticsSummary;
import com.lnf.client.repository.TaskRepository;
import com.lnf.client.model.Client;
import com.lnf.dto.client.ClientDto;
import com.lnf.dto.common.DashboardDto;
import com.lnf.dto.common.StatisticsDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final ClientRepository clientRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;

    /**
     * Return client dashboard statistics
     *
     * @return DashboardDto
     */
    public DashboardDto getClientDashboardStatistics() {
        DashboardDto dashboardDto = new DashboardDto();
        dashboardDto.setTotal(clientRepository.count());
        mapStatistics(dashboardDto, clientRepository.clientsByYearAndMonth());
        return dashboardDto;
    }

    /**
     * Return project dashboard statistics
     *
     * @return DashboardDto
     */
    public DashboardDto getProjectDashboardStatistics() {
        DashboardDto dashboardDto = new DashboardDto();
        dashboardDto.setTotal(projectRepository.count());
        mapStatistics(dashboardDto, projectRepository.projectsByYearAndMonth());
        return dashboardDto;
    }

    /**
     * Return task dashboard statistics
     *
     * @return DashboardDto
     */
    public DashboardDto getTaskDashboardStatistics() {
        DashboardDto dashboardDto = new DashboardDto();
        dashboardDto.setTotal(taskRepository.count());
        mapStatistics(dashboardDto, taskRepository.tasksByYearAndStatus());
        return dashboardDto;
    }

    private void mapStatistics(DashboardDto dashboardDto, List<StatisticsSummary> statisticsSummaries) {
        if (!statisticsSummaries.isEmpty()) {
            dashboardDto.setStatistics(statisticsSummaries.stream()
                    .map(cs -> new StatisticsDto(cs.getYear(), cs.getMonth(), cs.getStatus(), cs.getCount()))
                    .toList());
        }
    }

    public List<ClientDto> findNewlyAddedClients(int daysAgo) {
        LocalDate localDate = LocalDate.now().minusDays(daysAgo);
        List<Client> clients = clientRepository.findByDateAfter(localDate);

        return clients.stream()
                .map(ClientConverter::toTransportModel)
                .toList();

    }
}

