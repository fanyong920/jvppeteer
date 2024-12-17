package com.ruiyun.jvppeteer.bidi.entities;

import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;

public class SourceActions {
    private SourceActionsType type;
    private String id;
    private List<SourceActions> actions;
    private PointerParameters parameters;
    private String value;
    private Long duration;
    private Integer x;
    private Integer y;
    private ObjectNode origin;
    private Long button;
    private Integer deltaX;
    private Integer deltaY;

    private Long width;
    private Long height;
    private Double pressure;
    private Double tangentialPressure;
    private Double twist;
    private Double altitudeAngle;
    private Double azimuthAngle;

    public Long getButton() {
        return button;
    }

    public void setButton(Long button) {
        this.button = button;
    }

    public Integer getX() {
        return x;
    }

    public void setX(Integer x) {
        this.x = x;
    }

    public Integer getY() {
        return y;
    }

    public void setY(Integer y) {
        this.y = y;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public SourceActions() {
    }

    public SourceActionsType getType() {
        return type;
    }

    public void setType(SourceActionsType type) {
        this.type = type;
    }

    public PointerParameters getParameters() {
        return parameters;
    }

    public void setParameters(PointerParameters parameters) {
        this.parameters = parameters;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<SourceActions> getActions() {
        return actions;
    }

    public void setActions(List<SourceActions> actions) {
        this.actions = actions;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public ObjectNode getOrigin() {
        return origin;
    }

    public void setOrigin(ObjectNode origin) {
        this.origin = origin;
    }

    public Integer getDeltaX() {
        return deltaX;
    }

    public void setDeltaX(Integer deltaX) {
        this.deltaX = deltaX;
    }

    public Integer getDeltaY() {
        return deltaY;
    }

    public void setDeltaY(Integer deltaY) {
        this.deltaY = deltaY;
    }

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
        return "SourceActions{" +
                "type=" + type +
                ", id='" + id + '\'' +
                ", actions=" + actions +
                ", parameters=" + parameters +
                ", value='" + value + '\'' +
                ", duration=" + duration +
                ", x=" + x +
                ", y=" + y +
                ", origin=" + origin +
                ", button=" + button +
                ", deltaX=" + deltaX +
                ", deltaY=" + deltaY +
                ", width=" + width +
                ", height=" + height +
                ", pressure=" + pressure +
                ", tangentialPressure=" + tangentialPressure +
                ", twist=" + twist +
                ", altitudeAngle=" + altitudeAngle +
                ", azimuthAngle=" + azimuthAngle +
                '}';
    }
}
