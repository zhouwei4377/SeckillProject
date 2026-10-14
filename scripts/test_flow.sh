#!/usr/bin/env bash
set -euo pipefail
ROOT_DIR=$(cd "$(dirname "$0")/.." && pwd)

# Ensure services are running
$ROOT_DIR/scripts/start_local.sh

# Ensure product stock key and product row exist
PRODUCT_ID=${1:-1}
PRODUCT_STOCK=${2:-100}

echo "Setting Redis stock for product ${PRODUCT_ID} = ${PRODUCT_STOCK}"
docker exec seckill_redis redis-cli SET seckill:product:stock:${PRODUCT_ID} ${PRODUCT_STOCK}

# Insert a product record into MySQL (if not exists)
SQL="INSERT INTO seckill_product (id, name, description, original_price, seckill_price, initial_stock, current_stock, stock_version, sold_quantity, status, create_time)
VALUES (${PRODUCT_ID}, 'Test Product', 'Auto created for local test', 199.99, 99.99, ${PRODUCT_STOCK}, ${PRODUCT_STOCK}, 0, 0, 2, NOW())
ON DUPLICATE KEY UPDATE name=VALUES(name);"

echo "Inserting product row into DB (if not exists)"
docker exec -i seckill_mysql mysql -useckill -pseckill -e "$SQL"

echo "Please start seckill-product (port 8081) and seckill-order (port 8082) jars with --spring.profiles.active=local"

echo "Triggering a participate request (example):"
PAYLOAD='{"productId":'${PRODUCT_ID}',"quantity":1,"userId":100,"requestId":"local-req-1"}'

echo "curl -X POST http://localhost:8082/api/order/seckill/participate -H 'Content-Type: application/json' -d '$PAYLOAD'"

# Wait a bit and then show Redis/current stock
sleep 3
STOCK=$(docker exec seckill_redis redis-cli GET seckill:product:stock:${PRODUCT_ID} || true)
echo "Current Redis stock for product ${PRODUCT_ID}: ${STOCK}"

# Show latest order
echo "Latest order in DB:"
docker exec -i seckill_mysql mysql -useckill -pseckill -e "SELECT id, order_no, user_id, product_id, quantity, status, create_time FROM seckill_order ORDER BY id DESC LIMIT 5;"

# Optionally send a cancel event via Kafka (requires kafka-console-producer in container)
read -p "Send cancel event? (y/n) " RESP
if [ "$RESP" = "y" ]; then
  LATEST_ORDER_ID=$(docker exec -i seckill_mysql mysql -useckill -pseckill -N -e "SELECT id FROM seckill_order ORDER BY id DESC LIMIT 1;" | tr -d '\r' || echo "")
  if [ -z "$LATEST_ORDER_ID" ]; then
    echo "No order found to cancel"
    exit 1
  fi
  echo "Sending cancel event for orderId=$LATEST_ORDER_ID"
  MSG="{\"id\":$LATEST_ORDER_ID, \"productId\":$PRODUCT_ID, \"quantity\":1}"
  docker exec -i seckill_kafka bash -lc "echo '$MSG' | kafka-console-producer --broker-list localhost:9092 --topic seckill.order.cancel"
  echo "Sent"
fi
