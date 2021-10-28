package com.technofacts.lnf.client.converter;

import com.technofacts.lnf.client.dto.AddressDto;
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

        if (transport == null) {
            return null;
        }
        ClientAddress entity = new ClientAddress();
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
