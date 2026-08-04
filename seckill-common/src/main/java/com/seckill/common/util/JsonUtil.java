package com.seckill.common.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * JSON工具类
 * 基于FastJSON2的JSON转换工具
 */
@Slf4j
public class JsonUtil {
    
    /**
     * 对象转JSON字符串
     */
    public static String toJsonString(Object obj) {
        try {
            return JSON.toJSONString(obj);
        } catch (Exception e) {
            log.warn("JSON序列化失败: {}", obj, e);
            return null;
        }
    }
    
    /**
     * 对象转JSON字符串 (格式化)
     */
    public static String toJsonStringPretty(Object obj) {
        try {
            return JSON.toJSONString(obj, true);
        } catch (Exception e) {
            log.warn("JSON序列化失败: {}", obj, e);
            return null;
        }
    }
    
    /**
     * JSON字符串转对象
     */
    public static <T> T parseObject(String json, Class<T> clazz) {
        if (StringUtils.isEmpty(json)) {
            return null;
        }
        try {
            return JSON.parseObject(json, clazz);
        } catch (Exception e) {
            log.warn("JSON反序列化失败: {}, 目标类: {}", json, clazz, e);
            return null;
        }
    }
    
    /**
     * JSON字符串转对象 (带泛型)
     */
    public static <T> T parseObject(String json, Type type) {
        if (StringUtils.isEmpty(json)) {
            return null;
        }
        try {
            return JSON.parseObject(json, type);
        } catch (Exception e) {
            log.warn("JSON反序列化失败: {}, 目标类型: {}", json, type, e);
            return null;
        }
    }
    
    /**
     * JSON字符串转列表
     */
    public static <T> List<T> parseArray(String json, Class<T> clazz) {
        if (StringUtils.isEmpty(json)) {
            return null;
        }
        try {
            return JSON.parseArray(json, clazz);
        } catch (Exception e) {
            log.warn("JSON数组反序列化失败: {}, 元素类: {}", json, clazz, e);
            return null;
        }
    }
    
    /**
     * JSON字符串转JSONObject
     */
    public static JSONObject parseJSONObject(String json) {
        if (StringUtils.isEmpty(json)) {
            return null;
        }
        try {
            return JSON.parseObject(json);
        } catch (Exception e) {
            log.warn("JSON解析失败: {}", json, e);
            return null;
        }
    }
    
    /**
     * 检查JSON字符串有效性
     */
    public static boolean isValidJson(String json) {
        if (StringUtils.isEmpty(json)) {
            return false;
        }
        try {
            JSON.parseObject(json);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 获取JSON中的字段值
     */
    public static Object getValueFromJson(String json, String key) {
        JSONObject jsonObject = parseJSONObject(json);
        if (jsonObject == null) {
            return null;
        }
        return jsonObject.get(key);
    }
}
