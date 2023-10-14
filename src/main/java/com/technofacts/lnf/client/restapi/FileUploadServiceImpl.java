package com.technofacts.lnf.client.restapi;

import com.technofacts.lnf.exception.LnFEntityNotFoundException;
import com.technofacts.lnf.exception.LnFException;
import com.technofacts.lnf.service.File.FileUploadService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;


@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class FileUploadServiceImpl implements FileUploadService {

    private final WebClient webClient;

    @Value("${aws.s3.bucket.service}")
    private String s3Service;

    @Override
    public String uploadFile(String folder, MultipartFile file) {
        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
        bodyBuilder.part("folder", folder);
        bodyBuilder.part("file", file.getResource());

        try {
            String uploadedFileUrl = webClient.post()
                    .uri(s3Service + "/upload")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("Employee file uploaded successfully");
            return uploadedFileUrl;
        } catch (LnFEntityNotFoundException ex) {
            log.error("File Upload for Employee Is Failed",ex.getMessage());
        } catch (RuntimeException ex) {
            log.error("File Upload for Employee Is Failed", ex.getMessage());
        }
        return null;
    }

    @Override
    public void deleteObjects(List<String> keys) {
        try {
            String joinedKeys = String.join(",", keys);

            webClient
                    .delete()
                    .uri(s3Service + "/keys" +"?keys=" + joinedKeys)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (Exception e) {
            log.error("Failed to delete files with keys {}: {}", keys, e.getMessage());
            throw new LnFException("Failed to delete files with keys " + keys, e);
        }
    }

    public ResponseEntity<byte[]> retrieveObject(String key) {
        try {
            ResponseEntity<byte[]> response = webClient.get()
                    .uri(s3Service + "/key?key={key}", key)
                    .accept(MediaType.APPLICATION_OCTET_STREAM)
                    .retrieve()
                    .toEntity(byte[].class)
                    .block();
            log.info("file is retrieved");
            return response;
        } catch (Exception ex) {
            log.error("File is not retrieved", ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
