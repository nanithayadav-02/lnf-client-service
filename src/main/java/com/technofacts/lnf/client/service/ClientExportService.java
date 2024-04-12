package com.technofacts.lnf.client.service;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import com.technofacts.lnf.dto.client.ClientDto;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class ClientExportService {

    private final ClientService service;
    public void uploadFile(MultipartFile file) throws IOException {
        List<ClientDto> users = handleFile(file);
        ResponseEntity.ok("Data uploaded successfully!");
    }


    public List<ClientDto> handleFile(MultipartFile file) throws IOException {
        File csvFile = convertToCSV(file);
        List<ClientDto> projects;
        try {
            List<String[]> data = readCSV(csvFile);
            if (!data.isEmpty()) {
                data.remove(0);
            }
            projects = data.stream().map(this::toClientDto).toList();
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

    private ClientDto toClientDto(String[] rowData) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH);
        ClientDto client = new ClientDto();
        client.setCode(rowData[1]);
        client.setName(rowData[2]);
        client.setPan(rowData[3]);
        client.setTan(rowData[4]);
        client.setWorkingFrom(LocalDate.parse(rowData[6],formatter));
        client.setAgreementExpiryDate(LocalDate.parse(rowData[7],formatter));
        client.setStatus(rowData[5]);
        client.setServiceType(rowData[8]);
        client.setClientDetails(rowData[9]);
        service.create(client);
        return client;
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
