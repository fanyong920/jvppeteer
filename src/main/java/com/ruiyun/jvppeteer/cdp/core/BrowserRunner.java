package com.ruiyun.jvppeteer.cdp.core;

import com.ruiyun.jvppeteer.api.core.Connection;
import com.ruiyun.jvppeteer.cdp.entities.Protocol;
import com.ruiyun.jvppeteer.common.Constant;
import com.ruiyun.jvppeteer.common.ParamsFactory;
import com.ruiyun.jvppeteer.common.Product;
import com.ruiyun.jvppeteer.exception.JvppeteerException;
import com.ruiyun.jvppeteer.util.Base64Util;
import com.ruiyun.jvppeteer.util.FileUtil;
import com.ruiyun.jvppeteer.util.Helper;
import com.ruiyun.jvppeteer.util.NodeDownloader;
import com.ruiyun.jvppeteer.util.StringUtil;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import static com.ruiyun.jvppeteer.common.Constant.BACKUP_SUFFIX;
import static com.ruiyun.jvppeteer.common.Constant.JVPPETEER_PIPE_LAUNCH_RESOURCE_DIR;
import static com.ruiyun.jvppeteer.common.Constant.PREFS_JS;
import static com.ruiyun.jvppeteer.common.Constant.USER_JS;
import static com.ruiyun.jvppeteer.util.FileUtil.removeFolder;
import static com.ruiyun.jvppeteer.util.FileUtil.removeFolderOnExit;

public class BrowserRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(BrowserRunner.class);
    private final String executablePath;
    private final List<String> browserArgs;
    private final String tempDirectory;
    private Protocol protocol;
    private final Product product;
    private Process process;
    private Connection connection;
    private volatile boolean closed;
    private static final List<BrowserRunner> runners = Collections.synchronizedList(new ArrayList<>());
    private static boolean isRegisterShutdownHook = false;
    private final String customizedUserDataDir;
    private final Map<String, String> env;
    private final boolean usepipe;
    /**
     * 浏览器进程id
     */
    private String pid;

    public BrowserRunner(String executablePath, List<String> browserArgs, String tempDirectory, Product product, Protocol protocol, String customizedUserDataDir, Map<String, String> env, boolean usePipe) {
        super();
        this.executablePath = executablePath;
        this.browserArgs = browserArgs;
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
        this.env = env;
        this.usepipe = usePipe;
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
        if (usepipe) {
            String pipeDir = System.getProperty(JVPPETEER_PIPE_LAUNCH_RESOURCE_DIR);
            if (StringUtil.isEmpty(pipeDir)) {
                pipeDir = Helper.join(System.getProperty("user.dir"), ".pipe-resources-"+Constant.JVPPETEER_VERSION);
            }
            //添加node path
            arguments.add(getNodeExecutablePath(pipeDir));
            //添加launch-browser-pipe.js
            arguments.add(getPipeLaunchJsPath(pipeDir).toString());
            Map<String, Object> params = ParamsFactory.create();
            params.put("executablePath", this.executablePath);
            params.put("args", this.browserArgs);
            arguments.add(Base64Util.encode(Constant.OBJECTMAPPER.writeValueAsString(params).getBytes(StandardCharsets.UTF_8)));
        } else {
            arguments.add(this.executablePath);
            arguments.addAll(this.browserArgs);
        }
        ProcessBuilder processBuilder = new ProcessBuilder(arguments);
        if (usepipe) {
            processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);
        } else {
            processBuilder.redirectErrorStream(true);
        }
        if (Objects.nonNull(env)) processBuilder.environment().putAll(env);
        this.process = processBuilder.start();
        this.closed = false;
        registerHook();
    }

    private static Path getPipeLaunchJsPath(String pipeDir) throws IOException {
        Path pipePath = Paths.get(pipeDir);
        Path pipeLaunchJsPath;
        if (!Files.exists(pipePath) || !Files.isDirectory(pipePath)) {
            FileUtil.createDirs(pipePath);
            pipeLaunchJsPath = BrowserFetcher.copyResourceFileToDirectory("launch-browser-pipe.js", pipeDir, "/scripts/");
        } else {
            Path isExistJsPath = pipePath.resolve("launch-browser-pipe.js");
            boolean isJsFileExist = Files.exists(isExistJsPath);
            if (isJsFileExist) {
                pipeLaunchJsPath = isExistJsPath;
            } else {
                pipeLaunchJsPath = BrowserFetcher.copyResourceFileToDirectory("launch-browser-pipe.js", pipeDir, "/scripts/");
            }
        }
        return pipeLaunchJsPath;
    }

    /**
     * 如果本地没有找到，检查并下载 Node.js
     */
    public static String getNodeExecutablePath(String pipeDir) throws IOException {
        Path pipePath = Paths.get(pipeDir);
        Path nodeExecutable;
        if (!Files.exists(pipePath) || !Files.isDirectory(pipePath)) {
            FileUtil.createDirs(pipePath);
            // 如果系统未安装，则下载 Node.js
            Path nodeDir = NodeDownloader.downloadNode(pipeDir);
            nodeExecutable = NodeDownloader.getNodeExecutablePath(nodeDir);
        } else {
            Path existNodePath = NodeDownloader.getNodeExecutablePath(pipePath.resolve(NodeDownloader.archive()));
            boolean isNodeExist = Files.exists(existNodePath);
            if (isNodeExist && Files.isExecutable(existNodePath)) {
                nodeExecutable = existNodePath;
            } else {
                // 如果系统未安装，则下载 Node.js
                Path nodeDir = NodeDownloader.downloadNode(pipeDir);
                nodeExecutable = NodeDownloader.getNodeExecutablePath(nodeDir);
                if (!Files.isExecutable(nodeExecutable)) {
                    throw new IOException("Node.js is not executable,path : " + nodeExecutable);
                }
            }
        }
        if (!Files.exists(nodeExecutable)) {
            throw new IOException("Node.js executable not found at: " + nodeExecutable);
        }
        return nodeExecutable.toString();
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
                    if (LOGGER.isDebugEnabled()) {
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
            boolean finish = process.waitFor(30L, TimeUnit.SECONDS);
            if (!finish) {
                process.destroyForcibly();
                process.waitFor(2, TimeUnit.SECONDS);
            }
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
            if (Objects.equals(this.product, Product.Firefox) && Objects.equals(this.protocol, Protocol.WebDriverBiDi)) {
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

    public void setConnection(Connection connection) {
        this.connection = connection;
    }
}


