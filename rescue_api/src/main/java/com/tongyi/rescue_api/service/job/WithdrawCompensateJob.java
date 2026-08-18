package com.tongyi.rescue_api.service.job;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tongyi.rescue_api.common.WxPayApi;
import com.tongyi.rescue_api.common.utils.wx.HttpResponse;
import com.tongyi.rescue_api.common.utils.wx.WxPayUtils;
import com.tongyi.rescue_api.config.PayAppIdConfig;
import com.tongyi.rescue_api.domain.entity.WithdrawRecord;
import com.tongyi.rescue_api.domain.vo.PaySettingQuery;
import com.tongyi.rescue_api.repository.WithdrawRecordRepository;
import com.tongyi.rescue_api.service.PaySettingService;
import com.tongyi.rescue_api.service.WalletAccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMethod;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class WithdrawCompensateJob {

    private static final String WX_DOMAIN = "https://api.mch.weixin.qq.com";

    private final WithdrawRecordRepository withdrawRecordRepository;
    private final WalletAccountService walletAccountService;
    private final WxPayApi wxPayApi;
    private final PayAppIdConfig payAppIdConfig;
    private final PaySettingService paySettingService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Scheduled(fixedDelay = 60000)
    public void scanApplyingWithdrawRecords() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(2);
        List<WithdrawRecord> records = withdrawRecordRepository
                .findTop100ByStatusAndDeletedAndApplyTimeBeforeOrderByApplyTimeAsc("APPLYING", 0, threshold);

        if (records.isEmpty()) {
            return;
        }

        log.info("[withdraw-compensate] start, size={}", records.size());
        for (WithdrawRecord record : records) {
            try {
                compensateOne(record);
            } catch (Exception e) {
                log.error("[withdraw-compensate] error, applyNo={}", record.getApplyNo(), e);
            }
        }
    }

    private void compensateOne(WithdrawRecord record) throws Exception {
        if (record == null || !StringUtils.hasText(record.getOutBillNo())) {
            return;
        }

        String appId = StringUtils.hasText(record.getAppId())
                ? record.getAppId()
                : payAppIdConfig.getByClientType("shifu");
        PaySettingQuery setting = paySettingService.queryPaySetting(appId, "MINI_PROGRAM");

        String api = "/v3/fund-app/mch-transfer/transfer-bills/out-bill-no/" + record.getOutBillNo();
        HttpResponse response = wxPayApi.v3(appId, RequestMethod.GET, WX_DOMAIN, api, "");
        if (response == null || response.getStatus() != 200) {
            log.warn("[withdraw-compensate] query failed, applyNo={}, status={}",
                    record.getApplyNo(), response == null ? null : response.getStatus());
            return;
        }

        boolean verified = WxPayUtils.verifySignatureFromURL(response, setting.getPlatformCertPath());
        if (!verified) {
            log.warn("[withdraw-compensate] verify failed, applyNo={}", record.getApplyNo());
            return;
        }

        JsonNode body = objectMapper.readTree(response.getBody());
        String state = body.path("state").asText(null);
        String transferBillNo = body.path("transfer_bill_no").asText(null);
        String failReason = body.path("fail_reason").asText(null);

        String mapped = mapTransferState(state);
        if ("APPLYING".equals(mapped)) {
            return;
        }

        record.setTransferBillNo(StringUtils.hasText(transferBillNo) ? transferBillNo : record.getTransferBillNo());
        record.setStatus(mapped);

        if ("SUCCESS".equals(mapped)) {
            walletAccountService.confirmWithdrawSuccess(record.getMasterId(), record.getApplyNo(), record.getTransferAmount());
            record.setSuccessTime(LocalDateTime.now());
            record.setFailReason(null);
        } else {
            walletAccountService.rollbackWithdraw(record.getMasterId(), record.getApplyNo(), record.getTransferAmount(), failReason);
            record.setFailReason(StringUtils.hasText(failReason) ? failReason : state);
        }

        record.setUpdateTime(LocalDateTime.now());
        withdrawRecordRepository.save(record);

        log.info("[withdraw-compensate] updated, applyNo={}, state={}, mapped={}",
                record.getApplyNo(), state, mapped);
    }

    private String mapTransferState(String state) {
        if (!StringUtils.hasText(state)) {
            return "APPLYING";
        }
        return switch (state) {
            case "SUCCESS" -> "SUCCESS";
            case "CLOSED", "CANCELLED" -> "CLOSED";
            case "ACCEPTED", "PROCESSING", "WAIT_USER_CONFIRM" -> "APPLYING";
            default -> "ABNORMAL";
        };
    }
}
