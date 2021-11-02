package com.technofacts.lnf.client.converter;

import com.technofacts.lnf.client.dto.EscalationDto;
import com.technofacts.lnf.client.model.Escalation;


public class EscalationConverter {

    public static EscalationDto toTransportModel(Escalation entity) {
        if (entity == null) {
            return null;
        }

        EscalationDto dto = EscalationDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .mobileNumber(entity.getMobileNumber())
                .phoneNumber(entity.getPhoneNumber())
                .email(entity.getEmail())
                .build();

        return dto;
    }

    public static Escalation toEntityModel(EscalationDto transport) {
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
