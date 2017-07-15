package com.gizwits.bean;

import java.util.Map;

/**
 * Created by feel on 2017/7/13.
 */
public class PayloadBody {

    private String mac;
    private String did;
    private Map cmd;


    public PayloadBody() {
    }

    public PayloadBody(String mac, String did, Map cmd) {
        this.mac = mac;
        this.did = did;
        this.cmd = cmd;
    }

    public boolean isEmpty() {

        if (this.getCmd().isEmpty() || mac == "" || did == "") {
            return true;
        } else {
            return false;
        }
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getDid() {
        return did;
    }

    public void setDid(String did) {
        this.did = did;
    }

    public Map getCmd() {
        return cmd;
    }

    public void setCmd(Map cmd) {
        this.cmd = cmd;
    }

    @Override
    public String toString() {
        return "RequestBody{" +
                "mac='" + mac + '\'' +
                ", did='" + did + '\'' +
                ", cmd=" + cmd +
                '}';
    }
}
