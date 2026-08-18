package com.tongyi.rescue_api.common;

public enum BusinessStatusEnum {
    ROLE_ALREADY_EXISTS("R0400", "角色已存在"),
    MENU_ALREADY_EXISTS("M0400", "菜单以存在"),
    USER_ALREADY_EXISTS("U0400", "用户以存在"),
    CATEGORIZE_TO_EXIST("S0400", "分类以存在"),
    LABEL_TO_EXIST("L0400", "标签以存在"),
    OK("00000", "一切 ok");
    private String code;
    private String msg;

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

    BusinessStatusEnum(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}

