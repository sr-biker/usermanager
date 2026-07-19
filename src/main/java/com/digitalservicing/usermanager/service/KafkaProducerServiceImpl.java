package com.digitalservicing.usermanager.service;

import com.digitalservicing.types.ProfileCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaProducerServiceImpl {

    private static final String TOPIC_NAME = "logged-in-users";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendEvent(ProfileCreatedEvent event) {
        try {
            kafkaTemplate.send(TOPIC_NAME, event).get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Failed to send event to topic {}: {}", TOPIC_NAME, e.getMessage());
            throw new RuntimeException("Failed to send event to topic " + TOPIC_NAME, e);
        }
    }
}
