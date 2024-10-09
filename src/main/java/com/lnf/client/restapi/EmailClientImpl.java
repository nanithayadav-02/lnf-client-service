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

import com.lnf.dto.email.ExcelReportDto;
import com.lnf.dto.email.ThymeleafDocumentDto;
import com.lnf.exception.LnFException;
import com.lnf.service.email.ExcelReportService;
import com.lnf.service.email.ThymeleafDocumentService;
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
public class EmailClientImpl extends BaseWebClientService implements ThymeleafDocumentService, ExcelReportService {

    private final WebClient webClient;
    public static final String ERROR_OCCURRED_WHILE_GENERATING_EXCEL_BYTES_S = "Error occurred while generating Excel bytes [%s]";

    @Autowired
    public EmailClientImpl(@Qualifier("emailServiceWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public byte[] generateReport(ExcelReportDto requestDto) {
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
