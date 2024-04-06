package com.technofacts.lnf.client.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.technofacts.lnf.client.BaseTestClass;
import com.technofacts.lnf.client.service.EmployeeProjectTaskService;
import com.technofacts.lnf.dto.client.EmployeeProjectTaskDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EmployeeProjectTaskControllerTest extends BaseTestClass {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private EmployeeProjectTaskService service;
    private String employeeId;
    private UUID projectId;

    @BeforeAll
    void beforeAll() {
        employeeId = "HRD-FE-TF-1008";
        projectId = UUID.fromString("b24fbc1f-cc46-4a98-aa47-93457bc0afa0");
    }

    @BeforeEach
    void setUp() {
        // Common setup code if necessary
    }

    @Test
    void findTasksByEmployeeIdAndProjectId() throws Exception {

        EmployeeProjectTaskDto expectedDto = new EmployeeProjectTaskDto();

        given(service.findTasksByEmployeeIdAndProjectId(any(String.class), any(UUID.class))).willReturn(expectedDto);

        mockMvc.perform(get("/lnf/projects/{projectId}/task", projectId)
                .param("employeeId", employeeId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(asJsonString(expectedDto)));

        verify(service, times(1)).findTasksByEmployeeIdAndProjectId(any(String.class), any(UUID.class));
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
