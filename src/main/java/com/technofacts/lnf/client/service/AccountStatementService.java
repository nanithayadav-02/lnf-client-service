package com.technofacts.lnf.client.service;

import com.technofacts.lnf.dto.account.InvoiceDto;
import com.technofacts.lnf.dto.account.InvoiceItemDto;
import com.technofacts.lnf.dto.client.AccountStatementDto;
import com.technofacts.lnf.dto.client.ProjectDto;
import com.technofacts.lnf.service.account.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@Transactional
@RequiredArgsConstructor
@Log
public class AccountStatementService {

    private final AccountService accountService;
    private final ProjectService projectService;

    public List<AccountStatementDto> findByClientId(UUID clientId) {
        List<ProjectDto> projects = projectService.findProjectsByClientId(clientId);
        List<InvoiceDto> invoices = projects.stream()
                .map(ProjectDto::getId)
                .flatMap(projectId -> retrieveInvoiceDetails(projectId).stream())
                .toList();
        return createAccountStatements(null, invoices);
    }


    public List<AccountStatementDto> findStatementForDesiredMonths(UUID clientId, int months) {
        LocalDate startDate = LocalDate.now().minusMonths(months);
        LocalDate endDate = LocalDate.now();
        return findByClientIdAndDateRange(clientId, startDate, endDate);
    }

    public List<AccountStatementDto> findByClientIdAndDateRange(UUID clientId, LocalDate startDate, LocalDate endDate) {
        List<InvoiceDto> dtoList = retrieveInvoiceDetails(startDate, endDate);
        return createAccountStatements(clientId, dtoList);
    }

    public List<AccountStatementDto> findByDateRange(LocalDate startDate, LocalDate endDate) {
        List<InvoiceDto> dtoList = retrieveInvoiceDetails(startDate, endDate);
        return createAccountStatements(null, dtoList);
    }

    private List<AccountStatementDto> createAccountStatements(UUID clientId, List<InvoiceDto> dtoList) {
        Stream<InvoiceDto> filteredStream = dtoList.stream();
        if (clientId != null) {
            filteredStream = filteredStream.filter(invoiceDto -> invoiceDto.getClientId().equals(clientId));
        }
        return filteredStream
                .filter(invoiceDto -> invoiceDto.getProjectId() != null)
                .map(this::createAccountStatement)
                .toList();
    }

    private AccountStatementDto createAccountStatement(InvoiceDto invoiceDto) {
        ProjectDto projectDto = retrieveProjectDetails(invoiceDto.getProjectId());

        AccountStatementDto statementDto = new AccountStatementDto();
        statementDto.setInvoiceReference(invoiceDto.getReference());
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

    private List<InvoiceDto> retrieveInvoiceDetails(UUID projectId) {
        try {
            return accountService.search(projectId);
        } catch (Exception e) {
            log.info("An error occurred while retrieving invoice details with projectId : " + projectId);
            return Collections.emptyList();
        }
    }

    private List<InvoiceDto> retrieveInvoiceDetails(LocalDate startDate, LocalDate endDate) {
        try {
            return accountService.findInvoicesByDateRange(startDate, endDate);
        } catch (Exception e) {
            log.info("An error occurred while retrieving invoice details : " + e.getMessage());
            return Collections.emptyList();
        }
    }
}


