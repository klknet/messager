//package com.whty.apigateway.test;
//
//import com.whty.apigateway.security.EncryptionUtils;
//
///**
// * ****************************************************************************************
// * 类描述: hmac加密
// *
// * @author Leo Fung
// * @date 2014-4-12
// * <p>
// * -----------------------------------------------------------------------------------
// *  <任务名称>          <修改时间>      <修改人>    <修改描述>
// * -----------------------------------------------------------------------------------
// * <p>
// *
// *
// *
// *
// * -----------------------------------------------------------------------------------
// * ****************************************************************************************
// */
//public class TokenGeneratorTest {
//	public static void main(String args[]) {
//		/*String appid = "abcdefgh";
//		String appkey = "abcdefgh";*/
//
//		String appid = "AP000000012274";
//		String appkey = "7ab43f7723eaec79b837c45ae53afb72";
//		// String ts=new Long(System.currentTimeMillis()).toString();
//		String ts = "1400232822323";
//		System.out.println("ts:" + ts);
//		String paramValues = appid + appkey + ts;
//		byte[] hmacSHA = EncryptionUtils.getHmacSHA1(paramValues, appkey);
////		String digest = EncryptionUtils.bytesToHexString(hmacSHA);
//		digest = digest.toUpperCase();
//		System.out.println("digest:" + digest);
//	}
//}
