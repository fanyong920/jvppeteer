package com.ruiyun.jvppeteer.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.ruiyun.jvppeteer.core.Constant;
import com.ruiyun.jvppeteer.core.browser.BrowserRunner;
import com.ruiyun.jvppeteer.events.EventEmitter;
import com.ruiyun.jvppeteer.protocol.PageEvaluateType;
import com.ruiyun.jvppeteer.protocol.runtime.CallFrame;
import com.ruiyun.jvppeteer.protocol.runtime.ExceptionDetails;
import com.ruiyun.jvppeteer.protocol.runtime.RemoteObject;
import com.ruiyun.jvppeteer.transport.CDPSession;
import com.sun.jna.Platform;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.internal.disposables.CancellableDisposable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 一些公共方法
 */
public class Helper {
    private static final Logger LOGGER = LoggerFactory.getLogger(Helper.class);

    public static String createProtocolError(JsonNode node) {
        JsonNode methodNode = node.get(Constant.MESSAGE_METHOD_PROPERTY);
        JsonNode errNode = node.get(Constant.MESSAGE_MESSAGE_PROPERTY);
        JsonNode errorMsg = errNode.get(Constant.MESSAGE_MESSAGE_PROPERTY);
        String method = "";
        if (methodNode != null) {
            method = methodNode.asText();
        }
        String message = "Protocol error " + method + ": " + errorMsg;
        JsonNode dataNode = errNode.get(Constant.MESSAGE_DATA_PROPERTY);
        if (dataNode != null) {
            message += " " + dataNode;
        }
        return message;
    }




    public static String platform() {
        return System.getProperty("os.name");
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
     * @param isSync 是否是在新的线程中执行
     * @throws IOException 操作文件的异常
     * @return 可能是feture，可能是字节数组
     */
    public static Object readProtocolStream(CDPSession client, String handler, String path, boolean isSync) throws IOException {
        if (isSync) {
            return ForkJoinPool.commonPool().submit(() -> {
                try {
                    printPDF(client, handler, path);
                } catch (IOException e) {
                    LOGGER.error("Method readProtocolStream error",e);
                }
            });
        } else {
           return printPDF(client, handler, path);
        }
    }

    private static byte[] printPDF(CDPSession client, String handler, String path) throws IOException {
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
            List<byte[]> bufs = new ArrayList<>();
            int byteLength = 0;

            while (!eof) {
                JsonNode response = client.send("IO.read", params);
                JsonNode eofNode = response.get(Constant.MESSAGE_EOF_PROPERTY);
                JsonNode base64EncodedNode = response.get(Constant.MESSAGE_BASE64ENCODED_PROPERTY);
                JsonNode dataNode = response.get(Constant.MESSAGE_STREAM_DATA_PROPERTY);
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
     * @param bufs 数组集合
     * @param byteLength 数组总长度
     * @return 总数组
     */
    private static byte[] getBytes(List<byte[]> bufs, int byteLength) {
        //返回字节数组
        byte[] resultBuf = new byte[byteLength];
        int destPos = 0;
        for (byte[] buf : bufs) {
            System.arraycopy(buf,0,resultBuf,destPos,buf.length);
            destPos += buf.length;
        }
        return  resultBuf;
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
    // 定义通用方法用于创建Observable
    public static  <T, EventType> io.reactivex.rxjava3.core.Observable<T> fromEmitterEvent(EventEmitter<EventType> emitter, EventType eventType) {
        return io.reactivex.rxjava3.core.Observable.create(subscriber -> {
            if (!subscriber.isDisposed()) {
                Consumer<T> listener = subscriber::onNext;
                emitter.on(eventType, listener);
                Disposable disposable = new CancellableDisposable(() -> emitter.off(eventType, listener));
                subscriber.setDisposable(disposable);
            }
        });
    }
    public static boolean isString(Object value) {
        if (value == null)
            return false;
        return value.getClass().equals(String.class);
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
        if (StringUtil.isEmpty(remoteObject.getObjectId()))
            return;
        Map<String, Object> params = new HashMap<>();
        params.put("objectId", remoteObject.getObjectId());
        try {
            client.send("Runtime.releaseObject", params,null, isBlock);
        } catch (Exception e) {
            // Exceptions might happen in case of a page been navigated or closed.
            //重新导航到某个网页 或者页面已经关闭
            // Swallow these since they are harmless and we don't leak anything in this case.
            //在这种情况下不需要将这个错误在线程执行中抛出，打日志记录一下就可以了
        }
    }



    public static String evaluationString(String fun, PageEvaluateType type, Object... args) {
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




    /**
     * 判断js字符串是否是一个函数
     * @param pageFunction js字符串
     * @return true代表是js函数
     */
    public static boolean isFunction(String pageFunction){
        pageFunction = pageFunction.trim();
        return pageFunction.startsWith("function") || pageFunction.startsWith("async") || pageFunction.contains("=>");
    }

    /**
     * 获取进程id
     * @param process 进程
     * @return 进程id
     */
    public static String getProcessId(Process process) {
        long pid = -1;
        Field field;
        if (Platform.isWindows()) {
            try {
                field = process.getClass().getDeclaredField("handle");
                field.setAccessible(true);
                pid = BrowserRunner.Kernel32.INSTANCE.GetProcessId((Long) field.get(process));
            } catch (Exception e) {
                LOGGER.error("Failed to get processId on Windows platform.",e);
            }
        } else if (Platform.isLinux() || Platform.isAIX()) {
            try {
                String version = System.getProperty("java.version");
                double jdkversion = Double.parseDouble(version.substring(0, 3));
                Class<?> clazz;
                if (jdkversion <= 1.8) {
                    clazz = Class.forName("java.lang.UNIXProcess");
                } else {
                    clazz = Class.forName("java.lang.ProcessImpl");
                }
                field = clazz.getDeclaredField("pid");
                field.setAccessible(true);
                pid = (Integer) field.get(process);
            } catch (Throwable e) {
                LOGGER.error("Failed to get processId on Linux or Aix platform.",e);
            }
        }
        return String.valueOf(pid);
    }

    public static String createProtocolErrorMessage(JsonNode receivedNode) {
        String message = receivedNode.get("error").get("message").asText();
        if(receivedNode.hasNonNull("error") && receivedNode.get("error").hasNonNull("data")){
            message += " " + receivedNode.get("error").get("data").asText();
        }
        return message;
    }
}
