package com.seckill.order;

import com.seckill.common.constant.SeckillConstants;
import com.seckill.order.entity.OutboxEvent;
import com.seckill.order.mapper.OutboxEventMapper;
import com.seckill.order.service.OutboxEventService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

@SpringBootTest(
        properties = {
                "spring.cloud.nacos.enabled=false",
                "spring.cloud.bootstrap.enabled=false",
                "spring.main.allow-bean-definition-overriding=true",
                "spring.kafka.consumer.auto-offset-reset=earliest",
                "spring.kafka.listener.auto-startup=false",
                "spring.kafka.producer.retries=1",
                "spring.sql.init.mode=never"
        },
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@Testcontainers
public class OutboxEventServiceIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>(DockerImageName.parse("mysql:8.0.36"))
            .withDatabaseName("seckill_test")
            .withUsername("test")
            .withPassword("test");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7.2-alpine"))
            .withExposedPorts(6379);

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"));

    @Autowired
    private OutboxEventService outboxEventService;

    @Autowired
    private OutboxEventMapper outboxEventMapper;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> mysql.getJdbcUrl());
        registry.add("spring.datasource.username", () -> mysql.getUsername());
        registry.add("spring.datasource.password", () -> mysql.getPassword());
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
        registry.add("spring.redis.host", () -> redis.getHost());
        registry.add("spring.redis.port", () -> redis.getFirstMappedPort().toString());
        registry.add("spring.kafka.bootstrap-servers", () -> kafka.getBootstrapServers());
    }

    @Test
    void shouldPersistOutboxEvent_whenPublishingOrderEvent() {
        outboxEventService.publish("order", "1001", SeckillConstants.TOPIC_ORDER_CREATE, Map.of("orderId", 1001L));

        OutboxEvent saved = outboxEventMapper.selectList(null).stream()
                .filter(event -> "1001".equals(event.getAggregateId()))
                .findFirst()
                .orElse(null);

        Assertions.assertNotNull(saved);
        Assertions.assertEquals(0, saved.getStatus());
        Assertions.assertTrue(saved.getPayload().contains("orderId"));
    }
}
