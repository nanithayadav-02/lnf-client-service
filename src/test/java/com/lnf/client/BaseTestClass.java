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

package com.lnf.client;

import com.lnf.client.service.*;
import com.lnf.service.common.page.PaginationAndSortingHandler;
import org.junit.jupiter.api.TestInstance;
import org.mockito.InjectMocks;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ContextConfiguration
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BaseTestClass {

    @MockBean
    public AddressService addressService;

    @MockBean
    public ClientContactService clientContactService;

    @MockBean
    public ClientService clientService;

    @MockBean
    public ClientNotesService clientNotesService;

    @MockBean
    public DashboardService dashboardService;

    @MockBean
    public EmployeeProjectService employeeProjectService;

    @MockBean
    public EmployeeProjectTaskService employeeProjectTaskService;

    @MockBean
    public EmployeeTaskService employeeTaskService;

    @MockBean
    public EscalationService escalationService;

    @MockBean
    public GstService gstService;

    @MockBean
    public ProjectService projectService;

    @MockBean
    public ProjectEmployeeService projectEmployeeService;

    @MockBean
    public ProjectTaskEmployeeService projectTaskEmployeeService;

    @MockBean
    public TaskService taskService;

    @InjectMocks
    private PaginationAndSortingHandler paginationAndSortingHandler;

    @MockBean
    private ClientDirectoryService clientDirectoryService;

    @MockBean
    private ClientDetailsService clientDetailsService;

    @MockBean
    private EmployeeProjectAndTaskService employeeProjectAndTaskService;

}
