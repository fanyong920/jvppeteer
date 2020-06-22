package com.ruiyun.jvppeteer.options;

import java.util.List;

import com.ruiyun.jvppeteer.core.Environment;

public class LaunchOptionsBuilder {
	
	private LaunchOptions options;
	
	public LaunchOptionsBuilder() {
		options = new LaunchOptions();
	}
	
	public LaunchOptionsBuilder withExecutablePath(String executablePath) {
		options.setExecutablePath(executablePath);
		return this;
	}

	/**
	 * 是否忽略所欲的默认启动参数，默认是fasle
	 * @param ignoreAllDefaultArgs true为忽略所有启动参数
	 * @return LaunchOptionsBuilder
	 */
	public LaunchOptionsBuilder withIgnoreDefaultArgs(boolean ignoreAllDefaultArgs) {
		options.setIgnoreAllDefaultArgs(ignoreAllDefaultArgs);
		return this;
	}

	/**
	 * 忽略指定的默认启动参数，默认的启动参数见 {@link com.ruiyun.jvppeteer.core.Constant#DEFAULT_ARGS}
	 * @param ignoreDefaultArgs 要忽略的启动参数
	 * @return LaunchOptionsBuilder
	 */
	public LaunchOptionsBuilder withIgnoreDefaultArgs(List<String> ignoreDefaultArgs) {
		options.setIgnoreDefaultArgs(ignoreDefaultArgs);
		return this;
	}
	
	public LaunchOptionsBuilder withHandleSIGINT(boolean handleSIGINT) {
		options.setHandleSIGINT(handleSIGINT);
		return this;
	}
	
	public LaunchOptionsBuilder withHandleSIGTERM(boolean handleSIGTERM) {
		options.setHandleSIGTERM(handleSIGTERM);
		return this;
	}
	
	public LaunchOptionsBuilder withHandleSIGHUP(boolean handleSIGHUP) {
		options.setHandleSIGHUP(handleSIGHUP);
		return this;
	}
	
	public LaunchOptionsBuilder withDumpio(boolean dumpio) {
		options.setDumpio(dumpio);
		return this;
	}
	
	public LaunchOptionsBuilder withEnv(Environment env) {
		options.setEnv(env);
		return this;
	}
	
	public LaunchOptionsBuilder withPipe(boolean pipe) {
		options.setPipe(pipe);
		return this;
	}
	
	public LaunchOptionsBuilder withProduct(String product) {
		options.setProduct(product);
		return this;
	}
	
	public LaunchOptionsBuilder withIgnoreHTTPSErrors(boolean ignoreHTTPSErrors) {
		options.setIgnoreHTTPSErrors(ignoreHTTPSErrors);
		return this;
	}
	
	public LaunchOptionsBuilder withViewport(Viewport viewport) {
		options.setViewport(viewport);
		return this;
	}
	
	public LaunchOptionsBuilder withSlowMo(int slowMo) {
		options.setSlowMo(slowMo);
		return this;
	}
	
	public LaunchOptionsBuilder withHeadless(boolean headless) {
		options.setHeadless(headless);
		return this;
	}
	
	public LaunchOptionsBuilder withArgs(List<String> args) {
		options.setArgs(args);
		return this;
	}
	
	public LaunchOptionsBuilder withUserDataDir(String userDataDir) {
		options.setUserDataDir(userDataDir);
		return this;
	}
	
	public LaunchOptionsBuilder withDevtools(boolean devtools) {
		options.setDevtools(devtools);
		return this;
	}
	
	public LaunchOptions build() {
		return options;
	}
}
