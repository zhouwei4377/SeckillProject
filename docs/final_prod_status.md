# Productionized Seckill Branch Status

This branch represents the production-oriented evolution of the original seckill demo.

## Included changes
- Redis Lua atomic stock decrement and restore
- Redis Lua service for stock operations
- Internal product endpoints for service-to-service stock operations
- Product/service remote call pattern using RestTemplate
- Kafka event consumers with idempotency safeguards
- Order payment callback endpoint
- Dead-letter queue configuration guidance via Spring Kafka error handler
- Compensation records and retry workflow
- Local dependency stack via Docker Compose
- Local validation scripts and environment-config separation
- Test scaffolding and CI workflow ideas

## Current intent
The repository is now structured around the following production patterns:
- atomic inventory update in Redis
- DB-backed CAS validation to protect consistency
- message-driven order lifecycle
- refund/restore compensation on cancellation or failure
- externalized env/config for production deployment

## Recommended next steps before real production deployment
- configure real Kafka DLQ and monitor it
- inject production secrets via environment variables or Secret manager
- validate on staging with real MySQL/Redis/Kafka
- add contract tests for payment callback and order-cancel flow
- tune consumer concurrency and retry settings
- add alerting and metrics for stock and compensation failures

## Branch
feature/prodify-inventory
