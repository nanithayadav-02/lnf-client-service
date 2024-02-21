package com.technofacts.lnf.client.restapi;

import com.technofacts.lnf.dto.account.InvoiceDto;
import com.technofacts.lnf.service.account.InvoiceService;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

@Service
@Log
public class InvoiceServiceImpl extends BaseWebClientService implements InvoiceService {

    private final WebClient webClient;

    public InvoiceServiceImpl(@Qualifier("invoiceService") WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public List<InvoiceDto> search(UUID projectId) {
        return Collections.emptyList();
    }

    @Override
    public List<InvoiceDto> findAll(String searchCriteria) {
        List<InvoiceDto> invoiceDtos = new ArrayList<>();
        try {
            // Construct the URI with dynamic query parameters
            URI uri = UriComponentsBuilder.fromUriString("/lnf/invoices")
                    .queryParam("search", searchCriteria)
                    .build().toUri();

            WebClient.RequestHeadersSpec<?> spec = webClient.get()
                    .uri(uri)
                    .accept(MediaType.APPLICATION_JSON);

            // Conditionally add the JWT token to the request headers
            addJwtToken(spec);

            invoiceDtos = spec.retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<InvoiceDto>>() {})
                    .block();
        } catch (RuntimeException ex) {
            log.log(Level.SEVERE, "Error occurred fetching invoices with search criteria: " + searchCriteria, ex);
        }

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

}
