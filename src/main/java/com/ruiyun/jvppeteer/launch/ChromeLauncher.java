package com.ruiyun.jvppeteer.launch;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ruiyun.jvppeteer.Browser;
import com.ruiyun.jvppeteer.BrowserFetcher;
import com.ruiyun.jvppeteer.Launcher;
import com.ruiyun.jvppeteer.browser.RevisionInfo;
import com.ruiyun.jvppeteer.options.LaunchOptions;
import com.ruiyun.jvppeteer.util.FileUtil;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.ruiyun.jvppeteer.util.ValidateUtil;

public class ChromeLauncher implements Launcher {

	private static final Logger log = LoggerFactory.getLogger(ChromeLauncher.class);
	private boolean isPuppeteerCore;

	private static String root = env.getEnv("user.dir");

	public ChromeLauncher(boolean isPuppeteerCore) {
		super();
		this.isPuppeteerCore = isPuppeteerCore;
	}

	@Override
	public Browser launch(LaunchOptions options) {
		List<String> chromeArguments = new ArrayList<>();
		boolean pipe = options.getPipe();

		int timestamp = StringUtil.getTimestamp();

		if (!options.getIgnoreAllDefaultArgs()) {
			chromeArguments.addAll(DEFAULT_ARGS);
		}
		List<String> args = null;
		if (ValidateUtil.isNotEmpty(args = options.getArgs())) {
			chromeArguments.addAll(args);
		}
		List<String> ignoreDefaultArgs = null;
		if (ValidateUtil.isNotEmpty(ignoreDefaultArgs = options.getIgnoreDefaultArgs())) {
			chromeArguments.removeAll(ignoreDefaultArgs);
		}

		chromeArguments.forEach(arg -> {
			if (arg.startsWith("--remote-debugging-")) {
				chromeArguments.add(pipe ? "--remote-debugging-pipe" : "--remote-debugging-port=0");
			}
			if (arg.startsWith("--user-data-dir")) {
				String profilePath = FileUtil.createProfileDir(PROFILE_PREFIX);
				chromeArguments.add("--user-data-dir=" + profilePath);
			}
		});
		 String executablePath = resolveExecutablePath(options.getExecutablePath());
		return null;
	}

	private String resolveExecutablePath(String chromeExecutable) {
		boolean puppeteerCore = getIsPuppeteerCore();
		if (!puppeteerCore) {
			if (StringUtil.isNotEmpty(chromeExecutable)) {
				boolean assertDir = FileUtil.assertDir(chromeExecutable);
				if (!assertDir) {
					throw new IllegalArgumentException("given chromeExecutable is not real exist");
				}
				return chromeExecutable;
			} else {
				for (int i = 0; i < EXECUTABLE_ENV.length; i++) {
					chromeExecutable = env.getEnv(EXECUTABLE_ENV[i]);
					if (StringUtil.isNotEmpty(chromeExecutable)) {
						return chromeExecutable;
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

}
