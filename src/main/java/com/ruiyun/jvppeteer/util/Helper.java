package com.ruiyun.jvppeteer.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.ruiyun.jvppeteer.Constant;
import com.ruiyun.jvppeteer.events.impl.BrowserListenerWrapper;
import com.ruiyun.jvppeteer.events.impl.DefaultBrowserListener;
import com.ruiyun.jvppeteer.events.impl.EventEmitter;
import com.ruiyun.jvppeteer.protocol.runtime.CallFrame;
import com.ruiyun.jvppeteer.protocol.runtime.ExceptionDetails;
import com.ruiyun.jvppeteer.protocol.runtime.RemoteObject;
import com.ruiyun.jvppeteer.transport.websocket.CDPSession;
import sun.misc.BASE64Decoder;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 一些公共方法
 */
public class Helper {
    public static String createProtocolError(JsonNode node) {
        JsonNode methodNode = node.get(Constant.RECV_MESSAGE_METHOD_PROPERTY);
        JsonNode errNode = node.get(Constant.RECV_MESSAGE_ERROR_PROPERTY);
        JsonNode errorMsg = errNode.get(Constant.RECV_MESSAGE_ERROR_MESSAGE_PROPERTY);
        String method = "";
        if(methodNode != null){
            method = methodNode.asText();
        }
        String message = "Protocol error " + method + ": " + errorMsg;
        JsonNode dataNode = errNode.get(Constant.RECV_MESSAGE_ERROR_DATA_PROPERTY);
        if (dataNode != null) {
            message += " " + dataNode.toString();
        }
        return message;
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
            permissions.add(PosixFilePermission.OWNER_EXECUTE);
        } else if ('2' == chars[1]) {
            permissions.add(PosixFilePermission.OWNER_WRITE);
        } else if ('3' == chars[1]) {
            permissions.add(PosixFilePermission.OWNER_WRITE);
            permissions.add(PosixFilePermission.OWNER_EXECUTE);
        } else if ('4' == chars[1]) {
            permissions.add(PosixFilePermission.OWNER_READ);
        } else if ('5' == chars[1]) {
            permissions.add(PosixFilePermission.OWNER_READ);
            permissions.add(PosixFilePermission.OWNER_EXECUTE);
        } else if ('6' == chars[1]) {
            permissions.add(PosixFilePermission.OWNER_READ);
            permissions.add(PosixFilePermission.OWNER_WRITE);
        } else if ('7' == chars[1]) {
            permissions.add(PosixFilePermission.OWNER_READ);
            permissions.add(PosixFilePermission.OWNER_WRITE);
            permissions.add(PosixFilePermission.OWNER_EXECUTE);
        }
        //other
        if ('1' == chars[2]) {
            permissions.add(PosixFilePermission.OWNER_EXECUTE);
        } else if ('2' == chars[2]) {
            permissions.add(PosixFilePermission.OWNER_WRITE);
        } else if ('3' == chars[2]) {
            permissions.add(PosixFilePermission.OWNER_WRITE);
            permissions.add(PosixFilePermission.OWNER_EXECUTE);
        } else if ('4' == chars[2]) {
            permissions.add(PosixFilePermission.OWNER_READ);
        } else if ('5' == chars[2]) {
            permissions.add(PosixFilePermission.OWNER_READ);
            permissions.add(PosixFilePermission.OWNER_EXECUTE);
        } else if ('6' == chars[2]) {
            permissions.add(PosixFilePermission.OWNER_READ);
            permissions.add(PosixFilePermission.OWNER_WRITE);
        } else if ('7' == chars[2]) {
            permissions.add(PosixFilePermission.OWNER_READ);
            permissions.add(PosixFilePermission.OWNER_WRITE);
            permissions.add(PosixFilePermission.OWNER_EXECUTE);
        }

