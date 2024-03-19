package com.technofacts.lnf.client.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.technofacts.lnf.client.service.ProjectService;
import com.technofacts.lnf.dto.client.ClientDto;
import com.technofacts.lnf.dto.client.ProjectDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.aspectj.bridge.MessageUtil.fail;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ProjectService service;

    @Test
    void search() throws Exception {
        String status = "Active";

        List<ProjectDto> expectedDTOs = List.of(mockProject1(), mockProject2());

        given(service.findAll(status)).willReturn(expectedDTOs);

        String url = "/lnf/projects?search=" + status;

        mockMvc.perform(get(url)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(expectedDTOs.size()));

        verify(service, times(1)).findAll(status);
    }

    @Test
    void findByProjectId() throws Exception {
        UUID projectId = UUID.fromString("cfe94b9f-c86f-4733-be96-a9b619f7bca7");
        ProjectDto expectedDto = mockProject1();

        given(service.findByProjectId(any(UUID.class))).willReturn(expectedDto);

        String url = "/lnf/projects/" + projectId;

        performAndVerifyGet(url, status().isOk(), projectId.toString(), expectedDto);

        verify(service, times(1)).findByProjectId(any(UUID.class));
    }

    @Test
    void create() {

        ProjectDto requestDto = mockProject1();

        String url = "/lnf/projects";

        doNothing().when(service).create((requestDto));

        try {
            mockMvc.perform(MockMvcRequestBuilders.post(url)
                            .contentType(APPLICATION_JSON)
                            .content(asJsonString(requestDto)))
                    .andExpect(status().isCreated());
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }

        verify(service, times(1)).create(any(ProjectDto.class));
    }

    @Test
    void update() {
        UUID projectId = UUID.fromString("e17a4ac7-873f-460e-9b38-eb02d7bb8ba7");
        ProjectDto updatedProject = mockProject2();
        updatedProject.setId(projectId);

        Mockito.doNothing().when(service).update(Mockito.eq(projectId), Mockito.any(ProjectDto.class));
        String url = "/lnf/projects/" + projectId;
        ArgumentCaptor<ProjectDto> captor = ArgumentCaptor.forClass(ProjectDto.class);

        // Act
        try {
            mockMvc.perform(put(url)
                            .content(asJsonString(updatedProject)) // Convert ProjectDto to JSON string
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        } catch (Exception e) {
            Assertions.fail("Unexpected exception: " + e.getMessage());
        }

        //verify the service
        verify(service).update(eq(projectId), any(ProjectDto.class));

        // Assert
        Mockito.verify(service, times(1)).update(eq(projectId), captor.capture());
        ProjectDto actualType = captor.getValue();

        assertEquals(updatedProject.getId(), actualType.getId(), "Address IDs should match");
        assertEquals(updatedProject.getName(), actualType.getName(),"Address name should match");
    }

    @Test
    void testDeleteById() throws Exception {
        UUID projectId = UUID.fromString("cfe94b9f-c86f-4733-be96-a9b619f7bca7");
        String url = "/lnf/projects/" + projectId;
        mockMvc.perform(delete(url)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(service).delete(projectId);
    }

    private ProjectDto mockProject1() {
        return createProject("cfe94b9f-c86f-4733-be96-a9b619f7bca7", "PRJ83", "GSTIN139302hk2", "Project04", "Project Description",
                             "PO26032021", "Fixed", "active", "INR", "8500000.0", "8","Quarterly", "2024-12-31",
                              "2025-12-31");
    }

    private ProjectDto mockProject2() {
        return createProject("e17a4ac7-873f-460e-9b38-eb02d7bb8ba7", "PRJ-020", "Data Analytics", "Project08", "Testing",
                "PO26032043", "Variable", "On-Hold", "USD", "7500000.0", "10","Monthly", "2024-02-01",
                "2025-12-31");
    }

    private ProjectDto createProject(String id, String code, String name, String type, String description, String purchaseOrder,
                                     String budgetTerms, String status, String currency, String budget, String hoursPerDay,
                                     String billingTerm, String startDate, String endDate) {
        ProjectDto dto = new ProjectDto();
        dto.setId(UUID.fromString(id));
        dto.setCode(code);
        dto.setName(name);
        dto.setType(type);
        dto.setDescription(description);
        dto.setPurchaseOrder(purchaseOrder);
        dto.setBudgetTerms(budgetTerms);
        dto.setStatus(status);
        dto.setCurrency(currency);
        dto.setBudget(new BigDecimal(budget));
        dto.setHoursPerDay(Integer.valueOf(hoursPerDay));
        dto.setBillingTerm(billingTerm);
        dto.setStartDate(LocalDate.parse(startDate));
        dto.setEndDate(LocalDate.parse(endDate));


        return dto;
    }

    private void performAndVerifyGet(String url, ResultMatcher statusMatcher, String containsString,
                                     ProjectDto expectedDto) throws Exception {
        mockMvc.perform(get(url))
                .andExpect(statusMatcher)
                .andExpect(content().string(containsString(containsString)))
                .andExpect(jsonPath("$.id").value(expectedDto.getId().toString())) // Validate ID
                .andExpect(jsonPath("$.code").value(expectedDto.getCode()))
                .andExpect(jsonPath("$.name").value(expectedDto.getName()))
                .andExpect(jsonPath("$.type").value(expectedDto.getType()))
                .andExpect(jsonPath("$.description").value(expectedDto.getDescription()))
                .andExpect(jsonPath("$.purchaseOrder").value(expectedDto.getPurchaseOrder()))
                .andExpect(jsonPath("$.budgetTerms").value(expectedDto.getBudgetTerms()))
                .andExpect(jsonPath("$.status").value(expectedDto.getStatus()))
                .andExpect(jsonPath("$.currency").value(expectedDto.getCurrency()))
                .andExpect(jsonPath("$.budget").value(expectedDto.getBudget().toString()))
                .andExpect(jsonPath("$.hoursPerDay").value(expectedDto.getHoursPerDay()))
                .andExpect(jsonPath("$.billingTerm").value(expectedDto.getBillingTerm()))
                .andExpect(jsonPath("$.startDate").value(expectedDto.getStartDate().toString()))
                .andExpect(jsonPath("$.endDate").value(expectedDto.getEndDate().toString()));
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
