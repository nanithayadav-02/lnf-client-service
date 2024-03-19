package com.technofacts.lnf.client.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.technofacts.lnf.client.service.DashboardService;
import com.technofacts.lnf.dto.common.DashboardDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private DashboardService service;

    @BeforeAll
    void beforeAll() {
        //To be implemented
    }

    @BeforeEach
    void setUp() {
        // Common setup code if necessary
    }

    @Test
    void getClientDashboardStatistics() throws Exception {
        DashboardDto expectedDto = new DashboardDto();

        given(service.getClientDashboardStatistics()).willReturn(expectedDto);

        String url = "/lnf/dashboard/client";

        mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(asJsonString(expectedDto)));

        verify(service, times(1)).getClientDashboardStatistics();
    }

    @Test
    void getProjectDashboardStatistics() throws Exception {
        DashboardDto expectedDto = new DashboardDto();

        given(service.getProjectDashboardStatistics()).willReturn(expectedDto);

        String url = "/lnf/dashboard/project";

        mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(asJsonString(expectedDto)));

        verify(service, times(1)).getProjectDashboardStatistics();
    }

    @Test
    void getTaskDashboardStatistics() throws Exception {
        DashboardDto expectedDto = new DashboardDto();

        given(service.getTaskDashboardStatistics()).willReturn(expectedDto);

        String url = "/lnf/dashboard/task";

        mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(asJsonString(expectedDto)));

        verify(service, times(1)).getTaskDashboardStatistics();
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
