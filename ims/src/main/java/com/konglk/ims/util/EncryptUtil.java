package com.konglk.ims.util;

import org.apache.commons.lang3.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by konglk on 2019/6/6.
 */
public class EncryptUtil {

    private static String aesKey = "A1E3K3J3O9E8N6V5";
    private static String iv = "1w2e3r4t5j6i7i9k";
    private static String ALGORITHM = "AES/CBC/PKCS5Padding";


    public static String encrypt(String content) {
        if (StringUtils.isEmpty(content))
            return null;
        try {
            SecretKeySpec skeySpec = new SecretKeySpec(aesKey.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, new IvParameterSpec(iv.getBytes()));
            byte[] encrypted = cipher.doFinal(content.getBytes());
            return parseByte2HexStr(encrypted);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }


    public static String decrypt(String cipherText) {
        if (StringUtils.isEmpty(cipherText))
            return null;
        try {
            SecretKeySpec skeySpec = new SecretKeySpec(aesKey.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, new IvParameterSpec(iv.getBytes()));
            byte[] encrypted1 = parseHexStr2Byte(cipherText);
            byte[] original = cipher.doFinal(encrypted1);
            return new String(original);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**将二进制转换成16进制
     * @param buf
     * @return
     */
    public static String parseByte2HexStr(byte buf[]) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < buf.length; i++) {
            String hex = Integer.toHexString(buf[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex.toUpperCase());
        }
        return sb.toString();
    }

    /**将16进制转换为二进制
     * @param hexStr
     * @return
     */
    public static byte[] parseHexStr2Byte(String hexStr) {
        if (hexStr.length() < 1)
            return null;
        byte[] result = new byte[hexStr.length()/2];
        for (int i = 0;i< hexStr.length()/2; i++) {
            int high = Integer.parseInt(hexStr.substring(i*2, i*2+1), 16);
            int low = Integer.parseInt(hexStr.substring(i*2+1, i*2+2), 16);
            result[i] = (byte) (high * 16 + low);
        }
        return result;
    }


    public static String getAesKey() {
        return aesKey;
    }

    public static String getIv() {
        return iv;
    }

    public static void main(String[] args) {
        String text = "hello world";
        String cipher = encrypt(text);
        System.out.println(cipher);
        System.out.println(decrypt(cipher));
    }
}
