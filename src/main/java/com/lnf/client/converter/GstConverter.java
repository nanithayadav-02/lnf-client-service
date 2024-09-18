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

import com.lnf.client.model.Gst;
import com.lnf.dto.client.GstDto;

public class GstConverter {

    private GstConverter() {
    }

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
        if (transport == null) {
            return null;
        }
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
