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

import com.lnf.client.model.enums.AddressType;
import com.lnf.client.model.Client;
import com.lnf.dto.client.AddressDto;
import com.lnf.dto.client.ClientDetailsDto;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class ClientDetailsConverter {

    private ClientDetailsConverter() {
    }

    public static ClientDetailsDto toTransportModel(Client entity) {

        if (entity == null) {
            return null;
        }
        ClientDetailsDto dto = new ClientDetailsDto();
        dto.setCode(entity.getCode());
        dto.setName(entity.getName());
        dto.setAddresses(new ArrayList<>());
        dto.getContacts().addAll(entity.getClientContacts().stream().map(ContactConverter::toTransportModel).filter(Objects::nonNull).toList());
        dto.getGst().addAll(entity.getGst().stream().map(GstConverter::toTransportModel).filter(Objects::nonNull).toList());

        List<AddressDto> sortedAddresses = entity.getClientAddresses().stream()
                .map(AddressConverter::toTransportModel)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingInt(address ->
                        address.getAddressType() != null && address.getAddressType().equals(AddressType.Primary.name()) ? 0 : 1))
                .toList();

        dto.getAddresses().addAll(sortedAddresses);

        return dto;
    }

}
