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

import com.lnf.client.model.Agreement;
import com.lnf.client.model.enums.Status;
import com.lnf.dto.client.AgreementDto;

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
