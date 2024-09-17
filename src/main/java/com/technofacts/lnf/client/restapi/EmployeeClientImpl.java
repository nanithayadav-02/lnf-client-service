package com.technofacts.lnf.client.restapi;

import com.technofacts.lnf.dto.employee.EmployeeDto;
import com.technofacts.lnf.exception.LnFEntityNotFoundException;
import com.technofacts.lnf.exception.LnFException;
import com.technofacts.lnf.service.employee.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class EmployeeClientImpl extends BaseWebClientService implements EmployeeService {

    private final WebClient webClient;

    public EmployeeClientImpl(@Qualifier("employeeWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public EmployeeDto findOne(String employeeId) {
        try {
            // Create the web request, adding JWT token if available
            WebClient.RequestHeadersSpec<?> spec = webClient.get()
                    .uri("/lnf/employees/" + employeeId)
                    .accept(MediaType.APPLICATION_JSON);
            // Conditionally add the JWT token to the request headers
            addJwtToken(spec);
            return spec.retrieve()
                    .bodyToMono(EmployeeDto.class)
                    .block();
        } catch (LnFEntityNotFoundException ex) {
            log.error("Employee with id {} does not exist", employeeId);
            throw new LnFException("Fetching of employee failed due to exception : ", ex);
        } catch (RuntimeException ex) {
            log.error("Error occurred fetching the details of the employee {} with message : {}", employeeId, ex.getMessage());
            throw new LnFException("Employee finding failed due to exception ", ex);
        }
    }

    /**
     * Returns ArrayList of EmployeeDtos. Incase of any exception, the error is logged and an empty
     * array list is returned
     *
     * @param employeeIds List<String></String>
     * @return List of EmployeeDtos
     */
    @Override
    public List<EmployeeDto> findByEmployeeIds(List<String> employeeIds) {
        List<EmployeeDto> employeeDtos = new ArrayList<>();
        try {
            // POST the request
            WebClient.RequestHeadersSpec<?> spec = webClient.post()
                    .uri("/lnf/employeeList")
                    .body(BodyInserters.fromPublisher(Mono.just(employeeIds), new ParameterizedTypeReference<List<String>>() {
                    }))
                    .accept(MediaType.APPLICATION_JSON);
            // Conditionally add the JWT token to the request headers
            addJwtToken(spec);
            employeeDtos = spec.retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<EmployeeDto>>() {
                    })
                    .block();

        } catch (RuntimeException ex) {
            log.error("Error occurred fetching the employee details for the employee Ids - {}", employeeIds);
            throw new LnFException("Failed to retrieve the details of employeeIds due to exception : ", ex);
        }

        int responseSize = employeeDtos != null ? employeeDtos.size() : 0;
        log.debug("Queried for {} employees, Received {} employee details, " +
                "Unable to fetch {} employees details", employeeIds.size(), responseSize, employeeIds.size() - responseSize);

        return employeeDtos;
    }

    @Override
    public List<EmployeeDto> findAll(String search) {
        return Collections.emptyList();
    }

    @Override
    public List<String> findByStatuses(List<String> statuses) {
        List<String> employeeIds = new ArrayList<>();
        try {
            WebClient.RequestHeadersSpec<?> spec = webClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/lnf/employeeList/status")
                            .build())
                    .bodyValue(statuses)
                    .accept(MediaType.APPLICATION_JSON);
            addJwtToken(spec);
            employeeIds = spec.retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<String>>() {
                    })
                    .block();
        } catch (LnFEntityNotFoundException ex) {
            log.error("Employee with active status does not exist");
            throw new LnFException("Exception occurred while fetching the employee with active status",ex);
        } catch (RuntimeException ex) {
            log.error("Error occurred fetching the details of the employee with message {}", ex.getMessage());
            throw new LnFException("Employee not found with active status",ex);
        }
        return employeeIds;
    }

    @Override
    public String create(EmployeeDto resource) {
        return null;
    }

    @Override
    public void update(String employeeId, EmployeeDto resource) {
        //To be implemented
    }

    @Override
    public void delete(String employeeId) {
        //To be implemented
    }

    @Override
    public List<EmployeeDto> findDirectReports(String employeeId) {
        return Collections.emptyList();
    }

    @Override
    public Map<String, String> findEmployeeIdsAndFullNamesByStatuses(List<String> statuses) {
        return Collections.emptyMap();
    }

}
