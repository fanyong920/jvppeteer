package com.ruiyun.jvppeteer.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ruiyun.jvppeteer.api.core.CDPSession;
import com.ruiyun.jvppeteer.api.core.Connection;
import com.ruiyun.jvppeteer.bidi.entities.SameSite;
import com.ruiyun.jvppeteer.cdp.entities.CallFrame;
import com.ruiyun.jvppeteer.cdp.entities.Cookie;
import com.ruiyun.jvppeteer.cdp.entities.CookiePriority;
import com.ruiyun.jvppeteer.cdp.entities.CookieSameSite;
import com.ruiyun.jvppeteer.cdp.entities.CookieSourceScheme;
import com.ruiyun.jvppeteer.cdp.entities.ExceptionDetails;
import com.ruiyun.jvppeteer.cdp.entities.GetVersionResponse;
import com.ruiyun.jvppeteer.cdp.entities.RemoteObject;
import com.ruiyun.jvppeteer.cdp.entities.StackTrace;
import com.ruiyun.jvppeteer.common.Constant;
import com.ruiyun.jvppeteer.common.ParamsFactory;
import com.ruiyun.jvppeteer.exception.EvaluateException;
import com.ruiyun.jvppeteer.exception.JvppeteerException;
import com.ruiyun.jvppeteer.exception.ProtocolException;
import com.ruiyun.jvppeteer.exception.TimeoutException;
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
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import static com.ruiyun.jvppeteer.common.Constant.CDP_SPECIFIC_PREFIX;
import static com.ruiyun.jvppeteer.common.Constant.INTERNAL_URL;
import static com.ruiyun.jvppeteer.common.Constant.OBJECTMAPPER;
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
            lines = Optional.of(exceptionDetails).map(ExceptionDetails::getException).map(RemoteObject::getDescription).orElse("").split("\\n {4}at ");
            int size = Math.min(Optional.of(exceptionDetails).map(ExceptionDetails::getStackTrace).map(StackTrace::getCallFrames).map(List::size).orElse(0), lines.length - 1);
            lines = Arrays.stream(lines).limit(lines.length - size).toArray(String[]::new);
            String className = Optional.of(exceptionDetails).map(ExceptionDetails::getException).map(RemoteObject::getClassName).orElse("");
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

    public static Object createCdpEvaluationError(ExceptionDetails exceptionDetails) {
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

    public static void createBidiEvaluationError(com.ruiyun.jvppeteer.bidi.entities.ExceptionDetails details) {
        if (!Objects.equals("error", details.getException().getType())) {
            throw new EvaluateException(String.valueOf(details.getException().getValue()));
        }
        String[] parts = details.getText().split(": ", 2);
        String name = parts.length > 0 ? parts[0] : "";
        String message = parts.length > 1 ? String.join(": ", Arrays.copyOfRange(parts, 1, parts.length)) : "";
        LinkedList<String> stackLines = new LinkedList<>();
        StringBuilder stringBuilder = new StringBuilder();
        if (details.getStackTrace() != null) {
            for (int i = details.getStackTrace().getCallFrames().size(); i-- > 0; ) {
                CallFrame callFrame = details.getStackTrace().getCallFrames().get(i);
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
        throw new EvaluateException("name: " + name + ", text: " + details.getText() + ", message: " + message + String.join("\n", stackLines));
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

    public static String withSourcePuppeteerURLIfNone(String functionName, String pptrFunction) {
        if (SOURCE_URL_REGEX.matcher(pptrFunction).find()) {
            return pptrFunction;
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
            return pptrFunction + "\n" + "//# sourceURL=pptr:" + String.join(";", args) + "\n";
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
            //不关心释放结果，即使释放不成功，那么只是占了页面的一点资源。
            // 页面在跳转或者关闭的时候，也会自动释放的。
            client.send("Runtime.releaseObject", params, null, false);
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
     * @param pptrFunction js字符串
     * @return true代表是js函数
     */
    public static boolean isFunction(String pptrFunction) {
        pptrFunction = pptrFunction.trim();
        return pptrFunction.startsWith("function") || pptrFunction.startsWith("async") || pptrFunction.contains("=>");
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
            double jdkVersion = Double.parseDouble(version.substring(0, 3));
            Class<?> clazz;
            if (jdkVersion <= 1.8) {
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
        long startTime = System.currentTimeMillis();
        long elapsedTime = 0;
        long checkInterval = 100; // Start with 100ms
        long maxInterval = 2000; // Maximum interval of 2000ms
        while (true) {
            T result = conditionChecker.get();
            if (result != null) {
                return result;
            }
            elapsedTime = System.currentTimeMillis() - startTime;
            long remaining = timeout - elapsedTime;
            if (timeout > 0 && remaining <= 0) {
                throw new TimeoutException(errorMessage);
            }
            justWait(Math.min(checkInterval, remaining));
            // Increase checkInterval gradually, up to maxInterval (exponential backoff)
            checkInterval = Math.min(checkInterval * 2, maxInterval);
        }
    }

    public static void justWait(long timeout) {
        CountDownLatch latch = new CountDownLatch(1);
        try {
            latch.await(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new JvppeteerException(e);
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

    public static void throwError(Throwable error) {
        if (error instanceof RuntimeException) {
            throw (RuntimeException) error;
        } else {
            throw new JvppeteerException(error);
        }
    }

    public static GetVersionResponse getVersion(Connection connection) {
        return Constant.OBJECTMAPPER.convertValue(connection.send("Browser.getVersion"), GetVersionResponse.class);
    }

    public static void rewriteNavigationError(String message, int timeout, Exception error) {
        if (error instanceof ProtocolException) {
            String newMessage = error.getMessage() + "    at  " + message;
            throw new ProtocolException(newMessage, error);
        } else if (error instanceof TimeoutException) {
            String newMessage = "Navigation timeout of " + timeout + " ms exceeded";
            throw new TimeoutException(newMessage, error);
        }
        throwError(error);
    }

    public static String setSourceUrlComment(String pptrFunction) {
        if (SOURCE_URL_REGEX.matcher(pptrFunction).find()) {
            return pptrFunction;
        } else {
            return pptrFunction + "\n" + "//# sourceURL=" + INTERNAL_URL + "\n";
        }
    }

    public static ObjectNode convertCookiesPartitionKeyFromPuppeteerToCdp(JsonNode partitionKey) {
        if (Objects.isNull(partitionKey)) {
            return null;
        }
        ObjectNode objectNode = OBJECTMAPPER.createObjectNode();
        if (partitionKey.isTextual()) {

            objectNode.put("topLevelSite", partitionKey.asText());
            objectNode.put("hasCrossSiteAncestor", false);
        } else {
            objectNode.set("topLevelSite", partitionKey.get("sourceOrigin"));
            objectNode.put("hasCrossSiteAncestor", !partitionKey.path("hasCrossSiteAncestor").isMissingNode() && partitionKey.path("hasCrossSiteAncestor").asBoolean());
        }
        return objectNode;
    }

    public static Cookie bidiToPuppeteerCookie(JsonNode bidiCookie) {
        Cookie cookie = new Cookie();
        cookie.setName(bidiCookie.path("name").asText());
        cookie.setValue(bidiCookie.at("/value/value").asText());
        cookie.setDomain(bidiCookie.get("domain").asText());
        cookie.setPath(bidiCookie.get("path").asText());
        cookie.setSize(bidiCookie.get("size").asInt());
        cookie.setHttpOnly(bidiCookie.get("httpOnly").asBoolean());
        cookie.setSecure(bidiCookie.get("secure").asBoolean());
        cookie.setSameSite(convertCookiesSameSiteBiDiToCdp(bidiCookie.get("sameSite")));
        JsonNode expires = bidiCookie.path("expires");
        cookie.setExpires(expires.isMissingNode() ? -1 : expires.asInt());
        cookie.setSession(expires.isMissingNode() || expires.asInt() <= 0);
        JsonNode sameParty = bidiCookie.path(CDP_SPECIFIC_PREFIX + "sameParty");
        if (!sameParty.isMissingNode()) {
            cookie.setSameParty(sameParty.asBoolean());
        }
        JsonNode sourceScheme = bidiCookie.path(CDP_SPECIFIC_PREFIX + "sourceScheme");
        if (!sourceScheme.isMissingNode()) {
            cookie.setSourceScheme(CookieSourceScheme.valueOf(sourceScheme.asText()));
        }
        JsonNode partitionKeyOpaque = bidiCookie.path(CDP_SPECIFIC_PREFIX + "partitionKeyOpaque");
        if (!partitionKeyOpaque.isMissingNode()) {
            cookie.setPartitionKeyOpaque(partitionKeyOpaque.asBoolean());
        }
        JsonNode priority = bidiCookie.path(CDP_SPECIFIC_PREFIX + "priority");
        if (!priority.isMissingNode()) {
            cookie.setPriority(CookiePriority.valueOf(partitionKeyOpaque.asText()));
        }
        JsonNode partitionKey = bidiCookie.path(CDP_SPECIFIC_PREFIX + "partitionKey");
        if (!partitionKey.isMissingNode()) {
            if (partitionKey.isTextual()) {
                cookie.setPartitionKey(partitionKey);
            } else if (partitionKey.isObject()) {
                ObjectNode partitionKeyNode = Constant.OBJECTMAPPER.createObjectNode();
                partitionKeyNode.set("partitionKey", partitionKey.get("topLevelSite"));
                cookie.setPartitionKey(partitionKeyNode);
            }
        }
        return cookie;
    }

    public static CookieSameSite convertCookiesSameSiteBiDiToCdp(JsonNode sameSite) {
        return Objects.equals("strict", sameSite.asText()) ? CookieSameSite.Strict : Objects.equals("lax", sameSite.asText()) ? CookieSameSite.Lax : CookieSameSite.None;
    }

    public static SameSite convertCookiesSameSiteCdpToBiDi(CookieSameSite sameSite) {
        return Objects.equals(sameSite, CookieSameSite.Strict) ? SameSite.Strict : Objects.equals(sameSite, CookieSameSite.Lax) ? SameSite.Lax : SameSite.None;
    }

    public static String convertCookiesPartitionKeyFromPuppeteerToBiDi(JsonNode partitionKey) {
        if (Objects.isNull(partitionKey)) {
            return null;
        }
        if (partitionKey.isTextual()) {
            return partitionKey.asText();
        }
        if (!partitionKey.path("hasCrossSiteAncestor").isMissingNode()) {
            throw new UnsupportedOperationException("WebDriver BiDi does not support `hasCrossSiteAncestor` yet.");
        }
        return partitionKey.get("sourceOrigin").asText();
    }

    /**
     * 移除Map对象里面的null值
     * @param params map对象
     */
    public static void removeNull(Object params) {
        if (params instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) params;
            map.entrySet().removeIf(entry -> {
                if (entry.getValue() == null) {
                    return true;
                }
                if (entry.getValue() instanceof Map) {
                    removeNull(entry.getValue());
                }
                return false;
            });
        }
    }
}
