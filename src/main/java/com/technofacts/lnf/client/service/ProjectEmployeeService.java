package com.technofacts.lnf.client.service;

import java.util.List;
import java.util.UUID;

import com.technofacts.lnf.client.model.Project;
import com.technofacts.lnf.client.model.ProjectEmployee;
import com.technofacts.lnf.client.repository.ProjectEmployeeRepository;
import com.technofacts.lnf.client.repository.ProjectRepository;
import com.technofacts.lnf.dto.client.ProjectEmployeeDto;
import com.technofacts.lnf.exception.LnFEntityNotFoundException;
import com.technofacts.lnf.exception.LnFException;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Log
public class ProjectEmployeeService {

    private final ProjectEmployeeRepository repository;
    private final ProjectRepository projectRepository;

    /**
     * Get employees associated to the project
     *
     * @param projectId Project Id
     * @return ProjectEmployeeDto
     */
    public ProjectEmployeeDto findEmployeesByProjectId(final UUID projectId) {
        return null;
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
                ProjectEmployee projectEmployee = new ProjectEmployee();
                projectEmployee.setProject(project);
                projectEmployee.setEmployeeId(employeeId);
                save(projectEmployee);
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
        Project project = searchForProject(projectId);
        employeeIds.forEach(employeeId -> {
            try {
                search(projectId, employeeId);
                ProjectEmployee projectEmployee = new ProjectEmployee();
                projectEmployee.setProject(project);
                projectEmployee.setEmployeeId(employeeId);
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
        return repository.findByProjectCodeAndEmployeeId(projectId, employeeId).orElseThrow(() -> new LnFEntityNotFoundException(String.format("ProjectEmployee entity with projectId [%s] and employeeId [%s] does not exist", projectId, employeeId)));
    }

    private Project searchForProject(UUID projectId) {
        return projectRepository.findById(projectId).orElseThrow(() -> new LnFEntityNotFoundException(String.format("Project with id [%s] does not exist", projectId)));
    }


}
