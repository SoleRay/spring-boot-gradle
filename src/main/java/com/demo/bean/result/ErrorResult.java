package com.demo.bean.result;

public class ErrorResult<T> extends AbstractResult {

    private T errors;//详细错误

    protected ErrorResult(ResultCode resultCode) {
        this(resultCode,null);
    }

    protected ErrorResult(ResultCode resultCode,T errors) {
        super(resultCode);
    }

    public T getErrors() {
        return errors;
    }

    public void setErrors(T errors) {
        this.errors = errors;
    }

    public static ErrorResult failure(ResultCode resultCode){
        return new ErrorResult(resultCode);
    }

    public static ErrorResult failure(ResultCode resultCode,Object errors){
        return new ErrorResult(resultCode,errors);
    }
}
