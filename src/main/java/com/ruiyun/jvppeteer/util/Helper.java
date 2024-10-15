package com.ruiyun.jvppeteer.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.ruiyun.jvppeteer.common.Constant;
import com.ruiyun.jvppeteer.common.ParamsFactory;
import com.ruiyun.jvppeteer.entities.CallFrame;
import com.ruiyun.jvppeteer.entities.ExceptionDetails;
import com.ruiyun.jvppeteer.entities.RemoteObject;
import com.ruiyun.jvppeteer.entities.StackTrace;
import com.ruiyun.jvppeteer.exception.EvaluateException;
import com.ruiyun.jvppeteer.exception.JvppeteerException;
import com.ruiyun.jvppeteer.exception.TimeoutException;
import com.ruiyun.jvppeteer.transport.CDPSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.ruiyun.jvppeteer.common.Constant.INTERNAL_URL;
import static com.ruiyun.jvppeteer.common.Constant.SOURCE_URL_REGEX;

/**
 * 一些公共方法
 */
public class Helper {
    private static final Logger LOGGER = LoggerFactory.getLogger(Helper.class);

    public static Object createClientError(ExceptionDetails exceptionDetails) {
        String name = "";
        String message;
        if (exceptionDetails.getException() == null) {
            name = "Error";
            message = exceptionDetails.getText();
        } else if (!"object".equals(exceptionDetails.getException().getType()) || !"error".equals(exceptionDetails.getException().getSubtype()) && StringUtil.isEmpty(exceptionDetails.getException().getObjectId())) {
            return valueFromRemoteObject(exceptionDetails.getException());
        } else {
            String[] lines;
            lines = Optional.of(exceptionDetails).flatMap(details -> Optional.ofNullable(details.getException().getDescription())).orElse("").split("\\n {4}at ");
            int size = Math.min(Optional.of(exceptionDetails).flatMap(details -> Optional.of(details.getStackTrace().getCallFrames().size())).orElse(0), lines.length - 1);
            lines = Arrays.stream(lines).limit(lines.length - size).toArray(String[]::new);
            String className = Optional.of(exceptionDetails).flatMap(details -> Optional.of(details.getException().getClassName())).get();
            if (StringUtil.isNotEmpty(className)) {
                name = className;
            }
            message = String.join("\n", lines);
            if (StringUtil.isNotEmpty(name) && message.startsWith(name + ":")) {
                message = message.substring(name.length() + 2);
            }
        }
        EvaluateException error = new EvaluateException(message);
        error.setName(name);
        int messageHeight = message.split("\n").length;
        String stack = error.getStack();
        List<String> messageLines = new ArrayList<>();
        if (StringUtil.isNotEmpty(stack)) {
            messageLines = new ArrayList<>(Arrays.asList(stack.split("\n"))).subList(0, messageHeight);
        }
        List<String> stackLines = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();
        if (exceptionDetails.getStackTrace() != null) {
            for (CallFrame callframe : exceptionDetails.getStackTrace().getCallFrames()) {
                String location = callframe.getUrl() + ":" + (callframe.getLineNumber() + 1) + ":" + (callframe.getColumnNumber() + 1);
                String functionName = StringUtil.isNotEmpty(callframe.getFunctionName()) ? callframe.getFunctionName() : "<anonymous>";
                stringBuilder.append("\n    at ").append(functionName).append("(").append(location).append(")");
                stackLines.add(stringBuilder.toString());
                stringBuilder.setLength(0);
            }
        }
        messageLines.addAll(stackLines);
        error.setStack(String.join("\n", messageLines));
        return error;
    }

