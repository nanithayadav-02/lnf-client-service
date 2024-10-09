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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lnf.dto.common.PageRequestDto;
import com.lnf.dto.timesheet.TimesheetDto;
import com.lnf.exception.LnFException;
import com.lnf.service.timesheet.TimesheetService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@Slf4j
public class TimesheetClientImpl extends BaseWebClientService implements TimesheetService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Autowired
    public TimesheetClientImpl(@Qualifier("timesheetServiceWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public List<Map<String, Object>> findAllTimesheet(String employeeId, Integer month, Integer year) {
        return null;
    }

    @Override
    public Page<TimesheetDto> findTimeSheetsByEmployeeIds(List<String> employeeIds, List<UUID> projectIds,
                                                          Optional<Integer> month, Optional<Integer> year,
                                                          PageRequestDto pageRequestDto) {
        try {
            // Build the WebClient request spec
            WebClient.RequestHeadersSpec<?> spec = webClient.post()
                    .uri(uriBuilder -> uriBuilder.path("/lnf/timesheet/project-employees")
                            .queryParam("projectId", projectIds)
                            .queryParam("month", month.orElse(null))
                            .queryParam("year", year.orElse(null))
                            .queryParam("page", pageRequestDto.getPage())
                            .queryParam("size", pageRequestDto.getSize())
                            .build())
                    .body(BodyInserters.fromValue(employeeIds))
                    .accept(MediaType.APPLICATION_JSON);

            // Add JWT token to request headers if available
            addJwtToken(spec);

            String responseBody = spec.retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode responseMap = objectMapper.readTree(responseBody);
            List<TimesheetDto> content = objectMapper.convertValue(responseMap.get("content"), new TypeReference<>() {
            });
            int number = responseMap.get("number").asInt();
            int size = responseMap.get("size").asInt();
            long totalElements = responseMap.get("totalElements").asLong();

            return new PageImpl<>(content, PageRequest.of(number, size), totalElements);
        } catch (RuntimeException | JsonProcessingException ex) {
            log.error("Error occurred fetching the timesheet details for the employeeIds - {}", employeeIds, ex);
            throw new LnFException("Fetching timesheet details for employeeIds failed due to exception",ex);
        }
    }

}
