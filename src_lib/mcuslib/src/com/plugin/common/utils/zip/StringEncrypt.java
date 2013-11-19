package com.plugin.common.utils.zip;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 13-11-19
 * Time: PM2:29
 * To change this template use File | Settings | File Templates.
 */
public class StringEncrypt {

    /**
     * 加密
     *
     * @param content
     *            需要加密的内容
     * @param password
     *            加密密码
     * @return
     */
    public static byte[] encrypt(String content, String password) {
        try {
            byte[] enCodeFormat = password.substring(0, 16).getBytes();
            SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");// 创建密码器
            cipher.init(Cipher.ENCRYPT_MODE, key);// 初始化
            byte[] result = cipher.doFinal(content.getBytes(Charset.forName("UTF-8")));
            return result; // 加密
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static SecretKeySpec makeSecretKey(String password) throws Exception {
        SecretKeySpec key = null;
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG", "Crypto");
        sr.setSeed(password.getBytes());
        kgen.init(128, sr);
        SecretKey secretKey = kgen.generateKey();
        byte[] enCodeFormat = secretKey.getEncoded();
        key = new SecretKeySpec(enCodeFormat, "AES");

        return key;
    }

    /**
     * 解密
     *
     * @param content
     *            待解密内容
     * @param password
     *            解密密钥
     * @return
     */
    public static byte[] decrypt(byte[] content, String password) {
        try {
            byte[] enCodeFormat = password.substring(0, 16).getBytes();
            SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");// 创建密码器
            cipher.init(Cipher.DECRYPT_MODE, key);// 初始化
            byte[] result = cipher.doFinal(content);
            return result; // 加密
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static final int[] FIBONAACI_20 = new int[] { 1, 1, 2, 3, 5, 8, 13, 21, 34, 55, 89, 144, 233, 377, 610, 987, 1597, 2584, 4181 };

    public static int getFibonacciFator(int index) {
        if (index >= 0) {
            if (FIBONAACI_20.length > index) {
                return FIBONAACI_20[index];
            } else {
                fibonacci(index);
            }
        }

        return 0;
    }

    public static int fibonacci(int n) {
        if (n <= 2) {
            return 1;
        }

        int result = 0;
        int first = 0, second = 1;

        for (int i = 1; i < n; i++) {
            result = first + second;
            first = second;
            second = result;
        }

        return result;
    }

}
