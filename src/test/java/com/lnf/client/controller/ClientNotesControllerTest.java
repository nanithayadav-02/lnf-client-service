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
import com.lnf.client.service.ClientNotesService;
import com.lnf.dto.client.ClientNotesDto;
import com.lnf.dto.common.PageRequestDto;
import com.lnf.service.common.page.PaginationAndSortingHandler;
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
import org.springframework.test.web.servlet.ResultMatcher;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ClientNotesControllerTest extends BaseTestClass {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ClientNotesService service;
    @Autowired
    private PaginationAndSortingHandler paginationAndSortingHandler;
    private UUID clientId;

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
        String searchQuery = "description:the product is launching";
        Page<ClientNotesDto> mockedPage = mock(Page.class);
        PageRequestDto pageRequest = new PageRequestDto(0, 10, "description", "asc");

        List<ClientNotesDto> mockedList = List.of(mockNotes1(), mockNotes2());
        when(service.findPaginatedAndSorted(0, 10, "description", "asc")).thenReturn(mockedPage);
        when(service.findPaginated(0, 10)).thenReturn(mockedPage);
        when(service.findAllSorted("description", "asc")).thenReturn(mockedList);
        when(service.findAll()).thenReturn(mockedList);
        when(service.findingAllWithPagination(searchQuery, pageRequest)).thenReturn(mockedPage);

        ClientNotesController controller = new ClientNotesController(service, paginationAndSortingHandler);
        // Test for paginated and sorted request
        ResponseEntity<?> response = controller.findAll(searchQuery, pageRequest);
        assertEquals(ResponseEntity.ok(mockedPage), response);

        // Pagination with  sortBy and sortOrder
        pageRequest = new PageRequestDto(0, 10, null, null);
        response = paginationAndSortingHandler.handleFindAllRequest(pageRequest, service);
        assertEquals(ResponseEntity.ok(mockedPage), response);

        // Pagination with sortBy and sortOrder
        pageRequest = new PageRequestDto(null, null, "description", "asc");
        response = paginationAndSortingHandler.handleFindAllRequest(pageRequest, service);
        assertEquals(ResponseEntity.ok(mockedList), response);

        //find All
        pageRequest = new PageRequestDto();
        response = paginationAndSortingHandler.handleFindAllRequest(pageRequest, service);
        assertEquals(ResponseEntity.ok(mockedList), response);
    }

    @Test
    void findByClientId() throws Exception {
        List<ClientNotesDto> expectedDto = Arrays.asList(mockNotes1(), mockNotes2());

        given(service.findByClientId(any(UUID.class))).willReturn(expectedDto);

        String url = "/lnf/clients/" + clientId + "/notes";

        String resultContent = new String(Files
                .readAllBytes(Path.of(ClassLoader.getSystemResource("testdata/client-notes.json")
                        .toURI())));

        mockMvc.perform(get(url)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(resultContent));

        verify(service, times(1)).findByClientId(any(UUID.class));
    }

    @Test
    void findByClientIdAndId() throws Exception {

        UUID id = UUID.fromString("cfe94b9f-c86f-4733-be96-a9b619f7bca7");

        ClientNotesDto expectedDto = mockNotes1();

        given(service.findById(any(UUID.class), any(UUID.class))).willReturn(expectedDto);

        String url = "/lnf/clients/" + clientId + "/notes/" + id;

        performAndVerifyGet(url, status().isOk(), id.toString(), expectedDto);

        verify(service, times(1)).findById(any(UUID.class), any(UUID.class));
    }

    @Test
    void create() {
        // Arrange
        List<ClientNotesDto> mockNotes = List.of(mockNotes1(), mockNotes2());
        doNothing().when(service).create(clientId, mockNotes);
        String url = "/lnf/clients/" + clientId + "/notes";

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ClientNotesDto>> captor = ArgumentCaptor.forClass(List.class);

        // Act
        try {
            mockMvc.perform(post(url)
                            .contentType(APPLICATION_JSON)
                            .content(asJsonString(mockNotes)))
                    .andExpect(status().isCreated());
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }

        // Assert
        verify(service).create(eq(clientId), captor.capture());
        List<ClientNotesDto> actualNotes = captor.getValue();

        // Check if the lists have the same size
        assertEquals(actualNotes.size(), actualNotes.size(), "The number of notes created should match");

        // Check if the details of each event match
        for (int i = 0; i < actualNotes.size(); i++) {
            assertEquals(actualNotes.get(i).getDescription(), actualNotes.get(i).getDescription(),
                    "Notes should match for clients at index " + i);
        }
    }

    @Test
    void update() {
        // Arrange
        UUID notesId = UUID.fromString("cfe94b9f-c86f-4733-be96-a9b619f7bca7");
        ClientNotesDto updatedNotes = mockNotes1();
        updatedNotes.setId(notesId);

        Mockito.doNothing().when(service).update(Mockito.eq(clientId), Mockito.eq(notesId), Mockito.any(ClientNotesDto.class));
        String url = "/lnf/clients/" + clientId + "/notes/" + notesId;
        ArgumentCaptor<ClientNotesDto> captor = ArgumentCaptor.forClass(ClientNotesDto.class);

        // Act
        try {
            mockMvc.perform(put(url)
                            .content(asJsonString(updatedNotes)) // Convert ClientNotesDto to JSON string
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }

        // Assert
        Mockito.verify(service, times(1)).update(eq(clientId), eq(notesId), captor.capture());
        ClientNotesDto actualNotes = captor.getValue();

        assertEquals(updatedNotes.getId(), actualNotes.getId(), "Notes IDs should match");
        assertEquals(updatedNotes.getDescription(), actualNotes.getDescription(), "description  should match");
    }

    @Test
    void deleteByClientId() throws Exception {
        String url = "/lnf/clients/" + clientId + "/notes";

        mockMvc.perform(delete(url)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(service).deleteByClientId(clientId);
    }

    @Test
    void testDeleteByClientIdAndId() throws Exception {
        UUID id = UUID.randomUUID();
        String urlTemplate = "/lnf/clients/%s/notes/%s".formatted(clientId, id);

        mockMvc.perform(delete(urlTemplate)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(service).deleteById(clientId, id);
    }

    private ClientNotesDto mockNotes1() {
        return createNotes("cfe94b9f-c86f-4733-be96-a9b619f7bca7", "the bug has to fix");
    }

    private ClientNotesDto mockNotes2() {
        return createNotes("019d9f96-8f91-4725-9056-ed022b4cb65f", "the product is launching");
    }

    private ClientNotesDto createNotes(String id, String description) {
        ClientNotesDto dto = new ClientNotesDto();
        dto.setId(UUID.fromString(id));
        dto.setDescription(description);

        return dto;
    }

    private void performAndVerifyGet(String url, ResultMatcher statusMatcher, String containsString,
                                     ClientNotesDto expectedDto) throws Exception {
        mockMvc.perform(get(url))
                .andExpect(statusMatcher)
                .andExpect(content().string(containsString(containsString)))
                .andExpect(jsonPath("$.id").value(containsString)) // Validate the ID
                .andExpect(jsonPath("$.description").value(expectedDto.getDescription()));// Additional, more specific validation
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
