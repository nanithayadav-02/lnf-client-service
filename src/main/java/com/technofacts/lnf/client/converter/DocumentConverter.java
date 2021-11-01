package com.technofacts.lnf.client.converter;

import java.io.IOException;
import java.util.UUID;

import com.technofacts.lnf.client.dto.DocumentDto;
import com.technofacts.lnf.client.exception.LnFException;
import com.technofacts.lnf.client.model.ClientDocument;
import com.technofacts.lnf.client.model.enums.DocumentType;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

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

    public static String getDocumentUrl(UUID clientId, UUID documentId, DocumentType type) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(constructUrlFromType(clientId, type))
                .path(documentId.toString())
                .toUriString();
    }


    private static String constructUrlFromType(UUID clientId, DocumentType type) {
        String url = "";
        if (type == DocumentType.agreement) {
            url = String.format("/lnf/clients/%s/agreement/", clientId);
        } else if (type == DocumentType.image) {
            url = String.format("/lnf/clients/%s/image/", clientId);
        } else if (type == DocumentType.others) {
            url = String.format("/lnf/clients/%s/others/", clientId);
        } else {
            new LnFException("Unknown document type");
        }
        return url;
    }
}
