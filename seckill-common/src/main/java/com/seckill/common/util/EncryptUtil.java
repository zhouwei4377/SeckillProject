package com.seckill.common.util;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;

/**
 * 加密工具类
 * 提供MD5、SHA256、AES等加密方法
 */
public class EncryptUtil {
    
    private static final String AES_ALGORITHM = "AES";
    private static final int AES_KEY_SIZE = 256;
    
    /**
     * MD5 加密
     */
    public static String md5(String data) {
        return DigestUtils.md5Hex(data);
    }
    
    /**
     * SHA256 加密
     */
    public static String sha256(String data) {
        return DigestUtils.sha256Hex(data);
    }
    
    /**
     * SHA1 加密
     */
    public static String sha1(String data) {
        return DigestUtils.sha1Hex(data);
    }
    
    /**
     * 计算HMAC-SHA256
     */
    public static String hmacSha256(String data, String key) {
        try {
            return DigestUtils.sha256Hex(data + key);
        } catch (Exception e) {
            throw new RuntimeException("HMAC SHA256 加密失败", e);
        }
    }
    
    /**
     * AES 加密
     */
    public static String aesEncrypt(String data, String key) {
        try {
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                key.getBytes(StandardCharsets.UTF_8), 0, 16, AES_ALGORITHM
            );
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            byte[] encryptedData = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Hex.encodeHexString(encryptedData);
        } catch (Exception e) {
            throw new RuntimeException("AES 加密失败", e);
        }
    }
    
    /**
     * AES 解密
     */
    public static String aesDecrypt(String encryptedData, String key) {
        try {
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                key.getBytes(StandardCharsets.UTF_8), 0, 16, AES_ALGORITHM
            );
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            byte[] decodedData = Hex.decodeHex(encryptedData);
            byte[] decryptedData = cipher.doFinal(decodedData);
            return new String(decryptedData, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("AES 解密失败", e);
        }
    }
    
    /**
     * 生成随机密钥
     */
    public static String generateRandomKey(int size) {
        SecureRandom secureRandom = new SecureRandom();
        byte[] key = new byte[size / 8];
        secureRandom.nextBytes(key);
        return Hex.encodeHexString(key);
    }
}
