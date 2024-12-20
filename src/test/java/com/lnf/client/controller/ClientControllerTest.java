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
import com.lnf.client.service.ClientService;
import com.lnf.client.service.DataExportService;
import com.lnf.client.service.ProjectService;
import com.lnf.dto.client.*;
import com.lnf.dto.common.PageRequestDto;
import com.lnf.service.common.page.PaginationAndSortingHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.aspectj.bridge.MessageUtil.fail;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ClientControllerTest extends BaseTestClass {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ClientService service;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private DataExportService dataExportService;
    private UUID clientId;
    @Autowired
    private PaginationAndSortingHandler paginationAndSortingHandler;

    @BeforeAll
    void beforeAll() {
        clientId = UUID.fromString("123e4567-e89b-12d3-a456-556642440000");
    }

    @BeforeEach
    void setUp() {
        // Common setup code if necessary
    }

    @Test
    void findAll() {
        Page<ClientOverviewDto> mockedPage = mock(Page.class);
        PageRequestDto pageRequest = new PageRequestDto(0, 10, "degree", "asc");

        List<ClientOverviewDto> mockedList = List.of(createClientOverview());
        when(service.findPaginatedAndSorted(0, 10, "degree", "asc")).thenReturn(mockedPage);
        when(service.findPaginated(0, 10)).thenReturn(mockedPage);
        when(service.findAllSorted("degree", "asc")).thenReturn(mockedList);
        when(service.findAll()).thenReturn(mockedList);

        ClientController controller = new ClientController(service, projectService, paginationAndSortingHandler, dataExportService);
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
    void testSearch() throws Exception {
        String status = "Active";

        List<ClientDto> expectedDTOs = Arrays.asList(createClient1(), createClient2());

        given(service.findAll(status)).willReturn(expectedDTOs);

        String url = "/lnf/clients?search=" + status;

        mockMvc.perform(get(url)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(expectedDTOs.size()));

        verify(service, times(1)).findAll(status);
    }

    @Test
    void testFindByClientId() throws Exception {

        ClientDto expectedDto = createClient1();

        given(service.findByClientId(any(UUID.class))).willReturn(expectedDto);

        String url = "/lnf/clients/" + clientId;

        performAndVerifyGet(url, status().isOk(), String.valueOf(clientId), expectedDto);

        verify(service, times(1)).findByClientId(any(UUID.class));
    }

    @Test
    void testFindProjectsByClientId() throws Exception {

        List<ProjectOverviewDto> expectedDto = List.of(mockProjectOverview1());

        given(projectService.findProjectsByClientId(any(UUID.class))).willReturn(expectedDto);

        String url = "/lnf/clients/" + clientId + "/projects";

        String resultContent = new String(Files
                .readAllBytes(Path.of(ClassLoader.getSystemResource("testdata/client-project.json")
                        .toURI())));

        mockMvc.perform(get(url)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(resultContent));

        verify(projectService, times(1)).findProjectsByClientId(any(UUID.class));
    }

    @Test
    void testFindEmployeesByClientId() throws Exception {
        UUID clientId = UUID.fromString("030508ae-1ba8-4d0b-bf38-f99d09b1fee3");

        List<ClientEmployeeDto> expectedDto = Arrays.asList(mockEmployee1(), mockEmployee2());

        given(projectService.findEmployeesByClientId(any(UUID.class))).willReturn(expectedDto);

        String url = "/lnf/clients/" + clientId + "/employees";

        String resultContent = new String(Files
                .readAllBytes(Path.of(ClassLoader.getSystemResource("testdata/employee.json")
                        .toURI())));

        mockMvc.perform(get(url)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(resultContent));

        verify(projectService, times(1)).findEmployeesByClientId(any(UUID.class));
    }

    @Test
    void testCreate() {
        ClientDto requestDto = createClient1();

        String url = "/lnf/clients";

        doNothing().when(service).create(any(ClientDto.class));

        try {
            mockMvc.perform(MockMvcRequestBuilders.post(url)
                            .contentType(APPLICATION_JSON)
                            .content(asJsonString(requestDto)))
                    .andExpect(status().isCreated());
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }

        verify(service, times(1)).create(any(ClientDto.class));

    }

    @Test
    void testUpdate() {
        ClientDto updatedClient = createClient1();
        updatedClient.setId(clientId);

        Mockito.doNothing().when(service).update(Mockito.eq(clientId), Mockito.any(ClientDto.class));
        String url = "/lnf/clients/" + clientId;
        ArgumentCaptor<ClientDto> captor = ArgumentCaptor.forClass(ClientDto.class);

        // Act
        try {
            mockMvc.perform(put(url)
                            .content(asJsonString(updatedClient)) // Convert ClientDto to JSON string
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        } catch (Exception e) {
            Assertions.fail("Unexpected exception: " + e.getMessage());
        }

        //verify the service
        verify(service).update(eq(clientId), any(ClientDto.class));

        // Assert
        Mockito.verify(service, times(1)).update(eq(clientId), captor.capture());
        ClientDto actualType = captor.getValue();

        assertEquals(updatedClient.getId(), actualType.getId(), "Client IDs should match");
        assertEquals(updatedClient.getCode(), actualType.getCode(), "Client code should match");
        assertEquals(updatedClient.getName(), actualType.getName(), "Client name should match");
        assertEquals(updatedClient.getPan(), actualType.getPan(), "Client PAN should match");
        assertEquals(updatedClient.getTan(), actualType.getTan(), "Client TAN should match");
        assertEquals(updatedClient.getStatus(), actualType.getStatus(), "Client status should match");
        assertEquals(updatedClient.getWorkingFrom(), actualType.getWorkingFrom(), "Client working from should match");
        assertEquals(updatedClient.getAgreementExpiryDate(), actualType.getAgreementExpiryDate(), "Client agreement expiry date should match");
        assertEquals(updatedClient.getServiceType(), actualType.getServiceType(), "Client service type should match");
        assertEquals(updatedClient.getClientDetails(), actualType.getClientDetails(), "Client details should match");
    }

    @Test
    void testDeleteByClientId() throws Exception {
        String url = "/lnf/clients/" + clientId;
        mockMvc.perform(delete(url)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(service).delete(clientId);
    }

    @Test
    public void testHealth() throws Exception {

        String url = "/lnf/clients/health";
        // When
        ResultActions resultActions = mockMvc.perform(get(url));

        // Then
        resultActions.andExpect(status().isOk())
                .andExpect(content().string("Client SVC healthy!"));
    }

    @Test
    public void testClearCaches() throws Exception {

        String url = "/lnf/clients/refresh";
        // When
        ResultActions resultActions = mockMvc.perform(post(url));

        // Then
        resultActions.andExpect(status().isCreated());
        verify(service, times(1)).clearClientsCache();
    }

    private ClientDto createClient1() {
        return createClientDto("123e4567-e89b-12d3-a456-556642440000", "WXA-001778", "wexa", "PAN0065NUM",
                "QRES03023F", "Completed", "2021-08-21", "2024-03-25", "Permanent", "Dynpro Pvt Ltd");
    }

    private ClientDto createClient2() {
        return createClientDto("1b49b31c-573a-4b47-b13f-01d320e11ec5", "ASH-001", "Ashield", "KMUYT4390N",
                "ASWQ03028F", "On-Hold", "2024-02-01", "2025-12-31", "Staffing", "Accenture");
    }

    private ClientOverviewDto createClientOverview() {
        return createClientOverviewDto("123e4567-e89b-12d3-a456-556642440000", "WXA-001778", "wexa", "PAN0065NUM",
                "QRES03023F", "Completed", "2021-08-21", "2024-03-25");
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

    private ClientOverviewDto createClientOverviewDto(String id, String code, String name, String pan, String tan, String status,
                                                      String workingFrom, String agreementExpiryDate) {
        ClientOverviewDto dto = new ClientOverviewDto();
        dto.setId(UUID.fromString(id));
        dto.setCode(code);
        dto.setName(name);
        dto.setPan(pan);
        dto.setTan(tan);
        dto.setStatus(status);
        dto.setWorkingFrom(LocalDate.parse(workingFrom));
        dto.setAgreementExpiryDate(LocalDate.parse(agreementExpiryDate));

        return dto;
    }

    private void performAndVerifyGet(String url, ResultMatcher statusMatcher, String containsString,
                                     ClientDto expectedDto) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(url))
                .andExpect(statusMatcher)
                .andExpect(content().string(containsString(containsString)))
                .andExpect(jsonPath("$.id").value(expectedDto.getId().toString()))
                .andExpect(jsonPath("$.code").value(expectedDto.getCode()))
                .andExpect(jsonPath("$.name").value(expectedDto.getName())) // Adjusted for name field
                .andExpect(jsonPath("$.pan").value(expectedDto.getPan()))
                .andExpect(jsonPath("$.tan").value(expectedDto.getTan())) // Adjusted for tan field
                .andExpect(jsonPath("$.status").value(expectedDto.getStatus()))
                .andExpect(jsonPath("$.workingFrom").value(expectedDto.getWorkingFrom().toString()))
                .andExpect(jsonPath("$.agreementExpiryDate").value(expectedDto.getAgreementExpiryDate().toString()))
                .andExpect(jsonPath("$.serviceType").value(expectedDto.getServiceType()))
                .andExpect(jsonPath("$.clientDetails").value(expectedDto.getClientDetails()));
    }

    private ProjectOverviewDto mockProjectOverview1() {
        return createProjectOverview("e17a4ac7-873f-460e-9b38-eb02d7bb8ba7", "PRJ-020", "Data Analytics", "Project08", "Testing",
                "PO26032043", "8500000.00", "Variable", "On-Hold", "10", "Monthly", "2024-02-01", UUID.fromString("45a83fb6-78e0-45ff-9507-2781893722b7"),
                "Ajax technologies", "2024-05-01"
        );
    }

    private ProjectOverviewDto createProjectOverview(String id, String code, String name, String type, String description, String purchaseOrder,
                                                     String budget, String budgetTerms, String status, String hoursPerDay,
                                                     String billingTerm, String startDate, UUID clientId, String clientName, String endDate) {
        ProjectOverviewDto dto = new ProjectOverviewDto();
        dto.setId(UUID.fromString(id));
        dto.setCode(code);
        dto.setName(name);
        dto.setType(type);
        dto.setDescription(description);
        dto.setPurchaseOrder(purchaseOrder);
        dto.setBudget(new BigDecimal(budget));
        dto.setBudgetTerms(budgetTerms);
        dto.setStatus(status);
        dto.setHoursPerDay(Integer.valueOf(hoursPerDay));
        dto.setBillingTerm(billingTerm);
        dto.setStartDate(LocalDate.parse(startDate));
        dto.setClientId(clientId);
        dto.setClientName(clientName);
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

    private ClientEmployeeDto mockEmployee1() {
        return createEmployee("011becae-fd68-46c3-a857-59153d98a1b8", "HRD-TE-TF-5030", "Vikrant", "thakur", "Vicky@gmail.com",
                "9676099703", "ACTIVE", "BA");
    }

    private ClientEmployeeDto mockEmployee2() {
        return createEmployee("f12b86f8-7dab-42c6-a2db-6cf82e7e9e33", "HRD-CE-TF-3033", "Kushbu", "sharma", "Kushbu@gmail.com",
                "9876543210", "TERMINATED", "SE");
    }

    private void performAndVerifyGet(String url, ResultMatcher statusMatcher, String containsString,
                                     ClientEmployeeDto expectedDto) throws Exception {
        mockMvc.perform(get(url))
                .andExpect(statusMatcher)
                .andExpect(content().string(containsString(containsString)))
                .andExpect(jsonPath("$.id").value(expectedDto.getId().toString()))
                .andExpect(jsonPath("$.employeeId").value(expectedDto.getEmployeeId()))
                .andExpect(jsonPath("$.firstName").value(expectedDto.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(expectedDto.getLastName()))
                .andExpect(jsonPath("$.email").value(expectedDto.getEmail()))
                .andExpect(jsonPath("$.mobileNumber").value(expectedDto.getMobileNumber()))
                .andExpect(jsonPath("$.employmentStatus").value(expectedDto.getEmploymentStatus()))
                .andExpect(jsonPath("$.designation").value(expectedDto.getDesignation()));
    }

    private ClientEmployeeDto createEmployee(String id, String employeeId, String firstName, String lastName,
                                             String email, String mobileNumber, String employeeStatus, String designation) {
        ClientEmployeeDto dto = new ClientEmployeeDto();
        dto.setId(UUID.fromString(id));
        dto.setEmployeeId(employeeId);
        dto.setFirstName(firstName);
        dto.setLastName(lastName);
        dto.setEmail(email);
        dto.setMobileNumber(mobileNumber);
        dto.setEmploymentStatus(employeeStatus);
        dto.setDesignation(designation);

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
