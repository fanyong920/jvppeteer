package com.ruiyun.jvppeteer.entities;

public class TouchPoint extends Point {
    private double radiusX;
    private double radiusY;
    private double rotationAngle;
    private double force;
    private double tangentialPressure;
    private double tiltX;
    private double tiltY;
    private int twist;
    private double id;

    public double getRadiusX() {
        return radiusX;
    }

    public void setRadiusX(double radiusX) {
        this.radiusX = radiusX;
    }

    public double getRadiusY() {
        return radiusY;
    }

    public void setRadiusY(double radiusY) {
        this.radiusY = radiusY;
    }

    public double getRotationAngle() {
        return rotationAngle;
    }

    public void setRotationAngle(double rotationAngle) {
        this.rotationAngle = rotationAngle;
    }

    public double getForce() {
        return force;
    }

    public void setForce(double force) {
        this.force = force;
    }

    public double getTangentialPressure() {
        return tangentialPressure;
    }

    public void setTangentialPressure(double tangentialPressure) {
        this.tangentialPressure = tangentialPressure;
    }

    public double getTiltY() {
        return tiltY;
    }

    public void setTiltY(double tiltY) {
        this.tiltY = tiltY;
    }

    public double getTiltX() {
        return tiltX;
    }

    public void setTiltX(double tiltX) {
        this.tiltX = tiltX;
    }

    public int getTwist() {
        return twist;
    }

    public void setTwist(int twist) {
        this.twist = twist;
    }

    public double getId() {
        return id;
    }

    public void setId(double id) {
        this.id = id;
    }
}
