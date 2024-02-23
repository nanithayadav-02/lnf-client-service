package com.technofacts.lnf.client.service;

import com.technofacts.lnf.dto.account.ExpenseDto;
import com.technofacts.lnf.dto.client.ClientExpenseDto;
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

@Service
@Transactional
@RequiredArgsConstructor
@Log
public class ClientExpenseService {

    private final AccountService accountService;
    private final ProjectService projectService;

    public List<ClientExpenseDto> findByClientId(UUID clientId) {
        List<ExpenseDto> expenses = retrieveExpensesByClientId(clientId);
        return expenses.stream()
                .map(this::createAccountStatement)
                .toList();
    }

    public List<ClientExpenseDto> findForDesiredMonths(UUID clientId, int months) {
        LocalDate startDate = LocalDate.now().minusMonths(months);
        LocalDate endDate = LocalDate.now();
        List<ExpenseDto> dtoList = retrieveExpenseDetails(startDate, endDate);
        return filterAndMapExpenses(clientId, dtoList);
    }

    public List<ClientExpenseDto> findByClientIdAndDateRange(UUID clientId, LocalDate startDate, LocalDate endDate) {
        List<ExpenseDto> dtoList = retrieveExpenseDetails(startDate, endDate);
        return filterAndMapExpenses(clientId, dtoList);
    }

    public List<ClientExpenseDto> findByDateRange(LocalDate startDate, LocalDate endDate) {
        List<ExpenseDto> dtoList = retrieveExpenseDetails(startDate, endDate);
        return dtoList.stream()
                .filter(expenseDto -> expenseDto.getProjectId() != null)
                .map(this::createAccountStatement)
                .toList();
    }

    private List<ClientExpenseDto> filterAndMapExpenses(UUID clientId, List<ExpenseDto> dtoList) {
        return dtoList.stream()
                .filter(expenseDto -> {
                    UUID projectId = expenseDto.getProjectId();
                    return projectId != null && projectService.findByProjectId(projectId).getClientId().equals(clientId);
                })
                .map(this::createAccountStatement)
                .toList();
    }

    private List<ExpenseDto> retrieveExpensesByClientId(UUID clientId) {
        List<ProjectDto> projects = projectService.findProjectsByClientId(clientId);
        return projects.stream()
                .map(ProjectDto::getId)
                .flatMap(projectId -> retrieveExpenseDetails(projectId).stream())
                .toList();
    }

    private ClientExpenseDto createAccountStatement(ExpenseDto expenseDto) {
        ProjectDto projectDto = retrieveProjectDetails(expenseDto.getProjectId());

        return new ClientExpenseDto(
                expenseDto.getReference(),
                projectDto.getId(),
                projectDto.getName(),
                projectDto.getType(),
                projectDto.getPurchaseOrder(),
                projectDto.getBillingTerm(),
                expenseDto.getStatus(),
                projectDto.getStatus()
       );
    }

    private ProjectDto retrieveProjectDetails(UUID projectId) {
        return projectService.findByProjectId(projectId);
    }

    private List<ExpenseDto> retrieveExpenseDetails(UUID projectId) {
        try {
            return accountService.searchForExpense("projectId:%s".formatted(projectId));
        } catch (Exception e) {
            log.info("An error occurred while retrieving expense details with projectId : " + projectId);
            return Collections.emptyList();
        }
    }

    private List<ExpenseDto> retrieveExpenseDetails(LocalDate startDate, LocalDate endDate) {
        try {
            return accountService.findExpensesByDateRange(startDate, endDate);
        } catch (Exception e) {
            log.info("An error occurred while retrieving expense details : " + e.getMessage());
            return Collections.emptyList();
        }
    }
}
