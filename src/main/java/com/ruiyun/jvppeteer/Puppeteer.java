package com.ruiyun.jvppeteer;

import com.ruiyun.jvppeteer.launch.ChromeLauncher;
import com.ruiyun.jvppeteer.launch.FirefoxLauncher;
import com.ruiyun.jvppeteer.options.LaunchOptions;
import com.ruiyun.jvppeteer.util.StringUtil;

/**
 * Puppeteer 也可以用来控制 Chrome 浏览器， 但它与绑定的 Chromium
 * 版本在一起使用效果最好。不能保证它可以与任何其他版本一起使用。谨慎地使用 executablePath 选项。 如果 Google
 * Chrome（而不是Chromium）是首选，一个 Chrome Canary 或 Dev Channel 版本是建议的
 * 
 * @author fff
 *
 */
public class Puppeteer implements Constant {

	public static String _productName = null;

	private Launcher launcher;
	private Environment env = null;
//	private String projectRoot;
//
//	private String preferredRevision;

	private boolean isPuppeteerCore;

	public Puppeteer() {

	}

	public Puppeteer(boolean isPuppeteerCore) {
		super();
		this.isPuppeteerCore = isPuppeteerCore;
	}

	/**
	 * The method launches a browser instance with given arguments. The browser will
	 * be closed when the parent java process is closed.
	 */
	public Browser launch(LaunchOptions options) {
		if (_productName == null && !StringUtil.isNotBlank(options.getProduct())) {
			_productName = options.getProduct();
		}
		adapterLauncher();
		Browser browser = launcher.launch(options);
		return browser;
	}
	
	/**
	 * 适配chrome or firefox 浏览器
	 */
	public void adapterLauncher() {
		if (StringUtil.isEmpty(_productName) && !isPuppeteerCore) {
			env = System::getenv;
			for (int i = 0; i < PRODUCT_ENV.length; i++) {
				String envName = PRODUCT_ENV[i];
				_productName = env.getEnv(envName);
			}
		}
		switch (_productName) {
		case "firefox":
			launcher = new FirefoxLauncher(isPuppeteerCore);
		case "chrome":
		default:
			launcher = new ChromeLauncher(isPuppeteerCore);
		}
	}

}
