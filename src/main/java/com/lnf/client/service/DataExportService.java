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

import com.lnf.client.converter.ClientConverter;
import com.lnf.client.converter.ProjectConverter;
import com.lnf.client.converter.TaskConverter;
import com.lnf.client.model.*;
import com.lnf.client.model.enums.AddressType;
import com.lnf.dto.client.ClientDto;
import com.lnf.dto.client.ProjectDto;
import com.lnf.dto.client.TaskDto;
import com.lnf.exception.LnFException;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
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
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.ObjIntConsumer;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class DataExportService {

    public static final String DD_MM_YYYY = "dd/MM/yyyy";
    public static final String DD_MM_YYYY1 = "dd-MM-yyyy";
    public static final String DD_MMM_YYYY = "dd-MMM-yyyy";
    public static final String ROW_DATA_ARRAY_IS_NULL_OR_HAS_INSUFFICIENT_ELEMENTS = "rowData array is null or has insufficient elements.";
    public static final String ENCOUNTERED_AN_ERROR_WHILE_READING_THE_FILE = "Encountered an error while reading the file {}";
    public static final String ENCOUNTERED_AN_ERROR_WHILE_PARSING_THE_CSV_DATA = "Encountered an error while parsing the CSV data {}";
    public static final String COULD_NOT_DELETE_THE_FILE = "Could not delete the file ";

    private final ClientService clientService;
    private final ProjectService projectService;
    private final TaskService taskService;

    public ResponseEntity<String> uploadFile(MultipartFile file, Class<?> dtoClass, UUID id) throws IOException {
        Optional<UUID> idOpt = Optional.ofNullable(id);
        processFile(file, dtoClass, idOpt);
        return ResponseEntity.ok("Data uploaded successfully!");
    }

    private List<?> processFile(MultipartFile file, Class<?> dtoClass, Optional<UUID> optionalId) throws IOException {
        File csvFile = convertToCSV(file);
        String absoluteFilePath = csvFile.getAbsolutePath();
        List<?> parsedData;
        try {
            parsedData = extractData(csvFile, dtoClass, optionalId.orElse(null), absoluteFilePath);
        } catch (Exception e) {
            throw new IOException("Error processing file %s: %s".formatted(absoluteFilePath, e.getMessage()), e);
        } finally {
            deleteFile(absoluteFilePath);
        }
        return parsedData;
    }

    private List<?> extractData(File csvFile, Class<?> dtoClass, UUID id, String absoluteFilePath) {
        return parseDataFromCSVFile(csvFile, dtoClass, id, absoluteFilePath);
    }

    private List<?> parseDataFromCSVFile(File csvFile, Class<?> dtoClass, UUID id, String absoluteFilePath) {
        List<Object> parsedData = new ArrayList<>();
        try (CSVReader reader = new CSVReaderBuilder(new FileReader(csvFile)).build()) {
            List<String[]> rawData = reader.readAll();
            removeHeader(rawData);
            parsedData = convertRawDataToDtoList(rawData, dtoClass, id);
        } catch (IOException | CsvException e) {
            handleParsingError(e, absoluteFilePath);
        }
        return parsedData;
    }

    private List<Object> convertRawDataToDtoList(List<String[]> rawData, Class<?> dtoClass, UUID id) {
        List<Object> parsedData = new ArrayList<>();
        LocalDateTime localdatetime = LocalDateTime.now();

        for (int i = 0; i < rawData.size(); i++) {
            try {
                Object dto = toDto(rawData.get(i), dtoClass, id, localdatetime);
                parsedData.add(dto);
            } catch (Exception e) {
                log.error("Error processing row {}: {}", i + 1, e.getMessage());
            }
        }
        return parsedData;
    }

    private List<String[]> removeHeader(List<String[]> rawData) {
        deleteFirst(rawData);
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
            Files.deleteIfExists(Path.of(absoluteFilePath));
        } catch (IOException e) {
            log.error("Could not delete file {}: {}", absoluteFilePath, e.getMessage());
        }
    }

    private Object toDto(String[] rowData, Class<?> dtoClass, UUID id, LocalDateTime localDateTime) {
        return switch (dtoClass.getSimpleName()) {
            case "ClientDto" -> mapToClientDto(rowData, localDateTime);
            case "ProjectDto" -> mapToProjectDto(rowData, id, localDateTime);
            case "TaskDto" -> mapToTaskDto(rowData, id, localDateTime);
            default -> throw new IllegalArgumentException("Unsupported DTO class: " + dtoClass.getSimpleName());
        };
    }

    private ClientDto mapToClientDto(String[] rowData, LocalDateTime localDateTime) {
        ClientDto clientDto = new ClientDto();

        try {
            trimAndSet(rowData, clientDto, 0, ClientDto::setCode);
            trimAndSet(rowData, clientDto, 1, ClientDto::setName);
            trimAndSet(rowData, clientDto, 2, ClientDto::setPan);
            trimAndSet(rowData, clientDto, 3, ClientDto::setTan);
            trimAndSet(rowData, clientDto, 4, ClientDto::setStatus);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DD_MMM_YYYY, Locale.ENGLISH);

            trimAndSetDate(rowData, clientDto, 5, formatter, ClientDto::setWorkingFrom);
            trimAndSetDate(rowData, clientDto, 6, formatter, ClientDto::setAgreementExpiryDate);

            trimAndSet(rowData, clientDto, 7, ClientDto::setServiceType);
            trimAndSet(rowData, clientDto, 8, ClientDto::setClientDetails);
            clientDto.setUploadTime(localDateTime);

            clientService.create(clientDto);
        } catch (DataIntegrityViolationException e) {
            log.error("Duplicate key violation for client code {}: {}", rowData[0], e.getMessage());
        }
        return clientDto;
    }

    private ProjectDto mapToProjectDto(String[] rowData, UUID id, LocalDateTime localDateTime) {
        ProjectDto project = new ProjectDto();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DD_MMM_YYYY, Locale.ENGLISH);
        try {
            trimAndSet(rowData, project, 0, ProjectDto::setBillingTerm);
            trimAndSetBigDecimal(rowData, project, ProjectDto::setBudget);
            trimAndSet(rowData, project, 2, ProjectDto::setBudgetTerms);
            trimAndSet(rowData, project, 3, ProjectDto::setCode);
            trimAndSet(rowData, project, 4, ProjectDto::setCurrency);
            trimAndSet(rowData, project, 5, ProjectDto::setDescription);
            trimAndSetDate(rowData, project, 6, formatter, ProjectDto::setEndDate);
            trimAndSetInteger(rowData, project, ProjectDto::setHoursPerDay);
            trimAndSet(rowData, project, 8, ProjectDto::setName);
            trimAndSet(rowData, project, 9, ProjectDto::setPurchaseOrder);
            trimAndSetDate(rowData, project, 10, formatter, ProjectDto::setStartDate);
            trimAndSet(rowData, project, 11, ProjectDto::setStatus);
            trimAndSet(rowData, project, 12, ProjectDto::setType);
            project.setUploadTime(localDateTime);

            project.setClientId(id);

            projectService.create(project);
        } catch (DataIntegrityViolationException e) {
            log.error("Duplicate key violation for project code {}: {}", rowData[3], e.getMessage());
        }
        return project;
    }

    private TaskDto mapToTaskDto(String[] rowData, UUID id, LocalDateTime uploadTime) {
        TaskDto task = new TaskDto();
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DD_MMM_YYYY, Locale.ENGLISH);
            trimAndSet(rowData, task, 0, TaskDto::setDescription);
            trimAndSetDate(rowData, task, 1, formatter, TaskDto::setEndDate);
            trimAndSet(rowData, task, 2, TaskDto::setName);
            trimAndSetDate(rowData, task, 3, formatter, TaskDto::setStartDate);
            trimAndSet(rowData, task, 4, TaskDto::setStatus);
            trimAndSet(rowData, task, 5, TaskDto::setType);
            task.setUploadTime(uploadTime);
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

    private <T> void trimAndSetBigDecimal(String[] rowData, T object, BiConsumer<T, BigDecimal> setter) {
        BigDecimal value = new BigDecimal(StringUtils.trim(rowData[1]));
        setter.accept(object, value);
    }

    private <T> void trimAndSetInteger(String[] rowData, T object, ObjIntConsumer<T> setter) {
        int value = (int) Double.parseDouble(StringUtils.trim(rowData[7]));
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

    public List<ClientDto> retrieveClientFile(MultipartFile file) throws IOException {
        return handleUploadedClientFile(file);
    }

    public List<ClientDto> handleUploadedClientFile(MultipartFile file) throws IOException {
        File csvFile = convertToCSV(file);
        List<Client> clientList;
        try {
            List<String[]> data = readCSV(csvFile);

            deleteFirst(data);
            clientList = data.stream().map(this::toClientDto).toList();
            return clientList.stream().map(ClientConverter::toTransportModel).toList();
        } catch (LnFException e) {
            log.error(ENCOUNTERED_AN_ERROR_WHILE_READING_THE_FILE, file);
            throw new LnFException("Encountered an error while reading the file", e);
        } catch (CsvException e) {
            log.error(ENCOUNTERED_AN_ERROR_WHILE_PARSING_THE_CSV_DATA, file);
            throw new LnFException("Encountered an error while parsing the CSV data", e);
        } finally {
            if (!csvFile.delete()) {
                log.error(COULD_NOT_DELETE_THE_FILE + csvFile.getAbsolutePath());
            }
        }
    }

    private Client toClientDto(String[] rowData) {
        if (rowData == null || rowData.length < 27) {
            throw new IllegalArgumentException(ROW_DATA_ARRAY_IS_NULL_OR_HAS_INSUFFICIENT_ELEMENTS);
        }

        DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder()
                .appendOptional(DateTimeFormatter.ofPattern(DD_MM_YYYY1))
                .appendOptional(DateTimeFormatter.ofPattern(DD_MM_YYYY))
                .appendOptional(DateTimeFormatter.ofPattern(DD_MMM_YYYY))
                .toFormatter();

        String code = rowData[0];
        String name = rowData[1];
        String pan = rowData[2];
        String tan = rowData[3];
        String status = rowData[4];
        LocalDate workingFrom = LocalDate.parse(rowData[5], dateFormatter);
        LocalDate agreementExpiryDate = LocalDate.parse(rowData[6], dateFormatter);
        String serviceType = rowData[7];
        String clientDetails = rowData[8];
        String addressText = rowData[9];
        String town = rowData[10];
        String city = rowData[11];
        String state = rowData[12];
        String country = rowData[13];
        String postCode = rowData[14];
        String addressType = rowData[15];
        String contactName = rowData[16];
        String contactPhoneNumber = rowData[17];
        String contactDesignation = rowData[18];
        String contactDepartment = rowData[19];
        String contactEmail = rowData[20];
        String escalationName = rowData[21];
        String escalationEmail = rowData[22];
        String escalationMobileNumber = rowData[23];
        String escalationPhoneNumber = rowData[24];
        String gstNumber = rowData[25];
        String gstLocation = rowData[26];

        // Create objects only if valid
        ClientAddress clientAddress = buildClientAddress(addressText, town, city, state, country, postCode, addressType);
        ClientContact clientContact = buildClientContact(contactName, contactPhoneNumber, contactDesignation, contactDepartment, contactEmail);
        Escalation escalation = buildEscalation(escalationName, escalationEmail, escalationMobileNumber, escalationPhoneNumber);
        Gst gst = buildGst(gstLocation, gstNumber);

        // Set collections
        Set<ClientAddress> addressSet = createSetIfValid(clientAddress);
        Set<ClientContact> clientContactSet = createSetIfValid(clientContact);
        Set<Escalation> escalationSet = createSetIfValid(escalation);
        Set<Gst> gstSet = createSetIfValid(gst);

        return new Client(
                code, name, pan, null, tan, status, workingFrom, agreementExpiryDate, serviceType, clientDetails,
                null, addressSet, clientContactSet, escalationSet, gstSet, null, null, null, null, null
        );
    }

    private ClientAddress buildClientAddress(String addressText, String town, String city, String state, String country, String postCode, String addressType) {
        if (StringUtils.isNotBlank(addressText) && StringUtils.isNotBlank(town) && StringUtils.isNotBlank(city) &&
                StringUtils.isNotBlank(state) && StringUtils.isNotBlank(country) && StringUtils.isNotBlank(postCode) &&
                addressType != null) {
            ClientAddress clientAddress = new ClientAddress();
            clientAddress.setAddressText(addressText);
            clientAddress.setTown(town);
            clientAddress.setCity(city);
            clientAddress.setState(state);
            clientAddress.setCountry(country);
            clientAddress.setPostCode(postCode);
            clientAddress.setAddressType(AddressType.valueOf(addressType));
            return clientAddress;
        }
        return null;
    }

    private ClientContact buildClientContact(String name, String phoneNumber, String designation, String department, String email) {
        if (StringUtils.isNotBlank(name) && StringUtils.isNotBlank(phoneNumber) && StringUtils.isNotBlank(designation) &&
                StringUtils.isNotBlank(department) && StringUtils.isNotBlank(email)) {
            ClientContact clientContact = new ClientContact();
            clientContact.setName(name);
            clientContact.setPhoneNumber(phoneNumber);
            clientContact.setDesignation(designation);
            clientContact.setDepartment(department);
            clientContact.setEmail(email);
            return clientContact;
        }
        return null;
    }

    private Escalation buildEscalation(String name, String email, String mobileNumber, String phoneNumber) {
        if (StringUtils.isNotBlank(name) && StringUtils.isNotBlank(email) && StringUtils.isNotBlank(mobileNumber) &&
                StringUtils.isNotBlank(phoneNumber)) {
            Escalation escalation = new Escalation();
            escalation.setName(name);
            escalation.setEmail(email);
            escalation.setMobileNumber(mobileNumber);
            escalation.setPhoneNumber(phoneNumber);
            return escalation;
        }
        return null;
    }

    private Gst buildGst(String location, String number) {
        if (StringUtils.isNotBlank(location) && StringUtils.isNotBlank(number)) {
            Gst gst = new Gst();
            gst.setLocation(location);
            gst.setNumber(number);
            return gst;
        }
        return null;
    }

    private <T> Set<T> createSetIfValid(T object) {
        Set<T> set = new HashSet<>();
        if (object != null) {
            set.add(object);
        }
        return set;
    }

    public List<ProjectDto> retrieveProjectFile(MultipartFile file, UUID clientId) throws IOException {
        return handleUploadedProjectFile(file, clientId);
    }

    public List<ProjectDto> handleUploadedProjectFile(MultipartFile file, UUID clientId) throws IOException {
        File csvFile = convertToCSV(file);
        List<Project> projectList;
        try {
            List<String[]> data = readCSV(csvFile);

            deleteFirst(data);
            projectList = data.stream().map(entity ->
                    this.toProjectDto(entity, clientId)
            ).toList();
            return projectList.stream().map(ProjectConverter::toTransportModel).toList();
        } catch (LnFException e) {
            log.error(ENCOUNTERED_AN_ERROR_WHILE_READING_THE_FILE, file);
            throw new LnFException("Encountered an error while reading the file", e);
        } catch (CsvException e) {
            log.error(ENCOUNTERED_AN_ERROR_WHILE_PARSING_THE_CSV_DATA, file);
            throw new LnFException("Encountered an error while parsing the CSV data", e);
        } finally {
            if (!csvFile.delete()) {
                log.error(COULD_NOT_DELETE_THE_FILE + csvFile.getAbsolutePath());
            }
        }
    }

    private void deleteFirst(List<String[]> data) {
        if (!data.isEmpty()) {
            data.removeFirst();
        }
    }

    private Project toProjectDto(String[] rowData, UUID clientId) {
        if (rowData == null || rowData.length < 13) {
            throw new IllegalArgumentException(ROW_DATA_ARRAY_IS_NULL_OR_HAS_INSUFFICIENT_ELEMENTS);
        }
        Client client = clientService.search(clientId);

        DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder()
                .appendOptional(DateTimeFormatter.ofPattern(DD_MM_YYYY))
                .appendOptional(DateTimeFormatter.ofPattern(DD_MM_YYYY1))
                .appendOptional(DateTimeFormatter.ofPattern(DD_MMM_YYYY))
                .toFormatter();

        String billingTerm = rowData[0];
        BigDecimal budget = new BigDecimal(rowData[1]);
        String budgetTerms = rowData[2];
        String code = rowData[3];
        String currency = rowData[4];
        String description = rowData[5];
        LocalDate endDate = LocalDate.parse(rowData[6], dateFormatter);
        Integer hoursPerDay = (int) Double.parseDouble(rowData[7]);
        String name = rowData[8];
        String purchaseOrder = rowData[9];
        LocalDate startDate = LocalDate.parse(rowData[10], dateFormatter);
        String status = rowData[11];
        String type = rowData[12];

        return new Project(code, name, type, description, purchaseOrder, budgetTerms, status, currency, budget, hoursPerDay, billingTerm, startDate, endDate, null, client, null, null);
    }

    public List<TaskDto> retrieveFile(MultipartFile file, UUID projectId) throws IOException {
        return handleUploadedFile(file, projectId);
    }

    public List<TaskDto> handleUploadedFile(MultipartFile file, UUID projectId) throws IOException {
        File csvFile = convertToCSV(file);
        List<Task> taskList;
        try {
            List<String[]> data = readCSV(csvFile);

            deleteFirst(data);
            taskList = data.stream().map(entity ->
                    this.toTaskDto(entity, projectId)
            ).toList();
            return taskList.stream().map(TaskConverter::toTransportModel).toList();
        } catch (LnFException e) {
            log.error(ENCOUNTERED_AN_ERROR_WHILE_READING_THE_FILE, file);
            throw new LnFException("Encountered an error while reading the file", e);
        } catch (CsvException e) {
            log.error(ENCOUNTERED_AN_ERROR_WHILE_PARSING_THE_CSV_DATA, file);
            throw new LnFException("Encountered an error while parsing the CSV data", e);
        } finally {
            if (!csvFile.delete()) {
                log.error(COULD_NOT_DELETE_THE_FILE + csvFile.getAbsolutePath());
            }
        }
    }

    private Task toTaskDto(String[] rowData, UUID projectId) {
        if (rowData == null || rowData.length < 7) {
            throw new IllegalArgumentException(ROW_DATA_ARRAY_IS_NULL_OR_HAS_INSUFFICIENT_ELEMENTS);
        }
        Project project = projectService.search(projectId);

        DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder()
                .appendOptional(DateTimeFormatter.ofPattern(DD_MM_YYYY1))
                .appendOptional(DateTimeFormatter.ofPattern(DD_MM_YYYY))
                .appendOptional(DateTimeFormatter.ofPattern(DD_MMM_YYYY))
                .toFormatter();

        String description = rowData[0];
        LocalDate endDate = LocalDate.parse(rowData[1], dateFormatter);
        String name = rowData[2];
        LocalDate startDate = LocalDate.parse(rowData[3], dateFormatter);
        String status = rowData[4];
        String type = rowData[5];

        return new Task(name, type, status, description, startDate, endDate, null, project);
    }

}
