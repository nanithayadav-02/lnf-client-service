package com.technofacts.lnf.client.converter;

import com.technofacts.lnf.client.model.Agreement;
import com.technofacts.lnf.client.model.enums.Status;
import com.technofacts.lnf.dto.client.AgreementDto;

public class ClientAgreementConverter {

    private ClientAgreementConverter() {
    }

    public static AgreementDto toTransportModel(Agreement entity) {
        if (entity == null) {
            return null;
        }

        return AgreementDto.builder()
                .id(entity.getId())
                .fileName(entity.getFileName())
                .startDate(entity.getStartDate())
                .expiryDate(entity.getExpiryDate())
                .description(entity.getDescription())
                .status(entity.getStatus().name())
                .build();

    }

    public static Agreement toEntityModel(AgreementDto transport, Agreement entity) {
        if (transport == null || entity == null) {
            return null;
        }

        entity.setId(transport.getId());
        entity.setFileName(transport.getFileName());
        entity.setStartDate(transport.getStartDate());
        entity.setExpiryDate(transport.getExpiryDate());
        entity.setDescription(transport.getDescription());
        entity.setStatus(Status.valueOf(transport.getStatus()));

        return entity;
    }

}
