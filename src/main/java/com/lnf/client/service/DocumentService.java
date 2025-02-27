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

package com.lnf.client.service;

import com.lnf.client.model.Client;
import com.lnf.client.model.ClientDocument;
import com.lnf.client.model.enums.DocumentType;
import com.lnf.client.repository.ClientDocumentRepository;
import com.lnf.client.repository.ClientRepository;
import com.lnf.dto.client.DocumentDto;
import com.lnf.dto.file.FileDto;
import com.lnf.exception.LnFBadRequestException;
import com.lnf.exception.LnFEntityNotFoundException;
import com.lnf.exception.LnFException;
import com.lnf.service.file.FileFolderService;
import com.lnf.service.file.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class DocumentService {

    public static final String S_S_S_S = "%s/%s/%s/%s";
    public static final String S_S_S = "%s/%s/%s/";
    private final ClientRepository clientRepository;
    private final ClientDocumentRepository repository;
    private final FileFolderService fileFolderService;
    private final FileService fileService;

    @Value("${aws.s3.bucket.folderName}")
    private String folderName;

    /**
     * Returns DocumentDto client by clientId and document type.
     *
     * @param clientId Client Id
     * @param type     enum DocumentType
     * @return DocumentDto
     */
    public DocumentDto findByClientId(UUID clientId, String type) {
        try {
            searchForClient(clientId);
            var filePath = S_S_S.formatted(folderName, clientId, type);
            List<FileDto> filePaths = fileFolderService.findFiles(filePath);
            if (filePaths == null || filePaths.isEmpty()) {
                log.error("Document not found for client {} ", clientId);
                return null;
            }

            return setDocumentDto(clientId, type, filePaths);
        } catch (Exception e) {
            throw new LnFException("file not found for clientId:" + e.getMessage());
        }
    }

    public static String constructUrlFromType(UUID clientId, DocumentType type) {
        String url = "";
        switch (type) {
            case DocumentType.agreement -> "/lnf/clients/%s/agreement/".formatted(clientId);
            case DocumentType.image -> "/lnf/clients/%s/image/".formatted(clientId);
            case DocumentType.others -> "/lnf/clients/%s/others/".formatted(clientId);
            default -> throw new LnFException("Unknown document type");
        }
        return url;
    }

    private static DocumentDto setDocumentDto(UUID clientId, String type, List<FileDto> filePaths) {
        var fileName = StringUtils.substringAfterLast(filePaths.getFirst().getFileName(), "/");
        String url = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(constructUrlFromType(clientId, DocumentType.valueOf(type)))
                .path(fileName)
                .toUriString();

        DocumentDto documentDto = new DocumentDto();
        documentDto.setName(fileName);
        documentDto.setSize(filePaths.getFirst().getFileSize());
        documentDto.setUrl(url);
        return documentDto;
    }

    /**
     * Returns ResponseEntity<byte[]> client by clientId and document type.
     *
     * @param clientId Client Id
     * @param type     enum DocumentType
     * @return ResponseEntity<byte [ ]>
     */
    public ResponseEntity<byte[]> findClientAgreement(UUID clientId, DocumentType type, String fileName) {
        try {
            searchForClient(clientId);
            var filePath = S_S_S_S.formatted(folderName, clientId, type, fileName);
            return fileService.findFileContent(filePath);
        } catch (RuntimeException e) {
            String errorMessage = "file not found for Client[%s]".formatted(clientId);
            throw new com.lnf.exception.LnFException(errorMessage, e);
        }
    }

    /**
     * Returns ResponseEntity with byte[] of the Client by clientId and documentId
     *
     * @param clientId Client Id
     * @return ResponseEntity<byte [ ]>
     */
    public ResponseEntity<byte[]> findById(UUID clientId, DocumentType type, String fileName) {
        try {
            searchForClient(clientId);
            var filePath = S_S_S_S.formatted(folderName, clientId, type, fileName);
            return fileService.findFileContent(filePath);
        } catch (RuntimeException e) {
            String errorMessage = "file not found for client[%s]".formatted(clientId);
            throw new LnFException(errorMessage, e);
        }
    }

    /**
     * Creates the agreement with the client
     *
     * @param clientId Client Id
     * @param type     Enum DocumentType
     * @param file     Document in MultipartFile format
     */
    public void create(UUID clientId, DocumentType type, MultipartFile file) {
        try {
            searchForClient(clientId);
            var folder = S_S_S.formatted(folderName, clientId, type);
            uploadFile(folder, file);
        } catch (RuntimeException e) {
            String errorMessage = "Failed to create document[%s] for client [%s]".formatted(clientId, file.getOriginalFilename());
            throw new LnFException(errorMessage, e);
        }
    }

    /**
     * Updates the client document
     *
     * @param clientId Client id
     * @param file     Document in MultipartFile format
     */
    public void update(UUID clientId, DocumentType type, MultipartFile file) {
        LnFBadRequestException.throwOnCondition(Objects::isNull, file,
                "Failed to update document for client [%s] with null payload".formatted(clientId));
        try {
            searchForClient(clientId);
            var folder = S_S_S.formatted(folderName, clientId, type);
            uploadFile(folder, file);
        } catch (RuntimeException e) {
            String errorMessage = "Failed to update document for client [%s]".formatted(clientId);
            throw new LnFException(errorMessage, e);
        }
    }

    /**
     * Delete the client document by clientId and documentType
     *
     * @param clientId Client Id
     * @param type     Enum DocumentType
     */
    public void deleteByClientId(UUID clientId, DocumentType type, String fileName) {
        searchForClient(clientId);
        var filePath = S_S_S_S.formatted(folderName, clientId, type, fileName);
        List<String> filePaths = Collections.singletonList(filePath);
        fileService.delete(filePaths);
        log.debug("S3 object deleted for client");
    }

    private Client searchForClient(UUID clientId) {
        return clientRepository.findById(clientId).orElseThrow(() -> new LnFEntityNotFoundException("Client with id [%s] does not exist".formatted(clientId)));
    }

    private ClientDocument searchForDocument(UUID clientId, DocumentType type) {
        return repository.findByClientIdAndType(clientId, type).orElseThrow(() -> new LnFEntityNotFoundException("Document with clientId [%s] and type [%s] does not exist".formatted(clientId, type)));
    }

    private void uploadFile(String folder, MultipartFile file) {
        String filePath = fileService.uploadFile(folder, file);
        log.debug("File uploaded successfully to S3 bucket: {}", filePath);
    }

}
