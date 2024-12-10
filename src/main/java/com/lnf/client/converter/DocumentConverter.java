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

import com.lnf.client.model.ClientDocument;
import com.lnf.client.model.enums.DocumentType;
import com.lnf.dto.client.DocumentDto;
import com.lnf.exception.LnFException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.util.UUID;

public class DocumentConverter {

    private DocumentConverter() {
    }

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

    public static ClientDocument toEntityModel(MultipartFile transport, boolean awsS3BucketEnabled)
            throws IOException {
        if (transport == null) {
            return null;
        }
        ClientDocument entity = new ClientDocument();
        return toEntityModel(transport, entity, awsS3BucketEnabled);

    }

    public static ClientDocument toEntityModel(MultipartFile transport, ClientDocument entity,
                                               boolean awsS3BucketEnabled) throws IOException {
        if (transport == null || entity == null) {
            return null;
        }
        entity.setName(transport.getOriginalFilename() != null ? transport.getOriginalFilename() : transport.getName());
        entity.setContentType(transport.getContentType());
        entity.setSize(transport.getSize());
        entity.setContent(awsS3BucketEnabled ? new byte[0] : transport.getBytes());

        return entity;
    }

    public static String getDocumentUrl(UUID clientId, UUID documentId, DocumentType type) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(constructUrlFromType(clientId, type))
                .path(documentId.toString())
                .toUriString();
    }


    public static String constructUrlFromType(UUID clientId, DocumentType type) {
        String url = "";
        if (type == DocumentType.agreement) {
            url = "/lnf/clients/%s/agreement/".formatted(clientId);
        } else if (type == DocumentType.image) {
            url = "/lnf/clients/%s/image/".formatted(clientId);
        } else if (type == DocumentType.others) {
            url = "/lnf/clients/%s/others/".formatted(clientId);
        } else {
            throw new LnFException("Unknown document type");
        }
        return url;
    }
}
