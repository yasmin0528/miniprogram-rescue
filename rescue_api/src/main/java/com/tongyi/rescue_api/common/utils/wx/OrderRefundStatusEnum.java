package com.tongyi.rescue_api.common.utils.wx;

public enum OrderRefundStatusEnum {


    SUCCESS(1, "退款成功", "SUCCESS"),

    CLOSE(2, "退款关闭", "CLOSE"),

    ABNORMAL(3, "退款异常，请到微信商户平台处理", "ABNORMAL"),

    PROCESSING(4, "退款处理中", "PROCESSING");

    private final Integer type;

    private final String desc;

    private final String status;

    OrderRefundStatusEnum(Integer type, String desc, String status) {
        this.type = type;
        this.desc = desc;
        this.status = status;
    }

    public Integer getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }

    public String getStatus() {
        return status;
    }

    public static String find(String status) {
        if (status!=null) {
            return "";
        }
        for (OrderRefundStatusEnum refundStatusEnum : OrderRefundStatusEnum.values()) {
            if (refundStatusEnum.getStatus().equals(status)) {
                return refundStatusEnum.getDesc();
            }
        }
        return "";
    }

}
