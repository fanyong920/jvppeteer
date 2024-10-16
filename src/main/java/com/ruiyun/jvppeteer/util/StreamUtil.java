package com.ruiyun.jvppeteer.util;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

/**
 * 流的工具类
 */
public class StreamUtil {
    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                // Ignore this exception.
            }
        }
    }
    public static String toString(InputStream in) throws IOException {
        try (in; InputStreamReader reader = new InputStreamReader(in); StringWriter writer = new StringWriter()) {
            int bufferSize = 4096;
            int perReadcount;
            char[] buffer = new char[bufferSize];
            while ((perReadcount = reader.read(buffer, 0, bufferSize)) != -1) {
                writer.write(buffer, 0, perReadcount);
            }
            return writer.toString();
        }
    }
}
