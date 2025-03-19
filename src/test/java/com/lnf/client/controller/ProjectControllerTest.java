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
import com.lnf.client.service.DataExportService;
import com.lnf.client.service.ProjectService;
import com.lnf.dto.client.ProjectDto;
import com.lnf.dto.client.ProjectOverviewDto;
import com.lnf.dto.common.PageRequestDto;
import com.lnf.service.common.page.PaginationAndSortingHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

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

class ProjectControllerTest extends BaseTestClass {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ProjectService service;
    @Autowired
    private PaginationAndSortingHandler paginationAndSortingHandler;
    @Autowired
    private DataExportService dataExportService;

    @Test
    void findAll() {
        Page<ProjectOverviewDto> mockedPage = mock(Page.class);
        PageRequestDto pageRequest = new PageRequestDto(0, 10, "degree", "asc");

        List<ProjectOverviewDto> mockedList = List.of(mockProjectOverview());
        when(service.findPaginatedAndSorted(0, 10, "degree", "asc")).thenReturn(mockedPage);
        when(service.findPaginated(0, 10)).thenReturn(mockedPage);
        when(service.findAllSorted("degree", "asc")).thenReturn(mockedList);
        when(service.findAll()).thenReturn(mockedList);

        ProjectController controller = new ProjectController(service, paginationAndSortingHandler, dataExportService);
        // Test for paginated and sorted request
        ResponseEntity<?> response = controller.findAll(pageRequest);
        assertEquals(ResponseEntity.ok(mockedPage), response);

        // Pagination with  sortBy and sortOrder
        pageRequest = new PageRequestDto(0, 10, null, null);
        response = paginationAndSortingHandler.handleFindAllRequest(pageRequest, service);
        assertEquals(ResponseEntity.ok(mockedPage), response);

        // Pagination with sortBy and sortOrder
        pageRequest = new PageRequestDto(null, null, "degree", "asc");
        response = paginationAndSortingHandler.handleFindAllRequest(pageRequest, service);
        assertEquals(ResponseEntity.ok(mockedList), response);

        //find All
        pageRequest = new PageRequestDto();
        response = paginationAndSortingHandler.handleFindAllRequest(pageRequest, service);
        assertEquals(ResponseEntity.ok(mockedList), response);
    }

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
        assertEquals(updatedProject.getName(), actualType.getName(), "Address name should match");
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

    @Test
    void testClearCaches() throws Exception {
        // When
        ResultActions resultActions = mockMvc.perform(post("/lnf/projects/refresh"));

        // Then
        resultActions.andExpect(status().isCreated());
        verify(service, times(1)).clearProjectsCache();
    }

    @Test
    void testGetTimeSheetsByClientId() throws Exception {
        // Given
        UUID clientId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        Optional<Integer> month = Optional.of(5);
        Optional<Integer> year = Optional.of(2024);
        List<Map<String, Object>> timesheets = createMockEmployeeTimesheet();
        String status = "Approved";

        when(service.getTimeSheetsByClientId(clientId, projectId, month, year, status)).thenReturn(timesheets);

        mockMvc.perform(get("/lnf/projects/clients/{clientId}", clientId)
                .param("projectId", projectId.toString())
                .param("month", month.get().toString())
                .param("year", year.get().toString())
                .param("status", status)
                .accept(MediaType.APPLICATION_JSON));

        verify(service, times(1)).getTimeSheetsByClientId(clientId, projectId, month, year, status);
    }

    public List<Map<String, Object>> createMockEmployeeTimesheet() {

        String employeeId = "HRD-NE-TF-4028";
        String employeeName = "Kushbu ";
        double totalHours = 176.0;
        LocalDate date = LocalDate.parse("2023-05-31");
        double hours = 8.0;

        Map<String, Object> timesheetEntry = new HashMap<>();
        timesheetEntry.put("date", date.toString());
        timesheetEntry.put("hours", hours);

        List<String> timesheetEntryList = new ArrayList<>();
        timesheetEntryList.add(timesheetEntry.toString());

        Map<String, Object> employeeTimesheet = new HashMap<>();
        employeeTimesheet.put("employeeId", employeeId);
        employeeTimesheet.put(employeeName, employeeName);
        employeeTimesheet.put("timesheetEntryList", timesheetEntryList);
        employeeTimesheet.put("totalHours", totalHours);

        return List.of(employeeTimesheet);
    }

    private ProjectDto mockProject1() {
        return createProject("cfe94b9f-c86f-4733-be96-a9b619f7bca7", "PRJ83", "GSTIN139302hk2", "Project04", "Project Description",
                "PO26032021", "Fixed", "active", "INR", "8500000.0", "8", "Quarterly", "2024-12-31",
                "2025-12-31");
    }

    private ProjectDto mockProject2() {
        return createProject("e17a4ac7-873f-460e-9b38-eb02d7bb8ba7", "PRJ-020", "Data Analytics", "Project08", "Testing",
                "PO26032043", "Variable", "On-Hold", "USD", "7500000.0", "10", "Monthly", "2024-02-01",
                "2025-12-31");
    }

    private ProjectOverviewDto mockProjectOverview() {
        return createProjectOverview("cfe94b9f-c86f-4733-be96-a9b619f7bca7", "PRJ83", "GSTIN139302hk2", "Project04", "Project Description",
                "PO26032021", "Fixed", "active");
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

    private ProjectOverviewDto createProjectOverview(String id, String code, String name, String type, String description, String purchaseOrder,
                                                     String budgetTerms, String status) {
        ProjectOverviewDto dto = new ProjectOverviewDto();
        dto.setId(UUID.fromString(id));
        dto.setCode(code);
        dto.setName(name);
        dto.setType(type);
        dto.setDescription(description);
        dto.setPurchaseOrder(purchaseOrder);
        dto.setBudgetTerms(budgetTerms);
        dto.setStatus(status);

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
