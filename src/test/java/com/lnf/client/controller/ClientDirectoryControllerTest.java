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
import org.aspectj.bridge.MessageUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ClientDirectoryControllerTest extends BaseTestClass {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ClientDirectoryService service;
    private static UUID clientId;
    private static UUID directoryId;

    @BeforeAll
    static void beforeAll() {
        clientId = UUID.fromString("123e4567-e89b-12d3-a456-556642440000");
        directoryId = UUID.fromString("cfe94b9f-c86f-4733-be96-a9b619f7bca7");
    }

    @BeforeEach
    void setUp() {
        // Common setup code if necessary
    }

    @Test
    void create() throws Exception {
        ClientDirectoryDto requestDto = createClientDirectory1();
        String url = "/lnf/clients/{clientId}/directory";

        doNothing().when(service).create(any(UUID.class), any(ClientDirectoryDto.class));

        try {
            mockMvc.perform(MockMvcRequestBuilders.post(url, clientId, directoryId)
                            .contentType(APPLICATION_JSON)
                            .content(asJsonString(requestDto))) // Convert DTO to JSON string
                    .andExpect(status().isCreated());
        } catch (Exception e) {
            MessageUtil.fail("Unexpected exception: " + e.getMessage());
        }

        Mockito.verify(service, times(1)).create(any(UUID.class), any(ClientDirectoryDto.class));
    }

    @Test
    void testUpdate() {
        UUID clientId = UUID.fromString("123e4567-e89b-12d3-a456-556642440000");
        UUID directoryId = UUID.fromString("cfe94b9f-c86f-4733-be96-a9b619f7bca7");
        ClientDirectoryDto mockDto = createClientDirectory1();

        ArgumentCaptor<ClientDirectoryDto> captor = ArgumentCaptor.forClass(ClientDirectoryDto.class);

        doNothing().when(service).update(eq(clientId), eq(directoryId), any(ClientDirectoryDto.class));

        String url = "/lnf/clients/" + clientId + "/directory/" + directoryId;

        try {
            mockMvc.perform(MockMvcRequestBuilders.put(url)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJsonString(mockDto))) // Convert DTO to JSON string
                    .andExpect(status().isOk()); // Expect a 200 OK status
        } catch (Exception e) {
            MessageUtil.fail("Unexpected exception: " + e.getMessage());
        }

        verify(service, times(1)).update(eq(clientId), eq(directoryId), captor.capture());

        // Retrieve the actual DTO passed to the update method
        ClientDirectoryDto actualDto = captor.getValue();

        // Assert: Check that the values in the actual DTO match the expected values
        assertEquals(mockDto.getId(), actualDto.getId(), "IDs should match");
        assertEquals(mockDto.getFirstName(), actualDto.getFirstName(), "First names should match");
        assertEquals(mockDto.getLastName(), actualDto.getLastName(), "Last names should match");
        assertEquals(mockDto.getEmail(), actualDto.getEmail(), "Emails should match");
        assertEquals(mockDto.getPhoneNumber(), actualDto.getPhoneNumber(), "Phone numbers should match");
        assertEquals(mockDto.getActive(), actualDto.getActive(), "Active status should match");
    }

    @Test
    void findByClientId() throws Exception {
        UUID clientId = UUID.randomUUID();
        List<ClientDirectoryDto> expectedList = Arrays.asList(
                createClientDirectory1(),
                createClientDirectory2());

        given(service.findByClientId(any(UUID.class))).willReturn(expectedList);

        String url = "/lnf/clients/" + clientId + "/directory";

        String resultContent = readJsonFromFile("testdata/client-directories.json");

        mockMvc.perform(get(url)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(resultContent));

        verify(service, times(1)).findByClientId(any(UUID.class));
    }

    @Test
    void findByClientIdAndId() throws Exception {
        UUID clientId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        UUID directoryId = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");
        String url = "/lnf/clients/" + clientId + "/directory/" + directoryId;

        ClientDirectoryDto expectedResponse = createClientDirectory2();

        when(service.findByClientIdAndDirectoryId(clientId, directoryId)).thenReturn(expectedResponse);

        mockMvc.perform(get(url)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(new ObjectMapper().writeValueAsString(expectedResponse)));

        verify(service, times(1)).findByClientIdAndDirectoryId(clientId, directoryId);
    }

    @Test
    void deleteByClientId() throws Exception {
        String url = "/lnf/clients/" + clientId + "/directory";

        mockMvc.perform(delete(url)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(service, times(1)).deleteByClientId(clientId);
    }

    @Test
    void deleteByClientIdAndId() throws Exception {
        String url = "/lnf/clients/" + clientId + "/directory/" + directoryId;

        mockMvc.perform(delete(url)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(service, times(1)).deleteByClientIdAndDirectoryId(clientId, directoryId);
    }

    @Test
    void searchByEmail() throws Exception {
        String email = "john.doe@example.com";

        String url = "/lnf/clients/directory/search?email=" + email;

        List<ClientDirectoryDto> expectedResponse = List.of(
                createClientDirectory1(),
                createClientDirectory2());

        when(service.findByEmail(email)).thenReturn(expectedResponse);

        mockMvc.perform(get(url)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(new ObjectMapper().writeValueAsString(expectedResponse)));  // Assert the JSON response

        verify(service, times(1)).findByEmail(email);
    }

    private String readJsonFromFile(String filePath) throws Exception {
        return new String(Files.readAllBytes(Paths.get(ClassLoader.getSystemResource(filePath).toURI())));
    }

    private ClientDirectoryDto createClientDirectory1() {
        return createClientDirectoryDto(
                "123e4567-e89b-12d3-a456-426614174000",
                "John",
                "Doe",
                "john.doe@example.com",
                "1234567890",
                true
        );
    }

    private ClientDirectoryDto createClientDirectory2() {
        return createClientDirectoryDto(
                "123e4567-e89b-12d3-a456-426614174001",
                "Jane",
                "Smith",
                "jane.smith@example.com",
                "987654321",
                false
        );
    }

    private ClientDirectoryDto createClientDirectoryDto(String id, String firstName, String lastName, String email, String phoneNumber, boolean active) {
        ClientDirectoryDto dto = new ClientDirectoryDto();
        dto.setId(UUID.fromString(id));
        dto.setFirstName(firstName);
        dto.setLastName(lastName);
        dto.setEmail(email);
        dto.setPhoneNumber(phoneNumber);
        dto.setActive(active);
        return dto;
    }

    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
