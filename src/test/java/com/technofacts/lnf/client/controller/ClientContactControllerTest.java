package com.technofacts.lnf.client.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.technofacts.lnf.client.BaseTestClass;
import com.technofacts.lnf.client.service.ClientContactService;
import com.technofacts.lnf.dto.client.ContactDto;
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

import java.nio.file.Files;
import java.nio.file.Paths;
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

class ClientContactControllerTest extends BaseTestClass {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ClientContactService service;
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
        List<ContactDto> expectedDto = List.of(createContact1(), createContact2());

        String url = "/lnf/clients/" + clientId + "/contact";

        given(service.findByClientId(any(UUID.class))).willReturn(expectedDto);

        String resultContent = new String(Files
                .readAllBytes(Paths.get(ClassLoader.getSystemResource("testdata/client-contact.json")
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

        ContactDto expectedDto = createContact1();

        given(service.findById(any(UUID.class), any(UUID.class))).willReturn(expectedDto);

        String url = "/lnf/clients/" + clientId + "/contact/" + id;

        performAndVerifyGet(url, status().isOk(), id.toString(), expectedDto);

        verify(service, times(1)).findById(any(UUID.class), any(UUID.class));
    }

    @Test
    void create() {
        ContactDto requestDto = createContact2();

        String url = "/lnf/clients/" + clientId + "/contact";

        doNothing().when(service).create(eq(clientId), any(ContactDto.class));

        try {
            mockMvc.perform(MockMvcRequestBuilders.post(url)
                            .contentType(APPLICATION_JSON)
                            .content(asJsonString(requestDto)))
                    .andExpect(status().isCreated());
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }

        verify(service, times(1)).create(eq(clientId), any(ContactDto.class));
    }

    @Test
    void update() {
        UUID id = UUID.fromString("cfe94b9f-c86f-4733-be96-a9b619f7bca7");
        ContactDto updatedContact = createContact1();
        updatedContact.setId(id);

        Mockito.doNothing().when(service).update(Mockito.eq(clientId), eq(id), Mockito.any(ContactDto.class));
        String url = "/lnf/clients/" + clientId + "/contact/" + id;
        ArgumentCaptor<ContactDto> captor = ArgumentCaptor.forClass(ContactDto.class);

        // Act
        try {
            mockMvc.perform(put(url)
                            .content(asJsonString(updatedContact)) // Convert ContactDto to JSON string
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        } catch (Exception e) {
            Assertions.fail("Unexpected exception: " + e.getMessage());
        }

        //verify the service
        verify(service).update(eq(clientId), eq(id), any(ContactDto.class));

        // Assert
        Mockito.verify(service, times(1)).update(eq(clientId), eq(id), captor.capture());
        ContactDto actualType = captor.getValue();

        assertEquals(updatedContact.getId(), actualType.getId(), "Contact IDs should match");
        assertEquals(updatedContact.getName(), actualType.getName(), "Contact name should match");
    }

    @Test
    void deleteByClientId() throws Exception {
        String url = "/lnf/clients/" + clientId + "/contact";

        mockMvc.perform(delete(url)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(service).deleteByClientId(clientId);
    }

    @Test
    void testDelete() throws Exception {
        UUID id = UUID.randomUUID();
        String urlTemplate = String.format("/lnf/clients/%s/contact/%s", clientId, id);

        mockMvc.perform(delete(urlTemplate)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(service).deleteById(clientId, id);
    }

    private ContactDto createContact1() {
        return createContactDto("cfe94b9f-c86f-4733-be96-a9b619f7bca7", "Vijay", "9090998988",
                "vijay200@gmail.com", "Product Manager", "IT");
    }

    private ContactDto createContact2() {
        return createContactDto("1b49b31c-573a-4b47-b13f-01d320e11ec5", "Hari", "8671300589",
                "hari264@gmail.com", "Manager", "RECRUITMENT");
    }

    private ContactDto createContactDto(String id, String name, String phoneNumber, String email,
                                                          String designation, String department) {
        ContactDto dto = new ContactDto();
        dto.setId(UUID.fromString(id));
        dto.setName(name);
        dto.setEmail(email);
        dto.setPhoneNumber(phoneNumber);
        dto.setDesignation(designation);
        dto.setDepartment(department);

        return dto;
    }

    private void performAndVerifyGet(String url, ResultMatcher statusMatcher, String containsString,
                                     ContactDto expectedDto) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(url))
                .andExpect(statusMatcher)
                .andExpect(content().string(containsString(containsString)))
                .andExpect(jsonPath("$.id").value(expectedDto.getId().toString()))
                .andExpect(jsonPath("$.name").value(expectedDto.getName())) // Adjusted for name field
                .andExpect(jsonPath("$.email").value(expectedDto.getEmail()))
                .andExpect(jsonPath("$.phoneNumber").value(expectedDto.getPhoneNumber())) // Adjusted for PhoneNumber field
                .andExpect(jsonPath("$.designation").value(expectedDto.getDesignation()))
                .andExpect(jsonPath("$.department").value(expectedDto.getDepartment()));
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
