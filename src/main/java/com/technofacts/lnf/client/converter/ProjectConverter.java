package com.technofacts.lnf.client.converter;

import java.util.Objects;
import java.util.stream.Collectors;

import com.technofacts.lnf.client.model.Project;
import com.technofacts.lnf.dto.client.ProjectDto;

public class ProjectConverter {

    public static ProjectDto toTransportModel(Project entity) {

        if (entity == null) {
            return null;
        }
        ProjectDto dto = new ProjectDto();
        dto.setId(entity.getId());
        dto.setCode(entity.getCode());
        dto.setName(entity.getName());
        dto.setBudget(entity.getBudget());
        dto.setBudgetTerms(entity.getBudgetTerms());
        dto.setBillingTerm(entity.getBillingTerm());
        dto.setCurrency(entity.getCurrency());
        dto.setDescription(entity.getDescription());
        dto.setHoursPerDay(entity.getHoursPerDay());
        dto.setPurchaseOrder(entity.getPurchaseOrder());
        dto.setStatus(entity.getStatus());
        dto.setType(entity.getType());
        dto.setStartDate(entity.getStartDate());
        dto.setEndDate(entity.getEndDate());
        dto.setClientId(entity.getClient() != null ? entity.getClient().getId() : null);
        dto.getTasks().addAll(entity.getTasks().stream().map(TaskConverter::toTransportModel).filter(Objects::nonNull).collect(Collectors.toList()));
        return dto;
    }

    public static Project toEntityModel(ProjectDto transport) {
        Project entity = toEntityModel(transport, new Project());
        return entity;
    }

    public static Project toEntityModel(ProjectDto transport, Project entity) {

        if (transport == null || entity == null) {
            return null;
        }

        entity.setId(transport.getId());
        entity.setCode(transport.getCode());
        entity.setName(transport.getName());
        entity.setBudgetTerms(transport.getBudgetTerms());
        entity.setBudget(transport.getBudget());
        entity.setBillingTerm(transport.getBillingTerm());
        entity.setCurrency(transport.getCurrency());
        entity.setDescription(transport.getDescription());
        entity.setHoursPerDay(transport.getHoursPerDay());
        entity.setPurchaseOrder(transport.getPurchaseOrder());
        entity.setStatus(transport.getStatus());
        entity.setType(transport.getType());
        entity.setStartDate(transport.getStartDate());
        entity.setEndDate(transport.getEndDate());

        return entity;
    }

}
