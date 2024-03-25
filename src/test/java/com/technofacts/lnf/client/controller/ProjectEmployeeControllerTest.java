package com.technofacts.lnf.client.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.technofacts.lnf.client.BaseTestClass;
import com.technofacts.lnf.client.service.ProjectEmployeeService;
import com.technofacts.lnf.dto.client.ProjectEmployeeDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;
import java.util.UUID;

import static org.aspectj.bridge.MessageUtil.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
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
        ProjectEmployeeDto expectedDto = new ProjectEmployeeDto();

        given(service.findEmployeesByProjectId(any(UUID.class))).willReturn(expectedDto);

        mockMvc.perform(get("/lnf/projects/{projectId}/employees", projectId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(asJsonString(expectedDto)));

        verify(service, times(1)).findEmployeesByProjectId(any(UUID.class));
    }

    @Test
    void addEmployeesToProject() {
        List<String> requestedDto = List.of("HRD-FE-TF-1008","HRD-FE-TF-1009");

        String url = "/lnf/projects/" + projectId + "/employees";

        doNothing().when(service).addEmployeeToProject(any((UUID.class)), eq(requestedDto));

        try {
            mockMvc.perform(MockMvcRequestBuilders.post(url)
                            .contentType(APPLICATION_JSON)
                            .content(asJsonString(requestedDto)))
                    .andExpect(status().isCreated());
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
        verify(service, times(1)).addEmployeeToProject(any((UUID.class)), eq(requestedDto));
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
