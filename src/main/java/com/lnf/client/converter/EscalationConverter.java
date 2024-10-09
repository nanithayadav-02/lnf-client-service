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

import com.lnf.client.model.Escalation;
import com.lnf.dto.client.EscalationDto;


public class EscalationConverter {

    private EscalationConverter() {
    }

    public static EscalationDto toTransportModel(Escalation entity) {
        if (entity == null) {
            return null;
        }

        return EscalationDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .mobileNumber(entity.getMobileNumber())
                .phoneNumber(entity.getPhoneNumber())
                .email(entity.getEmail())
                .build();
    }

    public static Escalation toEntityModel(EscalationDto transport) {
        if (transport == null) {
            return null;
        }
        return toEntityModel(transport, new Escalation());
    }

    public static Escalation toEntityModel(EscalationDto transport, Escalation entity) {

        if (transport == null || entity == null) {
            return null;
        }

        entity.setId(transport.getId());
        entity.setName(transport.getName());
        entity.setMobileNumber(transport.getMobileNumber());
        entity.setPhoneNumber(transport.getPhoneNumber());
        entity.setEmail(transport.getEmail());

        return entity;
    }

}
