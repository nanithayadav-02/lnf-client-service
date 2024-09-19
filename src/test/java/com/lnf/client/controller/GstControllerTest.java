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
import com.lnf.client.service.GstService;
import com.lnf.dto.client.GstDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import java.nio.file.Files;
import java.nio.file.Paths;
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

class GstControllerTest extends BaseTestClass {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private GstService service;
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

        List<GstDto> expectedDto = List.of(mockGst1(), mockGst2());

        given(service.findByClientId(any(UUID.class))).willReturn(expectedDto);

        String url = "/lnf/clients/" + clientId + "/gst";

        String resultContent = new String(Files
                .readAllBytes(Paths.get(ClassLoader.getSystemResource("testdata/client-gsts.json")
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

        GstDto expectedDto = mockGst1 ();

        given(service.findById(any(UUID.class), any(UUID.class))).willReturn(expectedDto);

        String url = "/lnf/clients/" + clientId + "/gst/" + id;

        performAndVerifyGet(url, status().isOk(), id.toString(), expectedDto);

        verify(service, times(1)).findById(any(UUID.class), any(UUID.class));
    }

    @Test
    void create() {
        GstDto requestDto = mockGst2();

        String url = "/lnf/clients/" + clientId + "/gst";

        doNothing().when(service).create(eq(clientId), any(GstDto.class));

        try {
            mockMvc.perform(post(url)
                            .contentType(APPLICATION_JSON)
                            .content(asJsonString(requestDto)))
                    .andExpect(status().isCreated());
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
        verify(service, times(1)).create(eq(clientId), any(GstDto.class));
    }

    @Test
    void update() {
        // Arrange
        UUID gstId = UUID.fromString ("cfe94b9f-c86f-4733-be96-a9b619f7bca7");
        GstDto updatedGst = mockGst2();
        updatedGst.setId(gstId);

        Mockito.doNothing().when(service).update(Mockito.eq(clientId), Mockito.eq(gstId), Mockito.any(GstDto.class));
        String url = "/lnf/clients/" + clientId + "/gst/" + gstId;
        ArgumentCaptor<GstDto> captor = ArgumentCaptor.forClass(GstDto.class);

        // Act
        try {
            mockMvc.perform(put(url)
                            .content(asJsonString(updatedGst)) // Convert GstDto to JSON string
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }

        // Assert
        Mockito.verify(service, times(1)).update(eq(clientId), eq(gstId), captor.capture());
        GstDto actualGst = captor.getValue();

        assertEquals(updatedGst.getId(), actualGst.getId(), "Gst IDs should match");
        assertEquals(updatedGst.getNumber(), actualGst.getNumber(), "Gst Number should match");
        assertEquals(updatedGst.getLocation(), actualGst.getLocation(), "Gst Location should match");
    }

    @Test
    void deleteByClientId() throws Exception {
        String url = "/lnf/clients/" + clientId + "/gst";

        mockMvc.perform(delete(url)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(service).deleteByClientId(clientId);
    }

    @Test
    void testDeleteByClientIdAndId() throws Exception {
        UUID id = UUID.randomUUID();
        String urlTemplate = String.format("/lnf/clients/%s/gst/%s", clientId, id);

        mockMvc.perform(delete(urlTemplate)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(service).deleteById(clientId, id);
    }

    private GstDto mockGst1() {
        return createGst("cfe94b9f-c86f-4733-be96-a9b619f7bca7", "madhapur", "GSTIN139302hk2");
    }

    private GstDto mockGst2() {
        return createGst("019d9f96-8f91-4725-9056-ed022b4cb65f", "begumpet", "GSTIN1e793bo4nj");
    }

    private GstDto createGst(String id, String location,String number) {
        GstDto dto = new GstDto();
        dto.setId(UUID.fromString(id));
        dto.setLocation(location);
        dto.setNumber(number);

        return dto;
    }

    private void performAndVerifyGet(String url, ResultMatcher statusMatcher, String containsString,
                                     GstDto expectedDto) throws Exception {
        mockMvc.perform(get(url))
                .andExpect(statusMatcher)
                .andExpect(content().string(containsString(containsString))) // Validate the ID
                .andExpect(jsonPath("$.id").value(containsString)) // Additional, more specific validation
                .andExpect(jsonPath("$.location").value(expectedDto.getLocation()))
                .andExpect(jsonPath("$.number").value(expectedDto.getNumber()));

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
