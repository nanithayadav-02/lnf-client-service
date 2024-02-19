package com.technofacts.lnf.client.service;

import com.technofacts.lnf.dto.account.InvoiceDto;
import com.technofacts.lnf.dto.account.InvoiceItemDto;
import com.technofacts.lnf.dto.client.AccountStatementDto;
import com.technofacts.lnf.dto.client.ProjectDto;
import com.technofacts.lnf.exception.LnFEntityNotFoundException;
import com.technofacts.lnf.service.account.InvoiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Log
public class AccountStatementService {

    private final InvoiceService invoiceService;
    private final ProjectService projectService;

    public List<AccountStatementDto> findAccountStatement(LocalDate startDate, LocalDate endDate) {
        List<InvoiceDto> dtoList = retrieveInvoiceDetails(startDate, endDate);
        return dtoList.stream()
                .map(invoiceDto -> createAccountStatement(invoiceDto.getProjectId()))
                .toList();
    }

    private AccountStatementDto createAccountStatement(UUID projectId) {

        ProjectDto projectDto = retrieveProjectDetails(projectId);
        InvoiceDto invoiceDto = retrieveInvoiceDetails(projectId);

        AccountStatementDto statementDto = new AccountStatementDto();
        statementDto.setProjectId(projectDto.getId());
        statementDto.setProjectName(projectDto.getName());
        statementDto.setProjectType(projectDto.getType());
        statementDto.setDescription(projectDto.getDescription());
        statementDto.setClientId(projectDto.getClientId());
        statementDto.setClientName(projectDto.getClientName());

        long workingDays = invoiceDto.getItems().stream()
                .mapToLong(InvoiceItemDto::getWorkingDays)
                .sum();
        statementDto.setHours(workingDays * projectDto.getHoursPerDay());
        statementDto.setSubTotal(invoiceDto.getAmount().getSubTotal());
        statementDto.setTaxAmount(invoiceDto.getAmount().getTaxAmount());
        statementDto.setTotal(invoiceDto.getAmount().getTotal());

        return statementDto;
    }

    private ProjectDto retrieveProjectDetails(UUID projectId) {
        return projectService.findByProjectId(projectId);
    }

    private InvoiceDto retrieveInvoiceDetails(UUID projectId) {
        return invoiceService.search(projectId).stream().findFirst()
                .orElseThrow(() -> new LnFEntityNotFoundException("Error occurred while retrieving invoice details: " + projectId));
    }

    private List<InvoiceDto> retrieveInvoiceDetails(LocalDate startDate, LocalDate endDate) {
        try {
            return invoiceService.findInvoicesByDateRange(startDate, endDate);
        } catch (Exception e) {
            log.info("An error occurred while retrieving invoice details: " + e.getMessage());
            return Collections.emptyList();
        }
    }
}
