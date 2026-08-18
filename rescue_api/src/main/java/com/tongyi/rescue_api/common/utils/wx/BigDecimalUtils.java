package com.tongyi.rescue_api.common.utils.wx;

import java.math.BigDecimal;

public class BigDecimalUtils {

    /**
     * 把 BigDecimal 转成 int 类型
     * <p>
     * 0.01 = 1
     *
     * @param amount BigDecimal类型的金额
     * @return int 类型的金额
     */
    public static int toPenny(BigDecimal amount) {
        return amount.multiply(new BigDecimal(100)).intValue();
    }

    /**
     * 把 BigDecimal 转成 int 类型的比例
     * <p>
     * 15 = 0.15
     *
     * @param amount BigDecimal类型的金额
     * @return int 类型的金额
     */
    public static BigDecimal toBigRate(BigDecimal amount) {
        return amount.divide(new BigDecimal(100));
    }

    /**
     * 把 int 转成 BigDecimal 类型
     * <p>
     * 1 = 0.01
     *
     * @param amount int 类型的金额，单位分
     * @return { BigDecimal}
     */
    public static BigDecimal toBig(Integer amount) {
        return new BigDecimal(amount).divide(new BigDecimal(100));
    }

}
