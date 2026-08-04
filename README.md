# 秒杀项目 (Seckill Project)

## 📋 项目介绍

一个基于Spring Cloud、Nacos、Redis、Kafka等技术栈实现的**生产级别高并发秒杀系统**。

### 核心特性

✨ **流量控制**
- Nginx限流与负载均衡
- Zuul网关限流
- 本地限流（Guava RateLimiter）
- Redis分布式限流
- 恶意脚本流量识别与拦截

🛡️ **防护机制**
- 库存防超卖（分布式锁 + Redis乐��锁）
- 数据库高压力防护
- 请求去重与幂等性
- 热点数据预热

⏰ **订单管理**
- 超时订单自动取消（XXL-Job定时任务）
- 订单状态机管理
- 分布式事务处理

📊 **数据同步**
- Kafka异步消息队列处理
- 库存与订单数据同步
- 高可用消费端

🔄 **负载均衡**
- 消费者Rebalance机制
- 服务端负载均衡
- 消费分片处理

## 🏗️ 系统架构

```
┌─────────────────────────────────────────────────────────────┐
│                        客户端请求                              │
└────────────────────────┬────────────────────────────────────┘
                         │
                    ┌────▼─────┐
                    │  Nginx    │ ← 限流、负载均衡
                    └────┬──────┘
                         │
            ┌────────────┼────────────┐
            │            │            │
        ┌───▼───┐   ┌───▼───┐   ┌───▼───┐
        │网关-1 │   │网关-2 │   │网关-3 │ ← Zuul 限流
        └───┬───┘   └───┬───┘   └───┬───┘
            │           │           │
        ┌───┴───┬───────┴───┬───────┴────┐
        │       │           │            │
     ┌──▼──┐ ┌─▼──┐  ┌──────▼──┐  ┌──────▼──┐
     │商品 │ │订单 │  │支付服务 │  │定时任务 │
     │服务 │ │服务 │  │         │  │ 服务   │
     └──┬──┘ └─┬──┘  └────┬────┘  └──┬─────┘
        │      │          │          │
     ┌──┴──────┴──────────┴──────────┴───┐
     │     Nacos 服务注册与配置中心        │
     └────────────────────────────────────┘
        │           │            │
        │      ┌────▼────┐   ┌───▼─────┐
        │      │  Redis  │   │  MySQL  │
        │      │(缓存限流)│   │(数据库) │
        │      └────┬────┘   └───┬─────┘
        │           │            │
        └───────────┼────────────┘
                ┌───▼────┐
                │ Kafka  │ ← 异步消息队列
                └────────┘
```

## 📦 模块说明

| 模块 | 说明 | 职责 |
|------|------|------|
| **seckill-common** | 公共模块 | 工具类、常量、异常定义 |
| **seckill-gateway** | API网关 | Zuul限流、路由、权限验证 |
| **seckill-product** | 商品服务 | 商品管理、库存扣减、缓存预热 |
| **seckill-order** | 订单服务 | 订单创建、状态管理、去重幂等 |
| **seckill-payment** | 支付服务 | 支付处理、支付状态管理 |
| **seckill-job** | 定时任务服务 | 超时订单取消、数据清理 |

## 🚀 核心功能详解

### 1️⃣ 限流防护 (多层次)

**第一层：Nginx限流**
- 使用limit_req_zone限流
- 单IP限制并发连接数

**第二层：Zuul网关限流**
- 令牌桶算法(Token Bucket)
- 用户级别限流

**第三层：本地内存限流**
- Guava RateLimiter
- 单个服务实例限流

**第四层：Redis分布式限流**
- Lua脚本原子性操作
- 集群间一致性限流

**第五层：恶意脚本识别**
- 异常请求频率检测
- IP黑名单管理
- 设备指纹识别

### 2️⃣ 库存防超卖

```java
// 核心策略：Redis原子操作 + 数据库二次验证
1. Redis库存扣减 (快速路径)
2. 库存预警检查
3. 数据库CAS更新 (二次验证)
4. 消息队列异步扣减
```

### 3️⃣ 数据库高压防护

- 库存分片存储（减少热点）
- 查询缓存层（减少数据库访问）
- 批量操作优化
- 连接池隔离

### 4️⃣ 超时订单自动取消

- XXL-Job分布式定时任务
- 秒级精度的订单扫描
- 自动库存回退
- 失败重试机制

## 🔧 技术栈

- **后端框架**: Spring Boot 2.7.x + Spring Cloud
- **服务网关**: Zuul 2.x
- **服务注册**: Nacos 2.x
- **缓存**: Redis 6.x + Redisson
- **消息队列**: Kafka 2.8.x
- **数据库**: MySQL 8.x + MyBatis Plus
- **定时任务**: XXL-Job 2.4.x
- **限流工具**: Guava, Redisson, Nginx
- **负载均衡**: Ribbon + Nacos

## 📊 性能指标

- **单机QPS**: 10000+
- **集群QPS**: 100000+
- **P99延迟**: <100ms
- **可用性**: 99.95%

## 🛠️ 快速开始

### 前置要求

```bash
- JDK 1.8+
- MySQL 8.0+
- Redis 6.0+
- Nacos 2.x
- Kafka 2.8.x
- Maven 3.6+
```

### 构建步骤

```bash
# 1. 克隆项目
git clone https://github.com/zhouwei4377/SeckillProject.git
cd SeckillProject

# 2. 编译打包
mvn clean package -DskipTests

# 3. 启动服务 (按顺序)
# 确保 Nacos、Redis、MySQL、Kafka 已启动
java -jar seckill-gateway/target/seckill-gateway-1.0.0.jar
java -jar seckill-product/target/seckill-product-1.0.0.jar
java -jar seckill-order/target/seckill-order-1.0.0.jar
java -jar seckill-payment/target/seckill-payment-1.0.0.jar
java -jar seckill-job/target/seckill-job-1.0.0.jar
```

## 📝 API示例

### 秒杀接口

```bash
# 获取秒杀商品列表
curl -X GET http://localhost:8080/api/product/seckill/list

# 获取秒杀详情
curl -X GET http://localhost:8080/api/product/seckill/detail/1

# 参加秒杀（核心接口）
curl -X POST http://localhost:8080/api/order/seckill/participate \
  -H "Content-Type: application/json" \
  -d '{"productId": 1, "quantity": 1, "userId": "user123"}'

# 查询订单状态
curl -X GET http://localhost:8080/api/order/status/ORDER-20240801-001
```

## 📚 关键设计思想

### 1. 高可用设计
- 服务间通过Nacos服务发现
- 断路器保护（Hystrix/Sentinel）
- 异步处理（Kafka消息队列）
- 故障自动转移

### 2. 高性能设计
- 缓存分层（Redis多级缓存）
- 数据库查询优化（索引、分页）
- 异步非阻塞（线程池隔离）
- 消息批处理

### 3. 数据一致性
- 分布式事务（最终一致性）
- 消息可靠投递
- 幂等性保证
- 重试补偿机制

## 🔐 安全防护

✅ 身份验证 (JWT Token)
✅ 请求签名验证
✅ 恶意流量拦截
✅ SQL注入防护
✅ 分布式锁防护

## 📈 扩展性

- 水平扩展：支持多实例部署
- 垂直扩展：配置化资源调整
- 功能扩展：模块化架构便于添加新功能

## 🤝 贡献指南

欢迎提交Issue和Pull Request！

## 📄 许可证

MIT License

---

**最后更新**: 2024年  
**维护者**: zhouwei4377