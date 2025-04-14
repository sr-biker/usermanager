package com.digitalservicing.usermanager.exception;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.FileNotFoundException;
import java.util.Properties;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler  {
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<String> handleUserNotFoundException(UserNotFoundException ex) {
        log.error(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("An error occurred: " + ex.getMessage());
    }

    @ExceptionHandler(UserLoginException.class)
    public ResponseEntity<String> handleLoginException(UserLoginException ex) {
        log.error(ex.getMessage(), ex);
        sendMessage("User Login Exception"  + ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("An error occurred: " + ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> catchAll(Exception ex) {
        log.error(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + ex.getMessage());
    }



    private String TOPIC_NAME = "logged-in-users";
    @Value("${usermanager.kafka.password}")
    private String TRUSTSTORE_PASSWORD;
    @Value("${usermanager.kafka.password}")
    private String KEYSTORE_PASSWORD;
    @Value("${usermanager.kafka.password}")
    private String KEY_PASSWORD;

    private void sendMessage(String message) {


        try {
            String trustStoreLocation = ResourceUtils.getFile("classpath:" + "client.truststore.jks").getPath();
            String keyStoreLocation = ResourceUtils.getFile("classpath:" + "client.keystore.p12").getPath();
            Properties properties = new Properties();
            properties.setProperty("bootstrap.servers", "kafka-8f9ccc7-sen-digitalservices-usermanagement.b.aivencloud.com:20594");
            properties.setProperty("security.protocol", "SSL");
            properties.setProperty("ssl.truststore.location", trustStoreLocation);
            properties.setProperty("ssl.truststore.password", TRUSTSTORE_PASSWORD);
            properties.setProperty("ssl.keystore.type", "PKCS12");
            properties.setProperty("ssl.keystore.location",keyStoreLocation);
            properties.setProperty("ssl.keystore.password", KEYSTORE_PASSWORD);
            properties.setProperty("ssl.key.password", KEY_PASSWORD);
            properties.setProperty("key.serializer", StringSerializer.class.getName());
            properties.setProperty("value.serializer", StringSerializer.class.getName());

            // create a producer
            KafkaProducer<String, String> producer = new KafkaProducer<>(properties);
            producer.send(new ProducerRecord<>(TOPIC_NAME, message));
            producer.flush();
            producer.close();

        } catch (FileNotFoundException e) {
            log.error("Failed to initialize Kafka producer", e);
        }

    }

}
