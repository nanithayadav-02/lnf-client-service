package com.technofacts.lnf.client.service;

import com.technofacts.lnf.dto.account.InvoiceDto;
import com.technofacts.lnf.dto.client.ClientInvoiceDto;
import com.technofacts.lnf.dto.client.ProjectDto;
import com.technofacts.lnf.exception.LnFBadRequestException;
import com.technofacts.lnf.service.account.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Log
public class ClientInvoiceService {

    private final AccountService accountService;
    private final ProjectService projectService;


    public List<ClientInvoiceDto> getInvoicesByClientId(UUID clientId, LocalDate startDate, LocalDate endDate) {
        List<InvoiceDto> invoiceDtos = (Objects.isNull(startDate) && Objects.isNull(endDate)) ?
                getAllInvoicesByClientId(clientId) : getInvoicesByDateRange(clientId,startDate, endDate);
        List<ProjectDto> projectsByClientId = projectService.findProjectsByClientId(clientId);
        return mapInvoicesToClientInvoiceDtos(invoiceDtos, projectsByClientId);
    }

    public List<InvoiceDto> getAllInvoicesByClientId(UUID clientId) {
        return accountService.searchForInvoice("clientId:%s".formatted(clientId));
    }

    public List<InvoiceDto> getInvoicesByDateRange(UUID clientId,LocalDate startDate, LocalDate endDate) {
        if (Objects.isNull(startDate)) {
            throw new LnFBadRequestException("Start date is required");
        }
        if (endDate != null && endDate.isBefore(startDate)) {
            throw new LnFBadRequestException("End date should not be before start date");
        }
        endDate = Objects.isNull(endDate) ? LocalDate.now() : endDate;
        return accountService.findInvoicesByDateRange(startDate, endDate).stream().filter(id -> id.getClientId().equals(clientId)).toList();
    }

    public List<ClientInvoiceDto> mapInvoicesToClientInvoiceDtos(List<InvoiceDto> invoices, List<ProjectDto> projects) {
        Map<UUID, ProjectDto> projectMap = projects.stream()
                .collect(Collectors.toMap(ProjectDto::getId, Function.identity()));
        return invoices.stream()
                .map(invoice -> toClientInvoiceDto(invoice, projectMap))
                .toList();
    }
    private ClientInvoiceDto toClientInvoiceDto(InvoiceDto invoice, Map<UUID, ProjectDto> projectMap) {
        ProjectDto project = projectMap.get(invoice.getProjectId());
        return ClientInvoiceDto.builder()
                .reference(invoice.getReference())
                .invoiceDate(invoice.getInvoiceDate())
                .terms(invoice.getProjectPaymentTerms())
                .purchaseOrderReference(invoice.getPurchaseOrderReference())
                .status(invoice.getStatus())
                .projectName(project != null ? project.getName() : null)
                .projectType(project != null ? project.getType() : null)
                .build();
    }
}
