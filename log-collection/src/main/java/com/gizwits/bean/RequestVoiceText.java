package com.gizwits.bean;

import java.util.List;

/**
 * Created by feel on 2017/7/14.
 */
public class RequestVoiceText {
    private String uid = "uid";
    private String text;
    private String source = "audio_device";
    private int type = 1;
    private String language = "zh_cn";
    private String modes = "modes";
    private List<DeviceInfo> devices;

    public RequestVoiceText() {
    }

    public RequestVoiceText(String text, List<DeviceInfo> devices) {
        this.text = text;

        this.devices = devices;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getModes() {
        return modes;
    }

    public void setModes(String modes) {
        this.modes = modes;
    }

    public List<DeviceInfo> getDevices() {
        return devices;
    }

    public void setDevices(List<DeviceInfo> devices) {
        this.devices = devices;
    }

    @Override
    public String toString() {
        return "RequestVoiceText{" +
                "uid='" + uid + '\'' +
                ", text='" + text + '\'' +
                ", source='" + source + '\'' +
                ", type=" + type +
                ", language='" + language + '\'' +
                ", modes='" + modes + '\'' +
                ", devices=" + devices +
                '}';
    }
}
