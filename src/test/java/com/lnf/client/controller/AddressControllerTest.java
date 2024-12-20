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
import com.lnf.client.service.AddressService;
import com.lnf.dto.client.AddressDto;
import org.junit.jupiter.api.Assertions;
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

class AddressControllerTest extends BaseTestClass {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private AddressService service;
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
    void testFindByClientId() throws Exception {

        List<AddressDto> expectedDto = List.of(mockAddress1());

        given(service.findByClientId(any(UUID.class))).willReturn(expectedDto);

        String url = "/lnf/clients/" + clientId + "/address";

        mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(asJsonString(expectedDto)));

        verify(service, times(1)).findByClientId(any(UUID.class));
    }

    @Test
    void testFindByClientIdAndId() throws Exception {
        UUID id = UUID.fromString("d8e3c50a-6adc-486a-a8de-126fb77cee41");

        AddressDto expectedDto = mockAddress1();

        given(service.findById(any(UUID.class), any(UUID.class))).willReturn(expectedDto);

        String url = "/lnf/clients/" + clientId + "/address/" + id;

        performAndVerifyGet(url, status().isOk(), id.toString(), expectedDto);

        verify(service, times(1)).findById(any(UUID.class), any(UUID.class));
    }

    @Test
    void testCreateAddress() {

        AddressDto requestDto = mockAddress1();

        String url = "/lnf/clients/" + clientId + "/address";

        doNothing().when(service).create(eq(clientId), any(AddressDto.class));

        try {
            mockMvc.perform(MockMvcRequestBuilders.post(url)
                            .contentType(APPLICATION_JSON)
                            .content(asJsonString(requestDto)))
                    .andExpect(status().isCreated());
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }

        verify(service, times(1)).create(eq(clientId), any(AddressDto.class));
    }

    @Test
    void testUpdateAddress() {
        UUID id = UUID.fromString("d8e3c50a-6adc-486a-a8de-126fb77cee41");
        AddressDto updatedAddress = createAddress();
        updatedAddress.setId(id);

        Mockito.doNothing().when(service).update(Mockito.eq(clientId), eq(id), Mockito.any(AddressDto.class));
        String url = "/lnf/clients/" + clientId + "/address/" + id;
        ArgumentCaptor<AddressDto> captor = ArgumentCaptor.forClass(AddressDto.class);

        // Act
        try {
            mockMvc.perform(put(url)
                            .content(asJsonString(updatedAddress)) // Convert AddressDto to JSON string
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        } catch (Exception e) {
            Assertions.fail("Unexpected exception: " + e.getMessage());
        }

        //verify the service
        verify(service).update(eq(clientId), eq(id), any(AddressDto.class));

        // Assert
        Mockito.verify(service, times(1)).update(eq(clientId), eq(id), captor.capture());
        AddressDto actualType = captor.getValue();

        assertEquals(updatedAddress.getId(), actualType.getId(), "Address IDs should match");
        assertEquals(updatedAddress.getAddressText(), actualType.getAddressText(), "Address addressText should match");
        assertEquals(updatedAddress.getCity(), actualType.getCity(), "Address city should match");
        assertEquals(updatedAddress.getCountry(), actualType.getCountry(), "Address country should match");
        assertEquals(updatedAddress.getTown(), actualType.getTown(), "Address Town should match");
        assertEquals(updatedAddress.getState(), actualType.getState(), "Address status should match");
        assertEquals(updatedAddress.getPostCode(), actualType.getPostCode(), "Address working from should match");
    }

    @Test
    void testDeleteByClientIdAndId() throws Exception {
        UUID id = UUID.randomUUID();
        String urlTemplate = "/lnf/clients/%s/address/%s".formatted(clientId, id);

        mockMvc.perform(delete(urlTemplate)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(service).deleteById(clientId, id);
    }

    @Test
    void testDeleteByClientId() throws Exception {
        String url = "/lnf/clients/" + clientId + "/address";
        mockMvc.perform(delete(url)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(service).deleteByClientId(clientId);
    }


    private AddressDto mockAddress1() {
        return createAddress();
    }

    private AddressDto createAddress() {
        AddressDto dto = new AddressDto();
        dto.setId(UUID.fromString("d8e3c50a-6adc-486a-a8de-126fb77cee41"));
        dto.setAddressText("123 Main Street");
        dto.setTown("kukatPhally");
        dto.setCity("Hyderabad");
        dto.setState("Telangana");
        dto.setPostCode("12345");
        dto.setCountry("india");

        return dto;
    }

    private void performAndVerifyGet(String url, ResultMatcher statusMatcher, String containsString,
                                     AddressDto expectedDto) throws Exception {
        mockMvc.perform(get(url))
                .andExpect(statusMatcher)
                .andExpect(content().string(containsString(containsString))) // Validate the ID
                .andExpect(jsonPath("$.id").value(containsString)) // Additional, more specific validation
                .andExpect(jsonPath("$.addressText").value(expectedDto.getAddressText()))
                .andExpect(jsonPath("$.town").value(expectedDto.getTown()))
                .andExpect(jsonPath("$.city").value(expectedDto.getCity()))
                .andExpect(jsonPath("$.state").value(expectedDto.getState()))
                .andExpect(jsonPath("$.postCode").value(expectedDto.getPostCode()))
                .andExpect(jsonPath("$.country").value(expectedDto.getCountry()));

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
