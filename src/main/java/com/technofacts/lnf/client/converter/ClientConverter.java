package com.technofacts.lnf.client.converter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.technofacts.lnf.client.model.*;
import com.technofacts.lnf.client.model.enums.DocumentType;
import com.technofacts.lnf.dto.client.ClientDto;
import com.technofacts.lnf.dto.client.DocumentDto;

public class ClientConverter {

    public static ClientDto toTransportModel(Client entity) {

        if (entity == null) {
            return null;
        }
        ClientDto dto = new ClientDto();
        dto.setId(entity.getId());
        dto.setCode(entity.getCode());
        dto.setName(entity.getName());
        dto.setPan(entity.getPan());
        dto.setTan(entity.getTan());
        dto.setStatus(entity.getStatus());
        dto.setWorkingFrom(entity.getWorkingFrom());
        dto.setAgreementExpiryDate(entity.getAgreementExpiryDate());
        dto.setServiceType(entity.getServiceType());
        dto.setClientDetails(entity.getClientDetails());
        dto.getContacts().addAll(entity.getClientContacts ().stream ().map (ContactConverter::toTransportModel).filter (Objects::nonNull).toList ());
        dto.getEscalations().addAll (entity.getEscalations ().stream ().map (EscalationConverter::toTransportModel).filter (Objects::nonNull).toList ());
        dto.setAddress(entity.getClientAddress() != null ? AddressConverter.toTransportModel(entity.getClientAddress()) : null);
        dto.getGst().addAll(entity.getGst ().stream ().map (GstConverter::toTransportModel).filter (Objects::nonNull).toList ());
        dto.setNotes(new ArrayList<>());
        dto.getNotes().addAll(entity.getNotes().stream()
                .map(ClientNotesConverter::toTransportModel).filter(Objects::nonNull).toList());

        return dto;
    }

    public static Client toEntityModel(ClientDto transport) {
        Client entity = toEntityModel(transport, new Client());
        addContactsToEntityModel(transport, entity);
        addEscalationToEntityModel(transport, entity);
        addAddressToEntityModel(transport, entity);
        addGstToEntityModel(transport, entity);
        addClientNotesToEntityModel(transport, entity);

        return entity;
    }

    public static Client toEntityModel(ClientDto transport, Client entity) {

        if (transport == null || entity == null) {
            return null;
        }

        entity.setId(transport.getId());
        entity.setCode(transport.getCode());
        entity.setName(transport.getName());
        entity.setPan(transport.getPan());
        entity.setTan(transport.getTan());
        entity.setStatus(transport.getStatus());
        entity.setWorkingFrom(transport.getWorkingFrom());
        entity.setAgreementExpiryDate(transport.getAgreementExpiryDate());
        entity.setServiceType(transport.getServiceType());
        entity.setClientDetails(transport.getClientDetails());

        return entity;
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
        transport.getEscalations ().stream().filter(Objects::nonNull).forEach(dto -> {
            Escalation escalation = EscalationConverter.toEntityModel(dto);
            escalation.setClient(client);
            escalationList.add(escalation);
        });
        client.getEscalations().addAll(escalationList);
    }

    private static void addAddressToEntityModel(ClientDto transport, Client client) {
        if (transport.getAddress() != null) {
            ClientAddress address = AddressConverter.toEntityModel(transport.getAddress());
            address.setClient(client);
            client.setClientAddress(address);
        }
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
