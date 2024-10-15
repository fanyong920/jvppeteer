package com.ruiyun.jvppeteer.util;

import java.util.Base64;

/**
 * Base64操作工具，兼容RFC4648和RFC2045
 *
 * @author sage.xue
 */
public class Base64Util {

    /**
     * BASE64字符串解码，如果抛出IllegalArgumentException尝试使用RFC2045标准解析（兼容JDK7及以下版本）
     *
     * @param src 字节码
     * @return 编码后的字节码
     */
    public static byte[] decode(byte[] src) {
        try {
            return Base64.getDecoder().decode(src);
        } catch (IllegalArgumentException e) {
            // maybe RFC2045(MIME)
            return Base64.getMimeDecoder().decode(src);
        }
    }
    /**
     * BASE64编码
     *
     * @param src 字节码
     * @return 编码后的BASE64字符串
     */
    public static String encode(byte[] src) {
        return Base64.getEncoder().encodeToString(src);
    }

}
