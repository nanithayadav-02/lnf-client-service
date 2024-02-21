package com.technofacts.lnf.client.service;

import com.technofacts.lnf.client.restapi.AccountClientImpl;
import com.technofacts.lnf.dto.account.InvoiceDto;
import com.technofacts.lnf.dto.client.ClientInvoiceDto;
import com.technofacts.lnf.dto.client.ProjectDto;
import com.technofacts.lnf.exception.LnFBadRequestException;
import com.technofacts.lnf.exception.LnFEntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Log
public class ClientInvoiceService {
    private final AccountClientImpl accountClient;

    private final ProjectService projectService;

    public List<ClientInvoiceDto> getInvoicesByClientId(UUID clientId, LocalDate startDate, LocalDate endDate) {
        List<InvoiceDto> invoiceDtos = retrieveClientInvoices(clientId, startDate, endDate);
        List<ProjectDto> projectsByClientId = projectService.findProjectsByClientId(clientId);
        return getClientInvoiceDtos(invoiceDtos, projectsByClientId);
    }

    private static List<ClientInvoiceDto> getClientInvoiceDtos(List<InvoiceDto> invoiceDtos, List<ProjectDto> projectsByClientId) {
        return invoiceDtos.stream().map(e -> {
            ClientInvoiceDto clientInvoiceDto = ClientInvoiceDto.builder()
                    .reference(e.getReference())
                    .purchaseOrderReference(e.getPurchaseOrderReference())
                    .status(e.getStatus()).build();
            if (!CollectionUtils.isEmpty(projectsByClientId)) {
                projectsByClientId.stream()
                        .filter(pro -> pro.getId().equals(e.getProjectId()))
                        .findFirst().ifPresent(p -> {
                            clientInvoiceDto.setProjectName(p.getName());
                            clientInvoiceDto.setProjectType(p.getType());
                        });
            }
            return clientInvoiceDto;
        }).toList();
    }

    private List<InvoiceDto> retrieveClientInvoices(UUID clientId , LocalDate startDate , LocalDate endDate) {
        List<InvoiceDto> invoiceDtos;
        if (Objects.isNull(startDate) && Objects.isNull(endDate)) {
            invoiceDtos = accountClient.findAll("clientId:%s".formatted(clientId));
        } else {
            if (Objects.isNull(startDate)) {
                throw new LnFBadRequestException("Start date is required");
            }
            if (Objects.nonNull(endDate) && endDate.isBefore(startDate)) {
                throw new LnFBadRequestException("End date should not be before start date");
            }
            endDate = Objects.isNull(endDate) ? LocalDate.now() : endDate;
            List<InvoiceDto> invoicesByDateRange = accountClient.findInvoicesByDateRange(startDate , endDate);
            if (CollectionUtils.isEmpty(invoicesByDateRange)) {
                throw new LnFEntityNotFoundException("Invoice details not found for clientId : " + clientId);
            }
            invoiceDtos = invoicesByDateRange.stream().filter(e -> e.getClientId().equals(clientId)).toList();
        }
        if (CollectionUtils.isEmpty(invoiceDtos)) {
            throw new LnFEntityNotFoundException("Invoice details not found for clientId : " + clientId);
        }
        return invoiceDtos;
    }
}
