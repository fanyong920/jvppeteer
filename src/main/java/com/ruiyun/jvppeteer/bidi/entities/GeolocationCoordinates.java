package com.ruiyun.jvppeteer.bidi.entities;

public class GeolocationCoordinates {
    /**
     * Must be between `-90` and `90`, inclusive.
     */
    private double latitude;
    /**
     * Must be between `-180` and `180`, inclusive.
     */
    private double longitude;
    /**
     * Must be greater than or equal to `0`.
     *
     */
    private double accuracy = 1;

    private Double altitude;
    /**
     * Must be greater than or equal to `0`.
     *
     */
    private Double altitudeAccuracy;
    /**
     * Must be between `0` and `360`.
     *
     */
    private Double heading;
    /**
     * Must be greater than or equal to `0`.
     *
     */
    private Double speed;

    public double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public Double getAltitude() {
        return altitude;
    }

    public void setAltitude(Double altitude) {
        this.altitude = altitude;
    }

    public Double getAltitudeAccuracy() {
        return altitudeAccuracy;
    }

    public void setAltitudeAccuracy(Double altitudeAccuracy) {
        this.altitudeAccuracy = altitudeAccuracy;
    }

    public Double getHeading() {
        return heading;
    }

    public void setHeading(Double heading) {
        this.heading = heading;
    }

    public Double getSpeed() {
        return speed;
    }

    public void setSpeed(Double speed) {
        this.speed = speed;
    }
}
