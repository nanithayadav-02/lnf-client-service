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

package com.lnf.client.converter;

import com.lnf.client.model.Project;
import com.lnf.dto.client.ClientEmployeeDto;
import com.lnf.dto.client.ProjectDto;
import com.lnf.dto.client.ProjectOverviewDto;
import com.lnf.dto.employee.EmployeeDto;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

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
        dto.setUploadTime(entity.getUploadTime());
        dto.setClientId(entity.getClient() != null ? entity.getClient().getId() : null);
        dto.setClientCode(entity.getClient() != null ? entity.getClient().getCode() : null);
        dto.setClientName(entity.getClient() != null ? entity.getClient().getName() : null);
        dto.getTasks().addAll(
                Optional.ofNullable(entity.getTasks())
                        .orElse(Collections.emptySet())
                        .stream()
                        .map(TaskConverter::toTransportModel)
                        .filter(Objects::nonNull)
                        .toList());
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
        dto.setBudget(entity.getBudget());
        dto.setBillingTerm(entity.getBillingTerm());
        dto.setStartDate(entity.getStartDate());
        dto.setClientName(entity.getClient() != null ? entity.getClient().getName() : null);
        dto.setClientId(entity.getClient() != null ? entity.getClient().getId() : null);
        dto.setDescription(entity.getDescription());
        dto.setPurchaseOrder(entity.getPurchaseOrder());
        dto.setStatus(entity.getStatus());
        dto.setEndDate(entity.getEndDate());

        return dto;
    }

    public static ClientEmployeeDto mapToClientEmployee(EmployeeDto employeeDto) {
        if (employeeDto == null) {
            return null;
        }
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
        if (transport == null) {
            return null;
        }
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
