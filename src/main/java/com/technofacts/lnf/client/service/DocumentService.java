package com.technofacts.lnf.client.service;

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
import com.technofacts.lnf.service.file.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static com.technofacts.lnf.client.converter.DocumentConverter.constructUrlFromType;

@Service
@Transactional
@RequiredArgsConstructor
@Log
public class DocumentService {

    private final ClientRepository clientRepository;
    private final ClientDocumentRepository repository;

    private final FileService fileService;

    @Value("${aws.s3.bucket.enabled}")
    private boolean awsS3BucketEnabled;

    @Value("${aws.s3.bucket.folderName}")
    private String folderName;

    /**
     * Returns DocumentDto client by clientId and document type.
     *
     * @param clientId Client Id
     * @param type     enum DocumentType
     * @return DocumentDto
     */
    public DocumentDto findByClientId (UUID clientId, DocumentType type) {
        try {
            if (awsS3BucketEnabled) {
                String filePath = String.format ("%s/%s/%s/", folderName, clientId, type);
                List<String> filePaths = fileService.findFilesInFolder (filePath);
                String fileName = Paths.get(filePaths.get (0)).getFileName().toString ();
                String url = ServletUriComponentsBuilder.fromCurrentContextPath ()
                                .path (constructUrlFromType(clientId, type))
                                .path(fileName)
                                .toUriString();

                DocumentDto documentDto = new DocumentDto ();
                documentDto.setName (fileName);
                documentDto.setUrl (url);

                return documentDto;
            } else {
                searchForClient (clientId);
                ClientDocument entity = searchForDocument (clientId, type);
                DocumentDto documentDto = DocumentConverter.toTransportModel (entity);
                documentDto.setUrl (DocumentConverter.getDocumentUrl (clientId, documentDto.getId (), type));
                return documentDto;
            }
        } catch (Exception e) {
            throw new LnFException ("file not found for clientId:" + e.getMessage ());
        }
    }

    /**
     * Returns ResponseEntity<byte[]> client by clientId and document type.
     *
     * @param clientId Client Id
     * @param type     enum DocumentType
     * @return ResponseEntity<byte [ ]>
     */
    public ResponseEntity<byte[]> findClientAgreement (UUID clientId, DocumentType type, String fileName) {
        try {
            if (awsS3BucketEnabled) {
                String filePath = String.format ("%s/%s/%s/%s", folderName, clientId, type, fileName);
                return fileService.findFileContent (filePath);
            } else {
                searchForClient (clientId);
                ClientDocument entity = searchForDocument (clientId, type);
                ClientDocument file = searchForDocument (entity.getId ());
                return ResponseEntity.ok ().header (HttpHeaders.CONTENT_TYPE, "application/pdf")
                        .contentType (MediaType.valueOf (file.getContentType ()))
                        .body (file.getContent ());
            }
        } catch (RuntimeException e) {
            String errorMessage = String.format ("file not found for Client[%s]", clientId);
            throw new com.technofacts.lnf.exception.LnFException (errorMessage, e);
        }
    }

    /**
     * Returns ResponseEntity with byte[] of the Client by clientId and documentId
     *
     * @param clientId   Client Id
     * @param documentId Document Id
     * @return ResponseEntity<byte [ ]>
     */
    public ResponseEntity<byte[]> findById (UUID clientId, UUID documentId, DocumentType type, String fileName) {
        try {
            if (awsS3BucketEnabled) {
                String filePath = String.format ("%s/%s/%s/%s", folderName, clientId, type, fileName);
                return fileService.findFileContent (filePath);
            } else {
                searchForClient (clientId);
                ClientDocument file = searchForDocument (documentId);
                return ResponseEntity.ok ().header (HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName () + "\"")
                        .contentType (MediaType.valueOf (file.getContentType ()))
                        .body (file.getContent ());
            }
        } catch (RuntimeException e) {
            String errorMessage = String.format ("file not found for client[%s]", clientId);
            throw new LnFException (errorMessage, e);
        }
    }

