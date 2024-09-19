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

import com.lnf.client.converter.StatementOfWorkConverter;
import com.lnf.client.model.Project;
import com.lnf.client.model.StatementOfWork;
import com.lnf.client.repository.ProjectRepository;
import com.lnf.client.repository.StatementOfWorkRepository;
import com.lnf.dto.client.StatementOfWorkDto;
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
public class StatementOfWorkService {

    public static final String PROJECT = "project";
    public static final String SOW = "sow";
    public static final String S_S_S_S_S = "%s/%s/%s/%s/%s/";
    private final StatementOfWorkRepository repository;
    private final ProjectRepository projectRepository;
    private final FileService fileService;
    private final FileFolderService fileFolderService;

    @Value("${aws.s3.bucket.folderName}")
    private String folderName;

    public List<StatementOfWorkDto> findByProjectIdAndClientId( UUID clientId, UUID projectId) {
        String filePath =  String.format(S_S_S_S_S, folderName, clientId, PROJECT, projectId, SOW);
        List<FileDto> files = fileFolderService.findFiles(filePath);
        List<StatementOfWorkDto> statementOfWorkDtos = new ArrayList<>();
        files.forEach(file -> {
            String fileName = StringUtils.substringAfterLast(file.getFileName(), "/");
            String downloadURL = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path(String.format("/lnf/clients/%s/project/%s/%s/%s", clientId, projectId, SOW, fileName))
                    .toUriString();
            setStatementOfWork(statementOfWorkDtos, file, fileName, downloadURL);
        });
        return statementOfWorkDtos;
    }

    private void setStatementOfWork(List<StatementOfWorkDto> statementOfWorkDtos, FileDto file, String fileName,
                                    String downloadURL) {
        StatementOfWork entity = searchForFileName(fileName);
        StatementOfWorkDto statementOfWorkDto = StatementOfWorkConverter.toTransportModel(entity);
        statementOfWorkDto.setFileName(fileName);
        statementOfWorkDto.setUrl(downloadURL);
        statementOfWorkDto.setSize(file.getFileSize());
        statementOfWorkDto.setLastModified(file.getLastModified());
        statementOfWorkDtos.add(statementOfWorkDto);
    }

    public ResponseEntity<byte[]> findById(UUID clientId, UUID projectId, String fileName) {
        try {
            searchForFileName(fileName);
            String filePath = String.format("%s/%s/%s/%s/%s/%s", folderName, clientId, PROJECT, projectId, SOW, fileName);
            return fileService.findFileContent(filePath);
        } catch (RuntimeException e) {
            String errorMessage = String.format("file not found for sows[%s]", projectId);
            throw new LnFException(errorMessage, e);
        }
    }

    public void create(UUID clientId, UUID projectId, MultipartFile[] files, List<StatementOfWorkDto> resource) {
        LnFBadRequestException.throwOnCondition(Objects::isNull, resource, String.format("failed to create sows with null payload [%s]", projectId));
        Project project = searchForProjectIdAndClientId(projectId, clientId);

        List<StatementOfWork> entities = new ArrayList<>();
        IntStream.range(0, files.length).forEach(i -> {
            MultipartFile file = files[i];
            StatementOfWorkDto statementOfWorkDto = resource.get(i);
            try {
                StatementOfWork entity = StatementOfWorkConverter.toEntityModel(statementOfWorkDto, new StatementOfWork());
                entity.setProject(project);
                entities.add(entity);
                save(entities);

                var folder = String.format(S_S_S_S_S, folderName, clientId, PROJECT, projectId, SOW);
                String filePath = fileService.uploadFile(folder, file);
                log.debug("File uploaded successfully to S3 bucket: " + filePath);
            } catch (RuntimeException e) {
                String errorMessage = String.format("Failed to create sows[%s] for project [%s]", file.getName(), projectId);
                throw new LnFException(errorMessage, e);
            }
        });
    }

    public void update(UUID clientId, UUID projectId, String fileName, MultipartFile file, StatementOfWorkDto resource) {
        com.lnf.exception.LnFBadRequestException.throwOnCondition(Objects::isNull, file,
                String.format("Failed to update file for sows [%s] with null payload", fileName));
        try {
            StatementOfWork entity = searchForFileName(fileName);
            StatementOfWork updatedEntity = StatementOfWorkConverter.toEntityModel(resource, entity);
            save(updatedEntity);
            String s3ObjectKey = String.format("%s/%s/%s/%s/%s/%s", folderName, clientId, PROJECT, projectId, SOW, fileName);
            List<String> filePaths = Collections.singletonList(s3ObjectKey);
            fileService.delete(filePaths);
            log.debug("S3 object deleted for sows file");

            //Before Updating the file we are deleting from the s3 bucket
            var folder = String.format(S_S_S_S_S, folderName, clientId, PROJECT, projectId, SOW);
            String filePath = fileService.uploadFile(folder, file);

            log.debug("File uploaded successfully to S3 bucket: " + filePath);
            log.debug("fileName {} for sows {} successfully updated", fileName, projectId);
        } catch (RuntimeException e) {
            String errorMessage = String.format("Failed to update fileName[%s] for sows [%s]", fileName, projectId);
            throw new LnFException(errorMessage, e);
        }
    }

    public void deleteByIdAndFileName(UUID clientId, UUID projectId, String fileName) {
        searchForProjectIdAndClientId(projectId, clientId);
        StatementOfWork entity = searchForFileName(fileName);
        try {
            String s3ObjectKey = String.format("%s/%s/%s/%s/%s/%s", folderName, clientId, PROJECT, projectId, SOW, fileName);
            List<String> filePaths = Collections.singletonList(s3ObjectKey);
            fileService.delete(filePaths);
            log.debug("S3 object deleted for sows file");
            repository.delete(entity);
            log.debug("file {} for sows {} successfully deleted", fileName, projectId);
        } catch (RuntimeException e) {
            String errorMessage = String.format("Failed to delete File[[%s] for sows [%s]", fileName, projectId);
            throw new LnFException(errorMessage);
        }
    }

    private void save(List<StatementOfWork> entities) {
        try {
            repository.saveAll(entities);
        } catch (RuntimeException e) {
            String errorMessage = String.format("Failed to save sows for project [%s]", entities.get(0).getProject().getId());
            throw new LnFException(errorMessage);
        }
    }

    private void save(StatementOfWork entities) {
        try {
            repository.save(entities);
        } catch (RuntimeException e) {
            String errorMessage = String.format("Failed to save sows for project [%s]", entities.getId());
            throw new LnFException(errorMessage);
        }
    }

    private StatementOfWork searchForFileName(String fileName) {
        return repository.findByFileName(fileName).
                orElseThrow(() -> new LnFEntityNotFoundException(
                        String.format("sows file with fileName [%s] does not exist", fileName)));
    }

    private Project searchForProjectIdAndClientId(UUID projectId, UUID clientId) {
        return projectRepository.findByProjectIdAndClientId(projectId, clientId)
                .orElseThrow(() -> new LnFEntityNotFoundException(String.format("Project with id [%s] does not exist", projectId)));
    }

}
