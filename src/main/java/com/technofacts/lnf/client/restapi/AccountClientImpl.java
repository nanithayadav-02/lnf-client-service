package com.technofacts.lnf.client.restapi;

import com.technofacts.lnf.dto.account.ExpenseDto;
import com.technofacts.lnf.dto.account.InvoiceDto;
import com.technofacts.lnf.service.account.AccountService;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

@Service
@Log
public class AccountClientImpl extends BaseWebClientService implements AccountService {

    private final WebClient webClient;

    public AccountClientImpl(@Qualifier("accountService") WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public List<InvoiceDto> searchForInvoice(String search) {
        List<InvoiceDto> invoiceDtos = new ArrayList<>();
        try {
            WebClient.RequestHeadersSpec<?> spec = webClient.get()
                    .uri("/lnf/invoices?search={search}", search)
                    .accept(MediaType.APPLICATION_JSON);
            // Conditionally add the JWT token to the request headers
            addJwtToken(spec);
            invoiceDtos = spec.retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<InvoiceDto>>() {})
                    .block();

        } catch (RuntimeException ex) {
            log.log(Level.SEVERE, "Error occurred fetching invoices with id : " + search, ex);
        }

        int responseSize = invoiceDtos != null ? invoiceDtos.size() : 0;
        log.info(String.format("Queried for [%d] client, Received [%d] invoice details, " +
                "Unable to fetch [%d] invoice details", invoiceDtos.size(), responseSize, invoiceDtos.size() - responseSize));

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
            log.log(Level.SEVERE, "Error occurred fetching the invoice details for the client", ex);
        }

        int responseSize = invoiceDtos != null ? invoiceDtos.size() : 0;
        log.info(String.format("Queried for [%d] client, Received [%d] invoice details, " +
                "Unable to fetch [%d] invoice details", invoiceDtos.size(), responseSize, invoiceDtos.size() - responseSize));

        return invoiceDtos;
    }

    @Override
    public List<ExpenseDto> searchForExpense(String search) {
        List<ExpenseDto> expenseDtos = new ArrayList<>();
        try {
            WebClient.RequestHeadersSpec<?> spec = webClient.get()
                    .uri("/lnf/account/expenses?search={search}", search)
                    .accept(MediaType.APPLICATION_JSON);
            // Conditionally add the JWT token to the request headers
            addJwtToken(spec);
            expenseDtos = spec.retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<ExpenseDto>>() {})
                    .block();

        } catch (RuntimeException ex) {
            log.log(Level.SEVERE, "Error occurred fetching invoices with projectId : " + search, ex);
        }

        int responseSize = expenseDtos != null ? expenseDtos.size() : 0;
        log.info(String.format("Queried for [%d] client, Received [%d] expense details, " +
                "Unable to fetch [%d] expense details", expenseDtos.size(), responseSize, expenseDtos.size() - responseSize));

        return expenseDtos;
    }

    @Override
    public List<ExpenseDto> findExpensesByDateRange(LocalDate startDate, LocalDate endDate) {
        List<ExpenseDto> expenseDtos = new ArrayList<>();
        try {
            WebClient.RequestHeadersSpec<?> spec = webClient.get()
                    .uri("/lnf/account/expenses/dateRange?startDate={startDate}&endDate={endDate}", startDate, endDate)
                    .accept(MediaType.APPLICATION_JSON);
            // Conditionally add the JWT token to the request headers
            addJwtToken(spec);
            expenseDtos = spec.retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<ExpenseDto>>() {})
                    .block();

        } catch (RuntimeException ex) {
            log.log(Level.SEVERE, "Error occurred fetching the expense details for the client", ex);
        }

        int responseSize = expenseDtos != null ? expenseDtos.size() : 0;
        log.info(String.format("Queried for [%d] client, Received [%d] expense details, " +
                "Unable to fetch [%d] expense details", expenseDtos.size(), responseSize, expenseDtos.size() - responseSize));

        return expenseDtos;
    }
}
