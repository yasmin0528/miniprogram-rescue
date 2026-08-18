package com.tongyi.rescue_api.service;

public interface WalletAccountService {

    /**
     * 确保钱包账户存在
     */
    void ensureAccount(String masterId);

    /**
     * 完单入账（幂等）
     */
    void creditOrderIncome(String masterId, String orderNo, Integer amountFen, String remark);

    /**
     * 提现申请发起时：冻结余额
     */
    void freezeForWithdraw(String masterId, String applyNo, Integer amountFen);

    /**
     * 提现成功时：扣减余额并解冻
     */
    void confirmWithdrawSuccess(String masterId, String applyNo, Integer amountFen);

    /**
     * 提现失败时：解冻回滚
     */
    void rollbackWithdraw(String masterId, String applyNo, Integer amountFen, String reason);
}
