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

package com.lnf.client.restapi;

import com.lnf.dto.file.FileDto;
import com.lnf.exception.LnFEntityNotFoundException;
import com.lnf.exception.LnFException;
import com.lnf.service.file.FileFolderService;
import com.lnf.service.file.FileService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Transactional
@Slf4j
public class FileClientImpl extends BaseWebClientService implements FileService, FileFolderService {

    private final WebClient webClient;

    @Value("${aws.s3.bucket.service}")
    private String s3Service;

    @Autowired
    public FileClientImpl(@Qualifier("fileServiceWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public String uploadFile(String folder, MultipartFile file) {
        // Build the multipart body
        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
        bodyBuilder.part("folder", folder);
        bodyBuilder.part("file", file.getResource());

        try {
            // Create the web request, adding JWT token if available
            WebClient.RequestHeadersSpec<?> spec = webClient.post()
                    .uri(s3Service + "/upload")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(bodyBuilder.build()));

            // Conditionally add the JWT token to the request headers
            addJwtToken(spec);

            // Execute the request and block to get the response, consider using subscribe for a non-blocking approach
            return spec.retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (WebClientResponseException e) {
            log.error("File Upload failed. Status code: {}, Message: {}", e.getStatusCode(), e.getMessage(), e);
            throw new LnFException("File Upload failed" + e);
        } catch (RuntimeException e) {
            log.error("Unexpected error occurred during file upload", e);
            throw new LnFException("File Upload for employee failed" + e);
        }
    }

    @Override
    public List<String> uploadFiles(String folder, List<MultipartFile> files) {
        return Collections.emptyList();
    }

    @Override
    public List<String> findFilesInFolder(String folderName) {
        List<String> files = new ArrayList<>();
        try {
            WebClient.RequestHeadersSpec<?> spec = webClient.get()
                    .uri(s3Service + "/folder-name?folderName={folderName}", folderName)
                    .accept(MediaType.APPLICATION_JSON);
            // Conditionally add the JWT token to the request headers
            addJwtToken(spec);
            // Execute the request and block to get the response, consider using subscribe for a non-blocking approach
            files = spec.retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<String>>() {
                    })
                    .block();

        } catch (Exception ex) {
            log.error("Failed to get the files in the folder with error message : {}", ex.getMessage());
        }
        return files;
    }

    @Override
    public void delete(List<String> filePaths) {
        try {
            String joinedKeys = String.join(",", filePaths);
            WebClient.RequestHeadersSpec<?> spec = webClient
                    .delete()
                    .uri(s3Service + "?filePaths=" + joinedKeys)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            addJwtToken(spec);
            spec.retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (Exception e) {
            log.error("Failed to delete files with keys {}: {}", filePaths, e.getMessage());
            throw new LnFException("Failed to delete files with keys " + filePaths, e);
        }
    }

    public ResponseEntity<byte[]> findFile(String filePath) {
        return null;
    }

    @Override
    public ResponseEntity<byte[]> findFileContent(String filePath) {
        try {
            WebClient.RequestHeadersSpec<?> spec = webClient.get()
                    .uri(s3Service + "/content" + "?filePath={filePath}", filePath);
            addJwtToken(spec);
            ResponseEntity<byte[]> response = spec
                    .retrieve()
                    .toEntity(byte[].class)
                    .block();
            log.info("file is retrieved");
            return response;
        } catch (Exception ex) {
            log.error("File is not retrieved {}", ex.getMessage());
            throw new LnFException("Failed to retrieve file content due to exception : ", ex);
        }
    }

    @Override
    public List<FileDto> findFiles(String folderName) {
        List<FileDto> files = new ArrayList<>();
        try {
            WebClient.RequestHeadersSpec<?> spec = webClient.get()
                    .uri(s3Service + "/folder-names?folderName={folderName}", folderName)
                    .accept(MediaType.APPLICATION_JSON);
            // Conditionally add the JWT token to the request headers
            addJwtToken(spec);
            // Execute the request and block to get the response, consider using subscribe for a non-blocking approach
            files = spec.retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<FileDto>>() {
                    })
                    .block();

        } catch (LnFEntityNotFoundException ex) {
            log.error("Failed to get the files in the folder with error message : {}", ex.getMessage());
            throw new LnFException("Failed to get the files with exception ", ex);
        }
        return files;
    }

}
