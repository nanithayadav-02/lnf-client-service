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
import com.lnf.client.service.EmployeeTaskService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EmployeeTaskControllerTest extends BaseTestClass {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private EmployeeTaskService service;
    private String employeeId;

    @BeforeAll
    void beforeAll() {
        employeeId = "HRD-FE-TF-1008";
    }

    @BeforeEach
    void setUp() {
        // Common setup code if necessary
    }

    @Test
    void findTasksByEmployeeId() throws Exception {
        Map<String, Object> expectedDto = new HashMap<>();
        expectedDto.put("employeeId", "HRD-CE-TF-3042");
        expectedDto.put("projectTasks", new ArrayList<>());
        expectedDto.put("totalPages", 1);
        expectedDto.put("totalElements", 0);

        given(service.findTasksByEmployeeId(anyString(), anyInt(), anyInt())).willReturn(expectedDto);

        mockMvc.perform(get("/lnf/tasks")
                        .param("employeeId", "HRD-CE-TF-3042")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(asJsonString(expectedDto)));

        verify(service, times(1)).findTasksByEmployeeId(anyString(), anyInt(), anyInt());
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
