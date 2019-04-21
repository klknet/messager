package com.konglk.ims.util;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

public class EncryptionUtils
{
  private int[] tmpData = new int[80];
  private int[] digestInt = new int[5];
  private final int[] abcde = { 1732584193, -271733879, -1732584194, 271733878, -1009589776 };
  
  private int process_input_bytes(byte[] bytedata)
  {
    System.arraycopy(this.abcde, 0, this.digestInt, 0, this.abcde.length);
    
    byte[] newbyte = byteArrayFormatData(bytedata);
    
    int MCount = newbyte.length / 64;
    for (int pos = 0; pos < MCount; pos++)
    {
      for (int j = 0; j < 16; j++) {
        this.tmpData[j] = byteArrayToInt(newbyte, pos * 64 + j * 4);
      }
      encrypt();
    }
    return 20;
  }
  
  private byte[] byteArrayFormatData(byte[] bytedata)
  {
    int zeros = 0;
    
    int size = 0;
    
    int n = bytedata.length;
    
    int m = n % 64;
    if (m < 56)
    {
      zeros = 55 - m;
      size = n - m + 64;
    }
    else if (m == 56)
    {
      zeros = 63;
      size = n + 8 + 64;
    }
    else
    {
      zeros = 63 - m + 56;
      size = n + 64 - m + 64;
    }
    byte[] newbyte = new byte[size];
    
    System.arraycopy(bytedata, 0, newbyte, 0, n);
    
    int l = n;
    
    newbyte[(l++)] = Byte.MIN_VALUE;
    for (int i = 0; i < zeros; i++) {
      newbyte[(l++)] = 0;
    }
    long N = n * 8L;
    byte h8 = (byte)(int)(N & 0xFF);
    byte h7 = (byte)(int)(N >> 8 & 0xFF);
    byte h6 = (byte)(int)(N >> 16 & 0xFF);
    byte h5 = (byte)(int)(N >> 24 & 0xFF);
    byte h4 = (byte)(int)(N >> 32 & 0xFF);
    byte h3 = (byte)(int)(N >> 40 & 0xFF);
    byte h2 = (byte)(int)(N >> 48 & 0xFF);
    byte h1 = (byte)(int)(N >> 56);
    newbyte[(l++)] = h1;
    newbyte[(l++)] = h2;
    newbyte[(l++)] = h3;
    newbyte[(l++)] = h4;
    newbyte[(l++)] = h5;
    newbyte[(l++)] = h6;
    newbyte[(l++)] = h7;
    newbyte[(l++)] = h8;
    
    return newbyte;
  }
  
  private int f1(int x, int y, int z)
  {
    return x & y | (x ^ 0xFFFFFFFF) & z;
  }
  
  private int f2(int x, int y, int z)
  {
    return x ^ y ^ z;
  }
  
  private int f3(int x, int y, int z)
  {
    return x & y | x & z | y & z;
  }
  
  private int f4(int x, int y)
  {
    return x << y | x >>> 32 - y;
  }
  
  private void encrypt()
  {
    for (int i = 16; i <= 79; i++) {
      this.tmpData[i] = f4(this.tmpData[(i - 3)] ^ this.tmpData[(i - 8)] ^ this.tmpData[(i - 14)] ^ 
        this.tmpData[(i - 16)], 1);
    }
    int[] tmpabcde = new int[5];
    for (int i1 = 0; i1 < tmpabcde.length; i1++) {
      tmpabcde[i1] = this.digestInt[i1];
    }
    for (int j = 0; j <= 19; j++)
    {
      int tmp = f4(tmpabcde[0], 5) + 
        f1(tmpabcde[1], tmpabcde[2], tmpabcde[3]) + tmpabcde[4] + 
        this.tmpData[j] + 1518500249;
      tmpabcde[4] = tmpabcde[3];
      tmpabcde[3] = tmpabcde[2];
      tmpabcde[2] = f4(tmpabcde[1], 30);
      tmpabcde[1] = tmpabcde[0];
      tmpabcde[0] = tmp;
    }
    for (int k = 20; k <= 39; k++)
    {
      int tmp = f4(tmpabcde[0], 5) + 
        f2(tmpabcde[1], tmpabcde[2], tmpabcde[3]) + tmpabcde[4] + 
        this.tmpData[k] + 1859775393;
      tmpabcde[4] = tmpabcde[3];
      tmpabcde[3] = tmpabcde[2];
      tmpabcde[2] = f4(tmpabcde[1], 30);
      tmpabcde[1] = tmpabcde[0];
      tmpabcde[0] = tmp;
    }
    for (int l = 40; l <= 59; l++)
    {
      int tmp = f4(tmpabcde[0], 5) + 
        f3(tmpabcde[1], tmpabcde[2], tmpabcde[3]) + tmpabcde[4] + 
        this.tmpData[l] + -1894007588;
      tmpabcde[4] = tmpabcde[3];
      tmpabcde[3] = tmpabcde[2];
      tmpabcde[2] = f4(tmpabcde[1], 30);
      tmpabcde[1] = tmpabcde[0];
      tmpabcde[0] = tmp;
    }
    for (int m = 60; m <= 79; m++)
    {
      int tmp = f4(tmpabcde[0], 5) + 
        f2(tmpabcde[1], tmpabcde[2], tmpabcde[3]) + tmpabcde[4] + 
        this.tmpData[m] + -899497514;
      tmpabcde[4] = tmpabcde[3];
      tmpabcde[3] = tmpabcde[2];
      tmpabcde[2] = f4(tmpabcde[1], 30);
      tmpabcde[1] = tmpabcde[0];
      tmpabcde[0] = tmp;
    }
    for (int i2 = 0; i2 < tmpabcde.length; i2++) {
      this.digestInt[i2] += tmpabcde[i2];
    }
    for (int n = 0; n < this.tmpData.length; n++) {
      this.tmpData[n] = 0;
    }
  }
  
