package com.technofacts.lnf.client.service;

import com.technofacts.lnf.client.converter.ClientAgreementConverter;
import com.technofacts.lnf.client.model.Agreement;
import com.technofacts.lnf.client.model.Client;
import com.technofacts.lnf.client.repository.ClientAgreementRepository;
import com.technofacts.lnf.client.repository.ClientRepository;
import com.technofacts.lnf.dto.client.AgreementDto;
import com.technofacts.lnf.dto.file.FileDto;
import com.technofacts.lnf.exception.LnFBadRequestException;
import com.technofacts.lnf.exception.LnFEntityNotFoundException;
import com.technofacts.lnf.exception.LnFException;
import com.technofacts.lnf.service.file.FileFolderService;
import com.technofacts.lnf.service.file.FileService;
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
        String filePath = String.format(S_S_S, folderName, clientId, AGREEMENTS);
        List<FileDto> files = fileFolderService.findFiles(filePath);
        List<AgreementDto> agreementDtos = new ArrayList<>();
        files.forEach(file -> {
            String fileName = StringUtils.substringAfterLast(file.getFileName(), "/");
            String downloadURL = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path(String.format("/lnf/clients/%s/agreements/%s", clientId, fileName))
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
            String filePath = String.format("%s/%s/%s/%s", folderName, clientId, AGREEMENTS, fileName);
            return fileService.findFileContent(filePath);
        } catch (RuntimeException e) {
            String errorMessage = String.format("file not found for agreement[%s]", clientId);
            throw new LnFException(errorMessage, e);
        }
    }

    public void create(UUID clientId, MultipartFile[] files, List<AgreementDto> resource) {
        LnFBadRequestException.throwOnCondition(Objects::isNull, resource, String.format("failed to create agreement with null payload [%s]", clientId));
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

                var folder = String.format(S_S_S, folderName, clientId, AGREEMENTS);
                String filePath = fileService.uploadFile(folder, file);
                log.error("File uploaded successfully to S3 bucket: " + filePath);
            } catch (RuntimeException e) {
                String errorMessage = String.format("Failed to create agreement[%s] for client [%s]", file.getName(), clientId);
                throw new LnFException(errorMessage, e);
            }
        });
    }

    public void update(UUID clientId, String fileName, MultipartFile file, AgreementDto resource) {
        com.technofacts.lnf.exception.LnFBadRequestException.throwOnCondition(Objects::isNull, file,
                String.format("Failed to update file for agreement [%s] with null payload", fileName));
        try {
            Agreement entity = searchForFileName(fileName);
            Agreement updatedEntity = ClientAgreementConverter.toEntityModel(resource, entity);
            save(updatedEntity);
            String s3ObjectKey = String.format("%s/%s/%s/%s", folderName, clientId, AGREEMENTS, fileName);
            List<String> filePaths = Collections.singletonList(s3ObjectKey);
            fileService.delete(filePaths);
            log.error("S3 object deleted for sows file");

            //Before Updating the file we are deleting from the s3 bucket
            var folder = String.format(S_S_S, folderName, clientId, AGREEMENTS);
            String filePath = fileService.uploadFile(folder, file);

            log.error("File uploaded successfully to S3 bucket: " + filePath);
            log.error("fileName {} for agreement {} successfully updated", fileName, clientId);
        } catch (RuntimeException e) {
            String errorMessage = String.format("Failed to update fileName[%s] for agreement [%s]", fileName, clientId);
            throw new LnFException(errorMessage, e);
        }
    }

    public void deleteByIdAndFileName(UUID clientId, String fileName) {
        searchForClientId(clientId);
        Agreement entity = searchForFileName(fileName);
        try {
            String s3ObjectKey = String.format("%s/%s/%s/%s", folderName, clientId, AGREEMENTS, fileName);
            List<String> filePaths = Collections.singletonList(s3ObjectKey);
            fileService.delete(filePaths);
            log.error("S3 object deleted for agreement file");
            repository.delete(entity);
            log.error("file {} for agreement {} successfully deleted", fileName, clientId);
        } catch (RuntimeException e) {
            String errorMessage = String.format("Failed to delete File[[%s] for agreement [%s]", fileName, clientId);
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
            String errorMessage = String.format("Failed to save agreement for client [%s]", entity.getId());
            throw new LnFException(errorMessage);
        }
    }

    private Agreement searchForFileName(String fileName) {
        return repository.findByFileName(fileName).
                orElseThrow(() -> new LnFEntityNotFoundException(
                        String.format("agreement file with fileName [%s] does not exist", fileName)));
    }

    private Client searchForClientId(UUID clientId) {
        return clientRepository.findByClientId(clientId)
                .orElseThrow(() -> new LnFEntityNotFoundException(String.format("Client with id [%s] does not exist", clientId)));
    }

}
