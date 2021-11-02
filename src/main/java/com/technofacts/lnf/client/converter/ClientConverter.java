package com.technofacts.lnf.client.converter;

import javax.print.Doc;
import java.util.*;
import java.util.stream.Collectors;

import com.technofacts.lnf.client.dto.ClientDto;
import com.technofacts.lnf.client.dto.DocumentDto;
import com.technofacts.lnf.client.exception.LnFException;
import com.technofacts.lnf.client.model.*;
import com.technofacts.lnf.client.model.enums.DocumentType;

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
        dto.setWorkingFrom(entity.getWorkingFrom());
        dto.setAgreementExpiryDate(entity.getAgreementExpiryDate());
        dto.setClientDetails(entity.getClientDetails());
        dto.getContacts().addAll(entity.getClientContacts().stream().map(ContactConverter::toTransportModel).filter(Objects::nonNull).collect(Collectors.toList()));
        dto.setEscalation(entity.getEscalation() != null ? EscalationConverter.toTransportModel(entity.getEscalation()) : null);
        dto.setAddress(entity.getClientAddress() != null ? AddressConverter.toTransportModel(entity.getClientAddress()) : null);
        dto.getGst().addAll(entity.getGst().stream().map(GstConverter::toTransportModel).filter(Objects::nonNull).collect(Collectors.toList()));
        Optional<ClientDocument> agreementOpt = entity.getFiles().stream().filter(file -> file.getType() == DocumentType.agreement).findFirst();

        if (agreementOpt.isPresent()) {
            DocumentDto documentDto = DocumentConverter.toTransportModel(agreementOpt.get());
            documentDto.setUrl(DocumentConverter.getDocumentUrl(entity.getId(), documentDto.getId(), DocumentType.agreement));
            dto.setAgreement(documentDto);
        }

        Optional<ClientDocument> imageOpt = entity.getFiles().stream().filter(file -> file.getType() == DocumentType.image).findFirst();
        if (imageOpt.isPresent()) {
            DocumentDto documentDto = DocumentConverter.toTransportModel(imageOpt.get());
            documentDto.setUrl(DocumentConverter.getDocumentUrl(entity.getId(), documentDto.getId(), DocumentType.image));
            dto.setClientLogo(documentDto);
        }

        return dto;
    }

    public static Client toEntityModel(ClientDto transport) {
        return toEntityModel(transport, new Client());
    }

    public static Client toEntityModel(ClientDto transport, Client entity) {

        if (transport == null || entity == null) {
            return null;
        }

        entity.setId(transport.getId());
        entity.setCode(transport.getCode());
        entity.setName(transport.getName());
        entity.setPan(transport.getPan());
        entity.setWorkingFrom(transport.getWorkingFrom());
        entity.setAgreementExpiryDate(transport.getAgreementExpiryDate());
        entity.setClientDetails(transport.getClientDetails());

        return entity;
    }
}
