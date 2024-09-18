/*
 * Copyright (c)  Lever And Fulcrum (LNF)
 */
package com.technofacts.lnf.client.service;

import com.google.common.collect.Lists;
import com.technofacts.lnf.client.converter.ClientDirectoryConverter;
import com.technofacts.lnf.client.model.Client;
import com.technofacts.lnf.client.model.ClientDirectory;
import com.technofacts.lnf.client.repository.ClientDirectoryRepository;
import com.technofacts.lnf.client.repository.ClientRepository;
import com.technofacts.lnf.dto.client.ClientDirectoryDto;
import com.technofacts.lnf.dto.client.ClientDirectoryExcelDto;
import com.technofacts.lnf.dto.common.PageRequestDto;
import com.technofacts.lnf.dto.email.ExcelReportDto;
import com.technofacts.lnf.dto.email.ThymeleafDocumentDto;
import com.technofacts.lnf.enums.ReportType;
import com.technofacts.lnf.exception.LnFBadRequestException;
import com.technofacts.lnf.exception.LnFEntityNotFoundException;
import com.technofacts.lnf.exception.LnFException;
import com.technofacts.lnf.service.common.page.PaginatedAndSortedService;
import com.technofacts.lnf.service.email.ExcelReportService;
import com.technofacts.lnf.service.email.ThymeleafDocumentService;
import com.technofacts.lnf.service.specification.GenericSpecificationBuilder;
import com.technofacts.lnf.util.RestUtil;
import com.technofacts.lnf.util.specification.SpecificationUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ClientDirectoryService implements PaginatedAndSortedService<ClientDirectoryDto> {

    public static final String FILE_NAME = "client_directory.pdf";
    private static final String CLIENT_EXCEL_FILE = "client_directory.xlsx";

    private final ThymeleafDocumentService documentService;
    private final ExcelReportService excelReportService;

    private final ClientDirectoryRepository repository;
    private final ClientRepository clientRepository;

    @Override
    public Page<ClientDirectoryDto> findPaginatedAndSorted(int page, int size, String sortBy, String sortOrder) {
        final Sort sortInfo = RestUtil.constructSort(sortBy, sortOrder);
        Page<ClientDirectory> resultPage = repository.findAll(PageRequest.of(page, size, sortInfo));
        return validateAndGetPages(page, resultPage);
    }

    @Override
    public Page<ClientDirectoryDto> findPaginated(int page, int size) {
        Page<ClientDirectory> resultPage = repository.findAll(PageRequest.of(page, size));
        return validateAndGetPages(page, resultPage);
    }

    @Override
    public List<ClientDirectoryDto> findAllSorted(String sortBy, String sortOrder) {
        final Sort sortInfo = RestUtil.constructSort(sortBy, sortOrder);
        List<ClientDirectory> entities = Lists.newArrayList(repository.findAll(sortInfo));
        return entities.stream().map(ClientDirectoryConverter::toTransportModel)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public List<ClientDirectoryDto> findAll() {
        List<ClientDirectory> entities = repository.findAll();
        return entities.stream().map(ClientDirectoryConverter::toTransportModel)
                .filter(Objects::nonNull)
                .toList();
    }

    private Page<ClientDirectoryDto> validateAndGetPages(int page, Page<ClientDirectory> resultPage) {
        if (page > resultPage.getTotalPages()) {
            throw new LnFEntityNotFoundException(String.format("Total number of pages [%d], " +
                    "requested page [%d] does not exist", resultPage.getTotalPages(), page));
        }
        return resultPage.map(ClientDirectoryConverter::toTransportModel);
    }

    public Page<ClientDirectoryDto> findingAllWithPagination(String search, PageRequestDto pageRequestDto) {
        Pageable pageable = PageRequest.of(pageRequestDto.getPage(), pageRequestDto.getSize(),
                RestUtil.constructSort(pageRequestDto.getSortBy(), pageRequestDto.getSortOrder()));
        Specification<ClientDirectory> specification = buildClientDirectorySpecification(search);
        Page<ClientDirectory> resultPage = repository.findAll(specification, pageable);
        return resultPage.map(ClientDirectoryConverter::toTransportModel);
    }

    private Specification<ClientDirectory> buildClientDirectorySpecification(String search) {
        GenericSpecificationBuilder<ClientDirectory> clientDirectoryBuilder = new GenericSpecificationBuilder<>();
        Function<String, Class<?>> fieldClassForClientDirectory = this::getFieldClassFromClientDirectory;
        return SpecificationUtil.buildSpecification(search, clientDirectoryBuilder, fieldClassForClientDirectory);
    }

    private Class<?> getFieldClassFromClientDirectory(String fieldName) {
        return SpecificationUtil.getFieldClass(ClientDirectory.class, fieldName);
    }

    public List<ClientDirectoryDto> findAll(String search) {
        Specification<ClientDirectory> specification = buildClientDirectorySpecification(search);
        List<ClientDirectory> entities = repository.findAll(specification);
        return convertToDtos(entities);
    }

    private List<ClientDirectoryDto> convertToDtos(List<ClientDirectory> entities) {
        return entities.stream()
                .map(ClientDirectoryConverter::toTransportModel)
                .filter(Objects::nonNull)
                .toList();
    }

    public ClientDirectoryDto findByClientIdAndDirectoryId(UUID clientId, UUID directoryId) {
        searchForClient(clientId);
        ClientDirectory entity = searchForClientDirectoryId(directoryId);
        return ClientDirectoryConverter.toTransportModel(entity);
    }

    private ClientDirectory searchForClientDirectoryId(UUID directoryId) {
        return repository.findById(directoryId).orElseThrow(() ->
                new LnFEntityNotFoundException(String.format("ClientDirectory with id [%s] does not exist", directoryId)));
    }

    private Client searchForClient(UUID clientId) {
        return clientRepository.findByClientId(clientId).orElseThrow(() ->
                new LnFEntityNotFoundException(String.format("Client with id [%s] does not exist", clientId)));
    }

    public void create(UUID clientId, ClientDirectoryDto resource) {
        LnFBadRequestException.throwOnCondition(Objects::isNull, resource,
                String.format("Failed to create ClientDirectory for Client [%s] with null payload", clientId));
        Client client = searchForClient(clientId);
        ClientDirectory entity = ClientDirectoryConverter.toEntityModel(resource);
        entity.setClient(client);
        saveEntity(entity);
        log.debug("ClientDirectory {} successfully created", entity.getId());
    }

    public void createAll(UUID clientId, List<ClientDirectoryDto> resource) {
        LnFBadRequestException.throwOnCondition(Objects::isNull, resource,
                String.format("Failed to create ClientDirectory [%s] with null payload", clientId));
        Client client = searchForClient(clientId);
        List<ClientDirectory> entities = new ArrayList<>();
        resource.stream().filter(Objects::nonNull).forEach(dto -> {
            ClientDirectory entity = ClientDirectoryConverter.toEntityModel(dto);
            entity.setClient(client);
            entities.add(entity);
        });
        save(entities);
        log.debug("ClientDirectory for Client is {} successfully created", clientId);
    }

    public void update(UUID clientId, UUID directoryId, ClientDirectoryDto resource) {
        LnFBadRequestException.throwOnCondition(Objects::isNull, resource,
                String.format("Failed to update ClientDirectory for Client [%s] with null payload", clientId));

        searchForClient(clientId);
        ClientDirectory entity = searchForClientDirectoryId(directoryId);
        saveEntity(ClientDirectoryConverter.toEntityModel(resource, entity));
        log.debug("ClientDirectory {} for Client {} successfully updated", entity.getClient().getId(), entity.getId());
    }

    public List<ClientDirectoryDto> findByClientId(UUID clientId) {
        List<ClientDirectory> entities = repository.findDirectoriesByClientId(clientId);
        return entities.stream().map(ClientDirectoryConverter::toTransportModel).filter(Objects::nonNull).toList();
    }

    public void deleteByClientId(UUID clientId) {
        searchForClient(clientId);
        List<ClientDirectory> entities = repository.findDirectoriesByClientId(clientId);
        try {
            repository.deleteAll(entities);
            log.debug("ClientDirectory {} for Client successfully deleted", clientId);
        } catch (RuntimeException e) {
            String errorMessage = String.format("Failed to delete ClientDirectories [%s] for Client", clientId);
            throw new LnFException(errorMessage);
        }
    }

    public void deleteByClientIdAndDirectoryId(UUID clientId, UUID directoryId) {
        searchForClient(clientId);
        ClientDirectory entity = searchForClientDirectoryId(directoryId);
        try {
            repository.delete(entity);
            log.debug("ClientDirectory {} for Client {} successfully deleted", clientId, directoryId);
        } catch (RuntimeException e) {
            String errorMessage = String.format("Failed to delete ClientDirectory [%s] for job [%s]", clientId, directoryId);
            throw new LnFException(errorMessage);
        }
    }

    public List<ClientDirectoryDto> findByEmail(String email) {
        List<ClientDirectory> clients = repository.findByEmail(email);
        return clients.stream()
                .map(ClientDirectoryConverter::toTransportModel)
                .toList();
    }

    public List<ClientDirectoryDto> findByClientIdAndEmail(UUID clientId, String email) {
        List<ClientDirectory> entities = repository.findByClientIdAndEmail(clientId, email);
        return entities.stream().map(ClientDirectoryConverter::toTransportModel).toList();
    }

    private void saveEntity(ClientDirectory entity) {
        try {
            repository.save(entity);
        } catch (RuntimeException e) {
            String errorMessage = String.format("Failed to save ClientDirectory [%s]", entity.toString());
            throw new LnFException(errorMessage, e);
        }
    }

    private void save(List<ClientDirectory> entities) {
        try {
            repository.saveAll(entities);
        } catch (RuntimeException e) {
            String errorMessage = String.format("Failed to save ClientDirectory for Client [%s]",
                    entities.get(0).getClient().getId());
            throw new LnFException(errorMessage);
        }
    }

    public byte[] clientDirectoryExcel(UUID clientId) {
        List<ClientDirectoryDto> summaries = findByClientId(clientId);
        ClientDirectoryExcelDto excelSpreadSheetDto = createExcelSpreadSheetDto(summaries);
        return excelReportService.generateReport(createExcelReportDto(excelSpreadSheetDto));
    }

    public byte[] clientDirectoryExcel() {
        List<ClientDirectoryDto> summaries = findAll();
        ClientDirectoryExcelDto excelSpreadSheetDto = createExcelSpreadSheetDto(summaries);
        return excelReportService.generateReport(createExcelReportDto(excelSpreadSheetDto));
    }

    private ClientDirectoryExcelDto createExcelSpreadSheetDto(List<ClientDirectoryDto> dto) {
        ClientDirectoryExcelDto excelSpreadSheetDto = new ClientDirectoryExcelDto();
        excelSpreadSheetDto.setDynamicData(dto);
        excelSpreadSheetDto.setFileName(CLIENT_EXCEL_FILE);

        return excelSpreadSheetDto;
    }

    private ExcelReportDto<ClientDirectoryExcelDto> createExcelReportDto(ClientDirectoryExcelDto clientDirectoryDto) {
        ExcelReportDto<ClientDirectoryExcelDto> dto = new ExcelReportDto<>();
        dto.setReportType(ReportType.CLIENT_DIRECTORY);
        dto.setReportData(clientDirectoryDto);
        return dto;
    }

    public byte[] downloadClientDirectoryAsPdf() {
        List<ClientDirectoryDto> clientDirectoryDtos = findAll();
        return generatePdfFromVendorDirectoryDtos(clientDirectoryDtos, FILE_NAME);
    }

    public byte[] downloadClientDirectoryAsPdf(UUID clientId) {
        List<ClientDirectoryDto> clientDirectoryDtos = findByClientId(clientId);
        return generatePdfFromVendorDirectoryDtos(clientDirectoryDtos, FILE_NAME);
    }

    private byte[] generatePdfFromVendorDirectoryDtos(List<ClientDirectoryDto> summaries, String fileName) {
        Map<String, Object> dynamicData = new HashMap<>();
        dynamicData.put("listObjects", summaries);

        ThymeleafDocumentDto thymeleafDocumentDto = new ThymeleafDocumentDto();
        thymeleafDocumentDto.setTemplateName("client-directory");
        thymeleafDocumentDto.setFileName(fileName);
        thymeleafDocumentDto.setDynamicData(dynamicData);

        return documentService.generatePdf(thymeleafDocumentDto);
    }

}
