package com.tongyi.rescue_api.service.impl;

import com.tongyi.rescue_api.domain.entity.Master;
import com.tongyi.rescue_api.domain.entity.MasterAccount;
import com.tongyi.rescue_api.repository.MasterAccountRepository;
import com.tongyi.rescue_api.repository.MasterRepository;
import com.tongyi.rescue_api.service.MasterService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import com.tongyi.rescue_api.common.IdWorker;

@Service
public class MasterServiceImpl implements MasterService {

    private static final int STATUS_ACTIVE = 1;
    private static final int NOT_DELETED = 0;

    private final MasterRepository masterRepository;
    private final MasterAccountRepository masterAccountRepository;
    private final IdWorker idWorker = new IdWorker();

    public MasterServiceImpl(MasterRepository masterRepository,
                             MasterAccountRepository masterAccountRepository) {
        this.masterRepository = masterRepository;
        this.masterAccountRepository = masterAccountRepository;
    }

    @Override
    public Master getMasterById(String id) {
        if (!StringUtils.hasText(id)) {
            return null;
        }
        Master master = masterRepository.findById(id).orElse(null);
        if (master == null || master.getIsDeleted() == null || master.getIsDeleted() != NOT_DELETED) {
            return null;
        }
        return master;
    }

    @Override
    public Master getMasterByOpenId(String openId) {
        if (!StringUtils.hasText(openId)) {
            return null;
        }
        return masterRepository.findByOpenIdAndIsDeleted(openId, NOT_DELETED).orElse(null);
    }

    @Override
    public Master getMasterByPhoneNumber(String phoneNumber) {
        if (!StringUtils.hasText(phoneNumber)) {
            return null;
        }
        return masterRepository.findByPhoneNumberAndIsDeleted(phoneNumber, NOT_DELETED).orElse(null);
    }

    @Override
    public Master createMaster(Master master) {
        if (master == null) {
            return null;
        }
        if (!StringUtils.hasText(master.getId())) {
            master.setId(idWorker.nextIds());
        }
        LocalDateTime now = LocalDateTime.now();
        if (master.getCreateTime() == null) {
            master.setCreateTime(now);
        }
        master.setUpdateTime(now);
        if (master.getIsDeleted() == null) {
            master.setIsDeleted(NOT_DELETED);
        }
        if (master.getStatus() == null) {
            master.setStatus(STATUS_ACTIVE);
        }
        return masterRepository.save(master);
    }

    @Override
    public Master updateMaster(Master master) {
        if (master == null || !StringUtils.hasText(master.getId())) {
            return null;
        }
        master.setUpdateTime(LocalDateTime.now());
        return masterRepository.save(master);
    }

    @Override
    public Master wechatLogin(String openId, String unionId, String phoneNumber, String nickName) {
        if (!StringUtils.hasText(openId)) {
            throw new IllegalArgumentException("openId不能为空");
        }

        LocalDateTime now = LocalDateTime.now();
        Master master = getMasterByOpenId(openId);

        if (master == null) {
            master = new Master();
            master.setId(idWorker.nextIds());
            master.setOpenId(openId);
            master.setUnionId(unionId);
            master.setPhoneNumber(phoneNumber);
            master.setNickName(nickName);
            master.setStatus(STATUS_ACTIVE);
            master.setIsDeleted(NOT_DELETED);
            master.setCreateTime(now);
            master.setUpdateTime(now);
            master = masterRepository.save(master);
            ensureWalletAccount(master.getId());
            return master;
        }

        boolean changed = false;
        if (StringUtils.hasText(unionId) && !unionId.equals(master.getUnionId())) {
            master.setUnionId(unionId);
            changed = true;
        }
        if (StringUtils.hasText(phoneNumber) && !phoneNumber.equals(master.getPhoneNumber())) {
            master.setPhoneNumber(phoneNumber);
            changed = true;
        }
        if (StringUtils.hasText(nickName) && !nickName.equals(master.getNickName())) {
            master.setNickName(nickName);
            changed = true;
        }
        if (master.getStatus() == null || master.getStatus() != STATUS_ACTIVE) {
            master.setStatus(STATUS_ACTIVE);
            changed = true;
        }
        if (master.getIsDeleted() == null || master.getIsDeleted() != NOT_DELETED) {
            master.setIsDeleted(NOT_DELETED);
            changed = true;
        }

        if (changed) {
            master.setUpdateTime(now);
            master = masterRepository.save(master);
        }
        ensureWalletAccount(master.getId());
        return master;
    }

    private void ensureWalletAccount(String masterId) {
        if (!StringUtils.hasText(masterId)) {
            return;
        }
        MasterAccount exists = masterAccountRepository.findByMasterIdAndDeleted(masterId, NOT_DELETED).orElse(null);
        if (exists != null) {
            return;
        }

        MasterAccount account = new MasterAccount();
        account.setId(idWorker.nextIds());
        account.setMasterId(masterId);
        account.setBalanceAmount(0);
        account.setFrozenAmount(0);
        account.setCreateTime(LocalDateTime.now());
        account.setUpdateTime(LocalDateTime.now());
        account.setDeleted(NOT_DELETED);
        try {
            masterAccountRepository.save(account);
        } catch (Exception ignore) {
            // 并发注册/登录可能出现唯一键冲突，忽略后由后续查询兜底
        }
    }
}
