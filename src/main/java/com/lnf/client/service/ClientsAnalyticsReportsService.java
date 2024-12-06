package com.lnf.client.service;

import com.lnf.client.model.Client;
import com.lnf.client.model.Project;
import com.lnf.client.repository.ClientRepository;
import com.lnf.client.repository.ProjectRepository;
import com.lnf.client.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

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

        long activeClientCount = countActiveEntities(clientRepository.findByStatus(ACTIVE));
        long activeProjectCount = countActiveEntities(projectRepository.findByStatus(ACTIVE));
        long activeTaskCount = countActiveEntities(taskRepository.findByStatus(ACTIVE));

        List<String> labels = Arrays.asList(
                "Jan", "Feb", "Mar", "April", "May", "Jun", "July", "Aug", "Sep", "Oct", "Nov", "Dec");

        // Get counts of projects and clients by month
        List<Integer> projectCountByMonth = getMonthlyCounts(projectRepository.findAll(), year, Project::getStartDate);
        List<Integer> clientCountByMonth = getMonthlyCounts(clientRepository.findAll(), year, Client::getWorkingFrom);

        // Add data to the report
        clientReports.put("totalActiveProjects", activeProjectCount);
        clientReports.put("totalActiveClients", activeClientCount);
        clientReports.put("totalActiveTasks", activeTaskCount);
        clientReports.put("labels", labels);
        clientReports.put("ClientsData", clientCountByMonth);
        clientReports.put("ProjectData", projectCountByMonth);

        return clientReports;
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
    private <T> List<Integer> getMonthlyCounts(List<T> entities, Integer year, java.util.function.Function<T, LocalDate> getDateFunction) {
        // Initialize list of 12 months, all starting with 0 count
        List<Integer> monthlyCounts = new ArrayList<>(Collections.nCopies(12, 0));

        entities.forEach(entity -> {
            LocalDate date = getDateFunction.apply(entity);
            if (date != null && date.getYear() == year) {
                int month = date.getMonthValue() - 1;
                monthlyCounts.set(month, monthlyCounts.get(month) + 1);
            }
        });

        return monthlyCounts;
    }

}
