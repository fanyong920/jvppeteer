package com.ruiyun.jvppeteer.core;

import com.ruiyun.jvppeteer.common.Constant;
import com.ruiyun.jvppeteer.exception.JvppeteerException;
import com.ruiyun.jvppeteer.exception.LaunchException;
import com.ruiyun.jvppeteer.exception.TimeoutException;
import com.ruiyun.jvppeteer.transport.Connection;
import com.ruiyun.jvppeteer.transport.WebSocketTransport;
import com.ruiyun.jvppeteer.transport.WebSocketTransportFactory;
import com.ruiyun.jvppeteer.util.Helper;
import com.ruiyun.jvppeteer.util.StringUtil;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import static com.ruiyun.jvppeteer.util.FileUtil.removeFolderOnExit;

public class BrowserRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(BrowserRunner.class);
    private static final Pattern WS_ENDPOINT_PATTERN = Pattern.compile("^DevTools listening on (ws://.*)$");
    private final String executablePath;
    private final List<String> processArguments;
    private final String tempDirectory;
    private Process process;
    private Connection connection;
    private volatile boolean closed;
    private static final List<BrowserRunner> runners = new ArrayList<>();
    private static boolean isRegisterShutdownHook = false;
    /**
     * 浏览器进程id
     */
    private String pid;

    public BrowserRunner(String executablePath, List<String> processArguments, String tempDirectory) {
        super();
        this.executablePath = executablePath;
        this.processArguments = processArguments;
        this.tempDirectory = tempDirectory;
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
        process = processBuilder.start();
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
            Process exec = null;
            String command = "";
            this.destroyProcess(this.process);
            if (Helper.isLinux() || Helper.isMac()) {
                command = "kill -9 " + pid;
                exec = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", command});
            } else {
                if (this.process.isAlive()) {
                    command = "cmd.exe /c taskkill /PID " + pid + " /F /T ";
                    exec = Runtime.getRuntime().exec(command);
                }
            }
            try {
                if (exec != null) {
                    LOGGER.info("kill chrome process by pid,command:  {}", command);
                    exec.waitFor(Constant.DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
                }
            } finally {
                this.destroyProcess(exec);
            }

        } catch (Exception e) {
            LOGGER.error("kill chrome process error ", e);
        } finally {
            deleteTempUserDir();
        }
    }

    public void destroyProcess(Process process) {
        if (process == null) {
            return;
        }
        process.destroy();
        if (process.isAlive()) {
            process.destroyForcibly();
        }
    }

    /**
     * 连接上浏览器
     *
     * @param usePipe 是否是pipe连接
     * @param timeout 超时时间
     * @param slowMo  放慢频率
     * @param dumpio  浏览器版本
     * @return 连接对象
     * @throws InterruptedException 打断异常
     */
    public Connection setUpConnection(boolean usePipe, int timeout, int slowMo, boolean dumpio) throws InterruptedException, IOException {
        if (usePipe) {/* pipe connection*/
            throw new LaunchException("Temporarily not supported pipe connect to chromuim.If you have a pipe connect to chromium idea,pleaze new a issue in github:https://github.com/fanyong920/jvppeteer/issues");
        } else {/*websocket connection*/
            String waitForWSEndpoint = this.waitForWSEndpoint(timeout, dumpio);
            LOGGER.info("Connect to browser by websocket url: {}", waitForWSEndpoint);
            WebSocketTransport transport = WebSocketTransportFactory.create(waitForWSEndpoint);
            this.connection = new Connection(waitForWSEndpoint, transport, slowMo, timeout);
        }
        return this.connection;
    }

    static class StreamReader {
        private final StringBuilder chromeOutputBuilder = new StringBuilder();
        private volatile String wsEndpoint = null;
        private final int timeout;
        private final boolean dumpio;
        private final InputStream inputStream;

        public StreamReader(int timeout, boolean dumpio, InputStream inputStream) {
            this.timeout = timeout;
            this.dumpio = dumpio;
            this.inputStream = inputStream;
            start();
        }

        private void start() {
            Runnable runnable = () -> {
                try (InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                     BufferedReader reader = new BufferedReader(inputStreamReader)) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (dumpio) {
                            System.out.println(line);
                        }
                        Matcher matcher = WS_ENDPOINT_PATTERN.matcher(line);
                        if (matcher.find()) {
                            wsEndpoint = matcher.group(1);
                            break;
                        }
                        chromeOutputBuilder.append(line).append(System.lineSeparator());
                    }
                } catch (Exception e) {
                    LOGGER.error("Failed to launch the browser process!{},{}", chromeOutputBuilder, e.getMessage(), e);
                }
            };
            new Thread(runnable).start();
        }

        public String waitFor() {
            long now = System.currentTimeMillis();
            long base = 0;
            while (true) {
                if (Objects.nonNull(wsEndpoint)) {
                    return wsEndpoint;
                }
                long remaining = timeout - base;
                if (remaining <= 0) {
                    throw new TimeoutException("Failed to launch the browser process!" + "Chrome output: " + chromeOutputBuilder);
                }
                base = System.currentTimeMillis() - now;
            }
        }


    }

    /**
     * waiting for browser ws url
     *
     * @param timeout 等待超时时间
     * @param dumpio  浏览器版本
     * @return ws url
     */
    private String waitForWSEndpoint(int timeout, boolean dumpio) {
        return new StreamReader(timeout, dumpio, process.getInputStream()).waitFor();
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
        if (this.connection != null && !this.connection.closed()) {
            this.connection.send("Browser.close");
        }
        this.destroy();
        this.closed = true;

    }

    private void deleteTempUserDir() {
        if (StringUtil.isNotEmpty(this.tempDirectory)) {
            try {
                removeFolderOnExit(this.tempDirectory);
            } catch (Exception ignored) {
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

    public Connection getConnection() {
        return connection;
    }
}


