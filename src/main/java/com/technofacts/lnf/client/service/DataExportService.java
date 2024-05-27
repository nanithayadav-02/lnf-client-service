package com.technofacts.lnf.client.service;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import com.technofacts.lnf.dto.client.ClientDto;
import com.technofacts.lnf.dto.client.ProjectDto;
import com.technofacts.lnf.dto.client.TaskDto;
import com.technofacts.lnf.exception.LnFException;
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
        List<?> data = handleFile(file, dtoClass, idOpt);
        return ResponseEntity.ok("Data uploaded successfully!");
    }

    private List<?> handleFile(MultipartFile file, Class<?> dtoClass, Optional<UUID> idOpt) throws IOException {
        File csvFile = convertToCSV(file);
        String absoluteFilePath = csvFile.getAbsolutePath();
        List<?> parsedData = new ArrayList<>();

        try {
            parsedData = parseDataFromCSVFile(csvFile, dtoClass, idOpt.orElse(null), absoluteFilePath);
        } catch (Exception e) {
            log.error("Error handling file {}: {}", absoluteFilePath, e.getMessage());
        } finally {
            deleteFile(absoluteFilePath, csvFile); // Ensure cleanup happens.
        }

        return parsedData;
    }

    private List<?> parseDataFromCSVFile(File csvFile, Class<?> dtoClass, UUID id, String absoluteFilePath) {
        List<Object> parsedData = new ArrayList<>();
        try (CSVReader reader = new CSVReaderBuilder(new FileReader(csvFile)).build()) {
            List<String[]> rawData = reader.readAll();
            rawData = removeHeader(rawData); // Remove header row
            for (int i = 0; i < rawData.size(); i++) {
                try {
                    Object dto = toDto(rawData.get(i), dtoClass, id);
                    parsedData.add(dto);
                } catch (Exception e) {
                    log.error("Error processing row {}: {}", i + 1, e.getMessage());
                }
            }
        } catch (IOException | CsvException e) {
            handleParsingError(e, absoluteFilePath);
        }
        return parsedData;
    }

    private List<String[]> removeHeader(List<String[]> rawData) {
        if (!rawData.isEmpty()) {
            rawData.remove(0);
        }
        return rawData;
    }

    private List<?> convertToDtoList(List<String[]> rawData, Class<?> dtoClass, UUID id) {
        return rawData.stream().map(rowData -> toDto(rowData, dtoClass, id)).toList();
    }

    private void handleParsingError(Exception e, String absoluteFilePath) {
        String message = e instanceof IOException
                ? "Encountered an error while reading the file"
                : "Encountered an error while parsing the CSV data";

        log.error("{} in file {}: {}", message, absoluteFilePath, e.getMessage());
        throw new LnFException(message, e);
    }

    private void deleteFile(String absoluteFilePath, File csvFile) {
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
        ClientDto client = new ClientDto();
        try {
            client.setCode(StringUtils.trim(rowData[0]));
            client.setName(StringUtils.trim(rowData[1]));
            client.setPan(StringUtils.trim(rowData[2]));
            client.setTan(StringUtils.trim(rowData[3]));
            client.setStatus(StringUtils.trim(rowData[4]));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH);
            client.setWorkingFrom(LocalDate.parse(StringUtils.trim(rowData[5]), formatter));
            client.setAgreementExpiryDate(LocalDate.parse(StringUtils.trim(rowData[6]), formatter));
            client.setServiceType(StringUtils.trim(rowData[7]));
            client.setClientDetails(StringUtils.trim(rowData[8]));
            clientService.create(client);
        } catch (DataIntegrityViolationException e) {
            log.error("Duplicate key violation for client code {}: {}", rowData[0], e.getMessage());
        }
        return client;
    }


    private ProjectDto mapToProjectDto(String[] rowData, UUID id) {
        ProjectDto project = new ProjectDto();
        try {
            project.setCode(StringUtils.trim(rowData[3]));
            project.setName(StringUtils.trim(rowData[8]));
            project.setType(StringUtils.trim(rowData[12]));
            project.setDescription(StringUtils.trim(rowData[5]));
            project.setPurchaseOrder(StringUtils.trim(rowData[9]));
            project.setBudgetTerms(StringUtils.trim(rowData[2]));
            project.setStatus(StringUtils.trim(rowData[11]));
            project.setCurrency(StringUtils.trim(rowData[4]));
            project.setBudget(new BigDecimal(StringUtils.trim(rowData[1])));
            project.setHoursPerDay((int) Double.parseDouble(StringUtils.trim(rowData[7])));
            project.setBillingTerm(StringUtils.trim(rowData[0]));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yyyy");
            project.setStartDate(LocalDate.parse(StringUtils.trim(rowData[10]), formatter));
            project.setEndDate(LocalDate.parse(StringUtils.trim(rowData[6]), formatter));
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
            task.setDescription(StringUtils.trim(rowData[0]));
            task.setEndDate(LocalDate.parse(StringUtils.trim(rowData[1]), formatter));
            task.setName(StringUtils.trim(rowData[2]));
            task.setStartDate(LocalDate.parse(StringUtils.trim(rowData[3]), formatter));
            task.setStatus(StringUtils.trim(rowData[4]));
            task.setType(StringUtils.trim(rowData[5]));
            task.setProjectId(id);
            taskService.create(task.getProjectId(), task);
        } catch (DataIntegrityViolationException e) {
            log.error("Duplicate key violation for task name {}: {}", rowData[2], e.getMessage());
        }
        return task;
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
