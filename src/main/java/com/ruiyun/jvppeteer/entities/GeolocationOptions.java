package com.ruiyun.jvppeteer.entities;

public class GeolocationOptions {
    /**
     * 纬度介于 -90 和 90 之间
     */
    public double longitude;
    /**
     * -180 和 180 之间的经度。
     */
    public double latitude;

    /**
     * 可选的非负精度值。
     */
    public double accuracy = 0;

    public GeolocationOptions(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public GeolocationOptions(double longitude, double latitude, double accuracy) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.accuracy = accuracy;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    @Override
    public String toString() {
        return "GeolocationOptions{" +
                "longitude=" + longitude +
                ", latitude=" + latitude +
                ", accuracy=" + accuracy +
                '}';
    }
}