        Files.setPosixFilePermissions(path1, permissions);
    }

    public static final String join(String root, String... args) {
        return java.nio.file.Paths.get(root, args).toString();
    }

    /**
     * read stream from protocol : example -> tracing  file
     *
     * @param client  CDPSession
     * @param handler 发送给websocket的参数
     * @param path    文件存放的路径
     */
    public static final void readProtocolStream(CDPSession client, JsonNode handler, String path) throws IOException {
        boolean eof = false;
        File file = null;
        BufferedOutputStream writer = null;
        if (StringUtil.isNotEmpty(path)) {
            file = new File(path);
        } else {
            throw new IllegalArgumentException("Path can't be null");
        }
        Map<String, Object> params = new HashMap<>();
        Iterator<String> fieldNames = handler.fieldNames();
        while (fieldNames.hasNext()) {
            String name = fieldNames.next();
            params.put(name, handler.get(name).asText());
        }
        try {
            writer = new BufferedOutputStream(new FileOutputStream(file));
            BASE64Decoder decoder = new BASE64Decoder();
            while (!eof) {
                JsonNode response = client.send("IO.read", params, true);
                JsonNode eofNode = response.get(Constant.RECV_MESSAGE_STREAM_EOF_PROPERTY);
                JsonNode base64EncodedNode = response.get(Constant.RECV_MESSAGE_BASE64ENCODED_PROPERTY);
                JsonNode dataNode = response.get(Constant.RECV_MESSAGE_STREAM_DATA_PROPERTY);
                if (dataNode != null) {
                    byte[] bytes;
                    if (base64EncodedNode != null) {
                        bytes = decoder.decodeBuffer(dataNode.toString());
                    } else {
                        bytes = dataNode.toString().getBytes();
                    }
                    writer.write(bytes);
                }
                eof = eofNode.asBoolean();
            }
            writer.flush();
            client.send("IO.close", params, false);
        } finally {
            StreamUtil.closeStream(writer);
        }
    }


    public static String getExceptionMessage(ExceptionDetails exceptionDetails) {
        if (exceptionDetails.getException() != null)
            return StringUtil.isNotEmpty(exceptionDetails.getException().getDescription()) ? exceptionDetails.getException().getDescription() : (String)exceptionDetails.getException().getValue();
        String message = exceptionDetails.getText();
        if (exceptionDetails.getStackTrace() != null) {
            for (CallFrame callframe : exceptionDetails.getStackTrace().getCallFrames()) {
                String location = callframe.getUrl() + ":" + callframe.getColumnNumber() + ":" + callframe.getColumnNumber();
                String functionName = StringUtil.isNotEmpty(callframe.getFunctionName()) ? callframe.getFunctionName() : "<anonymous>";
                message += "\n    at " + functionName + "(" + location + ")";
            }
        }
        return message;
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
        if (value.getClass().equals(String.class)) {
            return true;
        }
        return false;
    }

    public static final boolean isNumber(String s) {
        Pattern pattern = Pattern.compile("-?[0-9]+(\\.[0-9]+)?");
        Matcher matcher = pattern.matcher(s);
        if (matcher.matches()) {
            return true;
        }
        return false;
    }

    public static Object valueFromRemoteObject(RemoteObject remoteObject) {
        ValidateUtil.assertBoolean(StringUtil.isEmpty(remoteObject.getObjectId()), "Cannot extract value when objectId is given");
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

    public static void releaseObject(CDPSession client, RemoteObject remoteObject) {
        if (StringUtil.isEmpty(remoteObject.getObjectId()))
            return;
        Map<String, Object> params = new HashMap<>();
        params.put("objectId", remoteObject.getObjectId());
        client.send("Runtime.releaseObject", params, false);
    }

    //TODO 验证
    public static final void copyProperties(Object src,Object dest) throws IntrospectionException, InvocationTargetException, IllegalAccessException {
        Class<?> destClass = dest.getClass();
        BeanInfo srcBean = Introspector.getBeanInfo(src.getClass());
        PropertyDescriptor[] propertyDescriptors = srcBean.getPropertyDescriptors();
        for (PropertyDescriptor descriptor : propertyDescriptors) {
            PropertyDescriptor destDescriptor = new PropertyDescriptor(descriptor.getName(),destClass);
            destDescriptor.getWriteMethod().invoke(dest,descriptor.getReadMethod().invoke(src));
        }
    }
    public static <T> T waitForEvent(EventEmitter eventEmitter, String eventName, Predicate<T> predicate, int timeout, String abortPromise) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        final T[] result = (T[]) new Object[1];
        DefaultBrowserListener<T> listener = new DefaultBrowserListener<T>() {
            @Override
            public void onBrowserEvent(T event) {
                if (!predicate.test(event))
                    return;
                result[0] = event;
                latch.countDown();
            }
        };
        listener.setMothod(eventName);
        BrowserListenerWrapper<T> wrapper = addEventListener(eventEmitter, eventName, listener);
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
}
