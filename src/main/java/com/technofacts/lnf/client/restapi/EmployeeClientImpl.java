package com.technofacts.lnf.client.restapi;

import com.technofacts.lnf.dto.employee.EmployeeDto;
import com.technofacts.lnf.exception.LnFEntityNotFoundException;
import com.technofacts.lnf.service.employee.EmployeeService;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
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
public class EmployeeClientImpl implements EmployeeService {

    private final WebClient webClient;

    public EmployeeClientImpl(@Qualifier("employeeService") WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public EmployeeDto findOne(String employeeId) {
        Jwt jwt = getJwtToken();
        try {
            return webClient.get()
                    .uri("/lnf/employees/" + employeeId)
                    .headers(header -> header.setBearerAuth(jwt.getTokenValue()))
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(EmployeeDto.class)
                    .block();
        } catch (LnFEntityNotFoundException ex) {
            log.warning(String.format("Employee with id [%s] does not exist", employeeId));
        }
        catch (RuntimeException ex) {
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
        Jwt jwt = getJwtToken();
        try {
            // POST the request
             employeeDtos = webClient.post()
                     .uri("/lnf/employeeList")
                     .headers(header -> header.setBearerAuth(jwt.getTokenValue()))
                     .body(BodyInserters.fromPublisher(Mono.just(employeeIds), new ParameterizedTypeReference<List<String>>() {}))
                     .accept(MediaType.APPLICATION_JSON)
                     .retrieve()
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
    public List<String> findByStatuses(List<String> statuses) {
        return Collections.emptyList();
    }

    @Override
    public String create(EmployeeDto resource) {
        return null;
    }

    @Override
    public void update(String employeeId, EmployeeDto resource) {
    }

    @Override
    public void delete(String employeeId) {
    }

    @Override
    public List<EmployeeDto> findDirectReports(String employeeId) {
        return null;
    }

    private Jwt getJwtToken() {
        return (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

}
