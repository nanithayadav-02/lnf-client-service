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

import com.lnf.exception.LnFEntityNotFoundException;
import com.lnf.service.timesheet.TimesheetService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

@Service
@Transactional
@Slf4j
public class TimesheetClientImpl extends BaseWebClientService implements TimesheetService {

    private final WebClient webClient;
    private static final String TIMESHEET_ERROR_MSG = "Error occurred while fetching the timesheet";
    private static final String TIMESHEET_WARN_MSG = "Failed to get the timesheet [{}]";

    @Autowired
    public TimesheetClientImpl(@Qualifier("timesheetServiceWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public List<Map<String, Object>> findAllTimesheet(String employeeId, Integer month, Integer year, String status) {
        return Collections.emptyList();
    }

    @Override
    public List<Map<String, Object>> findTimeSheetsByEmployeeIds(List<String> employeeIds, List<UUID> projectIds,
                                                                 Optional<Integer> month, Optional<Integer> year,
                                                                 String status) {
        List<Map<String, Object>> weeklyTimesheet = new ArrayList<>();

        try {
            // Build the WebClient request spec
            WebClient.RequestHeadersSpec<?> spec = webClient.post()
                    .uri(uriBuilder -> uriBuilder.path("/lnf/timesheet/project-employees")
                            .queryParam("projectIds", projectIds)
                            .queryParam("month", month.orElse(null))
                            .queryParam("year", year.orElse(null))
                            .queryParam("status", status)
                            .build())
                    .body(BodyInserters.fromValue(employeeIds))
                    .accept(MediaType.APPLICATION_JSON);
            // Conditionally add the JWT token to the request headers
            addJwtToken(spec);
            return spec.retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {
                    })
                    .block();
        } catch (LnFEntityNotFoundException ex) {
            log.error(TIMESHEET_ERROR_MSG, ex);
            log.warn(TIMESHEET_WARN_MSG, getErrorMessage(ex));
        } catch (Exception ex) {
            log.error(TIMESHEET_ERROR_MSG, ex);
        }
        return weeklyTimesheet;
    }

    private String getErrorMessage(Exception ex) {
        return ex.getMessage() != null ? ex.getMessage() : "No detailed message available";
    }

}
