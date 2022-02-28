package com.ruiyun.jvppeteer.util;

import java.util.Base64;

/**
 * Base64操作工具，兼容RFC4648和RFC2045
 *
 * @author sage.xue
 * @date 2022/2/20 15:59
 */
public class Base64Util {

    /**
     * BASE64字符串解码，如果抛出IllegalArgumentException尝试使用RFC2045标准解析（兼容JDK7及以下版本）
     *
     * @param src
     * @return
     */
    public static byte[] decode(byte[] src) {
        try {
            return Base64.getDecoder().decode(src);
        } catch (IllegalArgumentException e) {
            // maybe RFC2045(MIME)
            return Base64.getMimeDecoder().decode(src);
        }
    }

}
