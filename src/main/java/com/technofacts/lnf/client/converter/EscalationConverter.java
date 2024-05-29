package com.technofacts.lnf.client.converter;

import com.technofacts.lnf.client.model.Escalation;
import com.technofacts.lnf.dto.client.EscalationDto;


public class EscalationConverter {

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
