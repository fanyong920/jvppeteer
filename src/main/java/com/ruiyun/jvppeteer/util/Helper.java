package com.ruiyun.jvppeteer.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.ruiyun.jvppeteer.core.Constant;
import com.ruiyun.jvppeteer.events.BrowserListenerWrapper;
import com.ruiyun.jvppeteer.events.DefaultBrowserListener;
import com.ruiyun.jvppeteer.events.EventEmitter;
import com.ruiyun.jvppeteer.protocol.PageEvaluateType;
import com.ruiyun.jvppeteer.protocol.runtime.CallFrame;
import com.ruiyun.jvppeteer.protocol.runtime.ExceptionDetails;
import com.ruiyun.jvppeteer.protocol.runtime.RemoteObject;
import com.ruiyun.jvppeteer.transport.CDPSession;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletionService;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.ruiyun.jvppeteer.core.Constant.COMMONT_THREAD_POOL_NUM;

/**
 * 一些公共方法
 */
public class Helper {

    /**
     * 单线程，一个浏览器只能有一个trcing 任务
     */
    private static ExecutorService COMMON_EXECUTOR = null;

    private static final Logger LOGGER = LoggerFactory.getLogger(Helper.class);
    private static final String os = System.getProperty("os.name");
    private static final boolean WINDOWS = os.startsWith("Windows");
    private static final boolean MAC = os.startsWith("Mac");
    private static final boolean LINUX = os.startsWith("Linux");

    public static String createProtocolError(JsonNode node) {
        JsonNode methodNode = node.get(Constant.RECV_MESSAGE_METHOD_PROPERTY);
        JsonNode errNode = node.get(Constant.RECV_MESSAGE_ERROR_PROPERTY);
        JsonNode errorMsg = errNode.get(Constant.RECV_MESSAGE_ERROR_MESSAGE_PROPERTY);
        String method = "";
        if (methodNode != null) {
            method = methodNode.asText();
        }
        String message = "Protocol error " + method + ": " + errorMsg;
        JsonNode dataNode = errNode.get(Constant.RECV_MESSAGE_ERROR_DATA_PROPERTY);
        if (dataNode != null) {
            message += " " + dataNode.toString();
        }
        return message;
    }

    /**
     * Returns true if the operating system is a form of Windows.
     */
    public static boolean isWindows(){
        return WINDOWS;
    }

    /**
     * Returns true if the operating system is a form of Mac OS.
     */
    public static boolean isMac(){
        return MAC;
    }

    /**
     * Returns true if the operating system is a form of Linux.
     */
    public static boolean isLinux(){
        return LINUX;
    }


    public static final boolean isWin64() {
        String arch = System.getProperty("os.arch");
        return arch.contains("64");
    }

    public static final String paltform() {
        return System.getProperty("os.name");
    }

