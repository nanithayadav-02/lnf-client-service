package com.technofacts.lnf.client.converter;

import com.technofacts.lnf.dto.client.AddressDto;
import com.technofacts.lnf.client.model.ClientAddress;


public class AddressConverter {

    public static AddressDto toTransportModel(ClientAddress entity) {
        if (entity == null) {
            return null;
        }

        AddressDto dto = AddressDto.builder()
                .id(entity.getId())
                .addressText(entity.getAddressText())
                .town(entity.getTown())
                .city(entity.getCity())
                .state(entity.getState())
                .country(entity.getCountry())
                .postCode(entity.getPostCode())
                .build();

        return dto;
    }

    public static ClientAddress toEntityModel(AddressDto transport) {
        return toEntityModel(transport, new ClientAddress());
    }

    public static ClientAddress toEntityModel(AddressDto transport, ClientAddress entity) {

        if (transport == null || entity == null) {
            return null;
        }
        entity.setId(transport.getId());
        entity.setAddressText(transport.getAddressText());
        entity.setTown(transport.getTown());
        entity.setCity(transport.getCity());
        entity.setState(transport.getState());
        entity.setCountry(transport.getCountry());
        entity.setPostCode(transport.getPostCode());

        return entity;
    }

}
