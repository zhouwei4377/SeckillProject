# 生产就绪健康检查与运行手册

## 1. Actuator / healthz

生产建议在每个微服务都加上 Spring Boot Actuator，并且设置一个健康检查 endpoint。

### 依赖

在 `pom.xml` 中加入：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

### 配置样例

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus
  endpoint:
    health:
      show-details: always
      probes:
        enabled: true
```

### 访问地址

- `/actuator/health`
- `/actuator/prometheus`

### 生产建议

- Kubernetes 使用 `readinessProbe` / `livenessProbe`
- 只有依赖检查通过才允许流量进入

---

## 2. Prometheus / Micrometer

建议在每一个业务模块加入 Micrometer 指标，用于更高可靠性的监控。

### 示例：order module 的指标埋点

```java
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

@Service
public class OrderMetrics {
    private final MeterRegistry meterRegistry;

    public OrderMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void recordOrderCreated() {
        meterRegistry.counter("seckill.order.created").increment();
    }

    public void recordOrderFailed() {
        meterRegistry.counter("seckill.order.failed").increment();
    }

    public void recordPaymentCallback() {
        meterRegistry.counter("seckill.payment.callback").increment();
    }
}
```

### 建议监控指标

- `seckill.order.created`
- `seckill.order.failed`
- `seckill.order.cancelled`
- `seckill.payment.callback`
- `seckill.kafka.dlq.count`
- `seckill.compensation.retry`
- `seckill.stock.decrement.failed`

---

## 3. 日志 / traceId

生产级日志必须含有：
- requestId
- userId
- orderId
- productId
- traceId

建议使用 MDC：

```java
MDC.put("traceId", UUID.randomUUID().toString());
MDC.put("requestId", requestId);
```

日志格式建议统一为 JSON 或至少稳定字段化日志。

---

## 4. 运行手册（部署 / 验证 / 回滚）

### 4.1 部署流程

1. 检查环境配置是否已注入：
   - DB url / user / password
   - Redis host / port / password
   - Kafka bootstrap servers
   - Nacos discovery / config
2. 执行数据库 migration：
   `mvn flyway:migrate`（若使用 flyway）
3. 启动服务顺序：
   - Nacos
   - MySQL
   - Redis
   - Kafka
   - product
   - order
   - payment
4. 验证：
   - `/actuator/health`
   - Kafka topic list
   - Redis key creation

### 4.2 回滚流程

1. 暂停流量（gateway 或 ingress）
2. 停止新版本服务
3. 回退到上一个稳定镜像/版本
4. 恢复数据库和缓存状态（如果需要）
5. 重新打开流量

### 4.3 补偿恢复流程

如果订单落库成功但库存回补失败：

1. 打开补偿任务日志
2. 确认 compensation_record 中状态
3. 手工执行补偿脚本/后端补偿接口
4. 校验库存是否恢复到目标值

---

## 5. 生产前最小验收标准

以下每项至少需要在 staging 环境验证通过：

- [ ] 下单成功，订单创建事件发出
- [ ] 订单重复请求不会重复生成订单
- [ ] 支付回调重复提交不会重复修改订单状态
- [ ] Kafka 重试/失败消息能进入 DLQ
- [ ] 补偿任务能恢复库存和状态
- [ ] `/actuator/health` 返回 UP
- [ ] Prometheus 指标可见
- [ ] 故障时能通过日志定位 requestId / orderId / traceId

---

## 6. 工程建议

- 生产配置必须从环境变量/Secret 注入，不允许硬编码
- 以 `feature/prodify-inventory-prod-ready` 作为生产准备分支，所有上线前修正都应在此分支落地
- 合并到 main 前必须通过 staging 压测和 runbook 验收
