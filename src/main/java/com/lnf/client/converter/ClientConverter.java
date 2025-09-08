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

import com.lnf.client.model.*;
import com.lnf.client.model.enums.AddressType;
import com.lnf.dto.client.AddressDto;
import com.lnf.dto.client.ClientDto;
import com.lnf.dto.client.ClientOverviewDto;

import java.util.*;

public class ClientConverter {

    private ClientConverter() {
    }

    public static ClientDto toTransportModel(Client entity) {

        if (entity == null) {
            return null;
        }
        ClientDto dto = new ClientDto();
        dto.setId(entity.getId());
        dto.setOpeningBalance(entity.getOpeningBalance());
        dto.setCode(entity.getCode());
        dto.setName(entity.getName());
        dto.setPan(entity.getPan());
        dto.setTan(entity.getTan());
        dto.setStatus(entity.getStatus());
        dto.setWorkingFrom(entity.getWorkingFrom());
        dto.setAgreementExpiryDate(entity.getAgreementExpiryDate());
        dto.setServiceType(entity.getServiceType());
        dto.setClientDetails(entity.getClientDetails());
        dto.setUploadTime(entity.getUploadTime());
        dto.setCategory(entity.getCategory());
        dto.setSubCategory(entity.getSubCategory());
        dto.setType(entity.getType());
        Optional.ofNullable(entity.getClientContacts())
                .ifPresent(clientContacts ->
                        dto.getContacts().addAll(
                                clientContacts.stream()
                                        .map(ContactConverter::toTransportModel)
                                        .filter(Objects::nonNull)
                                        .toList()
                        )
                );
        Optional.ofNullable(entity.getEscalations())
                .ifPresent(escalations ->
                        dto.getEscalations().addAll(
                                escalations.stream()
                                        .map(EscalationConverter::toTransportModel)
                                        .filter(Objects::nonNull)
                                        .toList()
                        )
                );

        Optional.ofNullable(entity.getGst())
                .ifPresent(gst ->
                        dto.getGst().addAll(
                                gst.stream()
                                        .map(GstConverter::toTransportModel)
                                        .filter(Objects::nonNull)
                                        .toList()
                        )
                );
        dto.setNotes(new ArrayList<>());
        dto.setAddresses(new ArrayList<>());
        Optional.ofNullable(entity.getNotes())
                .ifPresent(notes ->
                        dto.getNotes().addAll(
                                notes.stream()
                                        .map(ClientNotesConverter::toTransportModel)
                                        .filter(Objects::nonNull)
                                        .toList()
                        )
                );

        List<AddressDto> sortedAddresses = Optional.ofNullable(entity.getClientAddresses())
                .map(addresses -> addresses.stream()
                        .map(AddressConverter::toTransportModel)
                        .filter(Objects::nonNull)
                        .sorted(Comparator.comparingInt(address ->
                                address.getAddressType() != null && address.getAddressType().equals(AddressType.Primary.name()) ? 0 : 1))
                        .toList())
                .orElse(new ArrayList<>());

        dto.getAddresses().addAll(sortedAddresses);

        Optional.ofNullable(entity.getClientDirectories())
                .ifPresent(clientDirectories ->
                        dto.getClientDirectoryDtos().addAll(
                                clientDirectories.stream()
                                        .map(ClientDirectoryConverter::toTransportModel)
                                        .filter(Objects::nonNull)
                                        .toList()
                        )
                );

        return dto;
    }

    public static ClientOverviewDto toMiniTransportModel(Client entity) {

        if (entity == null) {
            return null;
        }
        ClientOverviewDto dto = new ClientOverviewDto();
        dto.setId(entity.getId());
        dto.setCode(entity.getCode());
        dto.setName(entity.getName());
        dto.setPan(entity.getPan());
        dto.setTan(entity.getTan());
        dto.setStatus(entity.getStatus());
        dto.setWorkingFrom(entity.getWorkingFrom());
        dto.setAgreementExpiryDate(entity.getAgreementExpiryDate());

        return dto;
    }