    public static Object createEvaluationError(ExceptionDetails exceptionDetails) {
        String name = "";
        String message;
        if (exceptionDetails.getException() == null) {
            name = "Error";
            message = exceptionDetails.getText();
        } else if (!"object".equals(exceptionDetails.getException().getType()) || !"error".equals(exceptionDetails.getException().getSubtype()) && StringUtil.isEmpty(exceptionDetails.getException().getObjectId())) {
            return valueFromRemoteObject(exceptionDetails.getException());
        } else {
            String[] lines = Optional.of(exceptionDetails).map(ExceptionDetails::getException).map(RemoteObject::getDescription).orElse("").split("\n {4}at ");
            int size = Math.min(Optional.of(exceptionDetails).map(ExceptionDetails::getStackTrace).map(StackTrace::getCallFrames).map(List::size).orElse(0), lines.length - 1);
            lines = Arrays.stream(lines).limit(lines.length - size).toArray(String[]::new);
            String className = Optional.of(exceptionDetails).flatMap(details -> Optional.of(details.getException().getClassName())).get();
            if (StringUtil.isNotEmpty(className)) {
                name = className;
            }
            message = String.join("\n", lines);
            if (StringUtil.isNotEmpty(name) && message.startsWith(name + ": ")) {
                message = message.substring(name.length() + 2);
            }
        }

        int messageHeight = message.split("\n").length;
        LinkedList<String> stackLines = new LinkedList<>();
        List<String> messageLines = new ArrayList<>();
        if (ValidateUtil.isNotEmpty(stackLines)) {
            messageLines = stackLines.subList(0, messageHeight);
        }
        stackLines.poll();
        StringBuilder stringBuilder = new StringBuilder();
        if (exceptionDetails.getStackTrace() != null) {
            for (int i = exceptionDetails.getStackTrace().getCallFrames().size(); i-- > 0; ) {
                CallFrame callFrame = exceptionDetails.getStackTrace().getCallFrames().get(i);
                if (isPuppeteerURL(callFrame.getUrl()) && !INTERNAL_URL.equals(callFrame.getUrl())) {
                    PuppeteerURL url = parse(callFrame.getUrl());
                    try {
                        stringBuilder.append("\n    jvppeteer print: at ").append(URLDecoder.decode(url.getSiteString(), StandardCharsets.UTF_8.name()));
                    } catch (UnsupportedEncodingException e) {
                        stringBuilder.append(url.getSiteString()).append(", <anonymous>:").append(callFrame.getLineNumber()).append(":").append(callFrame.getColumnNumber());
                    }
                    stackLines.addFirst(stringBuilder.toString());
                    stringBuilder.setLength(0);
                } else {
                    String functionName = StringUtil.isNotEmpty(callFrame.getFunctionName()) ? callFrame.getFunctionName() : "<anonymous>";
                    stringBuilder.append("\n    at ").append(functionName).append("(").append(callFrame.getUrl()).append(":").append(callFrame.getLineNumber()).append(":").append(callFrame.getColumnNumber());
                    stackLines.add(stringBuilder.toString());
                    stringBuilder.setLength(0);
                }
            }
        }
        messageLines.addAll(stackLines);
        EvaluateException error = new EvaluateException(name + " : " + message + String.join("\n", messageLines));
        error.setName(name);
        return error;
    }

    public static PuppeteerURL parse(String url) {
        url = url.substring("pptr:".length());
        String[] split = url.split(";");
        if (split.length == 2) {
            PuppeteerURL puppeteerURL = new PuppeteerURL();
            try {
                puppeteerURL.setSiteString(URLDecoder.decode(split[1], StandardCharsets.UTF_8.name()));
            } catch (UnsupportedEncodingException e) {
                throw new JvppeteerException(e);
            }
            puppeteerURL.setFunctionName(split[0]);
            return puppeteerURL;
        }
        return new PuppeteerURL();
    }

    public static String withSourcePuppeteerURLIfNone(String functionName, String pageFunction) {
        if (SOURCE_URL_REGEX.matcher(pageFunction).find()) {
            return pageFunction;
        } else {
            List<String> args = new ArrayList<>();
            // 获取当前调用堆栈
            StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
            args.add(functionName);
            try {
                args.add(URLEncoder.encode(stackTraceElements[3].toString(), StandardCharsets.UTF_8.name()));
            } catch (UnsupportedEncodingException e) {
                args.add("ModuleJob.run%20(node%3Ainternal%2Fmodules%2Fesm%2Fmodule_job%3A222%3A25)");
            }
            return pageFunction + "\n" + "//# sourceURL=pptr:" + String.join(";", args) + "\n";
        }
    }

    public static String platform() {
        return System.getProperty("os.name").toLowerCase();
    }

    public static String arch() {
        return System.getProperty("os.arch").toLowerCase();
    }

    public static boolean isWindows() {
        return platform().contains("win");
    }

    public static boolean is64() {
        return arch().contains("64");
    }

    public static boolean isLinux() {
        return platform().contains("linux");
    }

