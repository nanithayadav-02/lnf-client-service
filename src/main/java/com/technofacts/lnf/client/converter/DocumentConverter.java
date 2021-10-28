package com.technofacts.lnf.client.converter;

import java.io.IOException;

import com.technofacts.lnf.client.dto.DocumentDto;
import com.technofacts.lnf.client.model.ClientDocument;
import org.springframework.web.multipart.MultipartFile;

public class DocumentConverter {

    public static DocumentDto toTransportModel(ClientDocument entity) {
        if (entity == null) {
            return null;
        }
        DocumentDto dto = new DocumentDto();

        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setContentType(entity.getContentType());
        dto.setSize(entity.getSize());

        return dto;
    }

    public static ClientDocument toEntityModel(MultipartFile transport) throws IOException {
        if (transport == null) {
            return null;
        }
        ClientDocument entity = new ClientDocument();
        return toEntityModel(transport, entity);

    }

    public static ClientDocument toEntityModel(MultipartFile transport, ClientDocument entity) throws IOException {
        if (transport == null || entity == null) {
            return null;
        }
        entity.setName(transport.getOriginalFilename() != null ? transport.getOriginalFilename() : transport.getName());
        entity.setContentType(transport.getContentType());
        entity.setSize(transport.getSize());
        entity.setContent(transport.getBytes());

        return entity;
    }
}
