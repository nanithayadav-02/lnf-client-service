package com.technofacts.lnf.client.converter;

import com.technofacts.lnf.client.model.Project;
import com.technofacts.lnf.dto.client.ClientEmployeeDto;
import com.technofacts.lnf.dto.client.ProjectDto;
import com.technofacts.lnf.dto.client.ProjectOverviewDto;
import com.technofacts.lnf.dto.employee.EmployeeDto;

import java.util.Objects;

public class ProjectConverter {

    private ProjectConverter() {
    }

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
        dto.setClientCode(entity.getClient() != null ? entity.getClient().getCode() : null);
        dto.setClientName(entity.getClient() != null ? entity.getClient().getName() : null);
        dto.getTasks().addAll(entity.getTasks().stream().map(TaskConverter::toTransportModel).filter(Objects::nonNull).toList());
        return dto;
    }

    public static ProjectOverviewDto toMiniTransportModel(Project entity) {

        if (entity == null) {
            return null;
        }
        ProjectOverviewDto dto = new ProjectOverviewDto();
        dto.setId(entity.getId());
        dto.setCode(entity.getCode());
        dto.setName(entity.getName());
        dto.setType(entity.getType());
        dto.setBudgetTerms(entity.getBudgetTerms());
        dto.setHoursPerDay(entity.getHoursPerDay());
        dto.setBillingTerm(entity.getBillingTerm());
        dto.setStartDate(entity.getStartDate());
        dto.setClientName(entity.getClient().getName());
        dto.setClientId(entity.getClient().getId());
        dto.setDescription(entity.getDescription());
        dto.setPurchaseOrder(entity.getPurchaseOrder());
        dto.setStatus(entity.getStatus());

        return dto;
    }

    public static ClientEmployeeDto mapToClientEmployee(EmployeeDto employeeDto) {
        return ClientEmployeeDto.builder()
                .id(employeeDto.getId())
                .employeeId(employeeDto.getEmployeeId())
                .firstName(employeeDto.getFirstName())
                .lastName(employeeDto.getLastName())
                .designation(employeeDto.getDesignation())
                .email(employeeDto.getEmail())
                .mobileNumber(employeeDto.getMobileNumber())
                .employmentStatus(employeeDto.getEmploymentStatus())
                .image(employeeDto.getImage())
                .build();
    }

    public static Project toEntityModel(ProjectDto transport) {
        return toEntityModel(transport, new Project());
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
