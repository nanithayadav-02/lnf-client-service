/*
 * Copyright (c)  Lever And Fulcrum (LNF)
 */
package com.technofacts.lnf.client.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.technofacts.lnf.client.BaseTestClass;
import com.technofacts.lnf.client.service.ClientDirectoryService;
import com.technofacts.lnf.dto.client.ClientDirectoryDto;
import com.technofacts.lnf.dto.common.PageRequestDto;
import com.technofacts.lnf.service.common.page.PaginationAndSortingHandler;
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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ClientDirectoriesControllerTest extends BaseTestClass {

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
        PageRequestDto pageRequest = new PageRequestDto(0, 10, "firstName", "asc");

        List<ClientDirectoryDto> mockedList = List.of(createClientDirectory1());
        when(service.findPaginatedAndSorted(0, 10, "firstName", "asc")).thenReturn(mockedPage);
        when(service.findPaginated(0, 10)).thenReturn(mockedPage);
        when(service.findAllSorted("firstName", "asc")).thenReturn(mockedList);
        when(service.findAll()).thenReturn(mockedList);
        when(service.findingAllWithPagination(searchQuery, pageRequest)).thenReturn(mockedPage);

        ClientDirectoriesController controller = new ClientDirectoriesController(service, paginationAndSortingHandler);
        // Test for paginated and sorted request
        ResponseEntity<?> response = controller.findAll(searchQuery, pageRequest);
        assertEquals(ResponseEntity.ok(mockedPage), response);

        // Pagination with  sortBy and sortOrder
        pageRequest = new PageRequestDto(0, 10, null, null);
        response = paginationAndSortingHandler.handleFindAllRequest(pageRequest, service);
        assertEquals(ResponseEntity.ok(mockedPage), response);

        // Pagination with sortBy and sortOrder
        pageRequest = new PageRequestDto(null, null, "firstName", "asc");
        response = paginationAndSortingHandler.handleFindAllRequest(pageRequest, service);
        assertEquals(ResponseEntity.ok(mockedList), response);

        //find All
        pageRequest = new PageRequestDto();
        response = paginationAndSortingHandler.handleFindAllRequest(pageRequest, service);
        assertEquals(ResponseEntity.ok(mockedList), response);
    }

    @Test
    public void testCreateClientDirectories() throws Exception {
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
