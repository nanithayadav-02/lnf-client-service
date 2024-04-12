package com.technofacts.lnf.client.restapi;

import com.technofacts.lnf.dto.employee.EmployeeDto;
import com.technofacts.lnf.exception.LnFEntityNotFoundException;
import com.technofacts.lnf.service.employee.EmployeeService;
import lombok.extern.java.Log;
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
import java.util.logging.Level;

@Service
@Log
public class EmployeeClientImpl extends BaseWebClientService  implements EmployeeService {

    private final WebClient webClient;

    public EmployeeClientImpl(@Qualifier("employeeService") WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public EmployeeDto findOne(String employeeId) {
        try {
            // Create the web request, adding JWT token if available
            WebClient.RequestHeadersSpec<?> spec =  webClient.get()
                    .uri("/lnf/employees/" + employeeId)
                    .accept(MediaType.APPLICATION_JSON);
            // Conditionally add the JWT token to the request headers
            addJwtToken(spec);
            return spec.retrieve()
                    .bodyToMono(EmployeeDto.class)
                    .block();
        } catch (LnFEntityNotFoundException ex) {
            log.warning(String.format("Employee with id [%s] does not exist", employeeId));
        } catch (RuntimeException ex) {
            log.log(Level.SEVERE, String.format("Error occurred fetching the details of the employee [%s]", employeeId), ex);
        }
        return null;
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
                     .bodyToMono(new ParameterizedTypeReference<List<EmployeeDto>>() {})
                     .block();

        } catch (RuntimeException ex) {
            log.log(Level.SEVERE, String.format("Error occurred fetching the employee details for the employee Ids - [%s]", employeeIds), ex);
        }

        int responseSize = employeeDtos != null ? employeeDtos.size() : 0;
        log.info(String.format("Queried for [%d] employees, Received [%d] employee details, " +
                "Unable to fetch [%d] employees details", employeeIds.size(), responseSize, employeeIds.size() - responseSize));

        return employeeDtos;
    }

    @Override
    public List<EmployeeDto> findAll(String search) {
        List<EmployeeDto> employeeDtos = new ArrayList<>();
        try {
            WebClient.RequestHeadersSpec<?> spec = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/lnf/employees")
                            .queryParam("search", "status:" + search)
                            .build())
                    .accept(MediaType.APPLICATION_JSON);
            addJwtToken(spec);
            employeeDtos = spec.retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<EmployeeDto>>() {})
                    .block();
        } catch (LnFEntityNotFoundException ex) {
            log.warning(String.format("Employee with active status does not exist"));
        } catch (RuntimeException ex) {
            log.log(Level.SEVERE, String.format("Error occurred fetching the details of the employee", ex));
        }
        return employeeDtos;
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
                    .bodyToMono(new ParameterizedTypeReference<List<String>>() {})
                    .block();
        } catch (LnFEntityNotFoundException ex) {
            log.warning(String.format("Employee with active status does not exist"));
        } catch (RuntimeException ex) {
            log.log(Level.SEVERE, String.format("Error occurred fetching the details of the employee", ex));
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

}
