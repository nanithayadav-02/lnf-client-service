package com.technofacts.lnf.client.converter;

import com.technofacts.lnf.client.model.StatementOfWork;
import com.technofacts.lnf.client.model.enums.Status;
import com.technofacts.lnf.dto.client.StatementOfWorkDto;

public class StatementOfWorkConverter {

    public static StatementOfWorkDto toTransportModel(StatementOfWork entity) {
        if (entity == null) {
            return null;
        }

        return StatementOfWorkDto.builder()
                .id(entity.getId())
                .fileName(entity.getFileName())
                .startDate(entity.getStartDate())
                .expiryDate(entity.getExpiryDate())
                .description(entity.getDescription())
                .status(entity.getStatus().name())
                .build();

    }

    public static StatementOfWork toEntityModel(StatementOfWorkDto transport, StatementOfWork entity) {
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
