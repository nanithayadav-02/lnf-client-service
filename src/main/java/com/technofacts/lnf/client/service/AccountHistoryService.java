package com.technofacts.lnf.client.service;

import com.technofacts.lnf.dto.account.InvoiceDto;
import com.technofacts.lnf.dto.client.AccountHistoryDto;
import com.technofacts.lnf.dto.client.ProjectDto;
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
public class AccountHistoryService {

    private final ClientInvoiceService clientInvoiceService;
    private final ProjectService projectService;

    public List<AccountHistoryDto> getAccountHistoryByClientId(UUID clientId, LocalDate startDate, LocalDate endDate) {
        List<InvoiceDto> invoiceDtos = (Objects.isNull(startDate) && Objects.isNull(endDate)) ?
                clientInvoiceService.getAllInvoicesByClientId(clientId) : clientInvoiceService.getInvoicesByDateRange(clientId,startDate, endDate);
        List<ProjectDto> projectsByClientId = projectService.findProjectsByClientId(clientId);
        return mapInvoicesToClientInvoiceDtos(invoiceDtos,projectsByClientId);
    }

    public List<AccountHistoryDto> mapInvoicesToClientInvoiceDtos(List<InvoiceDto> invoices, List<ProjectDto> projects) {
        Map<UUID, ProjectDto> projectMap = projects.stream()
                .collect(Collectors.toMap(ProjectDto::getId, Function.identity()));
        return invoices.stream()
                .map(invoice -> toClientInvoiceDto(invoice, projectMap))
                .toList();
    }
    private AccountHistoryDto toClientInvoiceDto(InvoiceDto invoice, Map<UUID, ProjectDto> projectMap) {
        ProjectDto project = projectMap.get(invoice.getProjectId());
        return AccountHistoryDto.builder()
                .invoiceReference(invoice.getReference())
                .projectName(project != null ? project.getName() : null)
                .invoiceDate(invoice.getInvoiceDate())
                .description(invoice.getDescription())
                .paidAmount(invoice.getAmount().getTotal().subtract(invoice.getAmount().getBalanceAmount()))
                .dueAmount(invoice.getAmount().getBalanceAmount())
                .build();
    }
}
