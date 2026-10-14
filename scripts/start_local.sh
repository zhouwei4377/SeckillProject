#!/usr/bin/env bash
set -euo pipefail
ROOT_DIR=$(cd "$(dirname "$0")/.." && pwd)
COMPOSE_FILE="$ROOT_DIR/docker/docker-compose.yml"

echo "Starting local dependencies using docker-compose..."
docker-compose -f "$COMPOSE_FILE" up -d

echo "Waiting for MySQL to be healthy..."
until docker exec seckill_mysql mysqladmin ping -uroot -prootpass --silent >/dev/null 2>&1; do
  printf '.'; sleep 2;
done
echo "\nMySQL is up"

# Initialize database and user (if not exists) and load schema
echo "Initializing database and user..."
docker exec -i seckill_mysql mysql -uroot -prootpass <<'SQL'
CREATE DATABASE IF NOT EXISTS seckill DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'seckill'@'%' IDENTIFIED BY 'seckill';
GRANT ALL PRIVILEGES ON seckill.* TO 'seckill'@'%';
FLUSH PRIVILEGES;
SQL

# copy init.sql into container and apply schema
if docker cp "$ROOT_DIR/docker/mysql/init.sql" seckill_mysql:/init.sql >/dev/null 2>&1; then
  echo "Applying DB schema..."
  docker exec seckill_mysql bash -lc "mysql -uroot -prootpass seckill < /init.sql"
else
  echo "Warning: failed to copy init.sql to mysql container"
fi


echo "Waiting for Redis to be ready..."
until docker exec seckill_redis redis-cli ping >/dev/null 2>&1; do
  printf '.'; sleep 1;
done
echo "\nRedis is up"

echo "Waiting for Kafka listener port 9092..."
# Give Kafka some time to initialize
sleep 5
if docker exec seckill_kafka bash -lc "kafka-topics.sh --bootstrap-server localhost:9092 --list" >/dev/null 2>&1; then
  echo "Kafka seems available"
else
  echo "Warning: kafka-topics command failed - topics might not be ready yet"
fi

cat <<EOF

Local stack should be running:
- MySQL: 3306 (user: seckill / pass: seckill)
- Redis: 6379
- Kafka: localhost:9092 (topics created: seckill.order.create, seckill.order.cancel, seckill.payment.success, seckill.stock.update)

How to run services:
1) Build projects:
   mvn -T 1C clean package -DskipTests
2) Start seckill-product:
   java -jar seckill-product/target/seckill-product-1.0.0.jar --spring.profiles.active=local
3) Start seckill-order:
   java -jar seckill-order/target/seckill-order-1.0.0.jar --spring.profiles.active=local

Notes:
- application-local.yml should point to the above MySQL/Redis/Kafka endpoints. See docs/local_deploy.md for recommended properties.
- To teardown: docker-compose -f "$COMPOSE_FILE" down -v
EOF
