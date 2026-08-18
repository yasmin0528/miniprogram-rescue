package com.tongyi.rescue_api.service.impl;

import com.tongyi.rescue_api.common.IdWorker;
import com.tongyi.rescue_api.domain.entity.MasterAccount;
import com.tongyi.rescue_api.domain.entity.MasterAccountFlow;
import com.tongyi.rescue_api.repository.MasterAccountFlowRepository;
import com.tongyi.rescue_api.repository.MasterAccountRepository;
import com.tongyi.rescue_api.service.WalletAccountService;
import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletAccountServiceImpl implements WalletAccountService {

    private static final String SCENE_ORDER_INCOME = "ORDER_INCOME";
    private static final String SCENE_WITHDRAW_FREEZE = "WITHDRAW_FREEZE";
    private static final String SCENE_WITHDRAW_SUCCESS = "WITHDRAW_SUCCESS";
    private static final String SCENE_WITHDRAW_ROLLBACK = "WITHDRAW_ROLLBACK";

    private static final String CHANGE_IN = "IN";
    private static final String CHANGE_FREEZE = "FREEZE";
    private static final String CHANGE_UNFREEZE = "UNFREEZE";
    private static final String CHANGE_OUT = "OUT";

    private final MasterAccountRepository masterAccountRepository;
    private final MasterAccountFlowRepository masterAccountFlowRepository;
    private final IdWorker idWorker;

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void ensureAccount(String masterId) {
        if (!StringUtils.hasText(masterId)) {
            throw new IllegalArgumentException("masterId不能为空");
        }
        if (masterAccountRepository.findByMasterIdAndDeleted(masterId, 0).isPresent()) {
            return;
        }
        MasterAccount account = new MasterAccount();
        account.setId(idWorker.nextIds());
        account.setMasterId(masterId);
        account.setBalanceAmount(0);
        account.setFrozenAmount(0);
        account.setCreateTime(LocalDateTime.now());
        account.setUpdateTime(LocalDateTime.now());
        account.setDeleted(0);
        try {
            masterAccountRepository.save(account);
        } catch (Exception e) {
            log.debug("wallet account create maybe duplicated, masterId={}", masterId);
        }
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void creditOrderIncome(String masterId, String orderNo, Integer amountFen, String remark) {
        if (!StringUtils.hasText(masterId)) {
            throw new IllegalArgumentException("masterId不能为空");
        }
        if (!StringUtils.hasText(orderNo)) {
            throw new IllegalArgumentException("orderNo不能为空");
        }
        if (amountFen == null || amountFen <= 0) {
            throw new IllegalArgumentException("amountFen必须大于0");
        }

        ensureAccount(masterId);
        if (existsFlow(masterId, orderNo, SCENE_ORDER_INCOME)) {
            return;
        }

        MasterAccount account = getAccount(masterId);
        int balance = safe(account.getBalanceAmount());
        int frozen = safe(account.getFrozenAmount());
        int newBalance = balance + amountFen;

        account.setBalanceAmount(newBalance);
        account.setUpdateTime(LocalDateTime.now());
        saveAccountWithVersion(account, "完单入账失败，请稍后重试");

        String finalRemark = StringUtils.hasText(remark) ? remark : "订单完单入账";
        saveFlow(masterId, orderNo, SCENE_ORDER_INCOME, CHANGE_IN, amountFen,
                balance, newBalance, frozen, frozen, finalRemark);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void freezeForWithdraw(String masterId, String applyNo, Integer amountFen) {
        checkArgs(masterId, applyNo, amountFen);
        if (existsFlow(masterId, applyNo, SCENE_WITHDRAW_FREEZE)) {
            return;
        }

        MasterAccount account = getAccount(masterId);
        int balance = safe(account.getBalanceAmount());
        int frozen = safe(account.getFrozenAmount());
        int available = balance - frozen;
        if (available < amountFen) {
            throw new IllegalStateException("可提现余额不足");
        }

        int newFrozen = frozen + amountFen;
        account.setFrozenAmount(newFrozen);
        account.setUpdateTime(LocalDateTime.now());
        saveAccountWithVersion(account, "冻结余额失败，请稍后重试");

        saveFlow(masterId, applyNo, SCENE_WITHDRAW_FREEZE, CHANGE_FREEZE, amountFen,
                balance, balance, frozen, newFrozen, "提现申请冻结");
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void confirmWithdrawSuccess(String masterId, String applyNo, Integer amountFen) {
        checkArgs(masterId, applyNo, amountFen);
        if (existsFlow(masterId, applyNo, SCENE_WITHDRAW_SUCCESS)) {
            return;
        }

        MasterAccount account = getAccount(masterId);
        int balance = safe(account.getBalanceAmount());
        int frozen = safe(account.getFrozenAmount());
        if (frozen < amountFen || balance < amountFen) {
            throw new IllegalStateException("账户余额或冻结金额异常");
        }

        int newBalance = balance - amountFen;
        int newFrozen = frozen - amountFen;
        account.setBalanceAmount(newBalance);
        account.setFrozenAmount(newFrozen);
        account.setUpdateTime(LocalDateTime.now());
        saveAccountWithVersion(account, "提现成功记账失败，请稍后重试");

        saveFlow(masterId, applyNo, SCENE_WITHDRAW_SUCCESS, CHANGE_OUT, amountFen,
                balance, newBalance, frozen, newFrozen, "提现成功扣减");
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void rollbackWithdraw(String masterId, String applyNo, Integer amountFen, String reason) {
        checkArgs(masterId, applyNo, amountFen);
        if (existsFlow(masterId, applyNo, SCENE_WITHDRAW_ROLLBACK)) {
            return;
        }

        MasterAccount account = getAccount(masterId);
        int balance = safe(account.getBalanceAmount());
        int frozen = safe(account.getFrozenAmount());
        int unfreezeAmount = Math.min(frozen, amountFen);
        int newFrozen = frozen - unfreezeAmount;

        account.setFrozenAmount(newFrozen);
        account.setUpdateTime(LocalDateTime.now());
        saveAccountWithVersion(account, "提现回滚失败，请稍后重试");

        String rollbackReason = StringUtils.hasText(reason) ? reason : "微信提现失败";
        saveFlow(masterId, applyNo, SCENE_WITHDRAW_ROLLBACK, CHANGE_UNFREEZE, unfreezeAmount,
                balance, balance, frozen, newFrozen, "提现失败回滚:" + rollbackReason);
    }

    private void checkArgs(String masterId, String applyNo, Integer amountFen) {
        if (!StringUtils.hasText(masterId)) {
            throw new IllegalArgumentException("masterId不能为空");
        }
        if (!StringUtils.hasText(applyNo)) {
            throw new IllegalArgumentException("applyNo不能为空");
        }
        if (amountFen == null || amountFen <= 0) {
            throw new IllegalArgumentException("amountFen必须大于0");
        }
    }

    private MasterAccount getAccount(String masterId) {
        return masterAccountRepository.findByMasterIdAndDeleted(masterId, 0)
                .orElseThrow(() -> new IllegalStateException("钱包账户不存在"));
    }

    private boolean existsFlow(String masterId, String applyNo, String scene) {
        return masterAccountFlowRepository.existsByMasterIdAndBizNoAndSceneAndDeleted(masterId, applyNo, scene, 0);
    }

    private void saveAccountWithVersion(MasterAccount account, String errorMsg) {
        try {
            masterAccountRepository.saveAndFlush(account);
        } catch (OptimisticLockException e) {
            log.warn("wallet optimistic lock conflict, masterId={}", account.getMasterId(), e);
            throw new IllegalStateException(errorMsg);
        }
    }

    private void saveFlow(String masterId,
                          String applyNo,
                          String scene,
                          String changeType,
                          Integer amountFen,
                          Integer balanceBefore,
                          Integer balanceAfter,
                          Integer frozenBefore,
                          Integer frozenAfter,
                          String remark) {
        MasterAccountFlow flow = new MasterAccountFlow();
        flow.setId(idWorker.nextIds());
        flow.setMasterId(masterId);
        flow.setBizNo(applyNo);
        flow.setScene(scene);
        flow.setChangeType(changeType);
        flow.setAmount(amountFen);
        flow.setBalanceBefore(balanceBefore);
        flow.setBalanceAfter(balanceAfter);
        flow.setFrozenBefore(frozenBefore);
        flow.setFrozenAfter(frozenAfter);
        flow.setRemark(remark);
        flow.setCreateTime(LocalDateTime.now());
        flow.setUpdateTime(LocalDateTime.now());
        flow.setDeleted(0);
        masterAccountFlowRepository.save(flow);
    }

    private int safe(Integer n) {
        return n == null ? 0 : n;
    }
}
