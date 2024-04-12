package com.technofacts.lnf.client.service;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import com.technofacts.lnf.dto.client.ProjectDto;
import com.technofacts.lnf.exception.LnFException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class ProjectExportService {

    private final ProjectService service;
    public void uploadFile(MultipartFile file) throws IOException {
        List<ProjectDto> users = handleFile(file);
        ResponseEntity.ok("Data uploaded successfully!");
    }


    public List<ProjectDto> handleFile(MultipartFile file) throws IOException {
        File csvFile = convertToCSV(file);
        List<ProjectDto> projects;
        try {
            List<String[]> data = readCSV(csvFile);
            if (!data.isEmpty()) {
                data.remove(0);
            }
            projects = data.stream().map(this::toProjectDto).toList();
        } catch (IOException e) {
            log.info("Encountered an error while reading the file {}");
            throw new LnFException("Encountered an error while reading the file", e);
        } catch (CsvException e) {
            log.info("Encountered an error while parsing the CSV data {}");
            throw new LnFException("Encountered an error while parsing the CSV data", e);
        } finally {
            if (!csvFile.delete()) {
                log.info("Could not delete the file " + csvFile.getAbsolutePath());
            }
        }
        return projects;
    }


    private ProjectDto toProjectDto(String[] rowData) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yyyy");
        ProjectDto project = new ProjectDto();
        project.setCode(rowData[4]);
        project.setName(rowData[9]);
        project.setType(rowData[13]);
        project.setDescription(rowData[6]);
        project.setPurchaseOrder(rowData[10]);
        project.setBudgetTerms(rowData[3]);
        project.setStatus(rowData[12]);
        project.setCurrency(rowData[5]);
        project.setBudget(new BigDecimal(rowData[2]));
        double hoursPerDayDouble = Double.parseDouble(rowData[8]);
        int hoursPerDayInt = (int) hoursPerDayDouble;
        project.setHoursPerDay(hoursPerDayInt);
        project.setBillingTerm(rowData[1]);
        project.setStartDate(LocalDate.parse(rowData[11], formatter));
        project.setEndDate(LocalDate.parse(rowData[7], formatter));
        UUID clientId=UUID.fromString(rowData[14]);
        project.setClientId(clientId);
        service.create(project);
        return project;
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
                        throw new LnFException ("Error writing cell to CSV: " + e.getMessage ());
                    }
                });
                try {
                    writer.write("\n");
                } catch (IOException e) {
                    throw new LnFException ("Error writing character to CSV: " + e.getMessage ());
                }
            });
        }
    }

}
