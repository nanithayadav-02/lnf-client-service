package com.technofacts.lnf.client.converter;

import com.technofacts.lnf.client.dto.ClientDto;
import com.technofacts.lnf.client.model.Client;

public class ClientConverter {

    public static ClientDto toTransportModel(Client entity) {

        if (entity == null) {
            return null;
        }
        ClientDto dto = new ClientDto();
        dto.setId(entity.getId());
        dto.setClientId(entity.getClientId());
        return dto;
    }

    public static Client toEntityModel(ClientDto transport) {
        if (transport == null) {
            return null;
        }
        Client entity = new Client();
        entity.setId(transport.getId());
        entity.setClientId(transport.getClientId());
        return entity;
    }

}
