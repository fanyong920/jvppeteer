package com.ruiyun.example;

import com.ruiyun.jvppeteer.common.Constant;
import com.ruiyun.jvppeteer.core.Browser;
import com.ruiyun.jvppeteer.core.BrowserRunner;
import com.ruiyun.jvppeteer.core.Page;
import com.ruiyun.jvppeteer.core.Puppeteer;
import com.ruiyun.jvppeteer.entities.LaunchOptions;
import com.ruiyun.jvppeteer.entities.LaunchOptionsBuilder;
import com.ruiyun.jvppeteer.util.Helper;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.sun.jna.Native;
import com.sun.jna.win32.StdCallLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Z_KillExample {

    private static final Logger LOGGER = LoggerFactory.getLogger(BrowserRunner.class);
    /**
     * 多个browser的时候用pids储存pid
     */
    private static Map<String, Process> pids = new HashMap<>();

    public static void main(String[] args) throws IOException {
        LaunchOptions launchOptions = new LaunchOptionsBuilder().withExecutablePath("C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe").withIgnoreDefaultArgs(Collections.singletonList("--enable-automation")).withHeadless(false).build();
        Browser browser = Puppeteer.launch(launchOptions);
        Page page = browser.newPage();
        page.goTo("https://www.baidu.com/?tn=98012088_10_dg&ch=3");
        Process process = browser.process();
        String processId = getProcessId(process);
        Z_KillExample.LOGGER.info("process pid {}", processId);
        kill(processId);
        // 做一些其他操作
        browser.close();
    }

    public interface Kernel32 extends StdCallLibrary {
        Kernel32 INSTANCE = Native.load("kernel32", Kernel32.class);

        long GetProcessId(Long hProcess);
    }

    public static String getProcessId(Process process) {
        long pid = -1;
        Field field;
        if (Helper.isWindows()) {
            try {
                field = process.getClass().getDeclaredField("handle");
                field.setAccessible(true);
                pid = Z_KillExample.Kernel32.INSTANCE.GetProcessId((Long) field.get(process));
            } catch (Exception e) {
                Z_KillExample.LOGGER.error("Failed to get processId on Windows platform.", e);
            }
        } else if (Helper.isLinux() || Helper.isMac()) {
            try {
                String version = System.getProperty("java.version");
                double jdkversion = Double.parseDouble(version.substring(0, 3));
                Class<?> clazz;
                //如果生产环境是jdk8,就不用if判断了
                if (jdkversion <= 1.8) {
                    clazz = Class.forName("java.lang.UNIXProcess");
                } else {
                    clazz = Class.forName("java.lang.ProcessImpl");
                }
                field = clazz.getDeclaredField("pid");
                field.setAccessible(true);
                pid = (Integer) field.get(process);
            } catch (Throwable e) {
                Z_KillExample.LOGGER.error("Failed to get processId on Linux or Aix platform.", e);
            }
        }
        return String.valueOf(pid);
    }

    public static boolean kill(String pid) {
        try {
            if ("-1".equals(pid)) {
                LOGGER.warn("Chrome process pid is -1,will not use kill cmd");
                return false;
            }
            if (StringUtil.isEmpty(pid)) {
                LOGGER.warn("Chrome process pid is empty,will not use kill cmd");
                return false;
            }
            Process exec = null;
            String command;
            if (Helper.isWindows()) {
                command = "cmd.exe /c taskkill /PID " + pid + " /F /T ";
                exec = Runtime.getRuntime().exec(command);
            } else if (Helper.isLinux() || Helper.isMac()) {
                command = "kill -9 " + pid;
                exec = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", command});
            }
            if (exec != null) {
                return exec.waitFor(Constant.DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
            }
        } catch (Exception e) {
            LOGGER.error("kill chrome process error ", e);
            return false;
        }
        return false;
    }
}
