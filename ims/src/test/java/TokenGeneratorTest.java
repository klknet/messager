
import com.konglk.ims.util.EncryptionUtils;

/**
 * ****************************************************************************************
 * 类描述: hmac加密
 *
 * @author Leo Fung
 * @date 2014-4-12
 * <p>
 * -----------------------------------------------------------------------------------
 *  <任务名称>          <修改时间>      <修改人>    <修改描述>
 * -----------------------------------------------------------------------------------
 * <p>
 *
 *
 *
 *
 * -----------------------------------------------------------------------------------
 * ****************************************************************************************
 */
public class TokenGeneratorTest {
	public static void main(String args[]) {
		/*String appid = "abcdefgh";
		String appkey = "abcdefgh";*/

		String appid = "8A4B4DEB1919C9805F4B624121C3B378";
		String appkey = "57C3739DCB0192E593F78A3935C51AE9";
		 String ts=new Long(System.currentTimeMillis()).toString();
//		String ts = "1553750982598";
		System.out.println("ts:" + ts);
		String paramValues = appid + appkey + ts;
		byte[] hmacSHA = EncryptionUtils.getHmacSHA1(paramValues, appkey);
		String digest = EncryptionUtils.bytesToHexString(hmacSHA);
		digest = digest.toUpperCase();
		System.out.println("digest:" + digest);
	}
}
