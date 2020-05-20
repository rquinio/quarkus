package io.quarkus.it.kafka.streams;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import io.debezium.kafka.KafkaCluster;
import io.debezium.util.Testing;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

public class KafkaTestResource implements QuarkusTestResourceLifecycleManager {

    private KafkaCluster kafka;

    @Override
    public Map<String, String> start() {
        try {
            Properties props = new Properties();
            props.setProperty("zookeeper.connection.timeout.ms", "45000");
            //props.setProperty("auto.create.topics.enable", "false");
            File directory = Testing.Files.createTestingDirectory("kafka-data", true);
            kafka = new KafkaCluster().withPorts(2182, 19092)
                    .addBrokers(1)
                    .usingDirectory(directory)
                    .deleteDataUponShutdown(true)
                    .withKafkaConfiguration(props)
                    .deleteDataPriorToStartup(true)
                    .startup();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return Collections.emptyMap();
    }

    @Override
    public void inject(Object testInstance) {
        try {
            for (Field field : testInstance.getClass().getDeclaredFields()) {
                if (field.getType().isAssignableFrom(KafkaCluster.class)) {
                    field.setAccessible(true);
                    field.set(testInstance, kafka);
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to inject KafkaCluster", e);
        }
    }

    @Override
    public void stop() {
        if (kafka != null) {
            kafka.shutdown();
        }
    }
}
