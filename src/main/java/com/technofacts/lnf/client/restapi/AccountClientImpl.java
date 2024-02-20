package com.technofacts.lnf.client.restapi;

import com.technofacts.lnf.dto.account.InvoiceDto;
import com.technofacts.lnf.exception.LnFEntityNotFoundException;
import com.technofacts.lnf.service.account.InvoiceService;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

@Service
@Log
public class AccountClientImpl extends BaseWebClientService implements InvoiceService {

    private final WebClient webClient;

    public AccountClientImpl(@Qualifier("invoiceService") WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public List<InvoiceDto> search(UUID projectId) {
        List<InvoiceDto> invoiceDtos = new ArrayList<>();
        try {
            WebClient.RequestHeadersSpec<?> spec = webClient.get()
                    .uri("/lnf/invoices?search=projectId:{projectId}", projectId)
                    .accept(MediaType.APPLICATION_JSON);
            // Conditionally add the JWT token to the request headers
            addJwtToken(spec);
            invoiceDtos = spec.retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<InvoiceDto>>() {})
                    .block();

        } catch (RuntimeException ex) {
            log.log(Level.SEVERE, String.format("Error occurred fetching the project details for the client - [%s]", projectId), ex);
        }

        int responseSize = invoiceDtos != null ? invoiceDtos.size() : 0;
        log.info(String.format("Queried for [%d] client, Received [%d] project details, " +
                "Unable to fetch [%d] projects details", invoiceDtos.size(), responseSize, invoiceDtos.size() - responseSize));

        return invoiceDtos;
    }

    @Override
    public List<InvoiceDto> findInvoicesByDateRange(LocalDate startDate, LocalDate endDate) {
        List<InvoiceDto> invoiceDtos = new ArrayList<>();
        try {
            WebClient.RequestHeadersSpec<?> spec = webClient.get()
                    .uri("/lnf/invoices/dateRange?startDate={startDate}&endDate={endDate}", startDate, endDate)
                    .accept(MediaType.APPLICATION_JSON);
            // Conditionally add the JWT token to the request headers
            addJwtToken(spec);
            invoiceDtos = spec.retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<InvoiceDto>>() {})
                    .block();

        } catch (RuntimeException ex) {
            log.log(Level.SEVERE, "Error occurred fetching the project details for the client", ex);
        }

        int responseSize = invoiceDtos != null ? invoiceDtos.size() : 0;
        log.info(String.format("Queried for [%d] client, Received [%d] project details, " +
                "Unable to fetch [%d] projects details", invoiceDtos.size(), responseSize, invoiceDtos.size() - responseSize));

        return invoiceDtos;
    }

    @Override
    public List<InvoiceDto> findAll(String search) {
        List<InvoiceDto> invoiceDTOs = new ArrayList<>();

        try {
            WebClient.RequestHeadersSpec<?> spec =  webClient.get()
                    .uri("/lnf/invoices?search={search}", search)
                    .accept(MediaType.APPLICATION_JSON);

            addJwtToken(spec);

            invoiceDTOs = spec.retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<InvoiceDto>>() {})
                    .block();

        } catch (LnFEntityNotFoundException ex) {
            log.warning("Failed to get the Invoice details");
        } catch (RuntimeException ex) {
            log.log(Level.SEVERE, String.format("Error occurred fetching the Invoice details [%s]", ex.getMessage()));
        }

        return invoiceDTOs;
    }
}
