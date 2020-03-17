package com.ruiyun.jvppeteer.browser;

import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.List;

import com.ruiyun.jvppeteer.Connection;
import com.ruiyun.jvppeteer.Environment;

public class BrowserRunner {
	
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
		 ProcessBuilder processBuilder = new ProcessBuilder().command(arguments);
		 /** connect by pipe  默认就是pipe管道连接*/
//		 if(pipe) {
//			 processBuilder.redirectOutput(Redirect.PIPE).redirectInput(Redirect.PIPE);
//		 }
		 process = processBuilder.start();
		 //TODO 添加listener 
	}
	
	public void setupConnection() {
		
	}
}
