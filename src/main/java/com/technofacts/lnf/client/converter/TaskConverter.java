package com.technofacts.lnf.client.converter;

import com.technofacts.lnf.client.model.Task;
import com.technofacts.lnf.dto.client.TaskDto;

public class TaskConverter {

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

        return entity;
    }
}
