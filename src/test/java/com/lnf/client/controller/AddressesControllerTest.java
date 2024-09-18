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
import com.lnf.client.service.AddressService;
import com.lnf.dto.client.AddressDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AddressesControllerTest extends BaseTestClass {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AddressService service;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
    }

    @Test
    public void testCreateAddresses() throws Exception {
        // Given
        UUID clientId = UUID.randomUUID();
        List<AddressDto> addresses = Arrays.asList(createAddressDto());

        String url= "/lnf/clients/"+clientId+"/addresses";

        // When
        ResultActions resultActions = mockMvc.perform(post(url,addresses)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addresses)));

        // Then
        resultActions.andExpect(status().isCreated());
    }

    private AddressDto createAddressDto(){
        AddressDto address1 = new AddressDto();
        address1.setId(UUID.randomUUID());
        address1.setAddressText("123 Main St");
        address1.setCity("Springfield");
        address1.setPostCode("627015");
        address1.setState("Telangana");
        address1.setAddressType("Permanent");
        address1.setCountry("India");
        address1.setTown("Hyd");
        return address1;
    }

}
