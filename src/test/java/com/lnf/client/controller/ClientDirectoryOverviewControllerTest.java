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

package com.lnf.client.controller;

import com.lnf.client.BaseTestClass;
import com.lnf.client.service.ClientDirectoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.OK;

class ClientDirectoryOverviewControllerTest extends BaseTestClass {

    @Mock
    private ClientDirectoryService service;

    @InjectMocks
    private ClientDirectoryOverviewController controller;

    @BeforeEach
    void setUp() {
    }

    @Test
    void testClientDirectoryExcelWithClientId() {
        UUID clientId = UUID.randomUUID();
        byte[] excelBytes = new byte[]{1, 2, 3};
        when(service.clientDirectoryExcel(clientId)).thenReturn(excelBytes);

        ResponseEntity<byte[]> response = controller.clientDirectoryExcel(clientId);

        assertEquals(OK, response.getStatusCode());
        assertArrayEquals(excelBytes, response.getBody());

        HttpHeaders headers = response.getHeaders();
        // Corrected expected Content-Disposition format
        assertEquals("form-data; name=\"attachment\"; filename=\"client-directory-.xlsx\"",
                headers.getFirst(HttpHeaders.CONTENT_DISPOSITION));
        assertEquals(MediaType.APPLICATION_OCTET_STREAM, headers.getContentType());
    }

    @Test
    void testClientDirectoryExcelWithoutClientId() {
        byte[] excelBytes = new byte[]{1, 2, 3};
        when(service.clientDirectoryExcel()).thenReturn(excelBytes);

        ResponseEntity<byte[]> response = controller.clientDirectoryExcel();

        assertEquals(OK, response.getStatusCode());
        assertArrayEquals(excelBytes, response.getBody());
        HttpHeaders headers = response.getHeaders();
        assertEquals("form-data; name=\"attachment\"; filename=\"client-directory-.xlsx\"",
                headers.getFirst(HttpHeaders.CONTENT_DISPOSITION));
        assertEquals(MediaType.APPLICATION_OCTET_STREAM, headers.getContentType());
    }

    @Test
    void testDownloadClientDirectoryAsPdf() {
        byte[] pdfBytes = new byte[]{4, 5, 6};
        when(service.downloadClientDirectoryAsPdf()).thenReturn(pdfBytes);

        ResponseEntity<byte[]> response = controller.downloadClientDirectoryAsPdf();

        assertEquals(OK, response.getStatusCode());
        assertArrayEquals(pdfBytes, response.getBody());
        HttpHeaders headers = response.getHeaders();
        assertEquals("attachment; filename=clientDirectory.pdf", headers.getFirst(HttpHeaders.CONTENT_DISPOSITION));
        assertEquals(MediaType.APPLICATION_PDF, headers.getContentType());
    }

    @Test
    void testDownloadClientDirectoryAsPdfWithClientId() {
        UUID clientId = UUID.randomUUID();
        byte[] pdfBytes = new byte[]{4, 5, 6};
        when(service.downloadClientDirectoryAsPdf(clientId)).thenReturn(pdfBytes);

        ResponseEntity<byte[]> response = controller.downloadClientDirectoryAsPdf(clientId);

        assertEquals(OK, response.getStatusCode());
        assertArrayEquals(pdfBytes, response.getBody());
        HttpHeaders headers = response.getHeaders();
        assertEquals("attachment; filename=clientDirectory.pdf", headers.getFirst(HttpHeaders.CONTENT_DISPOSITION));
        assertEquals(MediaType.APPLICATION_PDF, headers.getContentType());
    }

}
