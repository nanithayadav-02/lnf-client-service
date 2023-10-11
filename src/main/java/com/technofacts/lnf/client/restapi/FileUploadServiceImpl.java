package com.technofacts.lnf.client.restapi;

import com.technofacts.lnf.exception.LnFEntityNotFoundException;
import com.technofacts.lnf.service.File.FileUploadService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${s3.service}")
    private String s3Service;

    @Override
    public String uploadFile(String folder, MultipartFile file) {
        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
        bodyBuilder.part("folder",folder);
        bodyBuilder.part("file", file.getResource());

        try {
            String uploadedFileUrl = webClient.post()
                    .uri(s3Service+"/upload")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("client file uploaded successfully");
            return uploadedFileUrl;
        } catch (LnFEntityNotFoundException ex) {
            log.error("File Upload for client Is Failed");
        } catch (RuntimeException ex) {
            log.error("File Upload for client Is Failed", ex.getMessage());
        }
        return null;
    }

    @Override
    public void deleteObjects(List<String> keys) {

    }

    @Override
    public ResponseEntity<byte[]> retrieveObject(String key) {
        return null;
    }

}
