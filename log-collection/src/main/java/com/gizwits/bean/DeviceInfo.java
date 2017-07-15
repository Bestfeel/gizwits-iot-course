package com.gizwits.bean;

/**
 * Created by feel on 2017/7/14.
 */
public class DeviceInfo {
    private String pk;
    private String pn = "gokit";
    private String pa = "小智";
    private String pt = "normal";
    private String did;
    private String alias = "小智";
    private boolean online = true;

    public DeviceInfo() {
    }

    public DeviceInfo(String pk, String did) {
        this.pk = pk;
        this.did = did;
    }

    public String getPk() {
        return pk;
    }

    public void setPk(String pk) {
        this.pk = pk;
    }

    public String getPn() {
        return pn;
    }

    public void setPn(String pn) {
        this.pn = pn;
    }

    public String getPa() {
        return pa;
    }

    public void setPa(String pa) {
        this.pa = pa;
    }

    public String getPt() {
        return pt;
    }

    public void setPt(String pt) {
        this.pt = pt;
    }

    public String getDid() {
        return did;
    }

    public void setDid(String did) {
        this.did = did;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    @Override
    public String toString() {
        return "DeviceInfo{" +
                "pk='" + pk + '\'' +
                ", pn='" + pn + '\'' +
                ", pa='" + pa + '\'' +
                ", pt='" + pt + '\'' +
                ", did='" + did + '\'' +
                ", alias='" + alias + '\'' +
                ", online=" + online +
                '}';
    }
}
