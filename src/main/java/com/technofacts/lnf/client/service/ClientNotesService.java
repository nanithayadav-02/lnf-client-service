package com.technofacts.lnf.client.service;

import com.google.common.collect.Lists;
import com.technofacts.lnf.client.converter.ClientNotesConverter;
import com.technofacts.lnf.client.model.Client;
import com.technofacts.lnf.client.model.ClientNotes;
import com.technofacts.lnf.client.repository.ClientNotesRepository;
import com.technofacts.lnf.client.repository.ClientRepository;
import com.technofacts.lnf.dto.client.ClientNotesDto;
import com.technofacts.lnf.dto.common.PageRequestDto;
import com.technofacts.lnf.exception.LnFBadRequestException;
import com.technofacts.lnf.exception.LnFEntityNotFoundException;
import com.technofacts.lnf.exception.LnFException;
import com.technofacts.lnf.service.common.page.PaginatedAndSortedService;
import com.technofacts.lnf.service.specification.GenericSpecificationBuilder;
import com.technofacts.lnf.util.RestUtil;
import com.technofacts.lnf.util.specification.SpecificationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ClientNotesService implements PaginatedAndSortedService<ClientNotesDto> {

    private final ClientNotesRepository repository;
    private final ClientRepository clientRepository;

    @Override
    public Page<ClientNotesDto> findPaginatedAndSorted(int page, int size, String sortBy, String sortOrder) {
        final Sort sortInfo = RestUtil.constructSort(sortBy, sortOrder);
        Page<ClientNotes> resultPage = repository.findAll(PageRequest.of(page, size, sortInfo));
        return validateAndGetPages(page, resultPage);
    }

    @Override
    public Page<ClientNotesDto> findPaginated(int page, int size) {
        Page<ClientNotes> resultPage = repository.findAll(PageRequest.of(page, size));
        return validateAndGetPages(page, resultPage);
    }

    @Override
    public List<ClientNotesDto> findAllSorted(String sortBy, String sortOrder) {
        final Sort sortInfo = RestUtil.constructSort(sortBy, sortOrder);
        List<ClientNotes> entities = Lists.newArrayList(repository.findAll(sortInfo));
        return entities.stream().map(ClientNotesConverter::toTransportModel)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public List<ClientNotesDto> findAll() {
        List<ClientNotes> entities = repository.findAll();
        return entities.stream().map(ClientNotesConverter::toTransportModel).filter(Objects::nonNull).toList();
    }

    public List<ClientNotesDto> findAll(String search) {
        Specification<ClientNotes> specification = buildClientNotesSpecification(search);
        List<ClientNotes> entities = repository.findAll(specification);
        return convertToDtos(entities);
    }

    private List<ClientNotesDto> convertToDtos(List<ClientNotes> entities) {
        return entities.stream()
                .map(ClientNotesConverter::toTransportModel)
                .filter(Objects::nonNull)
                .toList();
    }

    private Page<ClientNotesDto> validateAndGetPages(int page, Page<ClientNotes> resultPage) {
        if (page > resultPage.getTotalPages()) {
            throw new LnFEntityNotFoundException(String.format("Total number of pages [%d], " +
                    "requested page [%d] does not exist", resultPage.getTotalPages(), page));
        }
        return resultPage.map(ClientNotesConverter::toTransportModel);
    }

    public Page<ClientNotesDto> findingAllWithPagination(String search, PageRequestDto pageRequestDto) {
        Pageable pageable = PageRequest.of(pageRequestDto.getPage(), pageRequestDto.getSize(),
                RestUtil.constructSort(pageRequestDto.getSortBy(), pageRequestDto.getSortOrder()));
        Specification<ClientNotes> specification = buildClientNotesSpecification(search);
        Page<ClientNotes> resultPage = repository.findAll(specification, pageable);
        return resultPage.map(ClientNotesConverter::toTransportModel);
    }

    private Specification<ClientNotes> buildClientNotesSpecification(String search) {
        GenericSpecificationBuilder<ClientNotes> applicantNotesBuilder = new GenericSpecificationBuilder<>();
        Function<String, Class<?>> fieldClassForClientNotes = this::getFieldClassFromNotes;
        return SpecificationUtil.buildSpecification(search, applicantNotesBuilder, fieldClassForClientNotes);
    }

    private Class<?> getFieldClassFromNotes(String fieldName) {
        return SpecificationUtil.getFieldClass(ClientNotes.class, fieldName);
    }

    public List<ClientNotesDto> findByClientId(UUID clientId) {
        searchForClient(clientId);
        List<ClientNotes> entities = repository.findByClientId(clientId);
        return entities.stream().map(ClientNotesConverter::toTransportModel).filter(Objects::nonNull).toList();
    }

    public ClientNotesDto findById(UUID clientId, UUID notesId) {
        searchForClient(clientId);
        return ClientNotesConverter.toTransportModel(searchForNotes(notesId));
    }

    public void create(UUID clientId, List<ClientNotesDto> resource) {
        LnFBadRequestException.throwOnCondition(Objects::isNull, resource,
                String.format("Failed to create Notes for client [%s] with null payload", clientId));
        Client client = searchForClient(clientId);
        List<ClientNotes> entities = new ArrayList<>();
        resource.stream().filter(Objects::nonNull).forEach(notesDto -> {
            ClientNotes entity = ClientNotesConverter.toEntityModel(notesDto, new ClientNotes());
            entity.setClient(client);
            entities.add(entity);
        });
        save(entities);
        log.debug("Notes for Client[" + clientId + "] successfully created");
    }

    public void create(UUID clientId, ClientNotesDto resource) {
        LnFBadRequestException.throwOnCondition(Objects::isNull, resource, "Failed to create Notes with null payload");
        Client employeeEntity = searchForClient(clientId);
        ClientNotes entity = ClientNotesConverter.toEntityModel(resource, new ClientNotes());
        entity.setClient(employeeEntity);
        save(entity);
        log.debug("Notes for Client {} successfully created", clientId);
    }

    public void update(UUID clientId, UUID notesId, ClientNotesDto resource) {
        LnFBadRequestException.throwOnCondition(Objects::isNull, resource, "Failed to update Notes with null payload");
        searchForClient(clientId);
        ClientNotes entity = searchForNotes(notesId);
        save(ClientNotesConverter.toEntityModel(resource, entity));
        log.debug("Notes for Client {} successfully created", clientId);
    }

    public void deleteById(UUID clientId, UUID notesId) {
        searchForClient(clientId);
        ClientNotes entity = searchForNotes(notesId);
        try {
            repository.delete(entity);
            log.debug("Notes {} for client {} successfully deleted", notesId, clientId);
        } catch (RuntimeException e) {
            String errorMessage = String.format("Failed to delete Notes[[%s] for client [%s]", notesId, clientId);
            throw new LnFException(errorMessage);
        }
    }

    public void deleteByClientId(UUID clientId) {
        searchForClient(clientId);
        List<ClientNotes> entities = repository.findByClientId(clientId);
        try {
            repository.deleteAll(entities);
            log.debug("Notes for Client {} successfully deleted", clientId);
        } catch (RuntimeException e) {
            String errorMessage = String.format("Failed to delete Notes for client [%s]", clientId);
            throw new LnFException(errorMessage);
        }
    }

    private void save(ClientNotes entity) {
        try {
            repository.save(entity);
        } catch (RuntimeException e) {
            String errorMessage = String.format("Failed to save Notes for client [%s]", entity.getClient().getId());
            throw new LnFException(errorMessage);
        }
    }

    private void save(List<ClientNotes> entities) {
        try {
            repository.saveAll(entities);
        } catch (RuntimeException e) {
            String errorMessage = String.format("Failed to save Notes for client [%s]", entities.get(0).getClient().getId());
            throw new LnFException(errorMessage);
        }
    }

    private Client searchForClient(UUID clientId) {
        return clientRepository.findByClientId(clientId).
                orElseThrow(() -> new LnFEntityNotFoundException(String.format("Client with id [%s] does not exist", clientId)));
    }

    private ClientNotes searchForNotes(UUID notesId) {
        return repository.findById(notesId).
                orElseThrow(() -> new LnFEntityNotFoundException(String.format("Notes with id [%s] does not exist", notesId)));
    }

}
