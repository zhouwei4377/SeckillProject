package com.seckill.common.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 请求工具类
 * 用于解析和处理HTTP请求
 */
@Slf4j
public class RequestUtil {
    
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    
    /**
     * 获取请求体内容
     */
    public static String getRequestBody(HttpServletRequest request) {
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader bufferedReader = request.getReader()) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            log.warn("读取请求体失败", e);
            return null;
        }
        return stringBuilder.toString();
    }
    
    /**
     * 解析JSON请求体
     */
    public static JsonNode parseJsonBody(HttpServletRequest request) {
        String body = getRequestBody(request);
        if (StringUtils.isEmpty(body)) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readTree(body);
        } catch (Exception e) {
            log.warn("解析JSON失败: {}", body, e);
            return null;
        }
    }
    
    /**
     * 获取请求头
     */
    public static String getHeader(HttpServletRequest request, String headerName) {
        return request.getHeader(headerName);
    }
    
    /**
     * 获取User-Agent
     */
    public static String getUserAgent(HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }
    
    /**
     * 获取请求来源 (Referer)
     */
    public static String getReferer(HttpServletRequest request) {
        return request.getHeader("Referer");
    }
    
    /**
     * 获取所有请求头
     */
    public static Map<String, String> getAllHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        java.util.Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames != null) {
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                headers.put(headerName, request.getHeader(headerName));
            }
        }
        return headers;
    }
    
    /**
     * 获取请求协议
     */
    public static String getProtocol(HttpServletRequest request) {
        return request.getProtocol();
    }
    
    /**
     * 获取请求方法
     */
    public static String getMethod(HttpServletRequest request) {
        return request.getMethod();
    }
    
    /**
     * 获取请求URI
     */
    public static String getRequestUri(HttpServletRequest request) {
        return request.getRequestURI();
    }
}
