package com.technofacts.lnf.client.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.technofacts.lnf.client.BaseTestClass;
import com.technofacts.lnf.client.service.TaskService;
import com.technofacts.lnf.dto.client.TaskDto;
import com.technofacts.lnf.service.common.page.PaginationAndSortingHandler;
import com.technofacts.lnf.util.RestUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.aspectj.bridge.MessageUtil.fail;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TaskControllerTest extends BaseTestClass {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private TaskService service;
    private UUID projectId;

    @BeforeAll
    void beforeAll() {
        projectId = UUID.fromString("123e4567-e89b-12d3-a456-556642440000");
    }

    @BeforeEach
    void setUp() {
        // Common setup code if necessary
    }

    @Autowired
    private PaginationAndSortingHandler paginationAndSortingHandler;

    @Test
    void testFindPaginatedByProjectId() throws Exception {
        int page = 0;
        int size = 5;
        List<TaskDto> taskList = Arrays.asList(new TaskDto(), new TaskDto());
        Page<TaskDto> leavePage = new PageImpl<>(taskList, PageRequest.of(page, size), taskList.size());

        when(service.findPaginatedByProjectId(projectId, page, size)).thenReturn(leavePage);

        mockMvc.perform(get("/lnf/projects/"+projectId+"/tasks")
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk());

    }

    @Test
    void findAllSortedByEmployeeId() throws Exception {

        String sortBy = "status";
        String sortOrder = "asc";
        List<TaskDto> leaveList = Arrays.asList(new TaskDto(), new TaskDto());
        when(service.findAllSortedByProjectId(projectId, sortBy, sortOrder)).thenReturn(leaveList);

        mockMvc.perform(get("/lnf/projects/"+projectId+"/tasks")
                        .param("sortBy", sortBy)
                        .param("sortOrder", sortOrder))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

    }

    @Test
    void findPaginatedAndSortedByEmployeeId() throws Exception {

        int page = 0;
        int size = 5;
        String sortBy = "status";
        String sortOrder = "asc";
        final Sort sortInfo = RestUtil.constructSort(sortBy, sortOrder);
        List<TaskDto> leaveList = Arrays.asList(new TaskDto(), new TaskDto());
        Page<TaskDto> leavePage = new PageImpl<>(leaveList, PageRequest.of(page, size, sortInfo), leaveList.size());

        when(service.findPaginatedAndSortedByProjectId(projectId, page, size, sortBy, sortOrder)).thenReturn(leavePage);

        mockMvc.perform(get("/lnf/projects/"+projectId+"/tasks")
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size))
                        .param("sortBy", sortBy)
                        .param("sortOrder", sortOrder))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

    }


    @Test
    void findAllByProjectId() throws Exception {
        List<TaskDto> expectedDto = List.of(mockTask1(), mockTask2());

        given(service.findAllByProjectId(any(UUID.class))).willReturn(expectedDto);

        String url = "/lnf/projects/" + projectId + "/tasks";

        String resultContent = new String(Files
                .readAllBytes(Paths.get(ClassLoader.getSystemResource("testdata/client-task.json")
                        .toURI())));

        mockMvc.perform(get(url)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(resultContent));

        verify(service, times(1)).findAllByProjectId(any(UUID.class));

    }

    @Test
    void findByProjectIdAndTaskId() throws Exception {
        UUID id = UUID.fromString("cfe94b9f-c86f-4733-be96-a9b619f7bca7");

        TaskDto expectedDto = mockTask1();

        given(service.findByProjectIdAndTaskId(any(UUID.class), any(UUID.class))).willReturn(expectedDto);

        String url = "/lnf/projects/" + projectId + "/tasks/" + id;

        performAndVerifyGet(url, status().isOk(), id.toString(), expectedDto);

        verify(service, times(1)).findByProjectIdAndTaskId(any(UUID.class), any(UUID.class));
    }

    @Test
    void findByTaskId() throws Exception {
        TaskDto expectedDto = mockTask1();
        UUID id = UUID.fromString("cfe94b9f-c86f-4733-be96-a9b619f7bca7");

        given(service.findByTaskId(any(UUID.class))).willReturn(expectedDto);

        String url = "/lnf/tasks/" + id;

        performAndVerifyGet(url, status().isOk(), id.toString(), expectedDto);

        verify(service, times(1)).findByTaskId(any(UUID.class));
    }

    @Test
    void create() {
        TaskDto requestDto = mockTask1();

        String url = "/lnf/projects/" + projectId + "/tasks";

        doNothing().when(service).create(eq(projectId), eq(requestDto));

        try {
            mockMvc.perform(MockMvcRequestBuilders.post(url)
                            .contentType(APPLICATION_JSON)
                            .content(asJsonString(requestDto)))
                    .andExpect(status().isCreated());
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }

        verify(service, times(1)).create(eq(projectId), any(TaskDto.class));
    }

    @Test
    void update() {
        // Arrange
        UUID taskId = UUID.fromString ("cfe94b9f-c86f-4733-be96-a9b619f7bca7");
        TaskDto updatedTasks = mockTask1 ();
        updatedTasks.setId(taskId);

        Mockito.doNothing().when(service).update(Mockito.eq(projectId), Mockito.eq(taskId), Mockito.any(TaskDto.class));
        String url = "/lnf/projects/" + projectId + "/tasks/" + taskId;
        ArgumentCaptor<TaskDto> captor = ArgumentCaptor.forClass(TaskDto.class);

        // Act
        try {
            mockMvc.perform(put(url)
                            .content(asJsonString(updatedTasks)) // Convert TaskDto to JSON string
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        } catch (Exception e) {
            Assertions.fail("Unexpected exception: " + e.getMessage());
        }

        // Assert
        Mockito.verify(service, times(1)).update(eq(projectId), eq(taskId), captor.capture());
        TaskDto actualTask = captor.getValue();

        assertEquals(updatedTasks.getId(), actualTask.getId(), "Task IDs should match");
        assertEquals(updatedTasks.getDescription (), actualTask.getDescription (), "description  should match");
    }

    @Test
    void deleteByProjectId() throws Exception {
        String url = "/lnf/projects/" + projectId + "/tasks";

        mockMvc.perform(delete(url)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(service).deleteByProjectId(projectId);
    }

    @Test
    void deleteByProjectIdAndTaskId() throws Exception {
        UUID id = UUID.randomUUID();
        String urlTemplate = String.format("/lnf/projects/%s/tasks/%s", projectId, id);

        mockMvc.perform(delete(urlTemplate)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(service).deleteByProjectIdAndTaskId(projectId, id);
    }

    private TaskDto mockTask1() {
        return createTask("cfe94b9f-c86f-4733-be96-a9b619f7bca7", "Data Analytics", "Power BI", "Active",
                          "Development", "2022-11-01", "2023-12-31");
    }

    private TaskDto mockTask2() {
        return createTask("019d9f96-8f91-4725-9056-ed022b4cb65f",  "Testing", "QA", "InActive",
                "Testing", "2022-10-01", "2023-11-30");
    }

    private TaskDto createTask(String id, String name, String type, String status, String description,
                               String startDate, String endDate) {
        TaskDto dto = new TaskDto();
        dto.setId(UUID.fromString(id));
        dto.setName(name);
        dto.setType(type);
        dto.setStatus(status);
        dto.setStartDate(LocalDate.parse(startDate));
        dto.setEndDate(LocalDate.parse(endDate));
        dto.setDescription(description);

        return dto;
    }

    private void performAndVerifyGet(String url, ResultMatcher statusMatcher, String containsString,
                                     TaskDto expectedDto) throws Exception {
        mockMvc.perform(get(url))
                .andExpect(statusMatcher)
                .andExpect(content().string(containsString(containsString))) // Validate the ID
                .andExpect(jsonPath("$.id").value(containsString)) // Additional, more specific validation
                .andExpect(jsonPath("$.name").value(expectedDto.getName()))
                .andExpect(jsonPath("$.type").value(expectedDto.getType()))
                .andExpect(jsonPath("$.status").value(expectedDto.getStatus()))
                .andExpect(jsonPath("$.startDate").value(expectedDto.getStartDate().toString()))
                .andExpect(jsonPath("$.endDate").value(expectedDto.getEndDate().toString()))
                .andExpect(jsonPath("$.description").value(expectedDto.getDescription()));

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
