package com.technofacts.lnf.client.restapi;

import com.technofacts.lnf.dto.email.ThymeleafDocumentDto;
import com.technofacts.lnf.dto.email.ThymeleafEmailDto;
import com.technofacts.lnf.service.email.ThymeleafDocumentService;
import com.technofacts.lnf.service.email.ThymeleafEmailService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Transactional
@Slf4j
public class EmailClientImpl extends BaseWebClientService implements ThymeleafEmailService, ThymeleafDocumentService {

    private final WebClient webClient;

    @Autowired
    public EmailClientImpl(@Qualifier("emailService") WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public byte[] generatePdf(ThymeleafDocumentDto resource) {
        WebClient.RequestHeadersSpec<?> spec = webClient.post()
                .uri("/lnf/pdf")
                .body(BodyInserters.fromValue(resource));

        addJwtToken(spec);

        return spec.retrieve()
                .bodyToMono(byte[].class)
                .block();
    }

    @Override
    public void sendEmail(ThymeleafEmailDto resource) {

    }

    @Override
    public void sendEmailWithPdf(ThymeleafEmailDto resource) {
        try {
            sendEmail(resource,  "/lnf/email/pdf");
        } catch (Exception ex) {
            log.error(String.format("Error occurred while sending Thymeleaf email [%s]", ex));
        }
    }

    private void sendEmail(ThymeleafEmailDto resource,String uri) {

        WebClient.RequestHeadersSpec<?> spec = webClient.post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(resource));

        addJwtToken(spec);

        spec.retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