    public static Client toEntityModel(ClientDto transport) {

        if (transport == null) {
            return null;
        }

        Client entity = toEntityModel(transport, new Client());
        addContactsToEntityModel(transport, entity);
        addEscalationToEntityModel(transport, entity);
        addGstToEntityModel(transport, entity);
        addClientNotesToEntityModel(transport, entity);
        addAddressesToEntityModel(transport, entity);
        addClientDirectoryToEntityModel(transport, entity);

        return entity;
    }

    public static Client toEntityModel(ClientDto transport, Client entity) {

        if (transport == null || entity == null) {
            return null;
        }

        entity.setId(transport.getId());
        entity.setCode(transport.getCode());
        entity.setOpeningBalance(transport.getOpeningBalance());
        entity.setName(transport.getName());
        entity.setPan(transport.getPan());
        entity.setTan(transport.getTan());
        entity.setStatus(transport.getStatus());
        entity.setWorkingFrom(transport.getWorkingFrom());
        entity.setAgreementExpiryDate(transport.getAgreementExpiryDate());
        entity.setServiceType(transport.getServiceType());
        entity.setClientDetails(transport.getClientDetails());
        entity.setCategory(transport.getCategory());
        entity.setSubCategory(transport.getSubCategory());
        entity.setType(transport.getType());

        return entity;
    }

    private static void addClientDirectoryToEntityModel(ClientDto transport, Client client) {
        List<ClientDirectory> entityList = new ArrayList<>();
        transport.getClientDirectoryDtos().stream().filter(Objects::nonNull).forEach(dto -> {
            ClientDirectory entity = ClientDirectoryConverter.toEntityModel(dto);
            entity.setClient(client);
            entityList.add(entity);
        });
        client.getClientDirectories().addAll(entityList);

    }

    private static void addAddressesToEntityModel(ClientDto transport, Client client) {
        List<ClientAddress> clientAddressList = new ArrayList<>();
        transport.getAddresses().stream().filter(Objects::nonNull).forEach(dto -> {
            ClientAddress entity = AddressConverter.toEntityModel(dto);
            entity.setClient(client);
            clientAddressList.add(entity);
        });
        client.getClientAddresses().addAll(clientAddressList);
    }

    private static void addContactsToEntityModel(ClientDto transport, Client client) {
        List<ClientContact> contactList = new ArrayList<>();
        transport.getContacts().stream().filter(Objects::nonNull).forEach(dto -> {
            ClientContact entity = ContactConverter.toEntityModel(dto);
            entity.setClient(client);
            contactList.add(entity);
        });
        client.getClientContacts().addAll(contactList);
    }

    private static void addEscalationToEntityModel(ClientDto transport, Client client) {
        List<Escalation> escalationList = new ArrayList<>();
        transport.getEscalations().stream().filter(Objects::nonNull).forEach(dto -> {
            Escalation escalation = EscalationConverter.toEntityModel(dto);
            escalation.setClient(client);
            escalationList.add(escalation);
        });
        client.getEscalations().addAll(escalationList);
    }

    private static void addGstToEntityModel(ClientDto transport, Client client) {
        List<Gst> gstList = new ArrayList<>();
        transport.getGst().stream().filter(Objects::nonNull).forEach(dto -> {
            Gst entity = GstConverter.toEntityModel(dto);
            entity.setClient(client);
            gstList.add(entity);
        });
        client.getGst().addAll(gstList);
    }

    private static void addClientNotesToEntityModel(ClientDto transport, Client client) {
        List<ClientNotes> clientNotesList = new ArrayList<>();
        transport.getNotes().stream().filter(Objects::nonNull).forEach(dto -> {
            ClientNotes entity = ClientNotesConverter.toEntityModel(dto);
            entity.setClient(client);
            clientNotesList.add(entity);
        });
        client.getNotes().addAll(clientNotesList);
    }

}