    public static final void chmod(String path, String perms) throws IOException {

        if (StringUtil.isEmpty(path))
            throw new IllegalArgumentException("Path must not be empty");

        char[] chars = perms.toCharArray();
        if (chars.length != 3) throw new IllegalArgumentException("perms length must be 3");

        Path path1 = Paths.get(path);
        Set<PosixFilePermission> permissions = new HashSet<>();
        //own
        if ('1' == chars[0]) {
            permissions.add(PosixFilePermission.OWNER_EXECUTE);
        } else if ('2' == chars[0]) {
            permissions.add(PosixFilePermission.OWNER_WRITE);
        } else if ('3' == chars[0]) {
            permissions.add(PosixFilePermission.OWNER_WRITE);
            permissions.add(PosixFilePermission.OWNER_EXECUTE);
        } else if ('4' == chars[0]) {
            permissions.add(PosixFilePermission.OWNER_READ);
        } else if ('5' == chars[0]) {
            permissions.add(PosixFilePermission.OWNER_READ);
            permissions.add(PosixFilePermission.OWNER_EXECUTE);
        } else if ('6' == chars[0]) {
            permissions.add(PosixFilePermission.OWNER_READ);
            permissions.add(PosixFilePermission.OWNER_WRITE);
        } else if ('7' == chars[0]) {
            permissions.add(PosixFilePermission.OWNER_READ);
            permissions.add(PosixFilePermission.OWNER_WRITE);
            permissions.add(PosixFilePermission.OWNER_EXECUTE);
        }
        //group
        if ('1' == chars[1]) {
            permissions.add(PosixFilePermission.GROUP_EXECUTE);
        } else if ('2' == chars[1]) {
            permissions.add(PosixFilePermission.GROUP_WRITE);
        } else if ('3' == chars[1]) {
            permissions.add(PosixFilePermission.GROUP_WRITE);
            permissions.add(PosixFilePermission.GROUP_EXECUTE);
        } else if ('4' == chars[1]) {
            permissions.add(PosixFilePermission.GROUP_READ);
        } else if ('5' == chars[1]) {
            permissions.add(PosixFilePermission.GROUP_READ);
            permissions.add(PosixFilePermission.GROUP_EXECUTE);
        } else if ('6' == chars[1]) {
            permissions.add(PosixFilePermission.GROUP_READ);
            permissions.add(PosixFilePermission.GROUP_WRITE);
        } else if ('7' == chars[1]) {
            permissions.add(PosixFilePermission.GROUP_READ);
            permissions.add(PosixFilePermission.GROUP_WRITE);
            permissions.add(PosixFilePermission.GROUP_EXECUTE);
        }
        //other
        if ('1' == chars[2]) {
            permissions.add(PosixFilePermission.OTHERS_EXECUTE);
        } else if ('2' == chars[2]) {
            permissions.add(PosixFilePermission.OTHERS_WRITE);
        } else if ('3' == chars[2]) {
            permissions.add(PosixFilePermission.OTHERS_WRITE);
            permissions.add(PosixFilePermission.OTHERS_EXECUTE);
        } else if ('4' == chars[2]) {
            permissions.add(PosixFilePermission.OTHERS_READ);
        } else if ('5' == chars[2]) {
            permissions.add(PosixFilePermission.OTHERS_READ);
            permissions.add(PosixFilePermission.OTHERS_EXECUTE);
        } else if ('6' == chars[2]) {
            permissions.add(PosixFilePermission.OTHERS_READ);
            permissions.add(PosixFilePermission.OWNER_WRITE);
        } else if ('7' == chars[2]) {
            permissions.add(PosixFilePermission.OTHERS_READ);
            permissions.add(PosixFilePermission.OTHERS_WRITE);
            permissions.add(PosixFilePermission.OTHERS_EXECUTE);
        }

        Files.setPosixFilePermissions(path1, permissions);
    }

    public static final String join(String root, String... args) {
        return java.nio.file.Paths.get(root, args).toString();
    }

    /**
     * read stream from protocol : example for tracing  file
     *
     * @param client  CDPSession
     * @param handler 发送给websocket的参数
     * @param path    文件存放的路径
     * @param isNewThread 是否是在新的线程中执行
     * @throws IOException 操作文件的异常
     */
    public static final void readProtocolStream(CDPSession client, String handler, String path, boolean isNewThread) throws IOException {
        if (isNewThread) {
            Helper.commonExecutor().submit(() -> {
                try {
                    run(client, handler, path);
                } catch (IOException e) {
                    LOGGER.error("method readProtocolStream error",e);
                }
            });
        } else {
            run(client, handler, path);
        }
    }

