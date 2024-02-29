package com.technofacts.lnf.client.converter;

import com.technofacts.lnf.client.model.Gst;
import com.technofacts.lnf.dto.client.GstDto;

public class GstConverter {

    public static GstDto toTransportModel(Gst entity) {
        if (entity == null) {
            return null;
        }

        return GstDto.builder()
                .id(entity.getId())
                .location(entity.getLocation())
                .number(entity.getNumber())
                .build();
    }

    public static Gst toEntityModel(GstDto transport) {
        return toEntityModel(transport, new Gst());
    }

    public static Gst toEntityModel(GstDto transport, Gst entity) {

        if (transport == null || entity == null) {
            return null;
        }
        entity.setId(transport.getId());
        entity.setLocation(transport.getLocation());
        entity.setNumber(transport.getNumber());

        return entity;
    }
}
