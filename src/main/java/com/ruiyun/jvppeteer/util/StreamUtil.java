package com.ruiyun.jvppeteer.util;

import java.io.Closeable;
import java.io.IOException;

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
}
