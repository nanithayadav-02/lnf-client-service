/*
 *
 *  * Copyright © 2024 Lever And Fulcrum Solutions (hereinafter referred to as "LNF").
 *  * All rights reserved.
 *  *
 *  * This source code is the proprietary property of LNF
 *  *
 *  * Unauthorized copying, redistribution, or modification of this code,
 *  * via any medium, is strictly prohibited unless expressly authorized
 *  * in writing by LNF.
 *  *
 *  * This code is confidential and intended solely for the use of LNF
 *  * and its authorized personnel.
 *
 */

package com.lnf.client.service;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import com.lnf.dto.client.ClientDto;
import com.lnf.dto.client.ProjectDto;
import com.lnf.dto.client.TaskDto;
import com.lnf.exception.LnFException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.BiConsumer;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class DataExportService {

    private final ClientService clientService;
    private final ProjectService projectService;
    private final TaskService taskService;

    public ResponseEntity<String> uploadFile(MultipartFile file, Class<?> dtoClass, UUID id) throws IOException {
        Optional<UUID> idOpt = Optional.ofNullable(id);
        List<?> data = processFile(file, dtoClass, idOpt);
        return ResponseEntity.ok("Data uploaded successfully!");
    }

    private List<?> processFile(MultipartFile file, Class<?> dtoClass, Optional<UUID> optionalId) throws IOException {
        File csvFile = convertToCSV(file);
        String absoluteFilePath = csvFile.getAbsolutePath();
        List<?> parsedData;
        try {
            parsedData = extractData(csvFile, dtoClass, optionalId.orElse(null), absoluteFilePath);
        } catch (Exception e) {
            throw new IOException(String.format("Error processing file %s: %s", absoluteFilePath, e.getMessage()), e);
        } finally {
            deleteFile(absoluteFilePath);
        }
        return parsedData;
    }

    private List<?> extractData(File csvFile, Class<?> dtoClass, UUID id, String absoluteFilePath) throws Exception {
        return parseDataFromCSVFile(csvFile, dtoClass, id, absoluteFilePath);
    }

    private List<?> parseDataFromCSVFile(File csvFile, Class<?> dtoClass, UUID id, String absoluteFilePath) {
        List<Object> parsedData = new ArrayList<>();
        try (CSVReader reader = new CSVReaderBuilder(new FileReader(csvFile)).build()) {
            List<String[]> rawData = reader.readAll();
            rawData = removeHeader(rawData); // Remove header row
            parsedData = convertRawDataToDtoList(rawData, dtoClass, id);
        } catch (IOException | CsvException e) {
            handleParsingError(e, absoluteFilePath);
        }
        return parsedData;
    }

    private List<Object> convertRawDataToDtoList(List<String[]> rawData, Class<?> dtoClass, UUID id) {
        List<Object> parsedData = new ArrayList<>();
        for (int i = 0; i < rawData.size(); i++) {
            try {
                Object dto = toDto(rawData.get(i), dtoClass, id);
                parsedData.add(dto);
            } catch (Exception e) {
                log.error("Error processing row {}: {}", i + 1, e.getMessage());
            }
        }
        return parsedData;
    }

    private List<String[]> removeHeader(List<String[]> rawData) {
        if (!rawData.isEmpty()) {
            rawData.remove(0);
        }
        return rawData;
    }

    private void handleParsingError(Exception e, String absoluteFilePath) {
        String message = e instanceof IOException
                ? "Encountered an error while reading the file"
                : "Encountered an error while parsing the CSV data";

        log.error("{} in file {}: {}", message, absoluteFilePath, e.getMessage());
        throw new LnFException(message, e);
    }

    private void deleteFile(String absoluteFilePath) {
        try {
            Files.deleteIfExists(Paths.get(absoluteFilePath));
        } catch (IOException e) {
            log.error("Could not delete file {}: {}", absoluteFilePath, e.getMessage());
        }
    }

    private Object toDto(String[] rowData, Class<?> dtoClass, UUID id) {
        return switch (dtoClass.getSimpleName()) {
            case "ClientDto" -> mapToClientDto(rowData);
            case "ProjectDto" -> mapToProjectDto(rowData, id);
            case "TaskDto" -> mapToTaskDto(rowData, id);
            default -> throw new IllegalArgumentException("Unsupported DTO class: " + dtoClass.getSimpleName());
        };
    }

    private ClientDto mapToClientDto(String[] rowData) {
        ClientDto clientDto = new ClientDto();
        try {
            trimAndSet(rowData, clientDto, 0, ClientDto::setCode);
            trimAndSet(rowData, clientDto, 1, ClientDto::setName);
            trimAndSet(rowData, clientDto, 2, ClientDto::setPan);
            trimAndSet(rowData, clientDto, 3, ClientDto::setTan);
            trimAndSet(rowData, clientDto, 4, ClientDto::setStatus);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH);

            trimAndSetDate(rowData, clientDto, 5, formatter, ClientDto::setWorkingFrom);
            trimAndSetDate(rowData, clientDto, 6, formatter, ClientDto::setAgreementExpiryDate);

            trimAndSet(rowData, clientDto, 7, ClientDto::setServiceType);
            trimAndSet(rowData, clientDto, 8, ClientDto::setClientDetails);

            clientService.create(clientDto);
        } catch (DataIntegrityViolationException e) {
            log.error("Duplicate key violation for client code {}: {}", rowData[0], e.getMessage());
        }
        return clientDto;
    }

    private ProjectDto mapToProjectDto(String[] rowData, UUID id) {
        ProjectDto project = new ProjectDto();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH);
        try {
            trimAndSet(rowData, project, 0, ProjectDto::setBillingTerm);
            trimAndSetBigDecimal(rowData, project, 1, ProjectDto::setBudget);
            trimAndSet(rowData, project, 2, ProjectDto::setBudgetTerms);
            trimAndSet(rowData, project, 3, ProjectDto::setCode);
            trimAndSet(rowData, project, 4, ProjectDto::setCurrency);
            trimAndSet(rowData, project, 5, ProjectDto::setDescription);
            trimAndSetDate(rowData, project, 6, formatter, ProjectDto::setEndDate);
            trimAndSetInteger(rowData, project, 7, ProjectDto::setHoursPerDay);
            trimAndSet(rowData, project, 8, ProjectDto::setName);
            trimAndSet(rowData, project, 9, ProjectDto::setPurchaseOrder);
            trimAndSetDate(rowData, project, 10, formatter, ProjectDto::setStartDate);
            trimAndSet(rowData, project, 11, ProjectDto::setStatus);
            trimAndSet(rowData, project, 12, ProjectDto::setType);

            project.setClientId(id);

            projectService.create(project);
        } catch (DataIntegrityViolationException e) {
            log.error("Duplicate key violation for project code {}: {}", rowData[3], e.getMessage());
        }
        return project;
    }

    private TaskDto mapToTaskDto(String[] rowData, UUID id) {
        TaskDto task = new TaskDto();
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH);
            trimAndSet(rowData, task, 0, TaskDto::setDescription);
            trimAndSetDate(rowData, task, 1, formatter, TaskDto::setEndDate);
            trimAndSet(rowData, task, 2, TaskDto::setName);
            trimAndSetDate(rowData, task, 3, formatter, TaskDto::setStartDate);
            trimAndSet(rowData, task, 4, TaskDto::setStatus);
            trimAndSet(rowData, task, 5, TaskDto::setType);
            task.setProjectId(id);
            taskService.create(task.getProjectId(), task);
        } catch (DataIntegrityViolationException e) {
            log.error("Duplicate key violation for task name {}: {}", rowData[2], e.getMessage());
        }
        return task;
    }

    private <T> void trimAndSet(String[] rowData, T object, int index, BiConsumer<T, String> setter) {
        String value = StringUtils.trim(rowData[index]);
        setter.accept(object, value);
    }

    private <T> void trimAndSetDate(String[] rowData, T object, int index, DateTimeFormatter formatter,
                                    BiConsumer<T, LocalDate> setter) {
        LocalDate date = LocalDate.parse(StringUtils.trim(rowData[index]), formatter);
        setter.accept(object, date);
    }

    private <T> void trimAndSetBigDecimal(String[] rowData, T object, int index, BiConsumer<T, BigDecimal> setter) {
        BigDecimal value = new BigDecimal(StringUtils.trim(rowData[index]));
        setter.accept(object, value);
    }

    private <T> void trimAndSetInteger(String[] rowData, T object, int index, BiConsumer<T, Integer> setter) {
        Integer value = (int)Double.parseDouble(StringUtils.trim(rowData[index]));
        setter.accept(object, value);
    }

    private File convertToCSV(MultipartFile file) throws IOException {
        String filename = Optional.ofNullable(file.getOriginalFilename())
                .orElseThrow(() -> new IllegalArgumentException("File name cannot be null"));

        if (filename.endsWith(".xlsx") || filename.endsWith(".xls")) {
            return convertExcelToCSV(file);
        } else if (filename.endsWith(".csv")) {
            return multipartToFile(file);
        } else {
            throw new IllegalArgumentException("Unsupported file format.");
        }
    }

    private File multipartToFile(MultipartFile multipart) throws IOException {
        File convFile = File.createTempFile("temp", ".csv");
        multipart.transferTo(convFile);
        return convFile;
    }

    private List<String[]> readCSV(File csvFile) throws IOException, CsvException {
        try (CSVReader reader = new CSVReaderBuilder(new FileReader(csvFile)).build()) {
            return reader.readAll();
        }
    }

    private File convertExcelToCSV(MultipartFile file) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            File tempFile = File.createTempFile("temp", ".csv");
            writeToCSV(sheet, tempFile);
            return tempFile;
        }
    }

    private void writeToCSV(Sheet sheet, File file) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            sheet.forEach(row -> {
                row.forEach(cell -> {
                    try {
                        writer.write(cell.toString() + ",");
                    } catch (IOException e) {
                        throw new LnFException("Error writing cell to CSV: " + e.getMessage());
                    }
                });
                try {
                    writer.write("\n");
                } catch (IOException e) {
                    throw new LnFException("Error writing character to CSV: " + e.getMessage());
                }
            });
        }
    }

}
