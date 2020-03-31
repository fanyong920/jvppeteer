package com.ruiyun.jvppeteer.launch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.ruiyun.jvppeteer.exception.LaunchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ruiyun.jvppeteer.browser.Browser;
import com.ruiyun.jvppeteer.browser.BrowserFetcher;
import com.ruiyun.jvppeteer.browser.BrowserRunner;
import com.ruiyun.jvppeteer.browser.RevisionInfo;
import com.ruiyun.jvppeteer.options.LaunchOptions;
import com.ruiyun.jvppeteer.transport.Connection;
import com.ruiyun.jvppeteer.util.FileUtil;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.ruiyun.jvppeteer.util.ValidateUtil;

public class ChromeLauncher implements Launcher {

	private static final Logger LOGGER = LoggerFactory.getLogger(ChromeLauncher.class);
	
	private boolean isPuppeteerCore;

	public ChromeLauncher(boolean isPuppeteerCore) {
		super();
		this.isPuppeteerCore = isPuppeteerCore;
	}

	@Override
	public Browser launch(LaunchOptions options) {
		List<String> chromeArguments = new ArrayList<>();
		String temporaryUserDataDir = defaultArgs(options, chromeArguments);
		String chromeExecutable = resolveExecutablePath(options.getExecutablePath());
		boolean usePipe = chromeArguments.contains("--remote-debugging-pipe");
		
		LOGGER.info("will try launch chrome process with arguments:"+chromeArguments);
		BrowserRunner runner = new BrowserRunner(chromeExecutable, chromeArguments, temporaryUserDataDir);//
		try {
			runner.start(options.getHandleSIGINT(), options.getHandleSIGTERM(), options.getHandleSIGHUP(), options.getDumpio(), usePipe);
			Connection connection = runner.setUpConnection(usePipe,options.getTimeout(),options.getSlowMo(),"");
			Browser browser = Browser.create(connection, null, options.getIgnoreHTTPSErrors(), options.getDefaultViewport(), runner);
			browser.waitForTarget(t -> "page".equals(t.type()),options);
			return browser;
		} catch (IOException e) {
			runner.kill();
			throw new LaunchException("Failed to launch the browser process:"+e.getMessage(),e);
		}
	}

	/**
	 * @param options
	 * @param chromeArguments
	 * @param pipe
	 * @param temporaryUserDataDir
	 * @return
	 */
	@Override
	public String defaultArgs(LaunchOptions options, List<String> chromeArguments) {
		boolean pipe = options.getPipe();
		String temporaryUserDataDir = null;
		if (!options.getIgnoreAllDefaultArgs()) {
			chromeArguments.addAll(DEFAULT_ARGS);
		}
		List<String> args = null;
		if (ValidateUtil.isNotEmpty(args = options.getArgs())) {
			chromeArguments.add("about:blank");
			chromeArguments.addAll(args);
		}
		
		boolean devtools = options.getDevtools();
		boolean headless = options.getHeadless();
		if (devtools) {
			chromeArguments.add("--auto-open-devtools-for-tabs");
			headless = false;
		}
		      
		if(headless) {
			chromeArguments.add("--headless");
			chromeArguments.add("--hide-scrollbars");
			chromeArguments.add("--mute-audio");
		}
		List<String> ignoreDefaultArgs = null;
		if (ValidateUtil.isNotEmpty(ignoreDefaultArgs = options.getIgnoreDefaultArgs())) {
			chromeArguments.removeAll(ignoreDefaultArgs);
		}
		
		boolean isCustomUserDir = false;
		boolean isCustomRemoteDebugger = false;
		for (String arg : chromeArguments) {
			if (arg.startsWith("--remote-debugging-")) {
				isCustomRemoteDebugger = true;
			}
			if (arg.startsWith("--user-data-dir")) {
				isCustomUserDir = true;
			}
		}
		if(!isCustomUserDir) {
			temporaryUserDataDir = FileUtil.createProfileDir(PROFILE_PREFIX);
			chromeArguments.add("--user-data-dir=" + temporaryUserDataDir);
		}
		if(!isCustomRemoteDebugger) {
			chromeArguments.add(pipe ? "--remote-debugging-pipe" : "--remote-debugging-port=0");
		}
		return temporaryUserDataDir;
	}
	
	/**
	 * Ѱ�ҿ�ִ�е�chrome·��
	 * @param chromeExecutable
	 * @return
	 */
	@Override
	public String resolveExecutablePath(String chromeExecutable) {
		boolean puppeteerCore = getIsPuppeteerCore();
		if (!puppeteerCore) {
			if (StringUtil.isNotEmpty(chromeExecutable)) {
				boolean assertDir = FileUtil.assertExecutable(chromeExecutable);
				if (!assertDir) {
					throw new IllegalArgumentException("given chromeExecutable is not executable");
				}
				return chromeExecutable;
			} else {
				for (int i = 0; i < EXECUTABLE_ENV.length; i++) {
					chromeExecutable = env.getEnv(EXECUTABLE_ENV[i]);
					if (StringUtil.isNotEmpty(chromeExecutable)) {
						boolean assertDir = FileUtil.assertExecutable(chromeExecutable);
						if (!assertDir) {
							throw new IllegalArgumentException("given chromeExecutable is not is not executable");
						}
						return chromeExecutable;
					}
				}
				
				for (int i = 0; i < PROBABLE_CHROME_EXECUTABLE_PATH.length; i++) {
					chromeExecutable = PROBABLE_CHROME_EXECUTABLE_PATH[i];
					if (StringUtil.isNotEmpty(chromeExecutable)) {
						boolean assertDir = FileUtil.assertExecutable(chromeExecutable);
						if (assertDir) {
							return chromeExecutable;
						}
					}
				}
				
				throw new RuntimeException(
						"Tried to use PUPPETEER_EXECUTABLE_PATH env variable to launch browser but did not find any executable");
			}
		}

		BrowserFetcher browserFetcher = new BrowserFetcher();
		String revision = env.getEnv(PUPPETEER_CHROMIUM_REVISION_ENV);
		if (StringUtil.isNotEmpty(revision)) {
			RevisionInfo revisionInfo = browserFetcher.revisionInfo(revision);
			if (!revisionInfo.getLocal()) {
				throw new RuntimeException(
						"Tried to use PUPPETEER_CHROMIUM_REVISION env variable to launch browser but did not find executable at: "
								+ revisionInfo.getExecutablePath());
			}
			return revisionInfo.getExecutablePath();
		} else {
			throw new RuntimeException(
					"Tried to use PUPPETEER_CHROMIUM_REVISION env variable to launch browser but did not find executable ");
		}

	}
	
	public boolean getIsPuppeteerCore() {
		return isPuppeteerCore;
	}

	public void setIsPuppeteerCore(boolean isPuppeteerCore) {
		this.isPuppeteerCore = isPuppeteerCore;
	}

	@Override
	public Browser connect(Object object) {
		// TODO Auto-generated method stub
		return null;
	}

}
