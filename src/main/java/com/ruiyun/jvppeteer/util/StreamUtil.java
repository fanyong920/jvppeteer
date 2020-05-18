package com.ruiyun.jvppeteer.util;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

/**
 *流的工具类
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

	public static void close(Thread readLineThread) {
		if(readLineThread != null) {
			readLineThread = null;
		}
	}
	public static final String toString(InputStream in) throws IOException {
		StringWriter wirter = null;
		InputStreamReader reader = null;
		try {
			reader = new InputStreamReader(in);
			int bufferSize = 4096;
			int perReadcount = -1;
			char[] buffer = new char[bufferSize];
			wirter = new StringWriter();
			while ((perReadcount = reader.read(buffer, 0, bufferSize)) != -1) {
				wirter.write(buffer, 0, perReadcount);
			}
			return wirter.toString();
		}finally {
			StreamUtil.closeQuietly(wirter);
			StreamUtil.closeQuietly(reader);
		}
	}
}
