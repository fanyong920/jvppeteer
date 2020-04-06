package com.ruiyun.jvppeteer.options;

import java.util.List;

import com.ruiyun.jvppeteer.Environment;

public class OptionsBuilder {
	
	private LaunchOptions options;
	
	public OptionsBuilder() {
		options = new LaunchOptions();
	}
	
	public OptionsBuilder withExecutablePath(String executablePath) {
		options.setExecutablePath(executablePath);
		return this;
	}
	
	public OptionsBuilder withIgnoreAllDefaultArgs(boolean ignoreAllDefaultArgs) {
		options.setIgnoreAllDefaultArgs(ignoreAllDefaultArgs);
		return this;
	}
	
	public OptionsBuilder withIgnoreDefaultArgs(List<String> ignoreDefaultArgs) {
		options.setIgnoreDefaultArgs(ignoreDefaultArgs);
		return this;
	}
	
	public OptionsBuilder withHandleSIGINT(boolean handleSIGINT) {
		options.setHandleSIGINT(handleSIGINT);
		return this;
	}
	
	public OptionsBuilder withHandleSIGTERM(boolean handleSIGTERM) {
		options.setHandleSIGTERM(handleSIGTERM);
		return this;
	}
	
	public OptionsBuilder withHandleSIGHUP(boolean handleSIGHUP) {
		options.setHandleSIGHUP(handleSIGHUP);
		return this;
	}
	
	public OptionsBuilder withDumpio(boolean dumpio) {
		options.setDumpio(dumpio);
		return this;
	}
	
	public OptionsBuilder withEnv(Environment env) {
		options.setEnv(env);
		return this;
	}
	
	public OptionsBuilder withPipe(boolean pipe) {
		options.setPipe(pipe);
		return this;
	}
	
	public OptionsBuilder withProduct(String product) {
		options.setProduct(product);
		return this;
	}
	
	public OptionsBuilder withIgnoreHTTPSErrors(boolean ignoreHTTPSErrors) {
		options.setIgnoreHTTPSErrors(ignoreHTTPSErrors);
		return this;
	}
	
	public OptionsBuilder withViewport(Viewport viewport) {
		options.setViewport(viewport);
		return this;
	}
	
	public OptionsBuilder withSlowMo(int slowMo) {
		options.setSlowMo(slowMo);
		return this;
	}
	
	public OptionsBuilder withHeadless(boolean headless) {
		options.setHeadless(headless);
		return this;
	}
	
	public OptionsBuilder withArgs(List<String> args) {
		options.setArgs(args);
		return this;
	}
	
	public OptionsBuilder withUserDataDir(String userDataDir) {
		options.setUserDataDir(userDataDir);
		return this;
	}
	
	public OptionsBuilder withDevtools(boolean devtools) {
		options.setDevtools(devtools);
		return this;
	}
	
	public LaunchOptions build() {
		return options;
	}
}
