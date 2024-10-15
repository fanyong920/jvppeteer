package com.ruiyun.jvppeteer.entities;

public class RGBA {
    /**
     * The red component, in the [0-255] range.
     */
    public int r;
    /**
     * The green component, in the [0-255] range.
     */
    public int g;
    /**
     * The blue component, in the [0-255] range.
     */
    public int b;
    /**
     * The alpha component, in the [0-1] range (default: 1).
     */
    public double a;

    public RGBA(int r, int g, int b, double a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public int getR() {
        return r;
    }

    public void setR(int r) {
        this.r = r;
    }

    public int getG() {
        return g;
    }

    public void setG(int g) {
        this.g = g;
    }

    public int getB() {
        return b;
    }

    public void setB(int b) {
        this.b = b;
    }

    public double getA() {
        return a;
    }

    public void setA(double a) {
        this.a = a;
    }

    @Override
    public String toString() {
        return "RGBA{" +
                "r=" + r +
                ", g=" + g +
                ", b=" + b +
                ", a=" + a +
                '}';
    }
}
