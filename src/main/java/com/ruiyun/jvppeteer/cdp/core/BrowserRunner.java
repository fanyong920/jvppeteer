package com.ruiyun.jvppeteer.cdp.core;

import com.ruiyun.jvppeteer.api.core.Connection;
import com.ruiyun.jvppeteer.cdp.entities.Protocol;
import com.ruiyun.jvppeteer.common.Constant;
import com.ruiyun.jvppeteer.common.Product;
import com.ruiyun.jvppeteer.exception.JvppeteerException;
import com.ruiyun.jvppeteer.util.Helper;
import com.ruiyun.jvppeteer.util.StringUtil;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import static com.ruiyun.jvppeteer.common.Constant.BACKUP_SUFFIX;
import static com.ruiyun.jvppeteer.common.Constant.PREFS_JS;
import static com.ruiyun.jvppeteer.common.Constant.USER_JS;
import static com.ruiyun.jvppeteer.util.FileUtil.removeFolder;
import static com.ruiyun.jvppeteer.util.FileUtil.removeFolderOnExit;

public class BrowserRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(BrowserRunner.class);
    private final String executablePath;
    private final List<String> processArguments;
    private final String tempDirectory;
    private Protocol protocol;
    private final Product product;
    private Process process;
    private Connection connection;
    private volatile boolean closed;
    private static final List<BrowserRunner> runners = Collections.synchronizedList(new ArrayList<>());
    private static boolean isRegisterShutdownHook = false;
    private final String customizedUserDataDir;
    /**
     * 浏览器进程id
     */
    private String pid;

    public BrowserRunner(String executablePath, List<String> processArguments, String tempDirectory, Product product, Protocol protocol, String customizedUserDataDir) {
        super();
        this.executablePath = executablePath;
        this.processArguments = processArguments;
        this.tempDirectory = tempDirectory;
        this.product = product;
        this.protocol = protocol;
        this.customizedUserDataDir = customizedUserDataDir;
        if (Product.Firefox.equals(this.product)) {
            if (Objects.isNull(this.protocol)) {
                this.protocol = Protocol.WebDriverBiDi;
            }
        }
        this.closed = true;
    }

    /**
     * 启动浏览器进程
     * Start your browser
     *
     * @throws IOException io异常
     */
    public void start() throws IOException {
        if (process != null) {
            throw new JvppeteerException("This process has previously been started.");
        }
        List<String> arguments = new ArrayList<>();
        arguments.add(this.executablePath);
        arguments.addAll(this.processArguments);
        ProcessBuilder processBuilder = new ProcessBuilder(arguments).redirectErrorStream(true);
        this.process = processBuilder.start();
        this.closed = false;
        registerHook();
    }

    /**
     * 注册钩子函数，程序关闭时，关闭浏览器
     */
    private void registerHook() {
        runners.add(this);
        if (isRegisterShutdownHook) {
            return;
        }
        synchronized (BrowserRunner.class) {
            if (!isRegisterShutdownHook) {
                RuntimeShutdownHookRegistry hook = new RuntimeShutdownHookRegistry();
                hook.register(new Thread(this::closeAllBrowser));
                isRegisterShutdownHook = true;
            }
        }
    }


    /**
     * kill 掉浏览器进程
     */
    public void destroy() {
        if (this.closed) {
            return;
        }
        try {
            if ("-1".equals(pid) || StringUtil.isEmpty(pid)) {
                this.destroyProcess(this.process);
                return;
            }
            Process exec;
            String command;
            if (Helper.isUnixLike()) {
                command = "kill -9 " + pid;
                exec = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", command});
            } else {
                command = "cmd.exe /c taskkill /pid " + pid + " /F /T ";
                exec = Runtime.getRuntime().exec(command);
            }
            try {
                if (Objects.nonNull(exec)) {
                    if(LOGGER.isDebugEnabled()){
                        LOGGER.debug("kill chrome process by pid,command:  {}", command);
                    }
                    exec.waitFor(Constant.DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
                }
                this.destroyProcess(this.process);
            } finally {
                this.destroyProcess(exec);
            }

        } catch (Exception e) {
            LOGGER.error("kill chrome process error ", e);
        } finally {
            try {
                cleanUserDataDir();
            } catch (IOException ignored) {

            }
        }
    }

    public void destroyProcess(Process process) {
        if (Objects.isNull(process)) {
            return;
        }
        process.destroy();
        try {
            process.waitFor(30L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.error("operation interrupt", e);
        }
    }

    public Process getProcess() {
        return process;
    }

    //系统奔溃或正常关闭时候，关闭所有打开的浏览器
    public void closeAllBrowser() {
        for (BrowserRunner browserRunner : runners) {
            browserRunner.closeBrowser();
        }
    }

    /**
     * 关闭浏览器
     */
    public void closeBrowser() {
        if (this.closed) {
            return;
        }
        //发送关闭指令
        if (!Objects.equals(this.product, Product.Firefox)) {
            if (Objects.nonNull(this.connection) && !this.connection.closed()) {
                this.connection.send("Browser.close", null, null, false);
            }
        }
        this.destroy();
        this.closed = true;
    }

    /**
     * 清理运行浏览器而产生的用户数据目录，如果是临时目录，则尝试删除
     *
     * @throws IOException IO异常
     */
    private void cleanUserDataDir() throws IOException {
        if (this.closed) {
            return;
        }
        if (StringUtil.isNotEmpty(this.tempDirectory)) {
            try {
                removeFolder(this.tempDirectory);
                if (Paths.get(this.tempDirectory).toFile().exists()) {
                    removeFolderOnExit(this.tempDirectory);
                }
            } catch (Exception ignored) {
            }
        } else {
            if (Objects.equals(this.product, Product.Firefox) && Objects.equals(this.protocol,Protocol.WebDriverBiDi)) {
                //回复备份文件的名字
                String prefsPath = Helper.join(this.customizedUserDataDir, PREFS_JS);
                String userPath = Helper.join(this.customizedUserDataDir, USER_JS);
                Path prefsSource = Paths.get(prefsPath + BACKUP_SUFFIX);
                Files.copy(prefsSource, Paths.get(prefsPath), StandardCopyOption.REPLACE_EXISTING);
                Path userSource = Paths.get(userPath + BACKUP_SUFFIX);
                Files.copy(userSource, Paths.get(userPath), StandardCopyOption.REPLACE_EXISTING);
                Files.deleteIfExists(prefsSource);
                Files.deleteIfExists(userSource);
            }
        }
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    /**
     * 注册钩子
     */
    public interface ShutdownHookRegistry {
        /**
         * Registers a new shutdown hook thread.
         *
         * @param thread Thread.
         */
        default void register(Thread thread) {
            Runtime.getRuntime().addShutdownHook(thread);
        }

        /**
         * Removes a shutdown thread.
         *
         * @param thread Thread.
         */
        default void remove(Thread thread) {
            Runtime.getRuntime().removeShutdownHook(thread);
        }
    }

    /**
     * Runtime based shutdown hook.
     */
    public static class RuntimeShutdownHookRegistry implements ShutdownHookRegistry {

    }


    public boolean isClosed() {
        return this.closed;
    }

    public String getTempDirectory() {
        return tempDirectory;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }
}


