package com.ruiyun.jvppeteer.bidi.entities;

public class PointerCommonProperties {
    private Long width;
    private Long height;
    private Double pressure;
    private Double tangentialPressure;
    private Double twist;
    private Double altitudeAngle;
    private Double azimuthAngle;

    public Long getWidth() {
        return width;
    }

    public void setWidth(Long width) {
        this.width = width;
    }

    public Double getAzimuthAngle() {
        return azimuthAngle;
    }

    public void setAzimuthAngle(Double azimuthAngle) {
        this.azimuthAngle = azimuthAngle;
    }

    public Double getAltitudeAngle() {
        return altitudeAngle;
    }

    public void setAltitudeAngle(Double altitudeAngle) {
        this.altitudeAngle = altitudeAngle;
    }

    public Double getTwist() {
        return twist;
    }

    public void setTwist(Double twist) {
        this.twist = twist;
    }

    public Double getTangentialPressure() {
        return tangentialPressure;
    }

    public void setTangentialPressure(Double tangentialPressure) {
        this.tangentialPressure = tangentialPressure;
    }

    public Double getPressure() {
        return pressure;
    }

    public void setPressure(Double pressure) {
        this.pressure = pressure;
    }

    public Long getHeight() {
        return height;
    }

    public void setHeight(Long height) {
        this.height = height;
    }

    @Override
    public String toString() {
        return "PointerCommonProperties{" +
                "width=" + width +
                ", height=" + height +
                ", pressure=" + pressure +
                ", tangentialPressure=" + tangentialPressure +
                ", twist=" + twist +
                ", altitudeAngle=" + altitudeAngle +
                ", azimuthAngle=" + azimuthAngle +
                '}';
    }
}
