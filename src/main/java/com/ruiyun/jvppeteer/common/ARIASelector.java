package com.ruiyun.jvppeteer.common;

public class ARIASelector {
    private String name;
    private String role;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "ARIASelector{" +
                "name='" + name + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}