    public static boolean isMac() {
        return platform().contains("mac");
    }

    public static String join(String root, String... args) {
        return java.nio.file.Paths.get(root, args).toString();
    }

    /**
     * read stream from protocol : example for tracing  file
     *
     * @param client  CDPSession
     * @param handler 发送给websocket的参数
     * @param path    文件存放的路径
     * @return 可能是feture，可能是字节数组
     * @throws IOException 操作文件的异常
     */
    public static byte[] readProtocolStream(CDPSession client, String handler, String path) throws IOException {
        boolean eof = false;
        File file = null;
        BufferedOutputStream writer = null;
        BufferedInputStream reader = null;
        if (StringUtil.isNotEmpty(path)) {
            file = new File(path);
            FileUtil.createNewFile(path);
        }
        Map<String, Object> params = ParamsFactory.create();
        params.put("handle", handler);
        try {
            if (file != null) {
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                writer = new BufferedOutputStream(fileOutputStream);
            }
            byte[] buffer = new byte[Constant.DEFAULT_BUFFER_SIZE];
            byte[] bytes;
            List<byte[]> bufs = new ArrayList<>();
            int byteLength = 0;
            while (!eof) {
                JsonNode response = client.send("IO.read", params);
                JsonNode eofNode = response.get(Constant.EOF);
                JsonNode base64EncodedNode = response.get(Constant.BASE_64_ENCODED);
                JsonNode dataNode = response.get(Constant.DATA);
                String dataText;
                if (dataNode != null && StringUtil.isNotEmpty(dataText = dataNode.asText())) {
                    try {
                        if (base64EncodedNode != null && base64EncodedNode.asBoolean()) {
                            bytes = Base64.getDecoder().decode(dataText);
                        } else {
                            bytes = dataNode.asText().getBytes();
                        }
                        bufs.add(bytes);
                        byteLength += bytes.length;
                        //转成二进制流 io
                        if (file != null) {
                            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
                            reader = new BufferedInputStream(byteArrayInputStream);
                            int read;
                            while ((read = reader.read(buffer, 0, Constant.DEFAULT_BUFFER_SIZE)) != -1) {
                                writer.write(buffer, 0, read);
                                writer.flush();
                            }
                        }
                    } finally {
                        StreamUtil.closeQuietly(reader);
                    }
                }
                eof = eofNode == null || eofNode.asBoolean();
            }
            client.send("IO.close", params);
            return getBytes(bufs, byteLength);
        } finally {
            StreamUtil.closeQuietly(writer);
            StreamUtil.closeQuietly(reader);
        }
    }

    /**
     * 多个字节数组转成一个字节数组
     *
     * @param bufs       数组集合
     * @param byteLength 数组总长度
     * @return 总数组
     */
    private static byte[] getBytes(List<byte[]> bufs, int byteLength) {
        //返回字节数组
        byte[] resultBuf = new byte[byteLength];
        int destPos = 0;
        for (byte[] buf : bufs) {
            System.arraycopy(buf, 0, resultBuf, destPos, buf.length);
            destPos += buf.length;
        }
        return resultBuf;
    }


    public static boolean isNumber(String s) {
        Pattern pattern = Pattern.compile("-?[0-9]+(\\.[0-9]+)?");
        Matcher matcher = pattern.matcher(s);
        return matcher.matches();
    }

    public static Object valueFromRemoteObject(RemoteObject remoteObject) {
        ValidateUtil.assertArg(StringUtil.isEmpty(remoteObject.getObjectId()), "Cannot extract value when objectId is given");
        if (StringUtil.isNotEmpty(remoteObject.getUnserializableValue())) {
            if ("bigint".equals(remoteObject.getType()))
                return new BigInteger(remoteObject.getUnserializableValue().replace("n", ""));
            switch (remoteObject.getUnserializableValue()) {
                case "-0":
                case "NaN":
                case "Infinity":
                case "-Infinity":
                    return remoteObject.getUnserializableValue();
                default:
                    throw new JvppeteerException("Unsupported unserializable value: " + remoteObject.getUnserializableValue());
            }
        }
        return remoteObject.getValue();
    }

