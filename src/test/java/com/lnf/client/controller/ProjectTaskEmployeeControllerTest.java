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
import com.lnf.client.service.ProjectTaskEmployeeService;
import com.lnf.dto.client.ProjectEmployeeDto;
import com.lnf.dto.client.ProjectTaskEmployeeDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.aspectj.bridge.MessageUtil.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProjectTaskEmployeeControllerTest extends BaseTestClass {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ProjectTaskEmployeeService service;
    private UUID projectId;
    private UUID taskId;

    @BeforeAll
    void beforeAll() {
        projectId = UUID.fromString("b24fbc1f-cc46-4a98-aa47-93457bc0afa0");
        taskId = UUID.fromString("50ec6a06-8625-41dc-b804-9c82693a6c92");
    }

    @BeforeEach
    void setUp() {
        // Common setup code if necessary
    }

    @Test
    void findAllAssignedEmployeesWithPagination() throws Exception {
        // Mock data
        UUID projectId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        Map<String, Object> expectedResponse = new HashMap<>();
        expectedResponse.put("data", new ProjectEmployeeDto());
        expectedResponse.put("totalSize", 100);
        expectedResponse.put("pageSize", 10);

        // Mocking service method
        given(service.findAllAssignedEmployees(any(UUID.class), any(UUID.class), anyInt(), anyInt())).willReturn(expectedResponse);

        // Perform the request with pagination and assert the response
        int page = 1;
        int size = 10;
        mockMvc.perform(get("/lnf/projects/{projectId}/tasks/{taskId}/employees?page={page}&size={size}", projectId, taskId, page, size))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(asJsonString(expectedResponse)));

        // Verify service method invocation
        verify(service, times(1)).findAllAssignedEmployees(projectId, taskId, page, size);
    }

    @Test
    void findEmployeesByProjectIdAndTaskId() throws Exception {
        ProjectTaskEmployeeDto expectedDto = new ProjectTaskEmployeeDto();

        given(service.findEmployeesByProjectIdAndTaskId(any((UUID.class)), any((UUID.class)))).willReturn(expectedDto);

        mockMvc.perform(get("/lnf/projects/{projectId}/tasks/{taskId}/employees", projectId, taskId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(asJsonString(expectedDto)));

        verify(service, times(1)).findEmployeesByProjectIdAndTaskId(any(UUID.class), any((UUID.class)));
    }

    @Test
    void addEmployeesToProjectAndTask() {
        List<String> requestedDto = List.of("HRD-FE-TF-1008","HRD-FE-TF-1009");

        String url = "/lnf/projects/" + projectId + "/tasks/" + taskId +"/employees";

        doNothing().when(service).addEmployeesToProjectAndTask(any((UUID.class)),any((UUID.class)), eq(requestedDto));

        try {
            mockMvc.perform(MockMvcRequestBuilders.post(url)
                            .contentType(APPLICATION_JSON)
                            .content(asJsonString(requestedDto)))
                    .andExpect(status().isCreated());
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
        verify(service, times(1)).addEmployeesToProjectAndTask(any((UUID.class)), any((UUID.class)), eq(requestedDto));
    }

    @Test
    void addAllEmployeesToProjectAndTask() {
        List<String> requestedDto = List.of("HRD-FE-TF-1008","HRD-FE-TF-1009");

        String url = "/lnf/projects/" + projectId + "/tasks/" + taskId +"/project-employees";

        doNothing().when(service).addAllEmployeesToProjectAndTask(any((UUID.class)),any((UUID.class)));

        try {
            mockMvc.perform(MockMvcRequestBuilders.post(url)
                            .contentType(APPLICATION_JSON)
                            .content(asJsonString(requestedDto)))
                    .andExpect(status().isCreated());
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
        verify(service, times(1)).addAllEmployeesToProjectAndTask(any((UUID.class)), any((UUID.class)));
    }


    @Test
    void removeEmployeesFromProjectAndTask() throws Exception {
        List<String> requestedDto = List.of("HRD-FE-TF-1008","HRD-FE-TF-1009");
        String url = "/lnf/projects/" + projectId + "/tasks/" + taskId +"/employees";

        mockMvc.perform(delete(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(requestedDto)))
                .andExpect(status().isNoContent());

        verify(service).removeEmployeesFromProjectAndTask(projectId, taskId, requestedDto);
    }

    @Test
    public void testClearCaches() throws Exception {

        String url = "/lnf//projectTaskEmployees/refresh";
        // When
        ResultActions resultActions = mockMvc.perform(post(url));

        // Then
        resultActions.andExpect(status().isCreated());
        verify(service, times(1)).clearProjectTaskEmployeesCache();
    }

    @Test
    public void testAddAllTasksToEmployees() throws Exception {
        UUID projectId = UUID.randomUUID();
        List<String> employeeIds = List.of("HRD-FE-TF-1008","HRD-FE-TF-1009");

        String url = "/lnf/projects/" + projectId + "/tasks/employees";

        mockMvc.perform(MockMvcRequestBuilders.post(url)
                        .contentType(APPLICATION_JSON)
                        .content(asJsonString(employeeIds)))
                .andExpect(status().isCreated());


        verify(service, times(1)).addAllTasksToEmployee(projectId, employeeIds);
    }

    @Test
    public void testAddAllTasksToEmployee() throws Exception {
        UUID projectId = UUID.randomUUID();
        String employeeId = "HRD-FE-TF-1009";

        String url = "/lnf/projects/" + projectId + "/employee/"+ employeeId ;

        // When
        ResultActions resultActions = mockMvc.perform(post(url));

        // Then
        resultActions.andExpect(status().isCreated());
        verify(service, times(1)).addAllTasksToEmployee(projectId, employeeId);
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
