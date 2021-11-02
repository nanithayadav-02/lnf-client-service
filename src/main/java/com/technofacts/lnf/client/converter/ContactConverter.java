package com.technofacts.lnf.client.converter;

import com.technofacts.lnf.client.dto.ContactDto;
import com.technofacts.lnf.client.model.ClientContact;

public class ContactConverter {

    public static ContactDto toTransportModel(ClientContact entity) {
        if (entity == null) {
            return null;
        }

        ContactDto dto = ContactDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .department(entity.getDepartment())
                .designation(entity.getDesignation())
                .email(entity.getEmail())
                .phoneNumber(entity.getPhoneNumber())
                .build();

        return dto;
    }

    public static ClientContact toEntityModel(ContactDto transport) {
        return toEntityModel(transport, new ClientContact());
    }

    public static ClientContact toEntityModel(ContactDto transport, ClientContact entity) {

        if (transport == null || entity == null) {
            return null;
        }

        entity.setId(transport.getId());
        entity.setName(transport.getName());
        entity.setDepartment(transport.getDepartment());
        entity.setEmail(transport.getEmail());
        entity.setPhoneNumber(transport.getPhoneNumber());
        entity.setDesignation(transport.getDesignation());

        return entity;
    }
}
