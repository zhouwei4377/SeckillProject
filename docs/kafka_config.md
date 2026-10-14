# Kafka & Consumer/Producer Recommended Config (examples)

seckill-order and seckill-product recommended settings (application.yml fragment):

producer:
  properties:
    acks: all
    enable.idempotence: true
    retries: 5
    max.in.flight.requests.per.connection: 1
    delivery.timeout.ms: 120000

consumer:
  properties:
    max.poll.records: 100
    enable.auto.commit: false
    max.poll.interval.ms: 300000
  listener:
    ack-mode: record

# Dead letter / retry
# Use a dead-letter topic per main topic, e.g. seckill.order.create.dlq
# Configure broker-side or implement producer/consumer level retry logic.
