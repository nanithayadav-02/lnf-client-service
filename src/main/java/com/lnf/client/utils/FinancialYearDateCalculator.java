/*
 * Copyright (c)  Lever And Fulcrum (LNF)
 */
package com.lnf.client.utils;

import com.lnf.dto.common.DateRangeDto;
import com.lnf.exception.LnFException;

import java.time.LocalDate;
import java.time.Month;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FinancialYearDateCalculator {
    private static final Month FINANCIAL_YEAR_START_MONTH = Month.APRIL;

    public static DateRangeDto getDateRanges(Object result) {
        if (!(result instanceof Object[] row)) {
            return new DateRangeDto(null, null, null);
        }
        LocalDate startDate = Optional.ofNullable(row[0])
                .map(date -> (LocalDate) date)
                .orElse(null);
        LocalDate endDate = Optional.ofNullable(row[1])
                .map(date -> (LocalDate) date)
                .orElse(null);
        return new DateRangeDto(startDate, endDate, determineFinancialYears(startDate, endDate));
    }

    private static String determineFinancialYears(LocalDate startDate, LocalDate endDate) {
        validateDateRange(startDate, endDate);
        String firstFY = getFinancialYear(startDate);
        String lastFY = getFinancialYear(endDate);
        if (firstFY.equals(lastFY)) {
            return firstFY;
        }
        int startYear = Integer.parseInt(firstFY.split("-")[0]);
        int endYear = Integer.parseInt(lastFY.split("-")[0]);
        return IntStream.rangeClosed(startYear, endYear)
                .mapToObj(year -> String.format("%d-%02d", year, (year + 1) % 100))
                .collect(Collectors.joining(", "));
    }

    private static String getFinancialYear(LocalDate date) {
        int year = date.getYear();
        int startYear = date.getMonthValue() < FINANCIAL_YEAR_START_MONTH.getValue() ? year - 1 : year;
        return String.format("%d-%02d",
                startYear,
                (startYear + 1) % 100
        );
    }

    private static void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new LnFException("Start date and end date must not be null");
        }
        if (startDate.isAfter(endDate)) {
            throw new LnFException("Start date must be before or equal to end date");
        }
    }

}
