package com.digitalservicing.usermanager;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

public class TestKafka {



    public static void main(String[] args) throws IOException {
        String TOPIC_NAME = "logged-in-users";
        String TRUSTSTORE_PASSWORD = "5ba231b67d9696fc2127af256b549ee2f8fc9a97";
        String KEYSTORE_PASSWORD = "5ba231b67d9696fc2127af256b549ee2f8fc9a97";
        String KEY_PASSWORD = "5ba231b67d9696fc2127af256b549ee2f8fc9a97";

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
        producer.send(new ProducerRecord<String, String>(TOPIC_NAME, "message"));
        producer.close();
    }
}
