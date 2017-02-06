package cn.a6_79.bicycle;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

class HmacMD5 {
    private static final String KEY_MAC = "HmacMD5";

    /* Hmac加密 */
    private static byte[] encryptHmac(byte[] data, String key) {
        SecretKey secretKey = new SecretKeySpec(key.getBytes(), KEY_MAC);
        Mac mac;
        try {
            mac = Mac.getInstance(secretKey.getAlgorithm());
            mac.init(secretKey);
            return mac.doFinal(data);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
        }
        return null;
    }

    /*byte数组转换为HexString*/
    private static String byteArrayToHexString(byte[] b) {
        StringBuilder sb = new StringBuilder(b.length * 2);
        for (byte aB : b) {
            int v = aB & 0xff;
            if (v < 16) {
                sb.append('0');
            }
            sb.append(Integer.toHexString(v));
        }
        return sb.toString();
    }

    static String execute(String data, String key) {
        return byteArrayToHexString(encryptHmac(data.getBytes(), key));
    }
}