    private static void run(CDPSession client, String handler, String path) throws IOException {
        boolean eof = false;
        File file = null;
        BufferedOutputStream writer = null;
        BufferedInputStream reader = null;
        if (StringUtil.isNotEmpty(path)) {
            file = new File(path);
            FileUtil.createNewFile(file);
        }
        Map<String, Object> params = new HashMap<>();
        params.put("handle", handler);
        try {
            if (file != null) {
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                writer = new BufferedOutputStream(fileOutputStream);
            }
            byte[] buffer = new byte[Constant.DEFAULT_BUFFER_SIZE];
            byte[] bytes;
            while (!eof) {
                JsonNode response = client.send("IO.read", params, true);
                JsonNode eofNode = response.get(Constant.RECV_MESSAGE_STREAM_EOF_PROPERTY);
                JsonNode base64EncodedNode = response.get(Constant.RECV_MESSAGE_BASE64ENCODED_PROPERTY);
                JsonNode dataNode = response.get(Constant.RECV_MESSAGE_STREAM_DATA_PROPERTY);
                String dataText;
                if (dataNode != null && StringUtil.isNotEmpty(dataText = dataNode.asText())) {
                    try {
                        if (base64EncodedNode != null && base64EncodedNode.asBoolean()) {
                            bytes = Base64.decode(dataText);
                        } else {
                            bytes = dataNode.asText().getBytes();
                        }
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
                eof = eofNode.asBoolean();
            }
            client.send("IO.close", params, false);
        } finally {
            StreamUtil.closeQuietly(writer);
            StreamUtil.closeQuietly(reader);
        }
    }


    public static String getExceptionMessage(ExceptionDetails exceptionDetails) {
        if (exceptionDetails.getException() != null)
            return StringUtil.isNotEmpty(exceptionDetails.getException().getDescription()) ? exceptionDetails.getException().getDescription() : (String) exceptionDetails.getException().getValue();
        String message = exceptionDetails.getText();
        StringBuilder sb = new StringBuilder(message);
        if (exceptionDetails.getStackTrace() != null) {
            for (CallFrame callframe : exceptionDetails.getStackTrace().getCallFrames()) {
                String location = callframe.getUrl() + ":" + callframe.getColumnNumber() + ":" + callframe.getColumnNumber();
                String functionName = StringUtil.isNotEmpty(callframe.getFunctionName()) ? callframe.getFunctionName() : "<anonymous>";
                sb.append("\n    at ").append(functionName).append("(").append(location).append(")");
            }
        }
        return sb.toString();
    }

    public static final Set<DefaultBrowserListener> getConcurrentSet() {
        return new CopyOnWriteArraySet<>();
    }

    public static final <T> BrowserListenerWrapper<T> addEventListener(EventEmitter emitter, String eventName, DefaultBrowserListener<T> handler) {
        emitter.on(eventName, handler);
        return new BrowserListenerWrapper<>(emitter, eventName, handler);
    }

    public static final void removeEventListeners(List<BrowserListenerWrapper> eventListeners) {
        if (ValidateUtil.isEmpty(eventListeners)) {
            return;
        }
        for (int i = 0; i < eventListeners.size(); i++) {
            BrowserListenerWrapper wrapper = eventListeners.get(i);
            wrapper.getEmitter().removeListener(wrapper.getEventName(), wrapper.getHandler());
        }
    }

    public static final boolean isString(Object value) {
        if (value == null)
            return false;
        return value.getClass().equals(String.class);
    }

    public static final boolean isNumber(String s) {
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
                    return -0;
                case "NaN":
                    return "NaN";
                case "Infinity":
                    return "Infinity";
                case "-Infinity":
                    return "-Infinity";
                default:
                    throw new IllegalArgumentException("Unsupported unserializable value: " + remoteObject.getUnserializableValue());
            }
        }
        return remoteObject.getValue();
    }

    public static void releaseObject(CDPSession client, RemoteObject remoteObject, boolean isBlock) {
        if (isBlock) {
            releaseObject(client, remoteObject);
        } else {
            Helper.commonExecutor().submit(() -> releaseObject(client, remoteObject));
        }

    }

