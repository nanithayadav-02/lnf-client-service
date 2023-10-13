package com.technofacts.lnf.client.service;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import com.technofacts.lnf.client.converter.DocumentConverter;
import com.technofacts.lnf.client.model.Client;
import com.technofacts.lnf.client.model.ClientDocument;
import com.technofacts.lnf.client.model.enums.DocumentType;
import com.technofacts.lnf.client.repository.ClientDocumentRepository;
import com.technofacts.lnf.client.repository.ClientRepository;
import com.technofacts.lnf.dto.client.DocumentDto;
import com.technofacts.lnf.exception.LnFBadRequestException;
import com.technofacts.lnf.exception.LnFEntityNotFoundException;
import com.technofacts.lnf.exception.LnFException;
import com.technofacts.lnf.service.File.FileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
@RequiredArgsConstructor
@Log
public class DocumentService {

    private final ClientRepository clientRepository;
    private final ClientDocumentRepository repository;

    private final FileUploadService fileUploadService;

    /**
     * Returns DocumentDto client by clientId and document type.
     *
     * @param clientId Client Id
     * @param type     enum DocumentType
     * @return DocumentDto
     */
    public DocumentDto findByClientId(UUID clientId, DocumentType type) {
        searchForClient(clientId);
        ClientDocument entity = searchForDocument(clientId, type);
        DocumentDto documentDto = DocumentConverter.toTransportModel(entity);
        documentDto.setUrl(DocumentConverter.getDocumentUrl(clientId, documentDto.getId(), type));
        return documentDto;
    }

    /**
     * Returns ResponseEntity<byte[]> client by clientId and document type.
     *
     * @param clientId Client Id
     * @param type enum DocumentType
     * @return ResponseEntity<byte []>
     */
    public ResponseEntity<byte[]>  findClientAgreement(UUID clientId, DocumentType type) {
        searchForClient(clientId);
        ClientDocument entity = searchForDocument(clientId, type);
        ClientDocument file = searchForDocument(entity.getId());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
                .contentType(MediaType.valueOf(file.getContentType()))
                .body(file.getContent());
    }

    /**
     * Returns ResponseEntity with byte[] of the Client by clientId and documentId
     *
     * @param clientId   Client Id
     * @param documentId Document Id
     * @return ResponseEntity<byte [ ]>
     */
    public ResponseEntity<byte[]> findById(UUID clientId, UUID documentId) {
        searchForClient(clientId);
        ClientDocument file = searchForDocument(documentId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                .contentType(MediaType.valueOf(file.getContentType()))
                .body(file.getContent());
    }

    /**
     * Creates the agreement with the client
     *
     * @param clientId Client Id
     * @param type     Enum DocumentType
     * @param file     Dcoument in MutipartFile format
     */
    public void create(UUID clientId, DocumentType type, MultipartFile file) {
        Client client = searchForClient(clientId);
        UUID documentId = null;
        Optional<ClientDocument> entityOptional = repository.findByClientIdAndType(clientId, type);
        if (entityOptional.isPresent()) {
            documentId = entityOptional.get().getId();
        }

        try {
            LnFBadRequestException.throwOnCondition(Objects::isNull, file, String.format("Failed to create Document of type [%s] for client [%s] with null payload", type, client));
            ClientDocument entity = DocumentConverter.toEntityModel(file);
            if (documentId != null) {
                entity.setId(documentId);
            }
            entity.setClient(client);
            entity.setType(type);
            save(entity);
            log.info(() -> String.format("Document [%s] for Client[%s] successfully created", file.getOriginalFilename(), clientId));

        } catch (RuntimeException | IOException e) {

            String errorMessage = String.format("Failed to create document[%s] for client [%s]", clientId, file.getOriginalFilename());
            throw new LnFException(errorMessage, e);
        }

    }

    /**
     * Updates the client document
     *
     * @param clientId   Client id
     * @param documentId Document Id
     * @param file       Document in MutipartFile format
     */
    public void update(UUID clientId, UUID documentId, MultipartFile file) {
        LnFBadRequestException.throwOnCondition(Objects::isNull, file, String.format("Failed to update document for client [%s] with null payload", clientId));
        searchForClient(clientId);
        ClientDocument entity = searchForDocument(documentId);
        try {
            ClientDocument updatedEntity = DocumentConverter.toEntityModel(file, entity);
            save(updatedEntity);
        } catch (RuntimeException | IOException e) {
            String errorMessage = String.format("Failed to update document[%s] for client [%s]", documentId, clientId);
            throw new LnFException(errorMessage, e);
        }
        log.info(() -> String.format("Document [%s] for Client[%s] successfully updated", documentId, clientId));
    }

    /**
     * Delete the client document by clientId and documentType
     *
     * @param clientId Client Id
     * @param type     Enum DocumentType
     */
    public void deleteByClientId(UUID clientId, DocumentType type) {
        searchForClient(clientId);
        ClientDocument entity = searchForDocument(clientId, type);
        try {
            repository.delete(entity);
        } catch (RuntimeException e) {
            String errorMessage = String.format("Failed to delete document for client [%s]", clientId);
            throw new LnFException(errorMessage, e);
        }
    }

    /**
     * Delete the client document by clientId and documentId
     *
     * @param clientId   Client Id
     * @param documentId document Id
     */
    public void deleteById(UUID clientId, UUID documentId) {
        searchForClient(clientId);
        ClientDocument entity = searchForDocument(documentId);
        try {
            repository.delete(entity);
            log.info(() -> String.format("Document[%s] for client [%s] successfully deleted", documentId, clientId));
        } catch (RuntimeException e) {
            String errorMessage = String.format("Failed to delete Document[[%s] for client [%s]", documentId, clientId);
            throw new LnFException(errorMessage);
        }
    }

    private void save(ClientDocument entity) {
        try {
            repository.save(entity);
        } catch (RuntimeException e) {
            String errorMessage = String.format("Failed to save Document for employee [%s]", entity.getClient().getId());
            throw new LnFException(errorMessage);
        }
    }

    private Client searchForClient(UUID clientId) {
        return clientRepository.findById(clientId).
                orElseThrow(() -> new LnFEntityNotFoundException(String.format("Client with id [%s] does not exist", clientId)));
    }

    private ClientDocument searchForDocument(UUID documentId) {
        return repository.findById(documentId).
                orElseThrow(() -> new LnFEntityNotFoundException(String.format("Document with id [%s] does not exist", documentId)));
    }

    private ClientDocument searchForDocument(UUID clientId, DocumentType type) {
        return repository.findByClientIdAndType(clientId, type).
                orElseThrow(() -> new LnFEntityNotFoundException(String.format("Document with clientId [%s] and type [%s] does not exist", clientId, type)));
    }

    private String uploadFile(String folder, MultipartFile file) {
        return fileUploadService.uploadFile(folder,file);
    }
}
