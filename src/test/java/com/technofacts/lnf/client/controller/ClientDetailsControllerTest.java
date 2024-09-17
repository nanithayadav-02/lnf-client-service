package com.technofacts.lnf.client.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.technofacts.lnf.client.BaseTestClass;
import com.technofacts.lnf.client.service.ClientDetailsService;
import com.technofacts.lnf.dto.client.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

public class ClientDetailsControllerTest extends BaseTestClass {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ClientDetailsService clientDetailsService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
    }

    @Test
    void testFindByClientId() throws Exception {
        UUID clientId = UUID.randomUUID();
        ClientDetailsDto clientDetailsDto = createMockClientDetailsDto();

        // Convert the DTO to JSON string
        String clientDetailsDtoJson = objectMapper.writeValueAsString(clientDetailsDto);

        // Mock the service method
        when(clientDetailsService.findByClientId(clientId)).thenReturn(clientDetailsDto);

        String url = "/lnf/clients/" + clientId + "/details";

        // Perform the GET request
        mockMvc.perform(get(url)
                        .contentType("application/json"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(clientDetailsDtoJson)); // Compare JSON strings

        // Verify the service method was called once
        verify(clientDetailsService, times(1)).findByClientId(any(UUID.class));
    }

    public static ClientDetailsDto createMockClientDetailsDto() {
        // Create AddressDto instances
        AddressDto address1 = new AddressDto();
        address1.setAddressText("123 Main St");
        address1.setCity("Springfield");
        address1.setPostCode("62701");
        address1.setState("Telangana");
        address1.setAddressType("Permenant");
        address1.setCountry("India");
        address1.setTown("Hyd");

        AddressDto address2 = new AddressDto();
        address2.setAddressText("123 Main St");
        address2.setCity("Hyd");
        address2.setPostCode("632595");
        address2.setState("Telangana");
        address2.setAddressType("Secondary");
        address2.setCountry("India");
        address2.setTown("Hyd");

        // Create ContactDto instances
        ContactDto contact1 = new ContactDto();
        contact1.setName("Ram");
        contact1.setDepartment("IT");
        contact1.setDesignation("SE");
        contact1.setPhoneNumber("6325987402");
        contact1.setEmail("contact@example.com");

        ContactDto contact2 = new ContactDto();
        contact2.setName("Kiran");
        contact2.setDepartment("IT");
        contact2.setDesignation("SE");
        contact2.setPhoneNumber("6395846525");
        contact2.setEmail("contact1@example.com");

        // Create GstDto instances
        GstDto gst1 = new GstDto();
        gst1.setNumber("22AAAAA0000A1Z5");
        gst1.setLocation("Gujarat");

        // Create DocumentDto instance
        DocumentDto documentDto = new DocumentDto();
        documentDto.setName("Image");
        documentDto.setSize(203L);
        documentDto.setUrl("http://example.com/logo.png");
        documentDto.setContentType("image/png");

        // Create ClientDetailsDto instance and set its fields
        ClientDetailsDto clientDetailsDto = new ClientDetailsDto();
        clientDetailsDto.setCode("C12345");
        clientDetailsDto.setName("Example Client");

        List<AddressDto> addresses = new ArrayList<>();
        addresses.add(address1);
        addresses.add(address2);
        clientDetailsDto.setAddresses(addresses);

        List<ContactDto> contacts = new ArrayList<>();
        contacts.add(contact1);
        contacts.add(contact2);
        clientDetailsDto.setContacts(contacts);

        List<GstDto> gstList = new ArrayList<>();
        gstList.add(gst1);
        clientDetailsDto.setGst(gstList);

        clientDetailsDto.setClientLogo(documentDto);

        return clientDetailsDto;
    }

}
