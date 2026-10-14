# Productionize inventory & order consumers

This PR (branch: feature/prodify-inventory) contains a set of changes to move the seckill inventory and order consumers closer to a production-ready implementation.

Summary of key changes:

- seckill-product
  - Add Redis Lua scripts for atomic stock decrement and revert: resources/scripts/seckill_decr.lua, seckill_revert.lua
  - Add RedisLuaService to load/execute Lua scripts
  - Add internal HTTP endpoints for inter-service stock decrement/restore: /internal/product/{id}/decreaseStock, /internal/product/{id}/restoreStock
  - Use Lua + DB CAS pattern in SeckillProductServiceImpl.decreaseStock

- seckill-order
  - Replace pseudo remote calls with RestTemplate calls to internal product endpoints
  - Add Kafka consumers with idempotent handling:
    - OrderEventConsumer (order.create / order.cancel)
    - PaymentEventConsumer (payment.success)
  - Add CompensationRecord entity + CompensationService to persist and retry failed stock restore operations
  - Add scheduled compensator to retry pending compensation records

- docs
  - PR description and configuration examples included

Why:
- Move critical inventory decrement to Redis Lua for atomicity
- Avoid race conditions between Redis quick-path and DB update by rollback on DB failure
- Make event consumers idempotent and durable via Redis dedup keys and compensation mechanisms

Testing / verification (local):
1. Start dependencies (MySQL/Kafka/ZooKeeper/Redis) via provided docker-compose (not included in this PR). Ensure connectivity.
2. Build and run seckill-product and seckill-order (use profile with correct Kafka/Redis/MySQL URLs).
3. Preload stock keys in Redis or run cache warmup.
4. Call participate API in order service to create an order; check that Redis stock decreases and DB order is created.
5. Check Kafka topics (order.create) and consumer logs (order service) to verify processing.
6. Simulate order cancel (or payment timeout) and ensure compensation record is created and compensator restores stock.

Rollback: revert branch or specific commits if undesired. Compensation records allow async healing if remote restore fails.

Notes:
- This PR focuses on correctness and durability over performance tuning; further tuning (connection pools, async producers, batch consumers) is recommended before production.
