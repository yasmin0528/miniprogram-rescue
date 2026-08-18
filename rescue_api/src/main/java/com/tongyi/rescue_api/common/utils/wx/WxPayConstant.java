package com.tongyi.rescue_api.common.utils.wx;

public class WxPayConstant {

    /**
     * 域名
     */
    public interface Domain {
        /**
         * 中国国内
         */
        String CHINA = "https://api.mch.weixin.qq.com";

        /**
         * 中国国内(备用域名)
         */
        String CHINA2 = "https://api2.mch.weixin.qq.com";

        /**
         * 东南亚
         */
        String HK = "https://apihk.mch.weixin.qq.com";

        /**
         * 其它
         */
        String US = "https://apius.mch.weixin.qq.com";

        /**
         * 获取公钥
         */
        String FRAUD = "https://fraud.mch.weixin.qq.com";

        /**
         * 活动
         */
        String ACTION = "https://action.weixin.qq.com";

        /**
         * 刷脸支付
         * PAY_APP
         */
        String PAY_APP = "https://payapp.weixin.qq.com";

    }

    /**
     * 属性
     */
    public interface Attribute {

        /**
         * 认证类型，目前为固定值 WECHATPAY2-SHA256-RSA2048
         */
        String AUTH_TYPE = "WECHATPAY2-SHA256-RSA2048";

        /**
         * 微信支付，二维码图片存放的目录名称
         */
        String DIRECTORY = "WxPayQrCodeTempFiles";

        /**
         * 符合ISO 4217标准的三位字母代码，目前只支持人民币：CNY。
         */
        String CURRENCY = "CNY";
    }

    /**
     * url 接口地址
     */
    public interface Api {

        /**
         * 支付回调地址
         */
        String CALL_BACK_NOTIFY = "/wx/pay/callBackNotify";

        /**
         * 退款回调地址
         */
        String CALL_BACK_REFUND_NOTIFY = "/wx/pay/callBackRefundNotify";

        /**
         * 获取商户平台证书
         */
        String V3_CERTIFICATES = "/v3/certificates";

        /**
         * 微信支付订单号查询
         * <p>
         * 第一个%s：订单号
         * 第二个%s: 商户号
         */
        String V3_PAY_ID = "/v3/pay/transactions/id/%s?mchid=%s";

        /**
         * JSAPI下单
         */
        String V3_PAY_JSAPI = "/v3/pay/transactions/jsapi";

        /**
         * native下单
         */
        String V3_PAY_NATIVE = "/v3/pay/partner/transactions/native";

        /**
         * h5 下单
         */
        String V3_PAY_H5 = "/v3/pay/transactions/h5";
        /**
         * app 下单
         */
        String V3_PAY_APP = "/v3/pay/transactions/app";
        /**
         * 退款接口
         */
        String V3_REFUND = "/v3/refund/domestic/refunds";

        /**
         * 退款订单查询
         */
        String V3_REFUND_ID = "/v3/refund/domestic/refunds/%s";
        /**
         * 创建支付分订单
         */
        String V3_CREATE_A_PAYMENT_SUBORDER = "/v3/payscore/serviceorder";
        /**
         * 取消支付分订单
         */
        String V3_Cancel_the_payment_sub_order = "/v3/payscore/serviceorder/%s/cancel";
        /**
         * 先付后用开始支付
         */
        String V3_PAYSCORE_SERVICEORDER = "/v3/payscore/serviceorder/%s/complete";
        /**
         * 先用后付退款
         */
        String V3_REFUND_DOMESTIC_REFUNDS = "/v3/refund/domestic/refunds";
        /**
         * 查询支付分订单
         */
        String V3_CHECK_PAYMENT_SUB_ORDER = "/v3/payscore/serviceorder";
        /**
         * 转账
         */
        String V3_TRANSFER_BILLS = "/v3/fund-app/mch-transfer/transfer-bills";
        /**
         * 添加分账接收方
         */
        String V3_PROFITSHARING_ADD = "/v3/profitsharing/receivers/add";
        /**
         * 删除分账接收方
         */
        String V3_PROFITSHARING_DELETE = "/v3/profitsharing/receivers/delete";
        /**
         * 请求分账
         */
        String V3_PROFITSHARING = "/v3/profitsharing/orders";
        /**
         * 商户订单号查询订单
         */
        String V3_OUT_TRADENO = "/v3/pay/transactions/out-trade-no/";


    }

    /**
     * 状态
     */
    public interface State {
        /**
         * 商户已创建服务订单
         */
        String CREATED="CREATED";
        /**
         * 服务订单进行中
         */
        String DOING="DOING";
        /**
         * 服务订单完成(终态)
         */
        String DONE="DONE";
        /**
         * 商户取消服务订单(终态)
         */
        String REVOKED="REVOKED";
        /**
         * 服务订单已失效，"商户已创建服务订单"状态超过30天未变动，则订单失效(终态)
         */
        String EXPIRED="EXPIRED";
    }

    /**
     * 状态
     */
    public interface Name {
        /**
         * 代表押金
         */
        String DEPOSIT="DEPOSIT";
        /**
         * 代表预付款
         */
        String ADVANCE="ADVANCE";
        /**
         * 代表保证金
         */
        String CASH_DEPOSIT="CASH_DEPOSIT";
        /**
         * 先享模式：只能传“ESTIMATE_ORDER_COST”，代表订单预估费用。
         */
        String ESTIMATE_ORDER_COST="ESTIMATE_ORDER_COST";
    }
}