    /**
     * Creates the agreement with the client
     *
     * @param clientId Client Id
     * @param type     Enum DocumentType
     * @param file     Document in MultipartFile format
     */
    public void create (UUID clientId, DocumentType type, MultipartFile file) {
        try {
            if (awsS3BucketEnabled) {
                String folder = String.format ("%s/%s/%s/", folderName, clientId, type);
                String filePath = uploadFile (folder, file);
                log.info ("File uploaded successfully to S3 bucket: " + filePath);
            } else {
                Client client = searchForClient (clientId);
                UUID documentId = null;
                Optional<ClientDocument> entityOptional = repository.findByClientIdAndType (clientId, type);
                if (entityOptional.isPresent ()) {
                    documentId = entityOptional.get ().getId ();
                }
                LnFBadRequestException.throwOnCondition (Objects::isNull, file, String.format ("Failed to create Document of type [%s] for client [%s] with null payload", type, client));
                ClientDocument entity = DocumentConverter.toEntityModel (file, false);
                if (documentId != null) {
                    entity.setId (documentId);
                }
                entity.setClient (client);
                entity.setType (type);
                save (entity);
                log.info (() -> String.format ("Document [%s] for Client[%s] successfully created", file.getOriginalFilename (), clientId));
            }
        } catch (RuntimeException | IOException e) {

            String errorMessage = String.format ("Failed to create document[%s] for client [%s]", clientId, file.getOriginalFilename ());
            throw new LnFException (errorMessage, e);
        }
    }

    /**
     * Updates the client document
     *
     * @param clientId   Client id
     * @param documentId Document Id
     * @param file       Document in MultipartFile format
     */
    public void update (UUID clientId, UUID documentId, DocumentType type, MultipartFile file) {
        LnFBadRequestException.throwOnCondition (Objects::isNull, file, String.format ("Failed to update document for client [%s] with null payload", clientId));
        try {
            if (awsS3BucketEnabled) {
                String folder = String.format ("%s/%s/%s/", folderName, clientId, type);
                String filePath = uploadFile (folder, file);
                log.info (() -> String.format ("File [%s] for client  successfully updated in S3", filePath));
            } else {
                searchForClient (clientId);
                ClientDocument entity = searchForDocument (documentId);
                ClientDocument updatedEntity = DocumentConverter.toEntityModel (file, entity, false);
                save (updatedEntity);
            }
        } catch (RuntimeException | IOException e) {
            String errorMessage = String.format ("Failed to update document[%s] for client [%s]", documentId, clientId);
            throw new LnFException (errorMessage, e);
        }
        log.info (() -> String.format ("Document [%s] for Client[%s] successfully updated", documentId, clientId));
    }

    /**
     * Delete the client document by clientId and documentType
     *
     * @param clientId Client Id
     * @param type     Enum DocumentType
     */
    public void deleteByClientId (UUID clientId, DocumentType type, String fileName) {
        if (awsS3BucketEnabled) {
            String filePath = String.format ("%s/%s/%s/%s", folderName, clientId, type, fileName);
            List<String> filePaths = Collections.singletonList (filePath);
            fileService.delete (filePaths);
            log.info ("S3 object deleted for client");
        } else {
            searchForClient (clientId);
            ClientDocument entity = searchForDocument (clientId, type);
            try {
                repository.delete (entity);
            } catch (RuntimeException e) {
                String errorMessage = String.format ("Failed to delete document for client [%s]", clientId);
                throw new LnFException (errorMessage, e);
            }
        }
    }

    /**
     * Delete the client document by clientId and documentId
     *
     * @param clientId   Client Id
     * @param documentId document Id
     */
    public void deleteById (UUID clientId, UUID documentId) {
        searchForClient (clientId);
        ClientDocument entity = searchForDocument (documentId);
        try {
            repository.delete (entity);
            log.info (() -> String.format ("Document[%s] for client [%s] successfully deleted", documentId, clientId));
        } catch (RuntimeException e) {
            String errorMessage = String.format ("Failed to delete Document[[%s] for client [%s]", documentId, clientId);
            throw new LnFException (errorMessage);
        }
    }

    private void save (ClientDocument entity) {
        try {
            repository.save (entity);
        } catch (RuntimeException e) {
            String errorMessage = String.format ("Failed to save Document for employee [%s]", entity.getClient ().getId ());
            throw new LnFException (errorMessage);
        }
    }

    private Client searchForClient (UUID clientId) {
        return clientRepository.findById (clientId).orElseThrow (() -> new LnFEntityNotFoundException (String.format ("Client with id [%s] does not exist", clientId)));
    }

    private ClientDocument searchForDocument (UUID documentId) {
        return repository.findById (documentId).orElseThrow (() -> new LnFEntityNotFoundException (String.format ("Document with id [%s] does not exist", documentId)));
    }

    private ClientDocument searchForDocument (UUID clientId, DocumentType type) {
        return repository.findByClientIdAndType (clientId, type).orElseThrow (() -> new LnFEntityNotFoundException (String.format ("Document with clientId [%s] and type [%s] does not exist", clientId, type)));
    }

    private String uploadFile (String folder, MultipartFile file) {
        return fileService.uploadFile (folder, file);
    }
}
