package com.ruiyun.jvppeteer;

import com.ruiyun.jvppeteer.browser.Browser;
import com.ruiyun.jvppeteer.browser.BrowserFetcher;
import com.ruiyun.jvppeteer.launch.ChromeLauncher;
import com.ruiyun.jvppeteer.launch.FirefoxLauncher;
import com.ruiyun.jvppeteer.launch.Launcher;
import com.ruiyun.jvppeteer.options.ChromeArgOptions;
import com.ruiyun.jvppeteer.options.FetcherOptions;
import com.ruiyun.jvppeteer.options.LaunchOptions;
import com.ruiyun.jvppeteer.options.OptionsBuilder;
import com.ruiyun.jvppeteer.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Puppeteer 也可以用来控制 Chrome 浏览器， 但它与绑定的 Chromium
 * 版本在一起使用效果最好。不能保证它可以与任何其他版本一起使用。谨慎地使用 executablePath 选项。 如果 Google
 * Chrome（而不是Chromium）是首选，一个 Chrome Canary 或 Dev Channel 版本是建议的
 * 
 * @author fff
 *
 */
public class Puppeteer implements Constant {

	public String productName = null;

	private Launcher launcher;
	
	private Environment env = null;

	private String projectRoot;
//
//	private String preferredRevision;

	private boolean isPuppeteerCore;

	public Puppeteer() {

	}

	/**
	 * 以默认参数启动浏览器
	 * <br/>
	 * launch Browser by default options
	 * @return
	 */
	public static Browser launch(){
		return Puppeteer.rawLaunch();
	}

	public static  Browser launch(boolean headless) {
		return Puppeteer.rawLaunch(headless);
	}

	public static  Browser launch(LaunchOptions options) {
		Puppeteer puppeteer = new Puppeteer();
		return Puppeteer.rawLaunch(options,puppeteer);
	}

	private static Browser rawLaunch() {
		return Puppeteer.rawLaunch(true);
	}
	
	private static Browser rawLaunch(boolean headless) {
		Puppeteer puppeteer = new Puppeteer();
		return Puppeteer.rawLaunch(new OptionsBuilder().withHeadless(headless).build(),puppeteer);
	}
	
	/**
	 * The method launches a browser instance with given arguments. The browser will
	 * be closed when the parent java process is closed.
	 */
	private static Browser rawLaunch(LaunchOptions options,Puppeteer puppeteer) {
		if (!StringUtil.isNotBlank(options.getProduct())) {
			puppeteer.setProductName(options.getProduct()) ;
		}
		adapterLauncher(puppeteer);
		Browser browser = puppeteer.getLauncher().launch(options);
		return browser;
	}
	
	/**
	 * 适配chrome or firefox 浏览器
	 */
	private static void adapterLauncher(Puppeteer puppeteer) {
		String productName = null;
		Launcher launcher = null;
		Environment env;
		if (StringUtil.isEmpty(productName = puppeteer.getProductName()) && !puppeteer.getIsPuppeteerCore()) {

			if((env = puppeteer.getEnv()) == null){
				puppeteer.setEnv(env = System::getenv);
			}
			for (int i = 0; i < PRODUCT_ENV.length; i++) {
				String envProductName = PRODUCT_ENV[i];
				productName = env.getEnv(envProductName);
				if(StringUtil.isNotEmpty(productName)){
					puppeteer.setProductName(productName);
					break;
				}
			}
		}
		if(StringUtil.isEmpty(productName)){
			productName = "chrome";
			puppeteer.setProductName(productName);
		}
		switch (productName) {
		case "firefox":
			launcher = new FirefoxLauncher(puppeteer.getIsPuppeteerCore());
		case "chrome":
		default:
			launcher = new ChromeLauncher(puppeteer.getIsPuppeteerCore());
		}
		puppeteer.setLauncher(launcher);
	}

	public List<String> defaultArgs(ChromeArgOptions options) {
		List<String> chromeArguments = new ArrayList<>();
		 this.getLauncher().defaultArgs(options,chromeArguments);
		return chromeArguments;
	}

	public String executablePath() {
		return this.getLauncher().executablePath();
	}
	public BrowserFetcher createBrowserFetcher(FetcherOptions options){
		return new BrowserFetcher(this.projectRoot,options);
	}
	private   String getProductName() {
		return productName;
	}

	private   void setProductName(String productName) {
		this.productName = productName;
	}

	private boolean getIsPuppeteerCore() {
		return isPuppeteerCore;
	}


	private Launcher getLauncher() {
		return launcher;
	}

	private void setLauncher(Launcher launcher) {
		this.launcher = launcher;
	}

	private Environment getEnv() {
		return env;
	}

	private void setEnv(Environment env) {
		this.env = env;
	}
}