    public static void releaseObject(CDPSession client, RemoteObject remoteObject) {
        if (StringUtil.isEmpty(remoteObject.getObjectId()))
            return;
        Map<String, Object> params = ParamsFactory.create();
        params.put("objectId", remoteObject.getObjectId());
        try {
            client.send("Runtime.releaseObject", params);
        } catch (Exception e) {
            // Exceptions might happen in case of a page been navigated or closed.
            //重新导航到某个网页 或者页面已经关闭
            // Swallow these since they are harmless and we don't leak anything in this case.
            //在这种情况下不需要将这个错误在线程执行中抛出，打日志记录一下就可以了
            LOGGER.error("jvppeteer error: ", e);

        }
    }


    public static String evaluationString(String fun, Object... args) throws JsonProcessingException {
        if (!isFunction(fun)) {
            ValidateUtil.assertArg(args.length == 0, "Cannot evaluate a string with arguments");
            return fun;
        }
        List<String> argsList = new ArrayList<>();
        for (Object arg : args) {
            if (arg == null) {
                argsList.add("undefined");
            } else {
                argsList.add(Constant.OBJECTMAPPER.writeValueAsString(arg));
            }
        }
        return MessageFormat.format("({0})({1})", fun, String.join(",", argsList));
    }


    /**
     * 判断js字符串是否是一个函数
     *
     * @param pageFunction js字符串
     * @return true代表是js函数
     */
    public static boolean isFunction(String pageFunction) {
        pageFunction = pageFunction.trim();
        return pageFunction.startsWith("function") || pageFunction.startsWith("async") || pageFunction.contains("=>");
    }

    /**
     * 获取linux或mac平台下的进程id
     *
     * @param process 进程
     * @return 进程id
     * @throws ClassNotFoundException class not found
     * @throws NoSuchFieldException   field not found
     * @throws IllegalAccessException illegal access
     */
    public static long getPidForLinuxOrMac(Process process) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        long pid = -1;
        if (Helper.isMac() || Helper.isLinux()) {
            String version = System.getProperty("java.version");
            double jdkversion = Double.parseDouble(version.substring(0, 3));
            Class<?> clazz;
            if (jdkversion <= 1.8) {
                clazz = Class.forName("java.lang.UNIXProcess");
            } else {
                clazz = Class.forName("java.lang.ProcessImpl");
            }
            Field field = clazz.getDeclaredField("pid");
            field.setAccessible(true);
            pid = (Integer) field.get(process);
        }
        return pid;
    }

    public static String createProtocolErrorMessage(JsonNode receivedNode) {
        String message = receivedNode.get(Constant.ERROR).get(Constant.MESSAGE).asText();
        if (receivedNode.get(Constant.ERROR).hasNonNull(Constant.DATA)) {
            message += " " + receivedNode.get(Constant.ERROR).get(Constant.DATA).asText();
        }
        return message;
    }

    public static class PuppeteerURL {
        private String siteString;
        private String functionName;

        public String getSiteString() {
            return siteString;
        }

        public void setSiteString(String siteString) {
            this.siteString = siteString;
        }

        public String getFunctionName() {
            return functionName;
        }

        public void setFunctionName(String functionName) {
            this.functionName = functionName;
        }
    }

    public static <T> T waitForCondition(Supplier<T> conditionChecker, long timeout, String errorMessage) {
        long now = System.currentTimeMillis();
        long base = 0;
        while (true) {
            long remaining = timeout - base;
            if (remaining <= 0) {
                throw new TimeoutException(errorMessage);
            }
            T result = conditionChecker.get();
            if (result != null) {
                return result;
            }
            base = System.currentTimeMillis() - now;
        }
    }

    public static void justWait(long timeout) {
        long now = System.currentTimeMillis();
        long base = 0;
        while (true) {
            long remaining = timeout - base;
            if (remaining <= 0) {
                break;
            }
            base = System.currentTimeMillis() - now;
        }
    }

    public static <T> T filter(List<T> targets, Predicate<T> predicate) {
        if (ValidateUtil.isNotEmpty(targets)) {
            for (T target : targets) {
                if (predicate.test(target)) {
                    return target;
                }
            }
        }
        return null;
    }

    public static boolean isPuppeteerURL(String url) {
        return url.startsWith("pptr:");
    }

    public static void throwError(Exception error) {
        if (error instanceof RuntimeException) {
            throw (RuntimeException) error;
        } else {
            throw new JvppeteerException(error);
        }
    }

}
