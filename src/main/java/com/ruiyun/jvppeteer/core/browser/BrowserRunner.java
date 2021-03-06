package com.ruiyun.jvppeteer.core.browser;

import com.ruiyun.jvppeteer.events.BrowserListenerWrapper;
import com.ruiyun.jvppeteer.events.DefaultBrowserListener;
import com.ruiyun.jvppeteer.events.EventEmitter;
import com.ruiyun.jvppeteer.exception.LaunchException;
import com.ruiyun.jvppeteer.exception.TimeoutException;
import com.ruiyun.jvppeteer.options.LaunchOptions;
import com.ruiyun.jvppeteer.transport.Connection;
import com.ruiyun.jvppeteer.transport.WebSocketTransport;
import com.ruiyun.jvppeteer.transport.factory.WebSocketTransportFactory;
import com.ruiyun.jvppeteer.util.Helper;
import com.ruiyun.jvppeteer.util.StreamUtil;
import com.ruiyun.jvppeteer.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BrowserRunner extends EventEmitter implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(BrowserRunner.class);

    private static final Pattern WS_ENDPOINT_PATTERN = Pattern.compile("^DevTools listening on (ws://.*)$");

    private String executablePath;

    private List<String> processArguments;

    private String tempDirectory;

    private Process process;

    private Connection connection;

    private boolean closed;

    private List<BrowserListenerWrapper> listeners = new ArrayList<>();

    private static final List<BrowserRunner> runners = new ArrayList<>();

    private static boolean isRegisterShutdownHook = false;

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
     * @param options 启动参数
     * @throws IOException io异常
     */
    public void start(LaunchOptions options) throws IOException {
        if (process != null) {
            throw new RuntimeException("This process has previously been started.");
        }
        List<String> arguments = new ArrayList<>();
        arguments.add(executablePath);
        arguments.addAll(processArguments);


        ProcessBuilder processBuilder = new ProcessBuilder().command(arguments).redirectErrorStream(true);
        process = processBuilder.start();
        this.closed = false;

        registerHook();

        addProcessListener(options);

    }

    /**
     * 注册钩子函数，程序关闭时，关闭浏览器
     */
    private void registerHook() {
        runners.add(this);
        if (!isRegisterShutdownHook) {
            synchronized (BrowserRunner.class) {
                if ((!isRegisterShutdownHook)) {
                    RuntimeShutdownHookRegistry hook = new RuntimeShutdownHookRegistry();
                    hook.register(new Thread(this::close));
                    isRegisterShutdownHook = true;
                }
            }
        }
    }

    /**
     * 添加浏览器的一些事件监听 退出 SIGINT等
     *
     * @param options 启动参数
     */
    private void addProcessListener(LaunchOptions options) {
        DefaultBrowserListener<Object> exitListener = new DefaultBrowserListener<Object>() {
            @Override
            public void onBrowserEvent(Object event) {
                BrowserRunner runner = (BrowserRunner) this.getTarget();
                runner.kill();
            }
        };
        exitListener.setMethod("exit");
        exitListener.setTarget(this);
        this.listeners.add(Helper.addEventListener(this, exitListener.getMethod(), exitListener));

        if (options.getHandleSIGINT()) {
            DefaultBrowserListener<Object> sigintListener = new DefaultBrowserListener<Object>() {
                @Override
                public void onBrowserEvent(Object event) {
                    BrowserRunner runner = (BrowserRunner) this.getTarget();
                    runner.kill();
                }
            };
            sigintListener.setMethod("SIGINT");
            sigintListener.setTarget(this);
            this.listeners.add(Helper.addEventListener(this, sigintListener.getMethod(), sigintListener));
        }

        if (options.getHandleSIGTERM()) {
            DefaultBrowserListener<Object> sigtermListener = new DefaultBrowserListener<Object>() {
                @Override
                public void onBrowserEvent(Object event) {
                    BrowserRunner runner = (BrowserRunner) this.getTarget();
                    runner.close();
                }
            };
            sigtermListener.setMethod("SIGTERM");
            sigtermListener.setTarget(this);
            this.listeners.add(Helper.addEventListener(this, sigtermListener.getMethod(), sigtermListener));
        }

        if (options.getHandleSIGHUP()) {
            DefaultBrowserListener<Object> sighubListener = new DefaultBrowserListener<Object>() {
                @Override
                public void onBrowserEvent(Object event) {
                    BrowserRunner runner = (BrowserRunner) this.getTarget();
                    runner.close();
                }
            };
            sighubListener.setMethod("SIGHUP");
            sighubListener.setTarget(this);
            this.listeners.add(Helper.addEventListener(this, sighubListener.getMethod(), sighubListener));
        }
    }

    /**
     * kill 掉浏览器进程
     */
    public void kill() {
        this.destroyForcibly();
        //delete user-data-dir
        try {
            if (StringUtil.isNotEmpty(tempDirectory)) {
                removeFolderByCmd(tempDirectory);
//                FileUtil.removeFolder(tempDirectory);
                //同时把以前没删除干净的文件夹也重新删除一遍
//                Stream<Path> remainTempDirectories = Files.list(Paths.get(tempDirectory).getParent());
//                remainTempDirectories.forEach(path -> {
//                    if (path.getFileName().toString().startsWith(Constant.PROFILE_PREFIX)) {
////                        FileUtil.removeFolder(path.toString());
//                        try {
//                            removeFolderByCmd(path.toString());
//                        } catch (IOException | InterruptedException e) {
//
//                        }
//                    }
//                });
            }

        } catch (Exception e) {
            LOGGER.error("kill chrome process error ", e);
        }
    }

    /**
     * 强制结束浏览器进程
     */
    public void destroyForcibly() {
        if (process != null && process.isAlive()) {
            process.destroyForcibly();
        }
    }

    /**
     * 通过命令行删除文件夹
     * @param path 删除的路径
     * @throws IOException 异常
     * @throws InterruptedException 异常
     */
    private void removeFolderByCmd(String path) throws IOException, InterruptedException {
        if (StringUtil.isEmpty(path) || "*".equals(path)) {
            return;
        }
        Process delProcess = null;
        if (Helper.isWindows()) {
            delProcess = Runtime.getRuntime().exec("cmd /c rd /s /q " + path);
        } else if (Helper.isLinux() || Helper.isMac()) {
            String[] cmd = new String[]{"/bin/sh", "-c", "rm -rf " + path};
            delProcess = Runtime.getRuntime().exec(cmd);
        }
        if (!delProcess.waitFor(10000, TimeUnit.MILLISECONDS)) {
            delProcess.destroyForcibly();
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
    public Connection setUpConnection(boolean usePipe, int timeout, int slowMo, boolean dumpio) throws InterruptedException {
        if (usePipe) {/* pipe connection*/
            throw new LaunchException("Temporarily not supported pipe connect to chromuim.If you have a pipe connect to chromium idea,pleaze new a issue in github:https://github.com/fanyong920/jvppeteer/issues");
//            InputStream pipeRead = this.getProcess().getInputStream();
//            OutputStream pipeWrite = this.getProcess().getOutputStream();
//            PipeTransport transport = new PipeTransport(pipeRead, pipeWrite);
//            this.connection = new Connection("", transport, slowMo);

        } else {/*websoket connection*/
            String waitForWSEndpoint = waitForWSEndpoint(timeout, dumpio);
            WebSocketTransport transport = WebSocketTransportFactory.create(waitForWSEndpoint);
            this.connection = new Connection(waitForWSEndpoint, transport, slowMo);
            LOGGER.info("Connect to browser by websocket url: " + waitForWSEndpoint);
        }
        return this.connection;
    }

    static class StreamReader {
        private final StringBuilder ws = new StringBuilder();
        private final AtomicBoolean success = new AtomicBoolean(false);
        private final AtomicReference<String> chromeOutput = new AtomicReference<>("");

        private int timeout;

        private boolean dumpio;

        private InputStream inputStream;

        private Thread readThread;

        public StreamReader(int timeout, boolean dumpio, InputStream inputStream) {
            this.timeout = timeout;
            this.dumpio = dumpio;
            this.inputStream = inputStream;
        }

        public void start() {
            readThread = new Thread(
                    () -> {
                        StringBuilder chromeOutputBuilder = new StringBuilder();
                        BufferedReader reader = null;
                        try {
                            reader = new BufferedReader(new InputStreamReader(inputStream));
                            String line;
                            while ((line = reader.readLine()) != null) {
                                if (dumpio) {
                                    System.out.println(line);
                                }
                                Matcher matcher = WS_ENDPOINT_PATTERN.matcher(line);
                                if (matcher.find()) {
                                    ws.append(matcher.group(1));
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
                            StreamUtil.closeQuietly(reader);
                        }
                    });

            readThread.start();
        }

        public String getResult() {
            try {
                readThread.join(timeout);
                if (!success.get()) {
                    StreamUtil.close(readThread);
                    throw new TimeoutException(
                            "Timed out after " + timeout + " ms while trying to connect to the browser!"
                                    + "Chrome output: "
                                    + chromeOutput.get());
                }
            } catch (InterruptedException e) {
                StreamUtil.close(readThread);
                throw new RuntimeException("Interrupted while waiting for dev tools server.", e);
            }
            String url = ws.toString();
            if (StringUtil.isEmpty(url)) {
                throw new LaunchException("Can't get WSEndpoint");
            }
            return url;
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
        BrowserRunner.StreamReader reader = new BrowserRunner.StreamReader(timeout, dumpio, process.getInputStream());
        reader.start();
        return reader.getResult();
    }

    public Process getProcess() {
        return process;
    }

    @Override
    public void close() {
        for (int i = 0; i < runners.size(); i++) {
            BrowserRunner browserRunner = runners.get(i);
            if (browserRunner.getClosed()) {
                break;
            }

            if(browserRunner.getConnection() != null && !browserRunner.getConnection().getClosed()){
                browserRunner.getConnection().send("Browser.close", null, false);
            }

            if (StringUtil.isNotEmpty(browserRunner.getTempDirectory())) {
                browserRunner.kill();
            }
        }
    }

    /**
     * 关闭浏览器
     */
    public void closeQuietly() {
        if (this.getClosed()) {
            return ;
        }
        Helper.removeEventListeners(this.listeners);

        //先发送指令关闭
        if (this.connection != null && !this.connection.getClosed()) {
            this.connection.send("Browser.close", null, false);
        }

        //再调用 java 的 api 去关闭，但是这个 api 成功率不是100%
        if (StringUtil.isNotEmpty(this.tempDirectory)) {
            this.kill();
        }

        this.closed = true;
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

    public boolean getClosed() {
        return closed;
    }

    public String getTempDirectory() {
        return tempDirectory;
    }

    public Connection getConnection() {
        return connection;
    }
}


