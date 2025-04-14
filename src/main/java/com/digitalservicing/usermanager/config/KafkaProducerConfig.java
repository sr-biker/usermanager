package com.digitalservicing.usermanager.config;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Configuration
public class KafkaProducerConfig {

    private String TOPIC_NAME = "logged-in-users";
    private String BOOTSTRAP_SERVERS = "kafka-8f9ccc7-sen-digitalservices-usermanagement.b.aivencloud.com:20594";
    @Value("${usermanager.kafka.password}")
    private String TRUSTSTORE_PASSWORD = "TRUSTSTORE_PASSWORD";
    @Value("${usermanager.kafka.password}")
    private String KEYSTORE_PASSWORD = "KEYSTORE_PASSWORD";
    @Value("${usermanager.kafka.password}")
    private String KEY_PASSWORD;


    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();

        configProps.put(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                BOOTSTRAP_SERVERS);
        configProps.put(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class);
        configProps.put(
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    private void logKafkaMessage(){

        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "kafka-8f9ccc7-sen-digitalservices-usermanagement.b.aivencloud.com:20594");
        properties.setProperty("security.protocol", "SSL");
        properties.setProperty("ssl.truststore.location", "client.truststore.jks");
        properties.setProperty("ssl.truststore.password", TRUSTSTORE_PASSWORD);
        properties.setProperty("ssl.keystore.type", "PKCS12");
        properties.setProperty("ssl.keystore.location", "client.keystore.p12");
        properties.setProperty("ssl.keystore.password", KEYSTORE_PASSWORD);
        properties.setProperty("ssl.key.password", KEY_PASSWORD);
        properties.setProperty("key.serializer", com.fasterxml.jackson.databind.ser.std.StringSerializer.class.getName());
        properties.setProperty("value.serializer", com.fasterxml.jackson.databind.ser.std.StringSerializer.class.getName());

        // create a producer
        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);

    }
}
