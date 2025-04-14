package com.digitalservicing.usermanager.service;

public interface KafkaProducerService {

    void sendMessage(String message);
}
