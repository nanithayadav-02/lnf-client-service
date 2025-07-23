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

import com.lnf.client.converter.ProjectConverter;
import com.lnf.client.model.Project;
import com.lnf.client.model.ProjectEmployee;
import com.lnf.client.repository.ProjectEmployeeRepository;
import com.lnf.client.repository.ProjectRepository;
import com.lnf.dto.client.ClientEmployeeDto;
import com.lnf.dto.client.ProjectEmployeeDto;
import com.lnf.dto.employee.EmployeeDto;
import com.lnf.exception.LnFEntityNotFoundException;
import com.lnf.exception.LnFException;
import com.lnf.service.employee.EmployeeService;
import com.lnf.tenant.core.context.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ProjectEmployeeService {

    private final ProjectEmployeeRepository repository;
    private final ProjectRepository projectRepository;
    private final EmployeeService employeeService;
    private final CacheManager cacheManager;

    @Value("${lnf.tenant.enabled:true}")
    private boolean tenantEnabled;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * Get employees associated to the project
     *
     * @param projectEmpId projectEmpId
     * @return ProjectEmployeeDto
     */
    @Cacheable(
            value = "projectEmployees",
            key = "T(com.lnf.tenant.core.util.CacheKeyUtils).tenantAwareKey(#projectEmpId)"
    )
    public ProjectEmployeeDto findEmployeesByProjectId(final UUID projectEmpId) {
        Project project = searchForProject(projectEmpId);

        // Get the list of employees associated with the project
        List<ProjectEmployee> employees = repository.findByProject(project);
        List<ClientEmployeeDto> employeeDtos = fetchEmployeeDetails(employees);

        return createProjectEmployeeDto(project, employeeDtos);
    }

    public Map<String, Object> findAllAssignedEmployees(final UUID projectId, int page, Integer size) {
        Project project = searchForProject(projectId);
        List<ProjectEmployee> employees = repository.findByProject(project);
        size = (size == null || size <= 0) ? employees.size() : size;
        List<ClientEmployeeDto> employeeDtos = fetchEmployeeDetails(paginateEmployees(employees, page, size));
        ProjectEmployeeDto dto = createProjectEmployeeDto(project, employeeDtos);
        return createPaginationContent(employees.size(), size, dto);
    }

    private ProjectEmployeeDto createProjectEmployeeDto(Project project, List<ClientEmployeeDto> employeeDtos) {
        ProjectEmployeeDto projectEmployeeDto = new ProjectEmployeeDto();
        projectEmployeeDto.setProjectId(project.getId());
        projectEmployeeDto.setProjectCode(project.getCode());
        projectEmployeeDto.setEmployees(employeeDtos);

        return projectEmployeeDto;
    }

    private List<ProjectEmployee> paginateEmployees(List<ProjectEmployee> employees, int page, int size) {
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, employees.size());
        return employees.subList(fromIndex, toIndex);
    }

    private List<ClientEmployeeDto> fetchEmployeeDetails(List<ProjectEmployee> pagedEmployees) {
        List<String> employeeIds = pagedEmployees.stream()
                .map(ProjectEmployee::getEmployeeId)
                .toList();
        return employeeService.findByEmployeeIds(employeeIds).stream()
                .map(ProjectConverter::mapToClientEmployee)
                .filter(Objects::nonNull)
                .toList();
    }

    private Map<String, Object> createPaginationContent(int totalEmployees, Integer size,
                                                        ProjectEmployeeDto projectEmployeeDto) {
        int totalPages = (totalEmployees + size - 1) / size;

        Map<String, Object> result = new HashMap<>();
        result.put("ProjectEmployees", projectEmployeeDto);
        result.put("totalElements", totalEmployees);
        result.put("totalPages", totalPages);
        return result;
    }

    /**
     * Add employees to the project
     *
     * @param projectId   Project Id
     * @param employeeIds List of Strings
     */
    @CacheEvict(
            value = "projectEmployees",
            key = "T(com.lnf.tenant.core.util.CacheKeyUtils).tenantAwareKey(#projectId)"
    )
    public void addEmployeeToProject(UUID projectId, List<String> employeeIds) {

        Project project = searchForProject(projectId);
        employeeIds.forEach(employeeId -> {
            try {
                search(projectId, employeeId);
                log.debug("Employee {} is already associated to the projectId {}", employeeId, projectId);

            } catch (LnFEntityNotFoundException ex) {
                EmployeeDto employeeDto = employeeService.findOne(employeeId);
                if (employeeDto != null) {
                    ProjectEmployee projectEmployee = new ProjectEmployee();
                    projectEmployee.setProject(project);
                    projectEmployee.setEmployeeId(employeeId);
                    save(projectEmployee);
                    log.debug("Successfully added the employee {} to the project {}", employeeId, project.getCode());
                } else {
                    log.error("Failed to add the employee {} to the project {}", employeeId, project.getCode());
                }
            }
        });
    }

    /**
     * Remove employees from
     * the project
     *
     * @param projectId   Project Id
     * @param employeeIds List of Strings
     */
    @CacheEvict(
            value = "projectEmployees",
            key = "T(com.lnf.tenant.core.util.CacheKeyUtils).tenantAwareKey(#projectId.toString())"
    )
    public void removeEmployeeFromProject(UUID projectId, List<String> employeeIds) {
        searchForProject(projectId);
        employeeIds.forEach(employeeId -> {
            try {
                ProjectEmployee projectEmployee = search(projectId, employeeId);
                repository.delete(projectEmployee);
            } catch (LnFEntityNotFoundException ex) {
                log.warn(ex.getMessage());
            }
        });
    }

    private void save(ProjectEmployee entity) {
        try {
            repository.save(entity);
        } catch (RuntimeException e) {
            String errorMessage = String.format("Failed to save Project [%s] and employeeId [%s]", entity.getProject().getCode(), entity.getEmployeeId());
            throw new LnFException(errorMessage);
        }
    }

    public void deleteProjectEmployeesByProject(Project project) {
        List<ProjectEmployee> projectEmployees = repository.findByProject(project);
        try {
            repository.deleteAll(projectEmployees);
            log.debug("projectEmployees is successfully removed from the Project {}", project.getId());
        } catch (RuntimeException e) {
            String errorMessage = "Failed to remove projectEmployees from the Project[%s]".formatted(project.getId());
            throw new LnFException(errorMessage, e);
        }
    }

    public void delete(ProjectEmployee entity) {
        try {
            repository.delete(entity);
            log.debug("Employee {} is successfully removed from the Project {}", entity.getEmployeeId(), entity.getProject().getCode());
        } catch (RuntimeException e) {
            String errorMessage = String.format("Failed to remove Employee [%s] from the Project[%s]", entity.getEmployeeId(), entity.getProject().getCode());
            throw new LnFException(errorMessage, e);
        }
    }

    /**
     * Clears the cache for projectEmployees.
     */
    public void clearProjectEmployeesCache() {
        String tenantId = TenantContext.getCurrentTenant();

        // If tenant mode is disabled or Redis is not used, clear in-memory caches
        if (!tenantEnabled || !(cacheManager instanceof RedisCacheManager)) {
            log.info("Clearing all in-memory caches (Redis disabled or tenant mode off).");
            cacheManager.getCacheNames().forEach(name -> {
                Cache cache = cacheManager.getCache(name);
                if (cache != null) {
                    cache.clear();
                }
            });
            return;
        }

        // Skip Redis cache clearing if tenant context is missing
        if (!StringUtils.hasText(tenantId)) {
            log.warn("TenantContext is not set. Skipping Redis cache clearing.");
            return;
        }

        log.info("Clearing Redis cache for tenant '{}'", tenantId);

        String pattern = "*::" + tenantId + ":*";

        Set<String> keysToDelete = redisTemplate.execute((RedisCallback<Set<String>>) connection -> {
            Set<String> keys = new HashSet<>();
            try (Cursor<byte[]> cursor = connection.scan(
                    ScanOptions.scanOptions().match(pattern).count(1000).build())) {
                cursor.forEachRemaining(key -> keys.add(new String(key, StandardCharsets.UTF_8)));
            }
            return keys;
        });

        if (!CollectionUtils.isEmpty(keysToDelete)) {
            redisTemplate.delete(keysToDelete);
            log.info("Deleted {} Redis keys for tenant '{}'", keysToDelete.size(), tenantId);
        } else {
            log.info("No Redis keys found for tenant '{}'", tenantId);
        }
    }

    private ProjectEmployee search(UUID projectId, String employeeId) {
        return repository.findByProjectIdAndEmployeeId(projectId, employeeId).orElseThrow(() -> new LnFEntityNotFoundException("ProjectEmployee entity with projectId [%s] and employeeId [%s] does not exist".formatted(projectId, employeeId)));
    }

    private Project searchForProject(UUID projectId) {
        return projectRepository.findById(projectId).orElseThrow(() -> new LnFEntityNotFoundException("Project with id [%s] does not exist".formatted(projectId)));
    }

    @CacheEvict(
            value = "projectEmployees",
            key = "T(com.lnf.tenant.core.util.CacheKeyUtils).tenantAwareKey(#projectId)"
    )
    public void addAllActiveEmployeeToProject(UUID projectId, List<String> statuses) {
        Project project = searchForProject(projectId);
        List<String> requiredIds = new ArrayList<>(employeeService.findByStatuses(statuses));
        requiredIds.removeAll(repository.findAllByProjectId(projectId)
                .stream().map(ProjectEmployee::getEmployeeId).toList());

        requiredIds.parallelStream().forEach(employeeId -> {

            ProjectEmployee projectEmployee = new ProjectEmployee();
            projectEmployee.setProject(project);
            projectEmployee.setEmployeeId(employeeId);
            save(projectEmployee);
            log.debug("Successfully added the employee {} to the project {}", employeeId, project.getCode());
        });
    }
}
