# Productionized seckill update

This branch contains a production-oriented refactor of the seckill flow:

- Redis Lua atomic stock decrement / restore
- internal stock endpoints for service-to-service calls
- order event consumers with idempotency control
- payment success consumer
- compensation record and retry workflow
- local docker-compose + validation scripts
- tests and CI configuration notes

This branch is intended to be reviewed and merged to main after validation.
