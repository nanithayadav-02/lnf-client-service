package com.technofacts.lnf.dto.client;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.*;

@ToString
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDto {

    private UUID id;

    @NotNull(message = "code cannot be null")
    @NotBlank(message = "code cannot be blank")
    private String code;

    @NotNull(message = "name cannot be null")
    @NotBlank(message = "name cannot be blank")
    private String name;

    @NotNull(message = "type cannot be null")
    @NotBlank(message = "type cannot be blank")
    private String type;

    private String description;

    @NotNull(message = "purchaseOrder cannot be null")
    @NotBlank(message = "purchaseOrder cannot be blank")
    private String purchaseOrder;

    @NotNull(message = "budgetTerms cannot be null")
    @NotBlank(message = "budgetTerms cannot be blank")
    private String budgetTerms;

    @NotNull(message = "status cannot be null")
    @NotBlank(message = "status cannot be blank")
    private String status;

    @NotNull(message = "currency cannot be null")
    @NotBlank(message = "currency cannot be blank")
    private String currency;

    private BigDecimal budget;

    private Integer hoursPerDay;

    @NotNull(message = "billingTerm cannot be null")
    @NotBlank(message = "billingTerm cannot be blank")
    private String billingTerm;

    @NotNull(message = "startDate cannot be null")
    @NotBlank(message = "startDate cannot be blank")
    private LocalDate startDate;

    private LocalDate endDate;

    private List<TaskDto> tasks = new ArrayList<>();
}

