package com.digitalservicing.usermanager.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * Calls the messagemanager service over the cluster-internal Service DNS name
 * (e.g. http://messagemanager:8080 within senthil-apis, or the FQDN across namespaces)
 * rather than anything Twilio-specific -- messagemanager owns the actual Twilio call.
 */
@Service
@Slf4j
public class MessageManagerClient {

    private final RestClient restClient;

    public MessageManagerClient(RestClient.Builder restClientBuilder,
                                 @Value("${messagemanager.base-url}") String baseUrl) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
    }

    public void sendSms(String toNumber, String body) {
        log.info("Sending SMS via messagemanager to {}", toNumber);
        restClient.post()
                .uri("/api/v1/messages")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .body(new SendMessageRequest(toNumber, body))
                .retrieve()
                .toBodilessEntity();
    }

    private record SendMessageRequest(String toNumber, String body) {
    }
}
