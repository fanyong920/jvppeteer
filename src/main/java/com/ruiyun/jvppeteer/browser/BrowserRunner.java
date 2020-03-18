package com.ruiyun.jvppeteer.browser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ruiyun.jvppeteer.Connection;
import com.ruiyun.jvppeteer.exception.LaunchTimeOutException;
import com.ruiyun.jvppeteer.transport.ConnectionTransport;
import com.ruiyun.jvppeteer.transport.WebSocketTransport;
import com.ruiyun.jvppeteer.util.StreamUtil;

public class BrowserRunner {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(BrowserRunner.class);
	
	private static final Pattern WS_ENDPOINT_PATTERN = Pattern.compile( "^DevTools listening on (ws:\\/\\/.*)$");

	private String executablePath; 
	
	private List<String> processArguments;
	
	private String tempDirectory;
	
	private Process process;
	
	private Connection connection;
	
	private boolean closed; 
	
	private List<String> listeners;

	public BrowserRunner(String executablePath, List<String> processArguments, String tempDirectory) {
		super();
		this.executablePath = executablePath;
		this.processArguments = processArguments;
		this.tempDirectory = tempDirectory;
	}
	/**
	 * 启动浏览器 ,默认已经是使用系统环境变量
	 * <br/>
	 * Start your browser
	 * @param handleSIGINT
	 * @param handleSIGTERM
	 * @param handleSIGHUP
	 * @param dumpio
	 * @param env
	 * @param pipe
	 * @throws IOException 
	 */
	public void start(boolean handleSIGINT,boolean handleSIGTERM,boolean handleSIGHUP,boolean dumpio,boolean pipe) throws IOException {
		if(process != null) {
			throw new RuntimeException("This process has previously been started.");
		}
		
		 List<String> arguments = new ArrayList<>();
		 arguments.add(executablePath);
		 arguments.addAll(processArguments);
		 ProcessBuilder processBuilder = new ProcessBuilder().command(arguments).redirectErrorStream(true);
		 /** connect by pipe  默认就是pipe管道连接*/
		 process = processBuilder.start();
		 //TODO 添加listener 
	}
	
	public Connection setUpConnection(boolean usePipe,int timeout,int slowMo,String preferredRevision) {
		Connection connection = null;
		if(usePipe) {/** pipe connection*/
			
		}else {/**websoket connection*/
			String waitForWSEndpoint = waitForWSEndpoint(timeout,preferredRevision);
			ConnectionTransport transport = new WebSocketTransport(waitForWSEndpoint);
			connection = new Connection(waitForWSEndpoint, transport , timeout);
		}
		return connection ;
	}
	/**
	 * acquired browser ws 
	 * @param timeout
	 * @param preferredRevision
	 * @return
	 */
	private String waitForWSEndpoint( int timeout, String preferredRevision) {
		final StringBuilder ws = new StringBuilder();
		final AtomicBoolean success = new AtomicBoolean(false);
		final AtomicReference<String> chromeOutput = new AtomicReference<>("");

	    Thread readLineThread =
	        new Thread(
	            () -> {
	              StringBuilder chromeOutputBuilder = new StringBuilder();
	              BufferedReader reader = null;
	              try {
	                reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

	                String line;
	                while ((line = reader.readLine()) != null) {
	                  Matcher matcher = WS_ENDPOINT_PATTERN.matcher(line);
	                  if (matcher.find()) {
	                	ws.append(matcher.group(1));
	                	LOGGER.info("ws:"+ws.toString());
	                    success.set(true);
	                    break;
	                  }

	                  if (chromeOutputBuilder.length() != 0) {
	                    chromeOutputBuilder.append(System.lineSeparator());
	                  }
	                  chromeOutputBuilder.append(line);
	                  chromeOutput.set(chromeOutputBuilder.toString());
	                }
	              } catch (Exception e) {
	            	  LOGGER.error("Failed to launch the browser process!please see TROUBLESHOOTING: https://github.com/puppeteer/puppeteer/blob/master/docs/troubleshooting.md:", e);
	              } finally {
	            	  StreamUtil.closeStream(reader);
	              }
	            });

	    readLineThread.start();

	    try {
	      readLineThread.join(timeout);

	      if (!success.get()) {
	    	  StreamUtil.close(readLineThread);

	        throw new LaunchTimeOutException(
	            "Timed out after "+timeout+" ms while trying to connect to the browser!"
	                + "Chrome output: "
	                + chromeOutput.get());
	      }
	    } catch (InterruptedException e) {
	    	StreamUtil.close(readLineThread);

	    	LOGGER.error("Interrupted while waiting for dev tools server.", e);
	      throw new RuntimeException("Interrupted while waiting for dev tools server.", e);
	    }

	    return ws.toString();
		
	}
}
