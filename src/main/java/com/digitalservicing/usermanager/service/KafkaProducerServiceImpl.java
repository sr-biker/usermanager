package com.digitalservicing.usermanager.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.FileNotFoundException;
import java.util.Properties;

@Service
@Slf4j
public class KafkaProducerServiceImpl implements KafkaProducerService, InitializingBean {

    private String TOPIC_NAME = "logged-in-users";
    @Value("${usermanager.kafka.password}")
    private String TRUSTSTORE_PASSWORD;
    @Value("${usermanager.kafka.password}")
    private String KEYSTORE_PASSWORD;
    @Value("${usermanager.kafka.password}")
    private String KEY_PASSWORD;
    private KafkaProducer<String, String> producer;

    @Override
    public void sendMessage(String message) {
        if (null != producer) {
            // create a producer
            producer.send(new ProducerRecord<>(TOPIC_NAME, message));
            producer.flush();
        }
    }

    @Override
    public void afterPropertiesSet()  {
        try {
            String trustStoreLocation = ResourceUtils.getFile("classpath:" + "client.truststore.jks").getPath();
            String keyStoreLocation = ResourceUtils.getFile("classpath:" + "client.keystore.p12").getPath();
            Properties properties = new Properties();
            properties.setProperty("bootstrap.servers", "kafka-8f9ccc7-sen-digitalservices-usermanagement.b.aivencloud.com:20594");
            properties.setProperty("security.protocol", "SSL");
            properties.setProperty("ssl.truststore.location", trustStoreLocation);
            properties.setProperty("ssl.truststore.password", TRUSTSTORE_PASSWORD);
            properties.setProperty("ssl.keystore.type", "PKCS12");
            properties.setProperty("ssl.keystore.location", keyStoreLocation);
            properties.setProperty("ssl.keystore.password", KEYSTORE_PASSWORD);
            properties.setProperty("ssl.key.password", KEY_PASSWORD);
            properties.setProperty("key.serializer", StringSerializer.class.getName());
            properties.setProperty("value.serializer", StringSerializer.class.getName());
            producer = new KafkaProducer<>(properties);
        }
        catch (FileNotFoundException e) {
            log.error("Failed to initialize Kafka producer", e);
        }
    }
}
