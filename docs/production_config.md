# Production configuration separation and DLQ

This document explains how to separate configuration for different environments and how the Kafka DLQ is configured in this project.

1) Configuration separation
- Keep sensitive/infra-specific properties out of source control; use environment variables or a configuration center (e.g., Nacos) for production values.
- Use profile-specific YAML files under resources, e.g.:
  - application.yml (common defaults)
  - application-local.yml (development)
  - application-prod.yml (production)

Example application-prod.yml (seckill-order):

spring:
  profiles: prod
  datasource:
    url: jdbc:mysql://prod-db-host:3306/seckill?useSSL=false&serverTimezone=UTC&characterEncoding=utf8mb4
    username: seckill
    password: ${DB_PASSWORD}
  redis:
    host: prod-redis-host
    port: 6379

kafka:
  bootstrap-servers: prod-kafka-1:9092,prod-kafka-2:9092

# Logging/monitoring
management:
  endpoints:
    web:
      exposure:
        include: metrics,health

2) Dead-letter queue (DLQ)
- This project configures a DLQ via Spring Kafka's DeadLetterPublishingRecoverer.
- On consumer exception after retries, messages are published to a DLQ topic named <topic>.dlq (e.g., seckill.order.create.dlq).
- Monitor DLQ topics and either manually replay or implement an automated recovery process.

3) Recommended production steps
- Configure Kafka with appropriate retention/partitions/replication for DLQ topics.
- Add alerting for high DLQ rates.
- Use consumer groups and partitioning keys to ensure ordering where required.

