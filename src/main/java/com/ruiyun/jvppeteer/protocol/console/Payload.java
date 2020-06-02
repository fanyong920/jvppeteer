package com.ruiyun.jvppeteer.protocol.console;

import java.util.List;

public class Payload {

    private List<Object> args;

    private String name;

    private int seq;

    public List<Object> getArgs() {
        return args;
    }

    public void setArgs(List<Object> args) {
        this.args = args;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }
}
