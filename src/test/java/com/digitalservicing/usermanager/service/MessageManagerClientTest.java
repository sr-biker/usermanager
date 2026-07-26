package com.digitalservicing.usermanager.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Verifies the HTTP request MessageManagerClient sends to messagemanager, without a
 * real network call -- MockRestServiceServer intercepts at the ClientHttpRequestFactory
 * level.
 */
class MessageManagerClientTest {

    private MockRestServiceServer mockServer;
    private MessageManagerClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        client = new MessageManagerClient(builder, "http://messagemanager:8080");
    }

    @Test
    void sendSms_postsToMessagesEndpoint_withToNumberAndBody() {
        mockServer.expect(requestTo("http://messagemanager:8080/api/v1/messages"))
                .andExpect(method(org.springframework.http.HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.toNumber").value("+17037550417"))
                .andExpect(jsonPath("$.body").value("sms_appointment_reminders"))
                .andRespond(withSuccess());

        client.sendSms("+17037550417", "sms_appointment_reminders");

        mockServer.verify();
    }
}
