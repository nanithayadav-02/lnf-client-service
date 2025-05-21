package com.lnf.client.restapi;

import com.lnf.dto.audit.AuditRecordDto;
import com.lnf.exception.LnFException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@Transactional
@Slf4j
public class AuditClientImpl extends BaseWebClientService {

    private final WebClient webClient;

    public AuditClientImpl(@Qualifier("auditWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public void createAuditLog(AuditRecordDto resource) {
        try {
            WebClient.RequestHeadersSpec<?> requestSpec = webClient.post()
                    .uri("/lnf/audit-log")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(resource));

            addJwtToken(requestSpec);

            requestSpec.retrieve()
                    .onStatus(
                            status -> status.is4xxClientError() || status.is5xxServerError(),
                            response -> response.bodyToMono(String.class).flatMap(body -> {
                                log.error("❌ Audit service error ({}): {}", response.statusCode(), body);
                                return Mono.error(new RuntimeException("Audit log rejected by audit service"));
                            })
                    )
                    .bodyToMono(Void.class)
                    .doOnSuccess(v -> log.debug("✅ Audit log successfully sent"))
                    .doOnError(e -> log.error("❌ Audit log failed: {}", e.getMessage(), e))
                    .subscribe();

        } catch (RuntimeException e) {
            throw new LnFException("Unexpected error occurred while sending audit log", e);
        }
    }
}
