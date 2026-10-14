# Environment and configuration guide

This document explains how sensitive values are externalized in the project and how to configure production environments using environment variables or a configuration center (Nacos).

1) Principle
- Never store production secrets (DB passwords, API keys) in source control.
- Prefer a secrets manager or CI/CD-provided environment variables for sensitive values.
- Use Nacos (or Spring Cloud Config) to store non-sensitive runtime configuration centrally; use environment variables for secrets or values that differ per host.

2) How it works in this repo
- application-prod.yml in each module references environment variables using the ${VAR:default} syntax.
- bootstrap-prod.yml configures Spring Cloud Alibaba Nacos connection (server addr / namespace). If Nacos is available, it will provide configuration overrides.
- Environment variables take precedence over values stored in Nacos unless Nacos is configured to override via bootstrap order (consult Nacos docs).

3) Deployment recommendations
- In Kubernetes, set env vars via Deployments/StatefulSets or use Secrets mounted as env vars.
- In VM/PAAS, inject environment variables through your service manager (systemd, Docker Compose with env_file, cloud provider env settings).
- For Nacos, create dataId entries for each application and profile (e.g., seckill-product.yml, seckill-order.yml) and place non-sensitive configuration there.
- Keep secrets in a dedicated secret store (Vault, Kubernetes Secrets) and reference them as env vars at runtime.

4) Examples
- To run seckill-product using environment variables:
  export DB_PASSWORD="supersecret"
  export NACOS_SERVER_ADDR="nacos.example.com:8848"
  java -jar seckill-product.jar --spring.profiles.active=prod

- To use .env file with Docker Compose (example):
  docker run --env-file .env.prod -p 8081:8081 seckill-product:latest

5) Validation
- After deployment, verify connectivity to DB, Redis and Nacos, and check that the application logs show the active profile 'prod'.

