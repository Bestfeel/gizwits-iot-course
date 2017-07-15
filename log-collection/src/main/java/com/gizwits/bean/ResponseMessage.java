package com.gizwits.bean;

/**
 * Created by feel on 2017/2/1.
 */
public class ResponseMessage {

    private Integer code;
    private String message;
    private String ref;

    public ResponseMessage() {
    }

    public ResponseMessage(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public ResponseMessage(Integer code, String message, String ref) {
        this.code = code;
        this.message = message;
        this.ref = ref;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    @Override
    public String toString() {
        return "ResponseMessage{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", ref='" + ref + '\'' +
                '}';
    }
}
