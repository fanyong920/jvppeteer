package com.ruiyun.jvppeteer.core.browser;

import com.ruiyun.jvppeteer.events.BrowserListenerWrapper;
import com.ruiyun.jvppeteer.events.DefaultBrowserListener;
import com.ruiyun.jvppeteer.events.EventEmitter;
import com.ruiyun.jvppeteer.exception.LaunchException;
import com.ruiyun.jvppeteer.exception.TimeoutException;
import com.ruiyun.jvppeteer.transport.Connection;
import com.ruiyun.jvppeteer.transport.factory.WebSocketTransportFactory;
import com.ruiyun.jvppeteer.transport.PipeTransport;
import com.ruiyun.jvppeteer.transport.WebSocketTransport;
import com.ruiyun.jvppeteer.util.FileUtil;
import com.ruiyun.jvppeteer.util.Helper;
import com.ruiyun.jvppeteer.util.StreamUtil;
import com.ruiyun.jvppeteer.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
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
     * <br/>
     * Start your browser
     *
     * @param handleSIGINT
     * @param handleSIGTERM
     * @param handleSIGHUP
     * @param dumpio
     * @param pipe
     * @throws IOException
     */
    public void start(boolean handleSIGINT, boolean handleSIGTERM, boolean handleSIGHUP, boolean dumpio, boolean pipe) throws IOException {
        if (process != null) {
            throw new RuntimeException("This process has previously been started.");
        }

        List<String> arguments = new ArrayList<>();
        arguments.add(executablePath);
        arguments.addAll(processArguments);
        ProcessBuilder processBuilder = new ProcessBuilder().command(arguments).redirectErrorStream(true);
        process = processBuilder.start();
        this.closed = false;

        runners.add(this);
        if (!this.isRegisterShutdownHook) {
            synchronized (BrowserRunner.class) {
                if ((!this.isRegisterShutdownHook)) {
                    RuntimeShutdownHookRegistry hook = new RuntimeShutdownHookRegistry();
                    hook.register(new Thread(() -> {
                        this.close();
                    }));

                }
            }
        }
        DefaultBrowserListener exitListener = new DefaultBrowserListener() {
            @Override
            public void onBrowserEvent(Object event) {
                BrowserRunner runner = (BrowserRunner) this.getTarget();
                runner.kill();
            }
        };
        exitListener.setMothod("exit");
        exitListener.setTarget(this);
        this.listeners.add(Helper.addEventListener(this, exitListener.getMothod(), exitListener));

        if (handleSIGINT) {
            DefaultBrowserListener sigintListener = new DefaultBrowserListener() {
                @Override
                public void onBrowserEvent(Object event) {
                    BrowserRunner runner = (BrowserRunner) this.getTarget();
                    runner.kill();
                }
            };
            sigintListener.setMothod("SIGINT");
            sigintListener.setTarget(this);
            this.listeners.add(Helper.addEventListener(this, sigintListener.getMothod(), sigintListener));
        }

        if (handleSIGTERM) {
            DefaultBrowserListener sigtermListener = new DefaultBrowserListener() {
                @Override
                public void onBrowserEvent(Object event) {
                    BrowserRunner runner = (BrowserRunner) this.getTarget();
                    runner.close();
                }
            };
            sigtermListener.setMothod("SIGTERM");
            sigtermListener.setTarget(this);
            this.listeners.add(Helper.addEventListener(this, sigtermListener.getMothod(), sigtermListener));
        }

        if (handleSIGHUP) {
            DefaultBrowserListener sighubListener = new DefaultBrowserListener() {
                @Override
                public void onBrowserEvent(Object event) {
                    BrowserRunner runner = (BrowserRunner) this.getTarget();
                    runner.close();
                }
            };
            sighubListener.setMothod("SIGHUP");
            sighubListener.setTarget(this);
            this.listeners.add(Helper.addEventListener(this, sighubListener.getMothod(), sighubListener));
        }

    }

    public void kill() {
        //kill chrome process
        if (process != null && process.isAlive()) {
            process.destroyForcibly();
            try {
                process.waitFor();
            } catch (InterruptedException e) {

            }
        }

        //delete user-data-dir
        try {
            if (StringUtil.isNotEmpty(tempDirectory)) {
                FileUtil.removeFolder(tempDirectory);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public Connection setUpConnection(boolean usePipe, int timeout, int slowMo, String preferredRevision) throws InterruptedException {
        if (usePipe) {/* pipe connection*/
            InputStream pipeRead = this.getProcess().getInputStream();
            OutputStream pipeWrite = this.getProcess().getOutputStream();
            PipeTransport transport = new PipeTransport(pipeRead, pipeWrite);
            this.connection = new Connection("", transport, slowMo);
        } else {/*websoket connection*/
            String waitForWSEndpoint = waitForWSEndpoint(timeout, preferredRevision);
            WebSocketTransport transport = WebSocketTransportFactory.create(waitForWSEndpoint);
            this.connection = new Connection(waitForWSEndpoint, transport, slowMo);
            LOGGER.info("Connect to browser by websocket url: " + waitForWSEndpoint);
        }
        return this.connection;
    }

    /**
     * acquired browser ws
     *
     * @param timeout
     * @param preferredRevision
     * @return
     */
    private String waitForWSEndpoint(int timeout, String preferredRevision) {
        final StringBuilder ws = new StringBuilder();
        final AtomicBoolean success = new AtomicBoolean(false);
        final AtomicReference<String> chromeOutput = new AtomicReference<>("");
        Thread readLineThread =
                new Thread(
                        () -> {
                            StringBuilder chromeOutputBuilder = new StringBuilder();
                            BufferedReader reader = null;
                            try {
                                reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                                String line;
                                while ((line = reader.readLine()) != null) {
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
                                StreamUtil.closeStream(reader);
                            }
                        });

        readLineThread.start();

        try {
            readLineThread.join(timeout);

            if (!success.get()) {
                StreamUtil.close(readLineThread);
                throw new TimeoutException(
                        "Timed out after " + timeout + " ms while trying to connect to the browser!"
                                + "Chrome output: "
                                + chromeOutput.get());
            }
        } catch (InterruptedException e) {
            StreamUtil.close(readLineThread);
            throw new RuntimeException("Interrupted while waiting for dev tools server.", e);
        }
        String url = ws.toString();
        if (StringUtil.isEmpty(url)) {
            throw new LaunchException("Can't get WSEndpoint");
        }
        return url;
    }

    public Process getProcess() {
        return process;
    }

    @Override
    public void close() {
        for (int i = 0; i < runners.size(); i++) {
            BrowserRunner browserRunner = runners.get(i);
            if (browserRunner.getClosed()) {
                return;
            }
            if (StringUtil.isNotEmpty(browserRunner.getTempDirectory())) {
                browserRunner.kill();
            } else if (browserRunner.getConnection() != null) {
                try {
                    browserRunner.getConnection().send("Browser.close", null, true);
                } catch (Exception e) {
                    browserRunner.kill();
                }
            }

        }
    }

    public boolean close(Object c) {
        if (this.getClosed()) {
            return true;
        }
        Helper.removeEventListeners(this.listeners);
        if (StringUtil.isNotEmpty(this.tempDirectory)) {
            this.kill();
        } else if (this.connection != null) {
            // Attempt to close the browser gracefully
            try {
                this.connection.send("Browser.close", null, false);
            } catch (Exception e) {
                this.kill();
            }
        }
        return true;
    }

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

    public void setTempDirectory(String tempDirectory) {
        this.tempDirectory = tempDirectory;
    }

    public Connection getConnection() {
        return connection;
    }
}