    private static void releaseObject(CDPSession client, RemoteObject remoteObject) {
        if (StringUtil.isEmpty(remoteObject.getObjectId()))
            return;
        Map<String, Object> params = new HashMap<>();
        params.put("objectId", remoteObject.getObjectId());
        try {
            client.send("Runtime.releaseObject", params, true);
        } catch (Exception e) {
            // Exceptions might happen in case of a page been navigated or closed.
            //重新导航到某个网页 或者页面已经关闭
            // Swallow these since they are harmless and we don't leak anything in this case.
            //在这种情况下不需要将这个错误在线程执行中抛出，打日志记录一下就可以了
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error("", e);
            }
        }
    }

    public static final void copyProperties(Object src, Object dest) throws IntrospectionException, InvocationTargetException, IllegalAccessException {
        Class<?> destClass = dest.getClass();
        BeanInfo srcBean = Introspector.getBeanInfo(src.getClass());
        PropertyDescriptor[] propertyDescriptors = srcBean.getPropertyDescriptors();
        for (PropertyDescriptor descriptor : propertyDescriptors) {
            PropertyDescriptor destDescriptor = new PropertyDescriptor(descriptor.getName(), destClass);
            destDescriptor.getWriteMethod().invoke(dest, descriptor.getReadMethod().invoke(src));
        }
    }

    public static Object waitForEvent(EventEmitter eventEmitter, String eventName, Predicate predicate, int timeout, String abortPromise) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        final Object[] result = {null};
        DefaultBrowserListener listener = new DefaultBrowserListener() {
            @Override
            public void onBrowserEvent(Object event) {
                if (!predicate.test(event))
                    return;
                result[0] = event;
                latch.countDown();
            }
        };
        listener.setMothod(eventName);
        BrowserListenerWrapper wrapper = addEventListener(eventEmitter, eventName, listener);
        try {
            boolean await = latch.await(timeout, TimeUnit.MILLISECONDS);
            if (!await) {
                throw new RuntimeException(abortPromise);
            }
            return result[0];
        } finally {
            List<BrowserListenerWrapper> removes = new ArrayList<>();
            removes.add(wrapper);
            removeEventListeners(removes);
        }

    }

    public static final String evaluationString(String fun, PageEvaluateType type, Object... args) {
        if (PageEvaluateType.STRING.equals(type)) {
            ValidateUtil.assertArg(args.length == 0, "Cannot evaluate a string with arguments");
            return fun;
        }
        List<String> argsList = new ArrayList<>();
        for (Object arg : args) {
            if (arg == null) {
                argsList.add("undefined");
            } else {
                try {
                    argsList.add(Constant.OBJECTMAPPER.writeValueAsString(arg));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return MessageFormat.format("({0})({1})", fun, String.join(",", argsList));
    }

    public static final ExecutorService commonExecutor() {
        if (COMMON_EXECUTOR == null) {
            synchronized (Helper.class) {
                if (COMMON_EXECUTOR == null) {
                    String customNum = System.getProperty(COMMONT_THREAD_POOL_NUM);
                    int threadNum = 0;
                    if(StringUtil.isNotEmpty(customNum)){
                        threadNum = Integer.parseInt(customNum);
                    }else {
                        threadNum = Math.max(1, Runtime.getRuntime().availableProcessors());
                    }
                    COMMON_EXECUTOR = new ThreadPoolExecutor(threadNum, threadNum, 30, TimeUnit.SECONDS, new LinkedBlockingDeque<>(), new CommonThreadFactory());
                }
            }
        }
        return COMMON_EXECUTOR;
    }

    public static final CompletionService completionService() {
        return new ExecutorCompletionService(Helper.commonExecutor());
    }

    static class CommonThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        CommonThreadFactory() {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                    Thread.currentThread().getThreadGroup();
            namePrefix = "jvppeteer-common-pool-" +
                    poolNumber.getAndIncrement() +
                    "-thread-";
        }

        public Thread newThread( Runnable r) {
            Thread t = new Thread(group, r,
                    namePrefix + threadNumber.getAndIncrement(),
                    0);
            t.setDaemon(true);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }
}
