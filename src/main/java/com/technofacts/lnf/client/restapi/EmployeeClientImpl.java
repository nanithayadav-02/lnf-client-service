package com.technofacts.lnf.client.restapi;

import java.util.List;

import com.technofacts.lnf.dto.employee.EmployeeDto;
import com.technofacts.lnf.service.employee.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class EmployeeClientImpl implements EmployeeService {

    private final WebClient webClient;

    @Override
    public EmployeeDto findOne(String employeeId) {

        return null;
    }

    @Override
    public List<EmployeeDto> findByEmployeeIds(List<String> employeeIds) {
        return null;
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
}
