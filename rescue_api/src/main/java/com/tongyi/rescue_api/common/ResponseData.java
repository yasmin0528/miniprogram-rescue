package com.tongyi.rescue_api.common;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

public class ResponseData<T> implements Serializable {
    private String code;

    private String msg;

    private Boolean success;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime time = LocalDateTime.now();

    private T data;

    private ResponseData(){}

    public static <T> ResponseData<T> ok(){
        return createResponseData(ResponseStatusEnum.OK.getCode(), ResponseStatusEnum.OK.getMsg(), null);
    }

    public static <T> ResponseData<T> ok(T data){
        return createResponseData(ResponseStatusEnum.OK.getCode(), ResponseStatusEnum.OK.getMsg(), data);
    }
    public static <T> ResponseData<T> ok(String msg,T data){
        return createResponseData(ResponseStatusEnum.OK.getCode(), msg, data);
    }

    public static <T> ResponseData<T> createResponseData(String code, String msg, T data){
        return createResponseData(code, msg, true, data);
    }

    public static <T> ResponseData<T> error(){
        return createResponseData(
                ResponseStatusEnum.SYS_ERROR.getCode(), ResponseStatusEnum.SYS_ERROR.getMsg(), false,  null);
    }

    public static <T> ResponseData<T> error(String msg){
        return createResponseData(ResponseStatusEnum.SYS_ERROR.getCode(), msg, false, null);
    }

    public static <T> ResponseData<T> error(String code, String msg){
        return createResponseData(code, msg, false, null);
    }

    public static <T> ResponseData<T> error(ResponseStatusEnum status){
        return  createResponseData(status.getCode(), status.getMsg(), false, null);
    }
    public static <T> ResponseData<T> error(BusinessStatusEnum status){
        return  createResponseData(status.getCode(), status.getMsg(), false, null);
    }

    public static <T> ResponseData<T> error(ResponseStatusEnum status, String msg){
        return  createResponseData(status.getCode(), msg, false, null);
    }

    public static <T> ResponseData<T> createResponseData(String code, String msg, Boolean success, T data){
        ResponseData<T> rd = new ResponseData<>();
        rd.code = code;
        rd.msg = msg;
        rd.success = success;
        rd.data = data;
        return rd;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

}

