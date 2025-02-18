package com.lnf.client.service;

import com.lnf.client.model.Client;
import com.lnf.client.model.Project;
import com.lnf.client.repository.ClientRepository;
import com.lnf.client.repository.ProjectRepository;
import com.lnf.client.repository.TaskRepository;
import com.lnf.client.utils.FinancialYearDateCalculator;
import com.lnf.dto.common.DateRangeDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ClientsAnalyticsReportsService {

    public static final String ACTIVE = "Active";
    private final ClientRepository clientRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;


    public Map<String, Object> clientAnalyticsReports(Integer year) {
        Map<String, Object> clientReports = new LinkedHashMap<>();

        long activeClientCount = countActiveEntities(clientRepository.findByStatusAndYear(ACTIVE, year));
        long activeProjectCount = countActiveEntities(projectRepository.findByStatusAndYear(ACTIVE, year));
        long activeTaskCount = countActiveEntities(taskRepository.findByStatusAndYear(ACTIVE, year));

        Map<String, Integer> projectCountByMonth = getMonthlyCounts(projectRepository.findByStatus(ACTIVE), year, Project::getStartDate);
        Map<String, Integer> clientCountByMonth = getMonthlyCounts(clientRepository.findByStatus(ACTIVE), year, Client::getWorkingFrom);

        clientReports.put("totalActiveProjects", activeProjectCount);
        clientReports.put("totalActiveClients", activeClientCount);
        clientReports.put("totalActiveTasks", activeTaskCount);
        clientReports.put("ClientsData", clientCountByMonth);
        clientReports.put("ProjectData", projectCountByMonth);

        return clientReports;
    }

    public DateRangeDto getClientDateRanges() {
        Object result = clientRepository.findClientDateLimits();
        return FinancialYearDateCalculator.getDateRanges(result);
    }

    /**
     * Helper method to count active entities.
     */
    private <T> long countActiveEntities(List<T> entities) {
        return entities.stream().count();
    }

    /**
     * Generic method to calculate monthly counts for any entity that has a date field.
     * This reduces code duplication for projects and clients.
     */
    private <T> Map<String, Integer> getMonthlyCounts(List<T> entities, Integer year, java.util.function.Function<T, LocalDate> getDateFunction) {
        Map<String, Integer> monthlyCounts = new LinkedHashMap<>();

        String[] monthNames = {
                "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        };

        for (String month : monthNames) {
            monthlyCounts.put(month, 0);
        }

        entities.forEach(entity -> {
            LocalDate date = getDateFunction.apply(entity);
            if (date != null && date.getYear() == year) {
                int month = date.getMonthValue() - 1;
                String monthName = monthNames[month];

                monthlyCounts.put(monthName, monthlyCounts.get(monthName) + 1);
            }
        });

        return monthlyCounts;
    }

}
