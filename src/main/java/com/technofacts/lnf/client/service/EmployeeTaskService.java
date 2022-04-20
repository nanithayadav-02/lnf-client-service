package com.technofacts.lnf.client.service;

import java.util.ArrayList;
import java.util.List;

import com.technofacts.lnf.dto.client.EmployeeProjectDto;
import com.technofacts.lnf.dto.client.EmployeeProjectTaskDto;
import com.technofacts.lnf.dto.client.EmployeeProjectTasksDto;
import com.technofacts.lnf.dto.client.ProjectTasksDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Service
@Transactional
@RequiredArgsConstructor
@Log
public class EmployeeTaskService {

    private final EmployeeProjectService employeeProjectService;
    private final EmployeeProjectTaskService employeeProjectTaskService;
    private static final String ACTIVE = "Active";

    public EmployeeProjectTasksDto findTasksByEmployeeId(final String employeeId) {

        EmployeeProjectTasksDto employeeProjectTasksDto = new EmployeeProjectTasksDto();
        employeeProjectTasksDto.setEmployeeId(employeeId);
        EmployeeProjectDto employeeProjectDto =  employeeProjectService.findProjectsByEmployeeId(employeeId);
        List<ProjectTasksDto> projectTaskDtos = new ArrayList<>();
        employeeProjectDto.getProjects().stream()
                .filter(projectDto -> ACTIVE.equalsIgnoreCase(projectDto.getStatus()))
                .forEach(projectDto -> {
                    ProjectTasksDto projectTaskDto = new ProjectTasksDto();
                    projectTaskDto.setProjectId(projectDto.getId());
                    projectTaskDto.setProjectCode(projectDto.getCode());
                    projectTaskDto.setProjectName(projectDto.getName());
                    projectTaskDto.setProjectType(projectDto.getType());
                    EmployeeProjectTaskDto employeeProjectTaskDto =
                            employeeProjectTaskService.findTasksByEmployeeIdAndProjectId(employeeId, projectDto.getId());
                    if (!CollectionUtils.isEmpty(employeeProjectTaskDto.getTasks())) {
                        projectTaskDto.getTasks().addAll(employeeProjectTaskDto.getTasks());
                    }
                    projectTaskDtos.add(projectTaskDto);
                });
        employeeProjectTasksDto.setProjectTasks(projectTaskDtos);
        return employeeProjectTasksDto;

    }

}
