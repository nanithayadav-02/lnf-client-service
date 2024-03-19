package com.technofacts.lnf.client.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.technofacts.lnf.client.service.ProjectTaskEmployeeService;
import com.technofacts.lnf.dto.client.ProjectTaskEmployeeDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;
import java.util.UUID;

import static org.aspectj.bridge.MessageUtil.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ProjectTaskEmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ProjectTaskEmployeeService service;
    private UUID projectId;
    private UUID taskId;

    @BeforeAll
    void beforeAll() {
        projectId = UUID.fromString("b24fbc1f-cc46-4a98-aa47-93457bc0afa0");
        taskId = UUID.fromString("50ec6a06-8625-41dc-b804-9c82693a6c92");
    }

    @BeforeEach
    void setUp() {
        // Common setup code if necessary
    }

    @Test
    void findEmployeesByProjectIdAndTaskId() throws Exception {
        ProjectTaskEmployeeDto expectedDto = new ProjectTaskEmployeeDto();

        given(service.findEmployeesByProjectIdAndTaskId(any((UUID.class)), any((UUID.class)))).willReturn(expectedDto);

        mockMvc.perform(get("/lnf/projects/{projectId}/tasks/{taskId}/employees", projectId, taskId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(asJsonString(expectedDto)));

        verify(service, times(1)).findEmployeesByProjectIdAndTaskId(any(UUID.class), any((UUID.class)));
    }

    @Test
    void addEmployeesToProjectAndTask() {
        List<String> requestedDto = List.of("HRD-FE-TF-1008","HRD-FE-TF-1009");

        String url = "/lnf/projects/" + projectId + "/tasks/" + taskId +"/employees";

        doNothing().when(service).addEmployeesToProjectAndTask(any((UUID.class)),any((UUID.class)), eq(requestedDto));

        try {
            mockMvc.perform(MockMvcRequestBuilders.post(url)
                            .contentType(APPLICATION_JSON)
                            .content(asJsonString(requestedDto)))
                    .andExpect(status().isCreated());
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
        verify(service, times(1)).addEmployeesToProjectAndTask(any((UUID.class)), any((UUID.class)), eq(requestedDto));
    }

    @Test
    void removeEmployeesFromProjectAndTask() throws Exception {
        List<String> requestedDto = List.of("HRD-FE-TF-1008","HRD-FE-TF-1009");
        String url = "/lnf/projects/" + projectId + "/tasks/" + taskId +"/employees";

        mockMvc.perform(delete(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(requestedDto)))
                .andExpect(status().isNoContent());

        verify(service).removeEmployeesFromProjectAndTask(projectId, taskId, requestedDto);
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