  private int byteArrayToInt(byte[] bytedata, int i)
  {
    return (bytedata[i] & 0xFF) << 24 | (bytedata[(i + 1)] & 0xFF) << 16 | 
      (bytedata[(i + 2)] & 0xFF) << 8 | bytedata[(i + 3)] & 0xFF;
  }
  
  private void intToByteArray(int intValue, byte[] byteData, int i)
  {
    byteData[i] = ((byte)(intValue >>> 24));
    byteData[(i + 1)] = ((byte)(intValue >>> 16));
    byteData[(i + 2)] = ((byte)(intValue >>> 8));
    byteData[(i + 3)] = ((byte)intValue);
  }
  
  private static String byteToHexString(byte ib)
  {
    char[] Digit = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 
      'b', 'c', 'd', 'e', 'f' };
    char[] ob = new char[2];
    ob[0] = Digit[(ib >>> 4 & 0xF)];
    ob[1] = Digit[(ib & 0xF)];
    
    String s = new String(ob);
    
    return s;
  }
  
  private static String byteArrayToHexString(byte[] bytearray)
  {
    String strDigest = "";
    for (int i = 0; i < bytearray.length; i++) {
      strDigest = strDigest + byteToHexString(bytearray[i]);
    }
    return strDigest;
  }
  
  public byte[] getDigestOfBytes(byte[] byteData)
  {
    process_input_bytes(byteData);
    
    byte[] digest = new byte[20];
    for (int i = 0; i < this.digestInt.length; i++) {
      intToByteArray(this.digestInt[i], digest, i * 4);
    }
    return digest;
  }
  
  public String getDigestOfString(byte[] byteData)
  {
    return byteArrayToHexString(getDigestOfBytes(byteData));
  }
  
  public static byte[] getHmacSHA1(String data, String key)
  {
    byte[] ipadArray = new byte[64];
    byte[] opadArray = new byte[64];
    byte[] keyArray = new byte[64];
    int ex = key.length();
    EncryptionUtils sha1 = new EncryptionUtils();
    if (key.length() > 64)
    {
      byte[] temp = sha1.getDigestOfBytes(key.getBytes());
      ex = temp.length;
      for (int i = 0; i < ex; i++) {
        keyArray[i] = temp[i];
      }
    }
    else
    {
      byte[] temp = key.getBytes();
      for (int i = 0; i < temp.length; i++) {
        keyArray[i] = temp[i];
      }
    }
    for (int i = ex; i < 64; i++) {
      keyArray[i] = 0;
    }
    for (int j = 0; j < 64; j++)
    {
      ipadArray[j] = ((byte)(keyArray[j] ^ 0x36));
      opadArray[j] = ((byte)(keyArray[j] ^ 0x5C));
    }
    byte[] tempResult = (byte[])null;
    try
    {
      tempResult = sha1.getDigestOfBytes(join(ipadArray, data
        .getBytes("utf-8")));
    }
    catch (UnsupportedEncodingException e)
    {
      tempResult = 
        sha1.getDigestOfBytes(join(ipadArray, data.getBytes()));
    }
    return sha1.getDigestOfBytes(join(opadArray, tempResult));
  }
  
  private static byte[] join(byte[] b1, byte[] b2)
  {
    int length = b1.length + b2.length;
    byte[] newer = new byte[length];
    for (int i = 0; i < b1.length; i++) {
      newer[i] = b1[i];
    }
    for (int i = 0; i < b2.length; i++) {
      newer[(i + b1.length)] = b2[i];
    }
    return newer;
  }
  
  public static String bytesToHexString(byte[] src)
  {
    StringBuilder stringBuilder = new StringBuilder("");
    if ((src == null) || (src.length <= 0)) {
      return null;
    }
    for (int i = 0; i < src.length; i++)
    {
      int value = src[i];
      int v1 = value / 16;
      int v2 = value % 16;
      int v = src[i] & 0xFF;
      String hv = Integer.toHexString(v).toUpperCase();
      if (hv.length() < 2) {
        stringBuilder.append(0);
      }
      stringBuilder.append(hv);
    }
    return stringBuilder.toString();
  }
  
  public static void main(String[] args)
  {
    String appid = "f08aaf9fad6b45ffb2790eabd43567ea";
    String appkey = "13998a898f2b474abe96d8c34634adf7";
    String timestamp = "111111";
    String digest = bytesToHexString(getHmacSHA1(
      "http://10.8.10.152:8091/cms_service/changecontentsresulthttp://localhost:8090/paymentservice/paymentrequestCP2013081020130810150610020130810CP20130812000120130901����������PD001����1nrc_admin561182d945f14ee286880ed79eb36e32", 
      appkey));
    System.out.println(digest);
  }
}
