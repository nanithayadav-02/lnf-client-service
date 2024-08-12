package com.technofacts.lnf.client.restapi;

import com.technofacts.lnf.dto.timesheet.TimesheetDto;
import com.technofacts.lnf.dto.timesheet.WeeklyTimesheetDto;
import com.technofacts.lnf.exception.LnFException;
import com.technofacts.lnf.service.timesheet.TimesheetService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.util.*;

@Service
@Transactional
@Slf4j
public class TimesheetClientImpl extends BaseWebClientService implements TimesheetService {

    private final WebClient webClient;

    @Autowired
    public TimesheetClientImpl(@Qualifier("timesheetService") WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public List<Map<String, Object>> findAllTimesheet(String employeeId, Integer month, Integer year) {
        return null;
    }

    @Override
    public List<TimesheetDto> findTimeSheetsByEmployeeIds(List<String> employeeIds, List<UUID> projectIds) {
        List<TimesheetDto> timesheetDtos = new ArrayList<>();
        try {

            WebClient.RequestHeadersSpec<?> spec = webClient.post()
                    .uri(uriBuilder -> uriBuilder.path("/lnf/timesheet/project-employees")
                            .queryParam("projectId", projectIds)
                            .build())
                    .body(BodyInserters.fromValue(employeeIds))
                    .accept(MediaType.APPLICATION_JSON);

            // Conditionally add the JWT token to the request headers
            addJwtToken(spec);

            // Retrieve the response
            timesheetDtos = spec.retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<TimesheetDto>>() {})
                    .block();
        } catch (RuntimeException ex) {
            log.error("Error occurred fetching the timesheet details for the employeeIds - {}", employeeIds, ex);
            throw new LnFException("Fetching timesheet details for employeeIds failed due to exception",ex);
        }

        return timesheetDtos;
    }

}
