package com.technofacts.lnf.client.service;

import com.technofacts.lnf.client.restapi.InvoiceServiceImpl;
import com.technofacts.lnf.dto.account.InvoiceDto;
import com.technofacts.lnf.dto.client.ClientInvoiceDto;
import com.technofacts.lnf.dto.client.ProjectDto;
import com.technofacts.lnf.exception.LnFBadRequestException;
import com.technofacts.lnf.exception.LnFEntityNotFoundException;
import com.technofacts.lnf.service.account.InvoiceService;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
@Log
public class ClientInvoiceService {

    private final InvoiceService invoiceService;
    private final ProjectService projectService;

    public ClientInvoiceService(InvoiceServiceImpl invoiceService, ProjectService projectService) {
        this.invoiceService = invoiceService;
        this.projectService = projectService;
    }

    public List<ClientInvoiceDto> getInvoicesByClientId(UUID clientId, LocalDate startDate, LocalDate endDate) {
        validateClientInvoiceParameters(startDate, endDate);
        LocalDate effectiveEndDate = Optional.ofNullable(endDate).orElse(LocalDate.now());

        List<InvoiceDto> invoiceDtos = invoiceService.findAll(String.format("clientId:[%s],startDate:[%s],endDate:[%s]",
                clientId, startDate, effectiveEndDate));
        if (CollectionUtils.isEmpty(invoiceDtos)) {
            throw new LnFEntityNotFoundException("Invoice details not found for clientId: " + clientId);
        }

        List<ProjectDto> projectsByClientId = projectService.findProjectsByClientId(clientId);
        return mapInvoicesToClientInvoiceDtos(invoiceDtos, projectsByClientId);
    }

    private List<ClientInvoiceDto> mapInvoicesToClientInvoiceDtos(List<InvoiceDto> invoices, List<ProjectDto> projects) {
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

    private void validateClientInvoiceParameters(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) {
            throw new LnFBadRequestException("Start date is required.");
        }
        if (endDate != null && endDate.isBefore(startDate)) {
            throw new LnFBadRequestException("End date should not be before start date.");
        }
    }

}
