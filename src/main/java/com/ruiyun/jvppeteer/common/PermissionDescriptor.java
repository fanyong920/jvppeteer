package com.ruiyun.jvppeteer.common;

public class PermissionDescriptor {
    /**
     * Name of permission.
     * See https://cs.chromium.org/chromium/src/third_party/blink/renderer/modules/permissions/permission_descriptor.idl for valid permission names.
     */
    private String name;
    /**
     * For "push" permission, may specify userVisibleOnly.
     * Note that userVisibleOnly = true is the only currently supported type.
     */
    private Boolean userVisibleOnly;
    /**
     * For "midi" permission, may also specify sysex control.
     */
    private Boolean sysex;
    /**
     * For "camera" permission, may specify panTiltZoom.
     */
    private Boolean panTiltZoom;
    /**
     * For "clipboard" permission, may specify allowWithoutSanitization.
     */
    private Boolean allowWithoutSanitization;
    /**
     * For "fullscreen" permission, must specify allowWithoutGesture:true.
     */
    private Boolean allowWithoutGesture;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getUserVisibleOnly() {
        return userVisibleOnly;
    }

    public void setUserVisibleOnly(Boolean userVisibleOnly) {
        this.userVisibleOnly = userVisibleOnly;
    }

    public Boolean getSysex() {
        return sysex;
    }

    public void setSysex(Boolean sysex) {
        this.sysex = sysex;
    }

    public Boolean getPanTiltZoom() {
        return panTiltZoom;
    }

    public void setPanTiltZoom(Boolean panTiltZoom) {
        this.panTiltZoom = panTiltZoom;
    }

    public Boolean getAllowWithoutSanitization() {
        return allowWithoutSanitization;
    }

    public void setAllowWithoutSanitization(Boolean allowWithoutSanitization) {
        this.allowWithoutSanitization = allowWithoutSanitization;
    }

    public Boolean getAllowWithoutGesture() {
        return allowWithoutGesture;
    }

    public void setAllowWithoutGesture(Boolean allowWithoutGesture) {
        this.allowWithoutGesture = allowWithoutGesture;
    }
}
