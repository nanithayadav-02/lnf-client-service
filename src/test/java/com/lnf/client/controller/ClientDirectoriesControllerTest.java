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
import com.lnf.client.BaseTestClass;
import com.lnf.client.service.ClientDirectoryService;
import com.lnf.dto.client.ClientDirectoryDto;
import com.lnf.dto.common.PageRequestDto;
import com.lnf.service.common.page.PaginationAndSortingHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ClientDirectoriesControllerTest extends BaseTestClass {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ClientDirectoryService service;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private PaginationAndSortingHandler paginationAndSortingHandler;

    @Test
    void findAll() {
        String searchQuery = "description:the resume has to be uploaded";
        Page<ClientDirectoryDto> mockedPage = mock(Page.class);
        List<ClientDirectoryDto> mockedList = List.of(createClientDirectory1());

        when(mockedPage.getContent()).thenReturn(mockedList);

        PageRequestDto pageRequest = new PageRequestDto(0, 10, "firstName", "asc");

        UUID mockUUID = UUID.randomUUID();

        when(service.findPaginatedAndSorted(0, 10, "firstName", "asc")).thenReturn(mockedPage);
        when(service.findPaginated(0, 10)).thenReturn(mockedPage);
        when(service.findAllSorted("firstName", "asc")).thenReturn(mockedList);
        when(service.findAll()).thenReturn(mockedList);
        when(service.findingAllWithPagination(searchQuery, mockUUID, pageRequest)).thenReturn(mockedPage);

        ClientDirectoriesController controller = new ClientDirectoriesController(service, paginationAndSortingHandler);

        // Test for paginated and sorted request
        ResponseEntity<?> response = controller.findAll(searchQuery, mockUUID, pageRequest);

        // Verify that the body of the response contains the expected page content
        Assertions.assertNotNull(response.getBody(), "Response body should not be null");
        Assertions.assertTrue(response.getBody() instanceof Page, "Response body should be of type Page");
        Assertions.assertEquals(mockedList, ((Page<ClientDirectoryDto>) response.getBody()).getContent());

        // Pagination without sortBy and sortOrder
        pageRequest = new PageRequestDto(0, 10, null, null);
        response = paginationAndSortingHandler.handleFindAllRequest(pageRequest, service);
        Assertions.assertNotNull(response.getBody(), "Response body should not be null");
        Assertions.assertEquals(mockedList, ((Page<ClientDirectoryDto>) response.getBody()).getContent());

        // Pagination with sortBy and sortOrder
        pageRequest = new PageRequestDto(null, null, "firstName", "asc");
        response = paginationAndSortingHandler.handleFindAllRequest(pageRequest, service);
        Assertions.assertNotNull(response.getBody(), "Response body should not be null");
        Assertions.assertEquals(mockedList, response.getBody());

        // Find all
        pageRequest = new PageRequestDto();
        response = paginationAndSortingHandler.handleFindAllRequest(pageRequest, service);
        Assertions.assertNotNull(response.getBody(), "Response body should not be null");
        Assertions.assertEquals(mockedList, response.getBody());
    }

    @Test
    void testCreateClientDirectories() throws Exception {
        List<ClientDirectoryDto> mockDtos = Arrays.asList(createClientDirectory1(), createClientDirectory2());
        UUID clientId = UUID.fromString("cfe94b9f-c86f-4733-be96-a9b619f7bca7");
        String requestBody = objectMapper.writeValueAsString(mockDtos);

        // Act
        mockMvc.perform(MockMvcRequestBuilders.post("/lnf/clients/{clientId}/directories", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());
    }

    private ClientDirectoryDto createClientDirectory1() {
        return ClientDirectoryDto.builder()
                .id(UUID.fromString("cfe94b9f-c86f-4733-be96-a9b619f7bca7"))
                .firstName("Client")
                .lastName("One")
                .email("client1@example.com")
                .phoneNumber("123-456-7890")
                .active(true)
                .build();
    }

    private ClientDirectoryDto createClientDirectory2() {
        return ClientDirectoryDto.builder()
                .id(UUID.fromString("019d9f96-8f91-4725-9056-ed022b4cb65f"))
                .firstName("Client")
                .lastName("Two")
                .email("client2@example.com")
                .phoneNumber("098-765-4321")
                .active(false)
                .build();
    }

}
