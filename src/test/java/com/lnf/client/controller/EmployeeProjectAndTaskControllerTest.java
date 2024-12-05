package com.lnf.client.controller;

import com.lnf.client.BaseTestClass;
import com.lnf.client.service.EmployeeProjectAndTaskService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;
import java.util.UUID;

import static com.lnf.client.controller.ProjectTaskEmployeeControllerTest.asJsonString;
import static org.aspectj.bridge.MessageUtil.fail;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EmployeeProjectAndTaskControllerTest extends BaseTestClass {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private EmployeeProjectAndTaskService service;

    @Mock
    private EmployeeProjectAndTaskController controller;
    private UUID projectId;
    private UUID taskId;

    @BeforeAll
    void beforeAll() {
        projectId = UUID.fromString("b24fbc1f-cc46-4a98-aa47-93457bc0afa0");
        taskId = UUID.fromString("50ec6a06-8625-41dc-b804-9c82693a6c92");
    }

    @Test
     void testAddEmployeesToProject() {
        List<String> requestedDto = List.of("HRD-FE-TF-1008", "HRD-FE-TF-1009");

        String url = "/lnf/project/" + projectId + "/employees";

        doNothing().when(service).addAllActiveEmployeeToProject(any((UUID.class)));

        try {
            mockMvc.perform(MockMvcRequestBuilders.post(url)
                            .contentType(APPLICATION_JSON)
                            .content(asJsonString(requestedDto)))
                    .andExpect(status().isCreated());
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
        verify(service, times(1)).addAllActiveEmployeeToProject(any((UUID.class)));

    }

    @Test
     void testAddAllTasksToEmployees() {
        List<String> requestedDto = List.of("HRD-FE-TF-1008", "HRD-FE-TF-1009");

        String url = "/lnf/project/" + projectId + "/tasks/employees";

        doNothing().when(service).addAllTasksToEmployee(any((UUID.class)));

        try {
            mockMvc.perform(MockMvcRequestBuilders.post(url)
                            .contentType(APPLICATION_JSON)
                            .content(asJsonString(requestedDto)))
                    .andExpect(status().isCreated());
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
        verify(service, times(1)).addAllTasksToEmployee(any((UUID.class)));

    }

    @Test
    void addAllEmployeesToProjectAndTask() {
        List<String> requestedDto = List.of("HRD-FE-TF-1008", "HRD-FE-TF-1009");

        String url = "/lnf/projects/" + projectId + "/tasks/" + taskId + "/project-employees";

        doNothing().when(service).addAllEmployeesToProjectAndTask(any((UUID.class)), any((UUID.class)));

        try {
            mockMvc.perform(MockMvcRequestBuilders.post(url)
                            .contentType(APPLICATION_JSON)
                            .content(asJsonString(requestedDto)))
                    .andExpect(status().isCreated());
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
        verify(service, times(1)).addAllEmployeesToProjectAndTask(any((UUID.class)), any((UUID.class)));
    }

    @Test
    void removeEmployeesFromProject() throws Exception {
        List<String> requestedDto = List.of("HRD-FE-TF-1008","HRD-FE-TF-1009");
        String url = "/lnf/project/" + projectId + "/employees";

        mockMvc.perform(delete(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(requestedDto)))
                .andExpect(status().isNoContent());

        verify(service).removeEmployeeFromProject(projectId);
    }

    @Test
    void removeEmployeesFromProjectAndTask() throws Exception {
        List<String> requestedDto = List.of("HRD-FE-TF-1008","HRD-FE-TF-1009");
        String url = "/lnf/project/" + projectId + "/task/" + taskId +"/employees";

        mockMvc.perform(delete(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(requestedDto)))
                .andExpect(status().isNoContent());

        verify(service).removeEmployeesFromProjectAndTask(projectId, taskId);
    }

    @Test
    void removeAllTasksToEmployee() throws Exception {
        List<String> requestedDto = List.of("HRD-FE-TF-1008","HRD-FE-TF-1009");
        String url = "/lnf/project/" + projectId + "/tasks/employees";

        mockMvc.perform(delete(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(requestedDto)))
                .andExpect(status().isNoContent());

        verify(service).removeAllTasksToEmployee(projectId);
    }

}
