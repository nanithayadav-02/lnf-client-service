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

import com.lnf.client.model.Task;
import com.lnf.dto.client.TaskDto;

public class TaskConverter {

    private TaskConverter() {
    }

    public static TaskDto toTransportModel(Task entity) {

        if (entity == null) {
            return null;
        }
        TaskDto dto = new TaskDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setStatus(entity.getStatus());
        dto.setType(entity.getType());
        dto.setStartDate(entity.getStartDate());
        dto.setEndDate(entity.getEndDate());
        dto.setUploadTime(entity.getUploadTime());

        return dto;
    }

    public static Task toEntityModel(TaskDto transport) {
        if (transport == null) {
            return null;
        }
        return toEntityModel(transport, new Task());
    }

    public static Task toEntityModel(TaskDto transport, Task entity) {

        if (transport == null || entity == null) {
            return null;
        }

        entity.setId(transport.getId());
        entity.setName(transport.getName());
        entity.setDescription(transport.getDescription());
        entity.setStatus(transport.getStatus());
        entity.setType(transport.getType());
        entity.setStartDate(transport.getStartDate());
        entity.setEndDate(transport.getEndDate());
        entity.setUploadTime(transport.getUploadTime());

        return entity;
    }
}
