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

import com.lnf.client.model.ClientAddress;
import com.lnf.client.model.enums.AddressType;
import com.lnf.dto.client.AddressDto;

public class AddressConverter {

    private AddressConverter() {
    }

    public static AddressDto toTransportModel(ClientAddress entity) {
        if (entity == null) {
            return null;
        }

        return AddressDto.builder()
                .id(entity.getId())
                .addressText(entity.getAddressText())
                .town(entity.getTown())
                .city(entity.getCity())
                .state(entity.getState())
                .country(entity.getCountry())
                .postCode(entity.getPostCode())
                .addressType(entity.getAddressType() != null ? entity.getAddressType().name() : null)
                .build();
    }

    public static ClientAddress toEntityModel(AddressDto transport) {
        if (transport == null) {
            return null;
        }

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
        entity.setAddressType(transport.getAddressType() != null ? AddressType.valueOf(transport.getAddressType()) : null);
        return entity;
    }

}
