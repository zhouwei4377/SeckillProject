# 本地部署与验证（Docker Compose）

此文档说明如何在本地使用 docker-compose 快速搭建依赖服务（MySQL / Redis / Zookeeper / Kafka）并启动项目进行集成验证。

目录结构
- docker/docker-compose.yml      # docker-compose 文件
- scripts/start_local.sh         # 启动脚本（初始化 DB / 等待服务）

快速开始
1. 启动依赖服务：

   bash scripts/start_local.sh

2. 配置 Spring Boot 应用：

   在各服务的 application-local.yml 中设置以下数据库/消息中间件连接（示例）：

   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/seckill?useSSL=false&serverTimezone=UTC&characterEncoding=utf8mb4
       username: seckill
       password: seckill
     redis:
       host: localhost
       port: 6379
     kafka:
       bootstrap-servers: localhost:9092

3. 构建并启动服务：

   mvn -T 1C clean package -DskipTests
   java -jar seckill-product/target/seckill-product-1.0.0.jar --spring.profiles.active=local
   java -jar seckill-order/target/seckill-order-1.0.0.jar --spring.profiles.active=local

验证流程
- 预热/初始化库存：确保 seckill-product 的缓存预热逻辑已将库存写入 Redis，或者手动在 Redis 设置 key: seckill:product:stock:<productId> 值
- 发起秒杀请求（调用 order 服务接口），检查：
  - Redis 中库存是否减少
  - order 表中是否创建了记录
  - Kafka topic seckill.order.create 是否收到消息，seckill-order 服务消费者是否处理
- 模拟订单取消或支付成功，观察补偿记录或订单状态更新

清理
- docker-compose -f docker/docker-compose.yml down -v

限制与注意
- 该 docker-compose 用于本地开发与验证，不建议用于生产
- Kafka 使用 bitnami 镜像并通过环境变量创建 topic；在复杂场景下请手动在 broker 中配置分区与副本策略
