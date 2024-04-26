package com.technofacts.lnf.client.service;

import com.technofacts.lnf.client.model.Project;
import com.technofacts.lnf.client.model.ProjectEmployee;
import com.technofacts.lnf.client.repository.ProjectEmployeeRepository;
import com.technofacts.lnf.client.repository.ProjectRepository;
import com.technofacts.lnf.dto.client.ProjectEmployeeDto;
import com.technofacts.lnf.dto.employee.EmployeeDto;
import com.technofacts.lnf.exception.LnFEntityNotFoundException;
import com.technofacts.lnf.exception.LnFException;
import com.technofacts.lnf.service.employee.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.logging.Level;

@Service
@Transactional
@RequiredArgsConstructor
@Log
public class ProjectEmployeeService {

    private final ProjectEmployeeRepository repository;
    private final ProjectRepository projectRepository;
    private final EmployeeService employeeService;

    /**
     * Get employees associated to the project
     *
     * @param projectId Project Id
     * @return ProjectEmployeeDto
     */
    public ProjectEmployeeDto findEmployeesByProjectId(final UUID projectId) {
        // Search if the projectId exists otherwise throw LnFEntityNotFoundException
        Project project = searchForProject(projectId);

        // Get the list of employees associated with the project
        List<ProjectEmployee> projectEmployees = findProjectEmployees(project);
        List<String> employeeIds = projectEmployees.stream().map(ProjectEmployee::getEmployeeId).toList();

        // Get the list of employee details from the Employee microservice
        List<EmployeeDto> employeeDtos = employeeService.findByEmployeeIds(employeeIds);

        return createProjectEmployeeDto(project,employeeDtos);

    }

    private List<ProjectEmployee>  findProjectEmployees(Project project) {

        List<ProjectEmployee> projectEmployees = repository.findByProject(project);
        return projectEmployees;
    }

    private ProjectEmployeeDto createProjectEmployeeDto(Project project, List<EmployeeDto> employeeDtos) {

        ProjectEmployeeDto projectEmployeeDto = new ProjectEmployeeDto();
        projectEmployeeDto.setProjectId(project.getId());
        projectEmployeeDto.setProjectCode(project.getCode());
        projectEmployeeDto.setEmployees(employeeDtos);

        return projectEmployeeDto;
    }

    public Map<String,Object> findAllAssignedEmployees(final UUID projectId, int page, Integer size) {
        // Search if the projectId exists otherwise throw LnFEntityNotFoundException
        Project project = searchForProject(projectId);

        // Get the list of employees associated with the project
        List<ProjectEmployee> projectEmployees = findProjectEmployees(project);

        int totalEmployees = projectEmployees.size();
        if(size == null){
            size=totalEmployees;
        }
        // Calculate pagination parameters
        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, totalEmployees);

        List<ProjectEmployee> pagedEmployees = projectEmployees.subList(startIndex, endIndex);

        // Get the list of employee details from the Employee microservice
        List<String> employeeIds = pagedEmployees.stream().map(ProjectEmployee::getEmployeeId).toList();
        List<EmployeeDto> employeeDtos = employeeService.findByEmployeeIds(employeeIds);

        // Return the ProjectEmployeeDtos with pagination information
        ProjectEmployeeDto projectEmployeeDto = createProjectEmployeeDto(project,employeeDtos);

        return createPaginationContent(totalEmployees,size,projectEmployeeDto);

    }

    private Map<String, Object> createPaginationContent(int totalEmployees, Integer size,
                                                        ProjectEmployeeDto projectEmployeeDto) {

        int totalPages = totalEmployees / size != 0 ? (totalEmployees % size) + 1 : (totalEmployees % size);

        Map<String,Object> result=new HashMap<>();
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
    public void addEmployeeToProject(UUID projectId, List<String> employeeIds) {

        Project project = searchForProject(projectId);
        employeeIds.forEach(employeeId -> {
            try {
                search(projectId, employeeId);
                log.info(String.format("Employee[%s] is already associated to the projectId [%s]", employeeId, projectId));

            } catch (LnFEntityNotFoundException ex) {
                EmployeeDto employeeDto = employeeService.findOne(employeeId);
                if (employeeDto != null) {
                    ProjectEmployee projectEmployee = new ProjectEmployee();
                    projectEmployee.setProject(project);
                    projectEmployee.setEmployeeId(employeeId);
                    save(projectEmployee);
                    log.log(Level.INFO, String.format("Successfully added the employee [%s] to the project [%s]", employeeId, project.getCode()));
                } else {
                    log.log(Level.SEVERE, String.format("Failed to add the employee [%s] to the project [%s]", employeeId, project.getCode()));
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
    public void removeEmployeeFromProject(UUID projectId, List<String> employeeIds) {
        searchForProject(projectId);
        employeeIds.forEach(employeeId -> {
            try {
                ProjectEmployee projectEmployee = search(projectId, employeeId);
                repository.delete(projectEmployee);
            } catch (LnFEntityNotFoundException ex) {
                log.warning(ex.getMessage());
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
            log.info(() -> String.format("projectEmployees is successfully removed from the Project[%s]", project.getId()));
        } catch (RuntimeException e) {
            String errorMessage = String.format("Failed to remove projectEmployees from the Project[%s]", project.getId());
            throw new LnFException(errorMessage, e);
        }
    }

    public void delete(ProjectEmployee entity) {
        try {
            repository.delete(entity);
            log.info(() -> String.format("Employee [%s] is successfully removed from the Project[%s]", entity.getEmployeeId(), entity.getProject().getCode()));
        } catch (RuntimeException e) {
            String errorMessage = String.format("Failed to remove Employee [%s] from the Project[%s]", entity.getEmployeeId(), entity.getProject().getCode());
            throw new LnFException(errorMessage, e);
        }
    }

    private ProjectEmployee search(UUID projectId, String employeeId) {
        return repository.findByProjectIdAndEmployeeId(projectId, employeeId).orElseThrow(() -> new LnFEntityNotFoundException(String.format("ProjectEmployee entity with projectId [%s] and employeeId [%s] does not exist", projectId, employeeId)));
    }

    private Project searchForProject(UUID projectId) {
        return projectRepository.findById(projectId).orElseThrow(() -> new LnFEntityNotFoundException(String.format("Project with id [%s] does not exist", projectId)));
    }

    public void addAllActiveEmployeeToProject(UUID projectId,List<String> statuses) {
        Project project = searchForProject(projectId);
        List<String> requiredIds = new ArrayList<>(employeeService.findByStatuses(statuses));
        requiredIds.removeAll(repository.findAllByProjectId(projectId)
                .stream().map(ProjectEmployee::getEmployeeId).toList());

        requiredIds.parallelStream().forEach(employeeId -> {

            ProjectEmployee projectEmployee = new ProjectEmployee();
            projectEmployee.setProject(project);
            projectEmployee.setEmployeeId(employeeId);
            save(projectEmployee);
            log.log(Level.INFO, String.format("Successfully added the employee [%s] to the project [%s]", employeeId, project.getCode()));
        });
    }
}
