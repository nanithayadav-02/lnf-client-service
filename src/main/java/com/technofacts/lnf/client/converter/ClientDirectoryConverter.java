/*
 * Copyright (c)  Lever And Fulcrum (LNF)
 */
package com.technofacts.lnf.client.converter;

import com.technofacts.lnf.client.model.ClientDirectory;
import com.technofacts.lnf.dto.client.ClientDirectoryDto;

public class ClientDirectoryConverter {

    private ClientDirectoryConverter() {
    }

    public static ClientDirectoryDto toTransportModel(ClientDirectory entity) {

        if (entity == null) {
            return null;
        }

        return ClientDirectoryDto.builder()
        		.id(entity.getId())
        		.firstName(entity.getFirstName())
        		.lastName(entity.getLastName())
        		.email(entity.getEmail())
        		.phoneNumber(entity.getPhoneNumber())
        		.active(entity.getActive())
        		.build();

    }

    public static ClientDirectory toEntityModel(ClientDirectoryDto transport) {
        if (transport == null) {
            return null;
        }
        return toEntityModel(transport, new ClientDirectory());
    }

    public static ClientDirectory toEntityModel(ClientDirectoryDto transport, ClientDirectory entity) {

        if (transport == null || entity == null) {
            return null;
        }

        entity.setFirstName(transport.getFirstName());
        entity.setLastName(transport.getLastName());
        entity.setEmail(transport.getEmail());
        entity.setPhoneNumber(transport.getPhoneNumber());
        entity.setActive(transport.getActive());
        entity.setId(transport.getId());
        return entity;
    }

}
