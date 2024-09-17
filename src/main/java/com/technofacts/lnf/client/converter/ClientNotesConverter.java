package com.technofacts.lnf.client.converter;

import com.technofacts.lnf.client.model.ClientNotes;
import com.technofacts.lnf.dto.client.ClientNotesDto;

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
