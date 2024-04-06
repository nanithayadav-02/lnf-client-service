package com.technofacts.lnf.client.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.technofacts.lnf.client.BaseTestClass;
import com.technofacts.lnf.client.service.DashboardService;
import com.technofacts.lnf.dto.client.ClientDto;
import com.technofacts.lnf.dto.common.DashboardDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DashboardControllerTest extends BaseTestClass {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
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

    @Test
    void getNewlyAddedClientDashboardStatistics() throws Exception {
        List<ClientDto> mockDto= Arrays.asList(createClient1());
        int daysAgo = 30;
        given(service.findNewlyAddedClients(daysAgo)).willReturn(mockDto);

        String url = "/lnf/dashboard/clients?addedSince="+daysAgo;
        mockMvc.perform(MockMvcRequestBuilders.get(url))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(service, times(1)).findNewlyAddedClients(daysAgo);
    }

    private ClientDto createClient1() {
        return createClientDto("1b49b31c-573a-4b47-b13f-01d320e11ec5", "ASH-001", "Ashield", "KMUYT4390N",
                "ASWQ03028F", "On-Hold", "2024-02-01", "2025-12-31","Staffing", "Accenture");
    }

    private ClientDto createClientDto(String id, String code, String name, String pan, String tan, String status, String workingFrom,
                                      String agreementExpiryDate, String serviceType, String clientDetails) {
        ClientDto dto = new ClientDto();
        dto.setId(UUID.fromString(id));
        dto.setCode(code);
        dto.setName(name);
        dto.setPan(pan);
        dto.setTan(tan);
        dto.setStatus(status);
        dto.setWorkingFrom(LocalDate.parse(workingFrom));
        dto.setAgreementExpiryDate(LocalDate.parse(agreementExpiryDate));
        dto.setServiceType(serviceType);
        dto.setClientDetails(clientDetails);

        return dto;
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
