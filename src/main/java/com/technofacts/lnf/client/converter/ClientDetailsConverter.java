package com.technofacts.lnf.client.converter;

import com.technofacts.lnf.client.model.Client;
import com.technofacts.lnf.client.model.enums.AddressType;
import com.technofacts.lnf.dto.client.AddressDto;
import com.technofacts.lnf.dto.client.ClientDetailsDto;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class ClientDetailsConverter {

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
