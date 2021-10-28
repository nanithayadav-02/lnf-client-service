package com.technofacts.lnf.client.converter;

import com.technofacts.lnf.client.dto.GstDto;
import com.technofacts.lnf.client.model.Gst;

public class GstConverter {

    public static GstDto toTransportModel(Gst entity) {
        if (entity == null) {
            return null;
        }

        GstDto dto = GstDto.builder()
                .id(entity.getId())
                .location(entity.getLocation())
                .number(entity.getNumber())
                .build();

        return dto;
    }

    public static Gst toEntityModel(GstDto transport) {

        if (transport == null) {
            return null;
        }
        Gst entity = new Gst();
        entity.setId(transport.getId());
        entity.setLocation(transport.getLocation());
        entity.setNumber(transport.getNumber());

        return entity;
    }
}
