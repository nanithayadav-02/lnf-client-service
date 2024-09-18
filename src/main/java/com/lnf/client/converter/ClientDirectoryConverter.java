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
package com.lnf.client.converter;

import com.lnf.client.model.ClientDirectory;
import com.lnf.dto.client.ClientDirectoryDto;

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
