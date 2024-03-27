package com.technofacts.lnf.client.service;

import com.technofacts.lnf.client.converter.ClientConverter;
import com.technofacts.lnf.client.model.Client;
import com.technofacts.lnf.client.repository.ClientRepository;
import com.technofacts.lnf.client.repository.ProjectRepository;
import com.technofacts.lnf.client.repository.StatisticsSummary;
import com.technofacts.lnf.client.repository.TaskRepository;
import com.technofacts.lnf.dto.client.ClientDto;
import com.technofacts.lnf.dto.common.DashboardDto;
import com.technofacts.lnf.dto.common.StatisticsDto;
import com.technofacts.lnf.dto.employee.EmployeeDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Log
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
        mapStatistics(dashboardDto,  clientRepository.clientsByYearAndMonth());
        return  dashboardDto;
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
        return  dashboardDto;
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
        return  dashboardDto;
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
        Date fromDate = java.sql.Date.valueOf(localDate);
        List<Client> clients = clientRepository.findByDateAfter(fromDate);

        return clients.stream()
                .map(ClientConverter::toTransportModel)
                .toList();

    }}

