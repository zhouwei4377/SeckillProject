package com.seckill.common.constant;

/**
 * 秒杀系统常量定义
 * 生产级别常量配置
 */
public class SeckillConstants {

    // ==================== Redis Key 前缀 ====================
    public static final String REDIS_PREFIX = "seckill:";
    
    // 商品库存缓存
    public static final String PRODUCT_STOCK_KEY = REDIS_PREFIX + "product:stock:";
    
    // 秒杀商品缓存
    public static final String SECKILL_PRODUCT_KEY = REDIS_PREFIX + "seckill:product:";
    
    // 用户限流记录
    public static final String USER_RATE_LIMIT_KEY = REDIS_PREFIX + "rate:limit:user:";
    
    // IP限流记录
    public static final String IP_RATE_LIMIT_KEY = REDIS_PREFIX + "rate:limit:ip:";
    
    // 恶意IP黑名单
    public static final String IP_BLACKLIST_KEY = REDIS_PREFIX + "blacklist:ip";
    
    // 用户参与秒杀记录（去重）
    public static final String USER_PARTICIPATE_KEY = REDIS_PREFIX + "participate:user:";
    
    // 分布式锁前缀
    public static final String LOCK_PREFIX = REDIS_PREFIX + "lock:";
    
    // 库存分片锁
    public static final String STOCK_LOCK_KEY = LOCK_PREFIX + "stock:";
    
    // 订单锁
    public static final String ORDER_LOCK_KEY = LOCK_PREFIX + "order:";
    
    // 热点数据缓存
    public static final String HOT_DATA_KEY = REDIS_PREFIX + "hot:data:";
    
    // ==================== 限流配置 ====================
    // 用户每分钟请求数限制
    public static final int USER_RATE_LIMIT_PER_MINUTE = 100;
    
    // IP每分钟请求数限制
    public static final int IP_RATE_LIMIT_PER_MINUTE = 1000;
    
    // 用户每分钟秒杀参与次数限制
    public static final int USER_SECKILL_ATTEMPT_LIMIT = 10;
    
    // 恶意IP每分钟异常请求阈值
    public static final int MALICIOUS_REQUEST_THRESHOLD = 50;
    
    // 限流检查时间窗口（秒）
    public static final long RATE_LIMIT_WINDOW = 60;
    
    // ==================== 超时配置 ====================
    // 订单超时时间（分钟）
    public static final int ORDER_TIMEOUT_MINUTES = 15;
    
    // 支付超时时间（分钟）
    public static final int PAYMENT_TIMEOUT_MINUTES = 30;
    
    // 缓存过期时间（小时）
    public static final long CACHE_EXPIRE_HOURS = 24;
    
    // ==================== 库存配置 ====================
    // 库存预警阈值（%）
    public static final int STOCK_WARNING_PERCENTAGE = 10;
    
    // 单次库存扣减数量
    public static final int STOCK_DECREMENT_BATCH_SIZE = 100;
    
    // ==================== 数据库配置 ====================
    // 单页面查询最大数量
    public static final int MAX_PAGE_SIZE = 1000;
    
    // 批量操作最大条数
    public static final int MAX_BATCH_SIZE = 500;
    
    // ==================== 消息队列配置 ====================
    // Kafka 主题
    public static final String TOPIC_ORDER_CREATE = "seckill.order.create";
    public static final String TOPIC_ORDER_CANCEL = "seckill.order.cancel";
    public static final String TOPIC_PAYMENT_SUCCESS = "seckill.payment.success";
    public static final String TOPIC_STOCK_UPDATE = "seckill.stock.update";
    
    // Kafka 消费者组
    public static final String GROUP_ORDER_SERVICE = "order-service-group";
    public static final String GROUP_STOCK_SERVICE = "stock-service-group";
    public static final String GROUP_PAYMENT_SERVICE = "payment-service-group";
    
    // ==================== 业务常量 ====================
    // 订单状态
    public static final int ORDER_STATUS_PENDING = 1;      // 待支付
    public static final int ORDER_STATUS_PAID = 2;         // 已支付
    public static final int ORDER_STATUS_SHIPPED = 3;      // 已发货
    public static final int ORDER_STATUS_RECEIVED = 4;     // 已收货
    public static final int ORDER_STATUS_CANCELLED = 5;    // 已取消
    public static final int ORDER_STATUS_REFUNDED = 6;     // 已退款
    
    // 支付状态
    public static final int PAYMENT_STATUS_PENDING = 1;    // 待支付
    public static final int PAYMENT_STATUS_SUCCESS = 2;    // 支付成功
    public static final int PAYMENT_STATUS_FAILED = 3;     // 支付失败
    public static final int PAYMENT_STATUS_REFUNDED = 4;   // 已退款
    
    // 秒杀状态
    public static final int SECKILL_STATUS_NOT_START = 1;  // 未开始
    public static final int SECKILL_STATUS_IN_PROGRESS = 2;// 进行中
    public static final int SECKILL_STATUS_ENDED = 3;      // 已结束
    public static final int SECKILL_STATUS_SOLD_OUT = 4;   // 已售罄
    
    // ==================== HTTP 状态码 ====================
    public static final int HTTP_SUCCESS = 200;
    public static final int HTTP_BAD_REQUEST = 400;
    public static final int HTTP_UNAUTHORIZED = 401;
    public static final int HTTP_FORBIDDEN = 403;
    public static final int HTTP_NOT_FOUND = 404;
    public static final int HTTP_CONFLICT = 409;
    public static final int HTTP_RATE_LIMIT = 429;
    public static final int HTTP_SERVER_ERROR = 500;
    
    // ==================== 业务错误码 ====================
    public static final String ERROR_CODE_SUCCESS = "0";
    public static final String ERROR_CODE_RATE_LIMIT = "1001";      // 限流
    public static final String ERROR_CODE_STOCK_INSUFFICIENT = "1002"; // 库存不足
    public static final String ERROR_CODE_SECKILL_NOT_START = "1003";  // 秒杀未开始
    public static final String ERROR_CODE_SECKILL_ENDED = "1004";      // 秒杀已结束
    public static final String ERROR_CODE_DUPLICATE_REQUEST = "1005";  // 重复请求
    public static final String ERROR_CODE_INVALID_PARAM = "1006";      // 参数无效
    public static final String ERROR_CODE_SYSTEM_ERROR = "9999";       // 系统错误
    
    // ==================== 设备指纹相关 ====================
    // 设备指纹检查窗口时间（秒）
    public static final long DEVICE_FINGERPRINT_WINDOW = 300;
    
    // 单设备最多参与次数
    public static final int MAX_DEVICE_ATTEMPTS = 5;
    
    // ==================== 超时任务配置 ====================
    // XXL-Job 任务执行超时时间（秒）
    public static final long JOB_TIMEOUT_SECONDS = 600;
    
    // 定时任务执行间隔（秒）
    public static final long JOB_INTERVAL_SECONDS = 30;
}
