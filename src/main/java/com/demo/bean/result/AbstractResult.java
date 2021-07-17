package com.demo.bean.result;

public abstract class AbstractResult {

    private String code; //编码

    private String message;  //结果说明

    public AbstractResult(String code, String message) {
        this.code = code;
        this.message = message;
    }

    protected AbstractResult(ResultCode resultCode) {
        this.code = resultCode.getCode();
        this.message = resultCode.getMsg();
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
