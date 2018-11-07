package com.lin.blockchain.blockchaindemo.util;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.codec.digest.DigestUtils;

import java.security.Key;
import java.security.MessageDigest;


/**
 * 公共方法
 */
public class CommonUtil {
    /**
     * 将输入的字符串进行加密
     * @param input
     * @return
     */
    public static String applySha256(String input) {
        MessageDigest digest = DigestUtils.getSha256Digest();
        byte[] hash = digest.digest(StringUtils.getBytesUtf8(input));
        return Hex.encodeHexString(hash);
    }

    public static String getStringFromKey(Key key) {
        /*eturn Base64.getEncoder().encodeToString(key.getEncoded());*/
        return key.getEncoded().toString();
    }
}
