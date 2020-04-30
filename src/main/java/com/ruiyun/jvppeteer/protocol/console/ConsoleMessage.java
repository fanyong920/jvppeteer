package com.ruiyun.jvppeteer.protocol.console;

import com.ruiyun.jvppeteer.protocol.js.JSHandle;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.ruiyun.jvppeteer.util.ValidateUtil;

import java.util.List;
import java.util.stream.Collectors;

public class ConsoleMessage {

    private String type;

    private List<JSHandle> args;

    private Object location;

    public ConsoleMessage(String type, List<JSHandle> args, Object location) {
        this.type = type;
        this.args = args;
        this.location = location;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<JSHandle> getArgs() {
        return args;
    }

    public void setArgs(List<JSHandle> args) {
        this.args = args;
    }

    public Object getLocation() {
        return location;
    }

    public void setLocation(Object location) {
        this.location = location;
    }

    /**
     * @return {string}
     */
    public String text() {
        List<String> collect = this.args.stream().map(arg -> {
            if (StringUtil.isNotEmpty(arg.getObjectId()))
                return arg.toString();
            return arg.deserializeValue(arg.getProtocolValue());
        }).collect(Collectors.toList());
        if(ValidateUtil.isEmpty(collect))
            return null;
        return String.join(" ",collect);
    }
}
