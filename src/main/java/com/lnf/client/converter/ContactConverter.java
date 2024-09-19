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

import com.lnf.client.model.ClientContact;
import com.lnf.dto.client.ContactDto;

public class ContactConverter {

    private ContactConverter() {
    }

    public static ContactDto toTransportModel(ClientContact entity) {
        if (entity == null) {
            return null;
        }

        return ContactDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .department(entity.getDepartment())
                .designation(entity.getDesignation())
                .email(entity.getEmail())
                .phoneNumber(entity.getPhoneNumber())
                .build();
    }

    public static ClientContact toEntityModel(ContactDto transport) {
        if (transport == null) {
            return null;
        }

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
