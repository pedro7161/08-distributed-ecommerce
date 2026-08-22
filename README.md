# 08 - Distributed E-commerce with Quarkus

An educational distributed backend that combines several reliability patterns in one small, understandable system.

This is **not** a production e-commerce platform. It is deliberately scoped so the important mechanics are visible in the code and easy to reproduce locally.

## What this project demonstrates

- Transactional Outbox
- Debezium CDC
- Inbox Pattern
- Idempotent Kafka consumers
- Saga choreography
- Compensating transactions
- Eventual consistency
- Retry with exponential backoff
- Dead Letter Queue
- Circuit Breaker
- Correlation IDs
- Cross-service correlation logging
- Service-owned databases

## Stack

- Java 21
- Maven
- Quarkus 3.38.3
- Quarkus REST + Jackson
- Hibernate ORM Panache
- PostgreSQL 18
- Flyway
- Apache Kafka 4.3.1
- Quarkus Messaging Kafka
- Debezium 3.6.1.Final
- SmallRye Fault Tolerance
- Docker Compose
- JUnit / QuarkusTest / RestAssured

See the full source package for detailed architecture, startup commands, failure experiments, and test coverage.
