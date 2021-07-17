/**  
 * @Title: Result.java
 * @Package com.globalvillage.bg.baseUtils
 * @Description: TODO
 * @author likeke
 * @date 2016年5月17日
 */
package com.demo.bean.result;

/**
 * ClassName: Result 
 * @Description: TODO
 * @author SoleRay
 * @date 2017年07月14日
 * @version 1.0
 */
public class Result<T> extends AbstractResult{

	private T data;//数据

	public Result(String code, String message) {
		this(code,message,null);
	}

	public Result(String code, String message,T data) {
		super(code, message);
		this.data = data;
	}

	private Result(ResultCode resultCode) {
		this(resultCode,null);
	}

	private Result(ResultCode resultCode, T data) {
		super(resultCode);
		this.data = data;
	}

	public static Result success(){
		return new Result(ResultCode.SUCCESS);
	}

	public static Result success(Object data){
		return new Result(ResultCode.SUCCESS,data);
	}

	public static Result failure(String code,String msg){
		return new Result(code,msg);
	}

	public static Result failure(String code,String msg,Object data){
		return new Result(code,msg,data);
	}

	public static Result failure(ResultCode resultCode){
		return new Result(resultCode);
	}

	public static Result failure(ResultCode resultCode,Object data){
		return new Result(resultCode,data);
	}

	public Object getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

}
