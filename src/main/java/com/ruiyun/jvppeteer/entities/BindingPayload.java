package com.ruiyun.jvppeteer.entities;

import java.util.List;

public class BindingPayload {

    private List<Object> args;
    private String name;
    private int seq;
    private String type;
    private boolean isTrivial;

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    public boolean getIsTrivial() {
        return isTrivial;
    }
    public void setIsTrivial(boolean isTrivial) {
        this.isTrivial = isTrivial;
    }
}
