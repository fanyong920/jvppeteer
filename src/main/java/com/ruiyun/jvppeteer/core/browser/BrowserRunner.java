package com.ruiyun.jvppeteer.core.browser;

import com.ruiyun.jvppeteer.core.Constant;
import com.ruiyun.jvppeteer.events.BrowserListenerWrapper;
import com.ruiyun.jvppeteer.events.DefaultBrowserListener;
import com.ruiyun.jvppeteer.events.EventEmitter;
import com.ruiyun.jvppeteer.exception.LaunchException;
import com.ruiyun.jvppeteer.exception.TimeoutException;
import com.ruiyun.jvppeteer.options.LaunchOptions;
import com.ruiyun.jvppeteer.transport.Connection;
import com.ruiyun.jvppeteer.transport.WebSocketTransport;
import com.ruiyun.jvppeteer.transport.factory.WebSocketTransportFactory;
import com.ruiyun.jvppeteer.util.FileUtil;
import com.ruiyun.jvppeteer.util.Helper;
import com.ruiyun.jvppeteer.util.StreamUtil;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.win32.StdCallLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BrowserRunner extends EventEmitter implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(BrowserRunner.class);

    private static final Map<Process,String> pidMap= new HashMap<>();

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
    public void start(LaunchOptions options) throws IOException, InterruptedException {
        if (process != null) {
            throw new RuntimeException("This process has previously been started.");
        }
        List<String> arguments = new ArrayList<>();
        arguments.add(executablePath);
        arguments.addAll(processArguments);
        ProcessBuilder processBuilder = new ProcessBuilder().command(arguments).redirectErrorStream(true);
        process = processBuilder.start();
        this.closed = false;
        pidMap.putIfAbsent(process,Helper.getProcessId(process));
        registerHook();
        addProcessListener(options);
    }

    /*
      获取chrome进程的pid，以方便在关闭浏览器时候能够完全杀死进程
      @return
     * @throws IOException
     */
//    private void findPid() throws IOException, InterruptedException {
//
//        Process exec = null;
//        String command = "ps -ef";
//        try{
//            exec = Runtime.getRuntime().exec(command);
//            BufferedReader reader = new BufferedReader(new InputStreamReader(exec.getInputStream()));
//            String line;
//            String pid = "";
//            while (StringUtil.isNotEmpty(line = reader.readLine())) {
//                if(!line.contains("chrome")) {
//                    continue;
//                }
//                if(line.contains("type=zygote") || !line.contains("type=gpu-process") || line.contains("type=utility") || line.contains("type=renderer") || line.contains("type=crashpad")){
//                   continue;
//                }
//                LOGGER.info("找到目标line {},{}", BrowserRunner.ahead,line);
//                String[] pidArray = line.trim().split("\\s+");
//                if(pidArray.length < 2){
//                    continue;
//                }
//                pid =  pidArray[1];
//                if (BrowserRunner.ahead){
//                    existedPidSet.add(pid);
//                    BrowserRunner.ahead = false;
//                }else {
//                    if(!existedPidSet.contains(pid)){
//                        String existedPid = pidMap.putIfAbsent(this.process, pid);
//                        if(existedPid == null){//找到一个新的pid,不再继续往下找
//                            LOGGER.info("found this process id {}", pid);
//                            break;
//                        }
//                    }
//                }
//
//
//            }
//
//            if(StringUtil.isEmpty(pid) && !ahead){
//                LOGGER.warn("The id of the process could not be found.");
//            }
//        }finally {
//            destroyCmdExec(exec,command);
//        }
//
//    }
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
                hook.register(new Thread(this::close));
                isRegisterShutdownHook = true;
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
    public boolean kill() {
        try {
            String pid = pidMap.get(this.process);
            if("-1".equals(pid)){
                LOGGER.warn("Chrome process pid is -1,will not use kill cmd");
                return false;
            }
            if(StringUtil.isEmpty(pid) ){
                LOGGER.warn("Chrome process pid is empty,will not use kill cmd");
                return false;
            }
            Process exec = null;
            String command = "";
            if (Platform.isWindows()) {
                command = "cmd.exe /c taskkill /PID " + pid + " /F /T ";
            } else if (Platform.isLinux() || Platform.isAIX()) {
                command = "kill -9 " + pid;
            }
            try {
                LOGGER.info("kill chrome process by pid,command: kill -9 {}", pid);
                exec = Runtime.getRuntime().exec(new String[]{"/bin/sh","-c",command});
                return exec.waitFor(Constant.DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
            } finally {
                this.destroyCmdProcess(exec,command);
                if (StringUtil.isNotEmpty(this.tempDirectory)) {
                    FileUtil.removeFolder(this.tempDirectory);
                }
            }
        } catch (Exception e) {
            LOGGER.error("kill chrome process error ", e);
            return false;
        }
    }


    /**
     * 关闭cmd exec
     * @param process 进程
     * @throws InterruptedException 异常
     */
    private void destroyCmdProcess(Process process,String command) throws InterruptedException {
        if (process == null) {
            return;
        }
        boolean waitForResult = process.waitFor(Constant.DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
        if(process.isAlive() && !waitForResult){
            process.destroyForcibly();
        }
        LOGGER.info("The current command ({}) exit result : {}.",command,waitForResult);
    }
    /**
     * 使用java自带方法关闭chrome进程
     */
    public void destroyProcess() throws InterruptedException {
        process.destroy();
        if (process != null && process.isAlive() && !process.waitFor(Constant.DEFAULT_TIMEOUT,TimeUnit.MILLISECONDS)) {
            process.destroyForcibly();
        }
    }

    /**
     * 通过命令行删除文件夹
     * @param path 删除的路径
     */
    private void deleteDir(String path) {
        if (StringUtil.isEmpty(path) || "*".equals(path)) {
            return;
        }
        FileUtil.removeFolder(path);
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

        } else {/*websocket connection*/
            String waitForWSEndpoint = waitForWSEndpoint(timeout, dumpio);
            WebSocketTransport transport = WebSocketTransportFactory.create(waitForWSEndpoint);
            this.connection = new Connection(waitForWSEndpoint, transport, slowMo);
            LOGGER.info("Connect to browser by websocket url: {}", waitForWSEndpoint);
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
        for (BrowserRunner browserRunner : runners) {
            closeQuietly(browserRunner);
        }
    }

    /**
     * 关闭浏览器
     */
    public void closeQuietly(BrowserRunner runner) {
        if (runner.getClosed()) {
            return ;
        }
        /*
          通过kill命令关闭
         */
        Helper.removeEventListeners(runner.listeners);
        boolean killResult = runner.kill();
        if (killResult){
            this.closed = true;
            return;
        }
        /*
          采用java的Process类进行关闭
         */
        try {
            this.destroyProcess();
        } catch (InterruptedException e) {
            LOGGER.error("Destroy chrome process error.",e);
        }
        /*
          发送关闭指令
         */
        if (runner.connection != null && !this.connection.getClosed()) {
            this.connection.send("Browser.close", null, false);
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

    public interface Kernel32 extends StdCallLibrary {
        Kernel32 INSTANCE = Native.load("kernel32", Kernel32.class);
        long GetProcessId(Long hProcess);
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


