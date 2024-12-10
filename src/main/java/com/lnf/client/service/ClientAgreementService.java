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

import com.lnf.client.converter.ClientAgreementConverter;
import com.lnf.client.model.Agreement;
import com.lnf.client.model.Client;
import com.lnf.client.repository.ClientAgreementRepository;
import com.lnf.client.repository.ClientRepository;
import com.lnf.dto.client.AgreementDto;
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

import java.util.*;
import java.util.stream.IntStream;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ClientAgreementService {

    public static final String AGREEMENTS = "agreements";
    public static final String S_S_S = "%s/%s/%s/";
    private final ClientAgreementRepository repository;
    private final ClientRepository clientRepository;
    private final FileService fileService;
    private final FileFolderService fileFolderService;
    @Value("${aws.s3.bucket.folderName}")
    private String folderName;

    public List<AgreementDto> findByClientId(UUID clientId) {
        String filePath = S_S_S.formatted(folderName, clientId, AGREEMENTS);
        List<FileDto> files = fileFolderService.findFiles(filePath);
        List<AgreementDto> agreementDtos = new ArrayList<>();
        files.forEach(file -> {
            String fileName = StringUtils.substringAfterLast(file.getFileName(), "/");
            String downloadURL = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/lnf/clients/%s/agreements/%s".formatted(clientId, fileName))
                    .toUriString();
            setClientAgreement(agreementDtos, file, fileName, downloadURL);
        });
        return agreementDtos;
    }

    private void setClientAgreement(List<AgreementDto> agreementDtos, FileDto file, String fileName, String downloadURL) {
        Agreement entity = searchForFileName(fileName);
        AgreementDto agreementDto = ClientAgreementConverter.toTransportModel(entity);
        agreementDto.setFileName(fileName);
        agreementDto.setUrl(downloadURL);
        agreementDto.setSize(file.getFileSize());
        agreementDto.setLastModified(file.getLastModified());
        agreementDtos.add(agreementDto);
    }

    public ResponseEntity<byte[]> findById(UUID clientId, String fileName) {
        try {
            searchForFileName(fileName);
            String filePath = "%s/%s/%s/%s".formatted(folderName, clientId, AGREEMENTS, fileName);
            return fileService.findFileContent(filePath);
        } catch (RuntimeException e) {
            String errorMessage = "file not found for agreement[%s]".formatted(clientId);
            throw new LnFException(errorMessage, e);
        }
    }

    public void create(UUID clientId, MultipartFile[] files, List<AgreementDto> resource) {
        LnFBadRequestException.throwOnCondition(Objects::isNull, resource, "failed to create agreement with null payload [%s]".formatted(clientId));
        Client client = searchForClientId(clientId);

        List<Agreement> entities = new ArrayList<>();
        IntStream.range(0, files.length).forEach(i -> {
            MultipartFile file = files[i];
            AgreementDto agreementDto = resource.get(i);
            try {
                Agreement entity = ClientAgreementConverter.toEntityModel(agreementDto, new Agreement());
                entity.setClient(client);
                entities.add(entity);
                save(entities);

                var folder = S_S_S.formatted(folderName, clientId, AGREEMENTS);
                String filePath = fileService.uploadFile(folder, file);
                log.debug("File uploaded successfully to S3 bucket: " + filePath);
            } catch (RuntimeException e) {
                String errorMessage = "Failed to create agreement[%s] for client [%s]".formatted(file.getName(), clientId);
                throw new LnFException(errorMessage, e);
            }
        });
    }

    public void update(UUID clientId, String fileName, MultipartFile file, AgreementDto resource) {
        com.lnf.exception.LnFBadRequestException.throwOnCondition(Objects::isNull, file,
                "Failed to update file for agreement [%s] with null payload".formatted(fileName));
        try {
            Agreement entity = searchForFileName(fileName);
            Agreement updatedEntity = ClientAgreementConverter.toEntityModel(resource, entity);
            save(updatedEntity);
            String s3ObjectKey = "%s/%s/%s/%s".formatted(folderName, clientId, AGREEMENTS, fileName);
            List<String> filePaths = Collections.singletonList(s3ObjectKey);
            fileService.delete(filePaths);
            log.debug("S3 object deleted for sows file");

            //Before Updating the file we are deleting from the s3 bucket
            var folder = S_S_S.formatted(folderName, clientId, AGREEMENTS);
            String filePath = fileService.uploadFile(folder, file);

            log.debug("File uploaded successfully to S3 bucket: " + filePath);
            log.debug("fileName {} for agreement {} successfully updated", fileName, clientId);
        } catch (RuntimeException e) {
            String errorMessage = "Failed to update fileName[%s] for agreement [%s]".formatted(fileName, clientId);
            throw new LnFException(errorMessage, e);
        }
    }

    public void deleteByIdAndFileName(UUID clientId, String fileName) {
        searchForClientId(clientId);
        Agreement entity = searchForFileName(fileName);
        try {
            String s3ObjectKey = "%s/%s/%s/%s".formatted(folderName, clientId, AGREEMENTS, fileName);
            List<String> filePaths = Collections.singletonList(s3ObjectKey);
            fileService.delete(filePaths);
            log.debug("S3 object deleted for agreement file");
            repository.delete(entity);
            log.debug("file {} for agreement {} successfully deleted", fileName, clientId);
        } catch (RuntimeException e) {
            String errorMessage = "Failed to delete File[[%s] for agreement [%s]".formatted(fileName, clientId);
            throw new LnFException(errorMessage);
        }
    }

    private void save(List<Agreement> entities) {
        try {
            repository.saveAll(entities);
        } catch (RuntimeException e) {
            String errorMessage = String.format("Failed to save agreement for client [%s]", entities.get(0).getClient().getId());
            throw new LnFException(errorMessage);
        }
    }

    private void save(Agreement entity) {
        try {
            repository.save(entity);
        } catch (RuntimeException e) {
            String errorMessage = "Failed to save agreement for client [%s]".formatted(entity.getId());
            throw new LnFException(errorMessage);
        }
    }

    private Agreement searchForFileName(String fileName) {
        return repository.findByFileName(fileName).
                orElseThrow(() -> new LnFEntityNotFoundException(
                        "agreement file with fileName [%s] does not exist".formatted(fileName)));
    }

    private Client searchForClientId(UUID clientId) {
        return clientRepository.findByClientId(clientId)
                .orElseThrow(() -> new LnFEntityNotFoundException("Client with id [%s] does not exist".formatted(clientId)));
    }

}
