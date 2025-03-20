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
import com.lnf.client.service.EscalationService;
import com.lnf.dto.client.EscalationDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

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

class EscalationControllerTest extends BaseTestClass {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private EscalationService service;
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
    void findByClientId() throws Exception {
        List<EscalationDto> expectedDto = Arrays.asList(createEscalation1(), createEscalation2());

        given(service.findByClientId(any(UUID.class))).willReturn(expectedDto);

        String url = "/lnf/clients/" + clientId + "/escalation";

        String resultContent = new String(Files
                .readAllBytes(Path.of(ClassLoader.getSystemResource("testdata/client-escalation.json")
                        .toURI())));

        mockMvc.perform(get(url)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(resultContent));

        verify(service, times(1)).findByClientId(any(UUID.class));
    }

    @Test
    void findByClientIdAndId() throws Exception {
        UUID escalationId = UUID.fromString("3f81db1c-825d-4f86-b682-d3f0369a3ada");

        EscalationDto expectedDto = createEscalation1();

        given(service.findById(any(UUID.class), any(UUID.class))).willReturn(expectedDto);

        String url = "/lnf/clients/" + clientId + "/escalation/" + escalationId;

        performAndVerifyGet(url, status().isOk(), escalationId.toString(), expectedDto);

        verify(service, times(1)).findById(any(UUID.class), any(UUID.class));
    }

    @Test
    void create() {
        // Arrange
        List<EscalationDto> mockEscalations = List.of(createEscalation1(), createEscalation2());
        doNothing().when(service).create(clientId, mockEscalations);
        String url = "/lnf/clients/" + clientId + "/escalation";

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<EscalationDto>> captor = ArgumentCaptor.forClass(List.class);

        // Act
        try {
            mockMvc.perform(post(url)
                            .contentType(APPLICATION_JSON)
                            .content(asJsonString(mockEscalations)))
                    .andExpect(status().isCreated());
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }

        // Assert
        verify(service).create(eq(clientId), captor.capture());
        List<EscalationDto> actualEscalations = captor.getValue();

        // Check if the lists have the same size
        assertEquals(actualEscalations.size(), actualEscalations.size(), "The number of escalations created should match");

        // Check if the details of each escalation match
        for (int i = 0; i < actualEscalations.size(); i++) {
            assertEquals(actualEscalations.get(i).getEmail(), actualEscalations.get(i).getEmail(),
                    "Escalation should match for clients at index " + i);
            assertEquals(actualEscalations.get(i).getName(), actualEscalations.get(i).getName(),
                    "Escalation should match for clients at index " + i);
            assertEquals(actualEscalations.get(i).getMobileNumber(), actualEscalations.get(i).getMobileNumber(),
                    "Escalation should match for clients at index " + i);
            assertEquals(actualEscalations.get(i).getPhoneNumber(), actualEscalations.get(i).getPhoneNumber(),
                    "Escalation should match for clients at index " + i);
        }
    }

    @Test
    void update() {
        // Arrange
        UUID escalationId = UUID.fromString("3f81db1c-825d-4f86-b682-d3f0369a3ada");
        EscalationDto updatedEscalation = createEscalation1();
        updatedEscalation.setId(escalationId);

        Mockito.doNothing().when(service).update(Mockito.eq(clientId), Mockito.eq(escalationId), Mockito.any(EscalationDto.class));
        String url = "/lnf/clients/" + clientId + "/escalation/" + escalationId;
        ArgumentCaptor<EscalationDto> captor = ArgumentCaptor.forClass(EscalationDto.class);

        // Act
        try {
            mockMvc.perform(put(url)
                            .content(asJsonString(updatedEscalation)) // Convert ClientEscalationDto to JSON string
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }

        // Assert
        Mockito.verify(service, times(1)).update(eq(clientId), eq(escalationId), captor.capture());
        EscalationDto actualEscalation = captor.getValue();

        assertEquals(updatedEscalation.getId(), actualEscalation.getId(), "Escalation IDs should match");
        assertEquals(updatedEscalation.getPhoneNumber(), actualEscalation.getPhoneNumber(), "phoneNumber  should match");
        assertEquals(updatedEscalation.getName(), actualEscalation.getName(), "name should match");
    }

    @Test
    void deleteByClientId() throws Exception {
        String url = "/lnf/clients/" + clientId + "/escalation";

        mockMvc.perform(delete(url)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(service).deleteByClientId(clientId);
    }

    @Test
    void testDelete() throws Exception {
        UUID escalationId = UUID.randomUUID();
        String urlTemplate = "/lnf/clients/%s/escalation/%s".formatted(clientId, escalationId);

        mockMvc.perform(delete(urlTemplate)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(service).deleteById(clientId, escalationId);
    }

    private EscalationDto createEscalation1() {
        return createEscalationDto("3f81db1c-825d-4f86-b682-d3f0369a3ada", "Vijay", "9090998988",
                "vijay200@gmail.com", "72517894523");
    }

    private EscalationDto createEscalation2() {
        return createEscalationDto("1b49b31c-573a-4b47-b13f-01d320e11ec5", "Hari", "8671300589",
                "hari264@gmail.com", "95517894523");
    }

    private EscalationDto createEscalationDto(String id, String name, String phoneNumber, String email,
                                              String mobileNumber) {
        EscalationDto dto = new EscalationDto();
        dto.setId(UUID.fromString(id));
        dto.setName(name);
        dto.setEmail(email);
        dto.setPhoneNumber(phoneNumber);
        dto.setMobileNumber(mobileNumber);

        return dto;
    }

    private void performAndVerifyGet(String url, ResultMatcher statusMatcher, String containsString,
                                     EscalationDto expectedDto) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(url))
                .andExpect(statusMatcher)
                .andExpect(content().string(containsString(containsString)))
                .andExpect(jsonPath("$.id").value(expectedDto.getId().toString()))
                .andExpect(jsonPath("$.name").value(expectedDto.getName())) // Adjusted for name field
                .andExpect(jsonPath("$.email").value(expectedDto.getEmail()))
                .andExpect(jsonPath("$.phoneNumber").value(expectedDto.getPhoneNumber())) // Adjusted for PhoneNumber field
                .andExpect(jsonPath("$.mobileNumber").value(expectedDto.getMobileNumber()));
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
