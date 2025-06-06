package com.lnf.client.restapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lnf.dto.audit.AuditRecordDto;
import com.lnf.service.audit.AuditLoggerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "lnf.audit.enabled", havingValue = "true",matchIfMissing = false)
public class AuditLoggerImpl implements AuditLoggerService {

    private final AuditClientImpl auditClient;
    private final ObjectMapper objectMapper;
    @Value("${spring.application.name}")
    private String applicationName;

    @Override
    public void log(String serviceName, String action, String entityId, String performedBy, String payloadJson) {
        try {
            JsonNode payload = objectMapper.readTree(payloadJson);
            String details = payload.has("details") ? payload.get("details").asText(null) : null;

            AuditRecordDto dto = AuditRecordDto.builder()
                    .module(applicationName.split("-")[1])
                    .action(action)
                    .entityId(entityId)
                    .performedBy(performedBy != null ? performedBy : "SYSTEM")
                    .inputPayload(payload)
                    .details(details)
                    .build();

            auditClient.createAuditLog(dto);
        } catch (Exception e) {
            log.error("Failed to send audit log", e);
        }
    }
}