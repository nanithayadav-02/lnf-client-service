/*
 * Copyright (c)  Lever And Fulcrum (LNF)
 */
package com.technofacts.lnf.client.restapi;

import com.technofacts.lnf.dto.email.ExcelReportRequestDto;
import com.technofacts.lnf.dto.email.ThymeleafDocumentDto;
import com.technofacts.lnf.exception.LnFException;
import com.technofacts.lnf.service.email.ExcelReportRequestService;
import com.technofacts.lnf.service.email.ThymeleafDocumentService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Transactional
@Slf4j
public class EmailClientImpl extends BaseWebClientService implements ExcelReportRequestService, ThymeleafDocumentService {

    private final WebClient webClient;
    public static final String ERROR_OCCURRED_WHILE_GENERATING_EXCEL_BYTES_S = "Error occurred while generating Excel bytes [%s]";

    @Autowired
    public EmailClientImpl(@Qualifier("emailService") WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public byte[] generateReport(ExcelReportRequestDto requestDto) {
        try {
            WebClient.RequestHeadersSpec<?> spec = webClient.post()
                    .uri("/lnf/reports/excel")
                    .body(BodyInserters.fromValue(requestDto));
            addJwtToken(spec);

            return spec.retrieve()
                    .bodyToMono(byte[].class)
                    .block();
        } catch (Exception ex) {
            log.error("Error occurred while generating Excel bytes {}", ex.getMessage(), ex);
            throw new LnFException(String.format(ERROR_OCCURRED_WHILE_GENERATING_EXCEL_BYTES_S, ex.getMessage()), ex);
        }
    }


    @Override
    public byte[] generatePdf(ThymeleafDocumentDto resource) {
        try {
            WebClient.RequestHeadersSpec<?> spec = webClient.post()
                    .uri("/lnf/pdf")
                    .body(BodyInserters.fromValue(resource));

            addJwtToken(spec);

            return spec.retrieve()
                    .bodyToMono(byte[].class)
                    .block();
        } catch (Exception ex) {
            log.error("Error occurred while generating PDF {}", ex.getMessage(), ex);
            throw new LnFException(String.format("Error occurred while generating PDF [%s]", ex.getMessage()), ex);
        }
    }

}
