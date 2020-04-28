package com.ruiyun.jvppeteer.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

/**
 * �������Ĺ���
 */
public class StreamUtil {
	
	public static void closeStream(Closeable closeable) {
	    if (closeable != null) {
	      try {
	        closeable.close();
	      } catch (IOException e) {
	        // Ignore this exception.
	      }
	    }
	  }

	public static void close(Thread readLineThread) {
		// TODO Auto-generated method stub
		if(readLineThread != null) {
			readLineThread = null;
		}
	}
	public static final String toString(InputStream in){
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
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			StreamUtil.closeStream(wirter);
			StreamUtil.closeStream(reader);
		}
		return null;
	}
}
