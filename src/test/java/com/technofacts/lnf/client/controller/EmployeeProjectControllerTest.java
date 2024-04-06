package com.technofacts.lnf.client.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.technofacts.lnf.client.BaseTestClass;
import com.technofacts.lnf.client.service.EmployeeProjectService;
import com.technofacts.lnf.dto.client.EmployeeProjectDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EmployeeProjectControllerTest extends BaseTestClass {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private EmployeeProjectService service;
    private String  employeeId;

    @BeforeAll
    void beforeAll() {
        employeeId = "HRD-FE-TF-1008";
    }

    @BeforeEach
    void setUp() {
        // Common setup code if necessary
    }

    @Test
    void findProjectsByEmployeeId() throws Exception {
        EmployeeProjectDto expectedDto = new EmployeeProjectDto();

        given(service.findProjectsByEmployeeId(any(String.class))).willReturn(expectedDto);

        mockMvc.perform(get("/lnf/project")
                        .param("employeeId", employeeId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(asJsonString(expectedDto)));

        verify(service, times(1)).findProjectsByEmployeeId(any(String.class));
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
