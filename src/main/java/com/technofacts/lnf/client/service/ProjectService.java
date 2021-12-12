package com.technofacts.lnf.client.service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import com.technofacts.lnf.client.converter.ProjectConverter;
import com.technofacts.lnf.client.dto.DashboardDto;
import com.technofacts.lnf.client.dto.ProjectDto;
import com.technofacts.lnf.client.dto.StatisticsDto;
import com.technofacts.lnf.client.exception.LnFBadRequestException;
import com.technofacts.lnf.client.exception.LnFEntityNotFoundException;
import com.technofacts.lnf.client.exception.LnFException;
import com.technofacts.lnf.client.model.Client;
import com.technofacts.lnf.client.model.Project;
import com.technofacts.lnf.client.repository.ClientRepository;
import com.technofacts.lnf.client.repository.ProjectRepository;
import com.technofacts.lnf.client.repository.StatisticsSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Log
public class ProjectService {

    private final ProjectRepository repository;
    private final ClientRepository clientRepository;

    public List<ProjectDto> findAll() {
        List<Project> entities = repository.findAll();
        return entities.stream().map(ProjectConverter::toTransportModel).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public List<ProjectDto> findByClientId(UUID clientId) {
        searchForClient(clientId);
        List<Project> entities = repository.findByClientId(clientId);
        return entities.stream().map(ProjectConverter::toTransportModel).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public ProjectDto findById(UUID clientId, UUID projectId) {
        searchForClient(clientId);
        return ProjectConverter.toTransportModel(searchForProject(projectId));
    }

    public void create(UUID clientId, ProjectDto resource) {
        LnFBadRequestException.throwOnCondition(Objects::isNull, resource, String.format("Failed to create Project for client[%s] with null payload", clientId));
        Client clientEntity = searchForClient(clientId);
        Project entity = ProjectConverter.toEntityModel(resource);
        entity.setClient(clientEntity);
        save(entity);
        log.info(() -> String.format("Project for Client[%s] successfully created", clientId));
    }

    public void update(UUID clientId, UUID projectId, ProjectDto resource) {
        LnFBadRequestException.throwOnCondition(Objects::isNull, resource, String.format("Failed to update Project for client[%s] with null payload", clientId));
        searchForClient(clientId);
        Project entity = searchForProject(projectId);
        Project updatedEntity = ProjectConverter.toEntityModel(resource, entity);
        save(updatedEntity);
        log.info(() -> String.format("Project for Client[%s] successfully created", clientId));
    }

    public void deleteById(UUID clientId, UUID projectId) {
        searchForClient(clientId);
        Project entity = searchForProject(projectId);
        try {
            repository.delete(entity);
            log.info(() -> String.format("Project[%s] for client [%s] successfully deleted", projectId, clientId));
        } catch (RuntimeException e) {
            String errorMessage = String.format("Failed to delete Project[[%s] for client [%s]", projectId, clientId);
            throw new LnFException(errorMessage);
        }
    }

    public void deleteByClientId(UUID clientId) {
        searchForClient(clientId);
        List<Project> entities = repository.findByClientId(clientId);
        try {
            repository.deleteAll(entities);
            log.info(() -> String.format("Projects for client[%s] successfully deleted", clientId));
        } catch (RuntimeException e) {
            String errorMessage = String.format("Failed to delete project(s) for client [%s]", clientId);
            throw new LnFException(errorMessage);
        }
    }

    public DashboardDto dashboard() {
        DashboardDto dashboardDto = new DashboardDto();
        dashboardDto.setTotal(repository.count());
        mapStatistics(dashboardDto, repository.projectsByYearAndMonth());
        return  dashboardDto;
    }

    private void mapStatistics(DashboardDto projectDashboardDto, List<StatisticsSummary> statisticsSummaries) {
        if (!statisticsSummaries.isEmpty()) {
            projectDashboardDto.setStatistics(statisticsSummaries.stream()
                    .map(cs -> new StatisticsDto(cs.getYear(), cs.getMonth(), cs.getStatus(), cs.getCount()))
                    .collect(Collectors.toList()));
        }
    }

    private void save(Project entity) {
        try {
            repository.save(entity);
        } catch (RuntimeException e) {
            String errorMessage = String.format("Failed to save Project for client [%s]", entity.getClient().getId());
            throw new LnFException(errorMessage);
        }
    }

    private Client searchForClient(UUID clientId) {
        return clientRepository.findById(clientId).
                orElseThrow(() -> new LnFEntityNotFoundException(String.format("Client with id [%s] does not exist", clientId)));
    }

    private Project searchForProject(UUID projectId) {
        return repository.findById(projectId).
                orElseThrow(() -> new LnFEntityNotFoundException(String.format("Project with id [%s] does not exist", projectId)));
    }

}

