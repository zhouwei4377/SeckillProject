# 秒杀系统 - 完整部署指南

## 📋 目录
1. [系统架构](#系统架构)
2. [环境要求](#环境要求)
3. [快速开始](#快速开始)
4. [详细部署步骤](#详细部署步骤)
5. [配置说明](#配置说明)
6. [服务启动](#服务启动)
7. [API测试](#api测试)
8. [监控与告警](#监控与告警)
9. [故障排查](#故障排查)

---

## 系统架构

### 整体架构图
```
┌─────────────┐
│   客户端     │
└──────┬──────┘
       │ HTTP
       ▼
┌──────────────────────┐
│  API网关 (Zuul)      │  <- 限流、限制滤
│  Port: 8080          │
└──────┬───────────────┘
       │
       ├─────────────────────────┬──────────────────────┬────────────────┐
       │                         │                      │                │
       ▼                         ▼                      ▼                ▼
┌─────────────────┐    ┌──────────────────┐   ┌──────────────┐  ┌──────────────┐
│ 商品服务        │    │ 订单服务         │   │ 支付服务     │  │ 定时任务服务 │
│ (seckill-prod)  │    │ (seckill-order)  │   │ (seckill-pay)│  │ (seckill-job)│
│ Port: 8081      │    │ Port: 8082       │   │ Port: 8083   │  │ Port: 8084   │
└────────┬────────┘    └──────┬───────────┘   └──────┬───────┘  └──────┬───────┘
         │                    │                      │                 │
         └────────────────────┼──────────────────────┼─────────────────┘
                              │
                 ┌────────────┴────────────┐
                 │                        │
                 ▼                        ▼
          ┌────────────────┐      ┌──────────────┐
          │ MySQL Database │      │ Redis Cache  │
          │ Port: 3306     │      │ Port: 6379   │
          └────────────────┘      └──────────────┘
                 │
                 ├─────────┬──────────┐
                 │         │          │
           seckill_db  seckill_test  seckill_log

                 ▼
         ┌────────────────┐
         │ Kafka Message  │
         │ Queue          │
         │ Port: 9092     │
         └────────────────┘
                 │
           ┌─────┴──────┐
           ▼            ▼
      Order Topic   Payment Topic
```

### 微服务依赖关系
```
网关层 (Gateway Layer)
  ↓
业务服务层 (Service Layer)
  ├─ 商品服务 (Product Service) 
  ├─ 订单服务 (Order Service) → 调用商品服务
  ├─ 支付服务 (Payment Service) → 调用订单服务
  └─ 定时任务服务 (Job Service) → 调用所有业务服务
  ↓
数据层 (Data Layer)
  ├─ MySQL (持久化存储)
  ├─ Redis (缓存层)
  └─ Kafka (消息队列)
```

---

## 环境要求

### 硬件要求
- **CPU**: 8核以上
- **内存**: 16GB以上
- **磁盘**: 500GB以上
- **网络**: 1Gbps以上

### 软件依赖

| 软件 | 版本 | 端口 | 说明 |
|------|------|------|------|
| JDK | 1.8+ | - | Java运行环境 |
| MySQL | 5.7+ | 3306 | 数据库 |
| Redis | 5.0+ | 6379 | 缓存服务 |
| Kafka | 2.6+ | 9092 | 消息队列 |
| Zookeeper | 3.4+ | 2181 | Kafka依赖 |
| Nacos | 1.4+ | 8848 | 服务注册和配置 |
| XXL-Job Admin | 2.3+ | 8080 | 定时任务管理 |

### 安装命令

```bash
# Ubuntu/Debian
sudo apt-get update
sudo apt-get install -y openjdk-8-jdk mysql-server redis-server

# CentOS/RHEL
sudo yum install -y java-1.8.0-openjdk mysql-server redis

# macOS
brew install openjdk@8 mysql redis kafka
```

---

## 快速开始

### 1. 克隆项目
```bash
git clone https://github.com/zhouwei4377/SeckillProject.git
cd SeckillProject
```

### 2. 使用Docker快速启动
```bash
# 启动所有依赖服务（MySQL, Redis, Kafka, Zookeeper）
docker-compose -f docker-compose.yml up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f
```

### 3. 初始化数据库
```bash
# 连接MySQL
mysql -h 127.0.0.1 -u root -p

# 执行初始化脚本
source docs/database/seckill_db_init.sql
source docs/database/seckill_db_advanced.sql

# 验证
SHOW TABLES IN seckill_db;
```

### 4. 编译项目
```bash
# 编译公共模块
cd seckill-common
mvn clean package -DskipTests

# 编译所有模块
cd ..
mvn clean package -DskipTests -T 1C
```

### 5. 启动服务
```bash
# 启动网关服务
cd seckill-gateway
java -jar target/seckill-gateway-1.0.0.jar &

# 启动商品服务
cd ../seckill-product
java -jar target/seckill-product-1.0.0.jar &

# 启动订单服务
cd ../seckill-order
java -jar target/seckill-order-1.0.0.jar &

# 启动支付服务
cd ../seckill-payment
java -jar target/seckill-payment-1.0.0.jar &

# 启动定时任务服务
cd ../seckill-job
java -jar target/seckill-job-1.0.0.jar &
```

### 6. 验证服务
```bash
# 检查网关
curl http://localhost:8080/health

# 获取商品列表
curl http://localhost:8080/api/product/list
```

---

## 详细部署步骤

### Step 1: 安装JDK
```bash
# 检查Java版本
java -version

# 如未安装，下载并配置
wget https://download.java.net/java/GA/jdk8/openjdk-8.tar.gz
tar -xzf openjdk-8.tar.gz
export JAVA_HOME=/path/to/jdk8
export PATH=$JAVA_HOME/bin:$PATH
```

### Step 2: 安装MySQL
```bash
# Docker方式
docker run --name seckill-mysql \
  -e MYSQL_ROOT_PASSWORD=root123 \
  -e MYSQL_DATABASE=seckill_db \
  -p 3306:3306 \
  -v /data/mysql:/var/lib/mysql \
  -d mysql:5.7

# 等待MySQL启动完成
sleep 10

# 初始化数据库
mysql -h 127.0.0.1 -u root -proot123 < docs/database/seckill_db_init.sql
mysql -h 127.0.0.1 -u root -proot123 < docs/database/seckill_db_advanced.sql

# 验证
mysql -h 127.0.0.1 -u root -proot123 -e "SHOW TABLES IN seckill_db;"
```

### Step 3: 安装Redis
```bash
# Docker方式
docker run --name seckill-redis \
  -p 6379:6379 \
  -v /data/redis:/data \
  -d redis:6 redis-server --appendonly yes

# 验证
redis-cli -h 127.0.0.1 -p 6379 ping
# 返回 PONG
```

### Step 4: 安装Kafka
```bash
# Docker方式
docker run --name seckill-zookeeper \
  -e ZOO_CFG_SERVER_1=zookeeper:2181 \
  -p 2181:2181 \
  -d zookeeper:latest

docker run --name seckill-kafka \
  -e KAFKA_BROKER_ID=1 \
  -e KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181 \
  -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://kafka:9092 \
  -e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1 \
  -p 9092:9092 \
  -d confluentinc/cp-kafka:6.0.0

# 创建主题
docker exec seckill-kafka kafka-topics --create \
  --bootstrap-server localhost:9092 \
  --topic seckill.order.create \
  --partitions 3 \
  --replication-factor 1
```

### Step 5: 安装Nacos
```bash
# 下载Nacos
wget https://github.com/alibaba/nacos/releases/download/2.0.3/nacos-server-2.0.3.tar.gz
tar -xzf nacos-server-2.0.3.tar.gz
cd nacos

# 启动Nacos
./bin/startup.sh -m standalone

# 访问Nacos控制台
# http://127.0.0.1:8848/nacos
# 默认用户名/密码: nacos/nacos
```

### Step 6: 编译和打包
```bash
# 清理旧的构建
mvn clean

# 编译和打包所有模块
mvn package -DskipTests -P release

# 查看生成的JAR包
find . -name "*.jar" -type f | grep target
```

### Step 7: 启动应用

#### 方式1: 直接启动（适合开发测试）
```bash
#!/bin/bash

# 创建日志目录
mkdir -p logs

# 启动网关
echo "启动API网关..."
nohup java -jar seckill-gateway/target/seckill-gateway-1.0.0.jar \
  --server.port=8080 \
  --spring.application.name=seckill-gateway \
  > logs/gateway.log 2>&1 &
echo $! > logs/gateway.pid

# 启动商品服务
echo "启动商品服务..."
nohup java -jar seckill-product/target/seckill-product-1.0.0.jar \
  --server.port=8081 \
  --spring.application.name=seckill-product \
  > logs/product.log 2>&1 &
echo $! > logs/product.pid

# 启动订单服务
echo "启动订单服务..."
nohup java -jar seckill-order/target/seckill-order-1.0.0.jar \
  --server.port=8082 \
  --spring.application.name=seckill-order \
  > logs/order.log 2>&1 &
echo $! > logs/order.pid

# 启动支付服务
echo "启动支付服务..."
nohup java -jar seckill-payment/target/seckill-payment-1.0.0.jar \
  --server.port=8083 \
  --spring.application.name=seckill-payment \
  > logs/payment.log 2>&1 &
echo $! > logs/payment.pid

# 启动定时任务服务
echo "启动定时任务服务..."
nohup java -jar seckill-job/target/seckill-job-1.0.0.jar \
  --server.port=8084 \
  --spring.application.name=seckill-job \
  > logs/job.log 2>&1 &
echo $! > logs/job.pid

echo "所有服务启动完成！"
sleep 3
echo "检查服务状态..."
ps -ef | grep "seckill" | grep -v grep
```

#### 方式2: Docker启动（适合生产环境）
```bash
# 构建Docker镜像
docker build -t seckill-gateway:1.0.0 -f seckill-gateway/Dockerfile .
docker build -t seckill-product:1.0.0 -f seckill-product/Dockerfile .
docker build -t seckill-order:1.0.0 -f seckill-order/Dockerfile .
docker build -t seckill-payment:1.0.0 -f seckill-payment/Dockerfile .
docker build -t seckill-job:1.0.0 -f seckill-job/Dockerfile .

# 使用Docker Compose启动
docker-compose -f docker-compose.prod.yml up -d
```

---

## 配置说明

### application.yml 配置

```yaml
spring:
  application:
    name: seckill-product
  
  # 数据库配置
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/seckill_db?useSSL=false&serverTimezone=UTC
    username: seckill_app
    password: seckill_password_123
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      maximum-pool-size: 30          # 最大连接数
      minimum-idle: 10               # 最小空闲连接
      connection-timeout: 30000      # 连接超时时间
      idle-timeout: 600000           # 空闲超时时间
      max-lifetime: 1800000          # 连接最大生命周期
  
  # Redis配置
  redis:
    host: 127.0.0.1
    port: 6379
    timeout: 2000ms
    password:                        # 如果Redis设置了密码，填写这里
    jedis:
      pool:
        max-active: 100              # 最大连接数
        max-idle: 50                 # 最大空闲连接
        min-idle: 10                 # 最小空闲连接
        max-wait: -1ms               # 等待时间
  
  # Kafka配置
  kafka:
    bootstrap-servers: 127.0.0.1:9092
    producer:
      acks: all                      # 确保消息被所有副本接收
      retries: 3                     # 重试次数
      batch-size: 16384              # 批处理大小
      linger-ms: 10                  # 等待时间
    consumer:
      group-id: seckill-product-group
      auto-offset-reset: earliest    # 从最早的消息开始消费
      enable-auto-commit: true       # 自动提交偏移量
  
  # Nacos配置
  cloud:
    nacos:
      discovery:
        server-addr: 127.0.0.1:8848
        namespace: seckill           # 命名空间
        group: DEFAULT_GROUP         # 分组
      config:
        server-addr: 127.0.0.1:8848
        namespace: seckill
        group: DEFAULT_GROUP
        file-extension: yaml

# MyBatis Plus配置
mybatis-plus:
  mapper-locations: classpath*:mapper/**/*.xml
  type-aliases-package: com.seckill.product.entity
  global-config:
    db-config:
      id-type: auto                  # ID生成策略
      logic-delete-field: deleted    # 逻辑删除字段
      logic-delete-value: 1          # 删除值
      logic-not-delete-value: 0      # 未删除值
      table-prefix:                  # 表前缀

# 日志配置
logging:
  level:
    root: INFO
    com.seckill: DEBUG
    com.xxl.job: INFO
  pattern:
    console: '%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n'
    file: '%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n'
  file:
    name: logs/seckill-product.log
    max-size: 10MB                   # 单个日志文件最大大小
    max-history: 30                  # 日志文件保留天数
    total-size-cap: 1GB              # 总日志大小上限

# 服务器配置
server:
  port: 8081
  servlet:
    context-path: /
  tomcat:
    threads:
      max: 200                        # Tomcat最大线程数
      min-spare: 10                  # Tomcat最小空闲线程
    connection-timeout: 30000        # 连接超时时间
    accept-count: 100                # 请求队列大小
```

---

## 服务启动

### 启动顺序（重要！）
1. **MySQL数据库** - 存储持久化数据
2. **Redis缓存** - 缓存热点数据和库存
3. **Kafka消息队列** - 异步处理消息
4. **Nacos服务中心** - 服务注册发现
5. **API网关** - 流量入口
6. **商品服务** - 被其他服务依赖
7. **订单服务** - 依赖商品服务
8. **支付服务** - 依赖订单服务
9. **定时任务服务** - 依赖所有业务服务

### 验证服务启动
```bash
# 查看所有Java进程
jps -l

# 查看网关健康状态
curl http://127.0.0.1:8080/actuator/health

# 查看Nacos已注册服务
curl http://127.0.0.1:8848/nacos/v1/ns/catalog/services?namespaceId=seckill

# 查看日志
tail -f logs/*.log
```

---

## API测试

### 获取商品列表
```bash
curl -X GET "http://127.0.0.1:8080/api/product/list" \
  -H "Content-Type: application/json"
```

### 参加秒杀
```bash
curl -X POST "http://127.0.0.1:8080/api/order/participate" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1001,
    "productId": 1000,
    "quantity": 1,
    "requestId": "req_20240804_001"
  }'
```

### 创建支付订单
```bash
curl -X POST "http://127.0.0.1:8080/api/payment/create" \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": 100001,
    "userId": 1001,
    "amount": 5999.00,
    "paymentMethod": 1,
    "paymentChannel": 1
  }'
```

### 执行支付
```bash
curl -X POST "http://127.0.0.1:8080/api/payment/1/pay" \
  -H "Content-Type: application/json" \
  -d '{"paymentPassword": "123456"}'
```

---

## 监控与告警

### Prometheus监控
```yaml
# prometheus.yml
global:
  scrape_interval: 15s
scrape_configs:
  - job_name: 'seckill-gateway'
    static_configs:
      - targets: ['127.0.0.1:8080']
  - job_name: 'seckill-product'
    static_configs:
      - targets: ['127.0.0.1:8081']
  # ... 其他服务
```

### Grafana可视化
- CPU使用率
- 内存使用率
- QPS(每秒请求数)
- 响应时间分布
- 错误率

### ELK日志分析
```bash
# Elasticsearch
docker run -d -p 9200:9200 docker.elastic.co/elasticsearch/elasticsearch:7.10.0

# Kibana
docker run -d -p 5601:5601 docker.elastic.co/kibana/kibana:7.10.0
```

---

## 故障排查

### 问题1: 无法连接MySQL
```bash
# 检查MySQL是否运行
docker ps | grep mysql

# 检查MySQL连接
mysql -h 127.0.0.1 -u root -p

# 查看MySQL日志
docker logs seckill-mysql
```

### 问题2: Redis连接超时
```bash
# 检查Redis是否运行
docker ps | grep redis

# 测试Redis连接
redis-cli -h 127.0.0.1 -p 6379 ping

# 查看Redis日志
docker logs seckill-redis
```

### 问题3: 服务注册失败
```bash
# 检查Nacos是否运行
curl http://127.0.0.1:8848/nacos

# 查看应用日志中的错误
tail -f logs/seckill-product.log | grep -i "nacos\|register"
```

### 问题4: 库存超卖
```bash
# 检查Redis中的库存
redis-cli -h 127.0.0.1
> GET "seckill:product:stock:1000"

# 检查MySQL中的库存
mysql -h 127.0.0.1 -u root -p
> SELECT id, current_stock, sold_quantity FROM t_seckill_product WHERE id = 1000;
```

---

## 性能优化建议

1. **数据库优化**
   - 添加适当的索引
   - 使用分区表
   - 定期清理过期数据

2. **缓存优化**
   - 使用Redis做库存缓存
   - 商品信息缓存24小时
   - 用户限流信息缓存1分钟

3. **并发控制**
   - 使用Redis+Lua脚本实现原子操作
   - 使用CAS乐观锁
   - 使用分布式锁

4. **异步处理**
   - 订单创建后异步处理
   - 使用Kafka解耦服务
   - 使用消息队列处理耗时操作

---

## 联系与支持

如有问题，请提交Issue或联系技术支持团队。
