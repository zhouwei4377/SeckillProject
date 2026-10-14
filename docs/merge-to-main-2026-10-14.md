# Merge to main — Merge Instructions and Runbook

Generated: 2026-10-14
Source branch: feature/prodify-inventory-prod-ready
Target branch: main

Purpose
-------
This file records the exact merge, migration and deployment steps to promote feature/prodify-inventory-prod-ready into main and prepare for production deployment. It does NOT perform the git merge; it documents the commands and runbook. Execute the steps in CI or on a machine with repo push rights.

1) Preconditions (must be completed before merge)
- All CI checks (unit + integration + Testcontainers) are green.
- Staging verification script passed: `scripts/verify_production_ready.sh`.
- Migration scripts ran and verified in staging:
  - `seckill-order/src/main/resources/db/migration/V1__prod_ready.sql`
  - `seckill-order/src/main/resources/db/migration/V2__prod_dedup_and_callback_log.sql`
- Runbook (`docs/prod-ready-runbook.md`) reviewed and owners assigned.
- On-call / release window and rollback owner assigned.

2) Merge commands (run on a machine with push permission)
```bash
# fetch and ensure up-to-date
git fetch origin

# checkout main and update
git checkout main
git pull origin main

# checkout feature branch and update
git checkout feature/prodify-inventory-prod-ready
git pull origin feature/prodify-inventory-prod-ready

# perform merge (no fast-forward to keep a clear merge commit)
git checkout main
git merge --no-ff feature/prodify-inventory-prod-ready -m "feat(prod): merge prodify-inventory-prod-ready — outbox, db idempotency, dlq, runbook"

# push merged main to remote
git push origin main
```

3) Migration step (run as part of deployment pipeline or manually in maintenance window)
- Recommended: run DB migrations in CI/CD deploy stage. If manual, run on DB in maintenance window.
- Migration files (non-destructive additions):
  - `seckill-order/src/main/resources/db/migration/V1__prod_ready.sql`
  - `seckill-order/src/main/resources/db/migration/V2__prod_dedup_and_callback_log.sql`

If using Flyway:
```bash
mvn -pl seckill-order -Dflyway.configFiles=... flyway:migrate
```
Or apply SQL files directly to the target DB in a controlled window.

4) Deploy (recommended Canary / Blue-Green)
- Deploy new service artifacts to Canary (10% traffic or single pod), observe for 30–60 minutes:
  - `/actuator/health` = UP and dependency checks OK
  - `/actuator/prometheus` available
  - key metrics: `seckill.order.created`, `seckill.payment.callback`, `seckill.kafka.dlq.count`, `outbox.pending.count`
  - Kafka consumer lag and outbox pending falling
  - No surge in errors/log exceptions with traceId/requestId
- If Canary stable, increase to 50% and observe another 30–60 minutes.
- If stable, promote to 100%.

5) Post-deploy validation (0–60 minutes)
- Actuator health: all green
- Error rate < baseline (e.g., <1%)
- Kafka consumer lag acceptable (< configured threshold)
- Outbox pending count decreasing to near 0
- DLQ count 0 or explainable entries

6) Quick rollback (if severe issues)
- Prefer service image rollback (Kubernetes):
  kubectl rollout undo deployment/<order-deployment>
- If reverting main commit required (rare), revert the merge commit and push, but DO NOT attempt to drop migration tables blindly in production.
- Follow Runbook for manual compensation steps if necessary.

7) Contacts & owners
- Code owner/reviewer: (assign in PR)
- DBA / Migration owner: (assign in runbook)
- Release owner / on-call: (assign in runbook)

8) Notes & constraints
- The merge contains schema additions: outbox_event, order_event_dedup, payment_callback_log, compensation_record, etc. These are additive and non-destructive, but must be present before consumers/producers rely on them.
- Do not remove or alter migration files after merging. Keep migration history intact.

9) After-merge housekeeping
- Create a release tag if desired, e.g. `git tag -a v1.0.0-prodify-<date> -m "prodify inventory release"`
- Open a PR/issue to schedule full production load testing if not done already.

---
This file was created automatically on behalf of the repository owner to record and standardize the merge-to-main procedure for branch `feature/prodify-inventory-prod-ready`.
