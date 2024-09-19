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

import com.lnf.client.model.ClientNotes;
import com.lnf.dto.client.ClientNotesDto;

public class ClientNotesConverter {

    private ClientNotesConverter() {
    }

    public static ClientNotesDto toTransportModel(ClientNotes entity) {
        if (entity == null) {
            return null;
        }
        ClientNotesDto dto = new ClientNotesDto();
        dto.setId(entity.getId());
        dto.setDescription(entity.getDescription());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedTime(entity.getCreatedTime());
        dto.setLastUpdatedBy(entity.getLastUpdatedBy());
        dto.setLastUpdatedTime(entity.getLastUpdatedTime());


        return dto;
    }

    public static ClientNotes toEntityModel(ClientNotesDto transport) {
        if (transport == null) {
            return null;
        }

        return toEntityModel(transport, new ClientNotes());
    }

    public static ClientNotes toEntityModel(ClientNotesDto transport, ClientNotes entity) {
        if (transport == null || entity == null) {
            return null;
        }
        entity.setId(transport.getId());
        entity.setDescription(transport.getDescription());
        entity.setCreatedBy(transport.getCreatedBy());
        entity.setCreatedTime(transport.getCreatedTime());
        entity.setLastUpdatedBy(transport.getLastUpdatedBy());
        entity.setLastUpdatedTime(transport.getLastUpdatedTime());

        return entity;
    }

}
