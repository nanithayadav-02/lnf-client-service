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

package com.lnf.client.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.lnf.client.BaseTestClass;
import com.lnf.client.service.ProjectEmployeeService;
import com.lnf.dto.client.ProjectEmployeeDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.*;

import static org.aspectj.bridge.MessageUtil.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProjectEmployeeControllerTest extends BaseTestClass {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProjectEmployeeService service;

    private UUID projectId;

    @BeforeAll
    void beforeAll() {
        projectId = UUID.fromString("b24fbc1f-cc46-4a98-aa47-93457bc0afa0");
    }

    @BeforeEach
    void setUp() {
        // Common setup code if necessary
    }

    @Test
    void findEmployeesByProjectId() throws Exception {
        // Mock data
        UUID projectId = UUID.randomUUID();
        ProjectEmployeeDto expectedDto = new ProjectEmployeeDto();

        // Mocking service method
        given(service.findEmployeesByProjectId(any(UUID.class))).willReturn(expectedDto);

        // Perform the request and assert the response
        mockMvc.perform(get("/lnf/projects/{projectId}/employees", projectId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(asJsonString(expectedDto)));

        // Verify service method invocation
        verify(service, times(1)).findEmployeesByProjectId(projectId);
    }

    @Test
    void findAllAssignedEmployeesWithPagination() throws Exception {
        // Mock data
        UUID projectId = UUID.randomUUID();
        Map<String, Object> expectedResponse = new HashMap<>();
        expectedResponse.put("data", new ProjectEmployeeDto());
        expectedResponse.put("totalSize", 100);
        expectedResponse.put("pageSize", 10);

        // Mocking service method
        given(service.findAllAssignedEmployees(any(UUID.class), anyInt(), anyInt())).willReturn(expectedResponse);

        // Perform the request with pagination and assert the response
        int page = 1;
        int size = 10;
        mockMvc.perform(get("/lnf/projects/{projectId}/employees?page={page}&size={size}", projectId, page, size))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(asJsonString(expectedResponse)));

        // Verify service method invocation
        verify(service, times(1)).findAllAssignedEmployees(projectId, page, size);
    }

    @Test
    void addEmployeesToProject() {
        List<String> employeeIds = List.of("HRD-FE-TF-1008", "HRD-FE-TF-1009");
        String url = "/lnf/projects/" + projectId + "/employees?type=specific";

        doNothing().when(service).addEmployeeToProject(any(UUID.class), eq(employeeIds));

        try {
            mockMvc.perform(MockMvcRequestBuilders.post(url)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJsonString(employeeIds)))
                    .andExpect(status().isCreated());
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
        verify(service, times(1)).addEmployeeToProject(any(UUID.class), eq(employeeIds));
    }

    @Test
    void addAllActiveEmployeesToProject() throws Exception {
        List<String> statuses = List.of("ACTIVE", "INACTIVE");
        String url = "/lnf/projects/" + projectId + "/employees?type=active";

        doNothing().when(service).addAllActiveEmployeeToProject(any(UUID.class), eq(statuses));

        mockMvc.perform(MockMvcRequestBuilders.post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(statuses)))
                .andExpect(status().isCreated());

        verify(service, times(1)).addAllActiveEmployeeToProject(any(UUID.class), eq(statuses));
    }

    @Test
    void removeEmployeesFromProject() throws Exception {

        List<String> requestedDto = List.of("HRD-FE-TF-1008","HRD-FE-TF-1009");
        String url = "/lnf/projects/" + projectId + "/employees";

        mockMvc.perform(delete(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(requestedDto)))
                .andExpect(status().isNoContent());

        verify(service).removeEmployeeFromProject(projectId, requestedDto);
    }

    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper()
                    .registerModule(new JavaTimeModule())
                    .writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
