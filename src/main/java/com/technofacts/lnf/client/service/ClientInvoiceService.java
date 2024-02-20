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
       List<InvoiceDto> invoiceDtos = (Objects.isNull(startDate) && Objects.isNull(endDate)) ?
               getAllInvoicesByClientId(clientId) : getInvoicesByDateRange(startDate, endDate);

       List<ProjectDto> projectsByClientId = projectService.findProjectsByClientId(clientId);

       return mapToClientInvoiceDtos(invoiceDtos, projectsByClientId, clientId);
   }

    private List<InvoiceDto> getAllInvoicesByClientId(UUID clientId) {
        return accountClient.findAll("clientId:%s".formatted(clientId));
    }

    private List<InvoiceDto> getInvoicesByDateRange(LocalDate startDate, LocalDate endDate) {
        if (Objects.isNull(startDate)) {
            throw new LnFBadRequestException("Start date is required");
        }
        if (endDate != null && endDate.isBefore(startDate)) {
            throw new LnFBadRequestException("End date should not be before start date");
        }
        endDate = Objects.isNull(endDate) ? LocalDate.now() : endDate;
        return accountClient.findInvoicesByDateRange(startDate, endDate);
    }

    private List<ClientInvoiceDto> mapToClientInvoiceDtos(List<InvoiceDto> invoiceDtos, List<ProjectDto> projectsByClientId, UUID clientId) {
        if (CollectionUtils.isEmpty(invoiceDtos)) {
            throw new LnFEntityNotFoundException("Invoice details not found for clientId: " + clientId);
        }

        return invoiceDtos.stream()
                .map(invoiceDto -> mapToClientInvoiceDto(invoiceDto, projectsByClientId))
                .toList();
    }

    private ClientInvoiceDto mapToClientInvoiceDto(InvoiceDto invoiceDto, List<ProjectDto> projectsByClientId) {
        ClientInvoiceDto clientInvoiceDto = ClientInvoiceDto.builder()
                .reference(invoiceDto.getReference())
                .purchaseOrderReference(invoiceDto.getPurchaseOrderReference())
                .status(invoiceDto.getStatus())
                .build();

        projectsByClientId.stream()
                .filter(projectDto -> projectDto.getId().equals(invoiceDto.getProjectId()))
                .findFirst()
                .ifPresent(projectDto -> {
                    clientInvoiceDto.setProjectName(projectDto.getName());
                    clientInvoiceDto.setProjectType(projectDto.getType());
                });

        return clientInvoiceDto;
    }
}
