package com.tongyi.rescue_api.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tongyi.rescue_api.common.IdWorker;
import com.tongyi.rescue_api.common.ResponseData;
import com.tongyi.rescue_api.common.WxPayApi;
import com.tongyi.rescue_api.common.utils.DateTimeZoneUtil;
import com.tongyi.rescue_api.common.utils.wx.AesUtils;
import com.tongyi.rescue_api.common.utils.wx.BigDecimalUtils;
import com.tongyi.rescue_api.common.utils.wx.HttpResponse;
import com.tongyi.rescue_api.common.utils.wx.WxPayUtils;
import com.tongyi.rescue_api.config.PayAppIdConfig;
import com.tongyi.rescue_api.domain.dto.JsApiPayDTO;
import com.tongyi.rescue_api.domain.dto.RefundPayDTO;
import com.tongyi.rescue_api.domain.dto.TransferAccountsApiPayDTO;
import com.tongyi.rescue_api.domain.dto.WithdrawCreateDTO;
import com.tongyi.rescue_api.domain.entity.Master;
import com.tongyi.rescue_api.domain.entity.MasterAccount;
import com.tongyi.rescue_api.domain.entity.Order;
import com.tongyi.rescue_api.domain.entity.OrderPay;
import com.tongyi.rescue_api.domain.entity.RefundRecord;
import com.tongyi.rescue_api.domain.entity.WithdrawRecord;
import com.tongyi.rescue_api.domain.vo.PaySettingQuery;
import com.tongyi.rescue_api.repository.MasterAccountRepository;
import com.tongyi.rescue_api.repository.MasterRepository;
import com.tongyi.rescue_api.repository.OrderPayRepository;
import com.tongyi.rescue_api.repository.OrderRepository;
import com.tongyi.rescue_api.repository.RefundRecordRepository;
import com.tongyi.rescue_api.repository.WithdrawRecordRepository;
import com.tongyi.rescue_api.event.PaymentSucceededEvent;
import com.tongyi.rescue_api.service.PayService;
import com.tongyi.rescue_api.service.PaySettingService;
import com.tongyi.rescue_api.service.WalletAccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMethod;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@Slf4j
public class PayServiceImpl implements PayService {

    private static final String WX_DOMAIN = "https://api.mch.weixin.qq.com";
    private static final String API_JSAPI = "/v3/pay/transactions/jsapi";
    private static final String API_REFUND = "/v3/refund/domestic/refunds";
    private static final String API_TRANSFER = "/v3/fund-app/mch-transfer/transfer-bills";

    private final WxPayApi wxPayApi;
    private final IdWorker idWorker;
    private final PayAppIdConfig payAppIdConfig;
    private final PaySettingService paySettingService;
    private final OrderPayRepository orderPayRepository;
    private final OrderRepository orderRepository;
    private final RefundRecordRepository refundRecordRepository;
    private final MasterRepository masterRepository;
    private final MasterAccountRepository masterAccountRepository;
    private final WithdrawRecordRepository withdrawRecordRepository;
    private final WalletAccountService walletAccountService;
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Integer timeRemaining = 30;
    public PayServiceImpl(WxPayApi wxPayApi,
                          IdWorker idWorker,
                          PaySettingService paySettingService,
                          PayAppIdConfig payAppIdConfig,
                          OrderPayRepository orderPayRepository,
                          OrderRepository orderRepository,
                          RefundRecordRepository refundRecordRepository,
                          MasterRepository masterRepository,
                          MasterAccountRepository masterAccountRepository,
                          WithdrawRecordRepository withdrawRecordRepository,
                          WalletAccountService walletAccountService,
                          ApplicationEventPublisher eventPublisher) {
        this.wxPayApi = wxPayApi;
        this.idWorker = idWorker;
        this.paySettingService = paySettingService;
        this.payAppIdConfig = payAppIdConfig;
        this.orderPayRepository = orderPayRepository;
        this.orderRepository = orderRepository;
        this.refundRecordRepository = refundRecordRepository;
        this.masterRepository = masterRepository;
        this.masterAccountRepository = masterAccountRepository;
        this.withdrawRecordRepository = withdrawRecordRepository;
        this.walletAccountService = walletAccountService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public ResponseData<?> jsApiPay(JsApiPayDTO dto) {
        try {
            if (dto == null) {
                return ResponseData.error("请求参数不能为空");
            }
            if (!StringUtils.hasText(dto.getOpenId())) {
                return ResponseData.error("openId不能为空");
            }
            if (dto.getPayAmount() == null || dto.getPayAmount().doubleValue() <= 0) {
                return ResponseData.error("payAmount必须大于0");
            }

            if (!StringUtils.hasText(dto.getNumber())) {
                dto.setNumber(idWorker.nextIds());
            }

            String appId = payAppIdConfig.getByClientType(dto.getClientType());
            PaySettingQuery setting = paySettingService.queryPaySetting(appId, "MINI_PROGRAM");

            Map<String, Object> req = new LinkedHashMap<>();
            req.put("appid", setting.getAppId());
            req.put("mchid", setting.getMchId());
            req.put("description", StringUtils.hasText(dto.getDescription()) ? dto.getDescription() : "及刻救援");
            req.put("out_trade_no", dto.getNumber());
            req.put("time_expire", DateTimeZoneUtil.dateToTimeZoneMinute(timeRemaining));

            Map<String, Object> amount = new HashMap<>();
            amount.put("total", BigDecimalUtils.toPenny(dto.getPayAmount()));
            amount.put("currency", "CNY");
            req.put("amount", amount);

            req.put("notify_url", setting.getNotifyUrl());

            Map<String, Object> payer = new HashMap<>();
            payer.put("openid", dto.getOpenId());
            req.put("payer", payer);

            String requestBody = objectMapper.writeValueAsString(req);
            HttpResponse response = wxPayApi.v3(appId, RequestMethod.POST, WX_DOMAIN, API_JSAPI, requestBody);

            if (response == null || response.getStatus() != 200) {
                return ResponseData.error("微信下单失败");
            }

            boolean verified = WxPayUtils.verifySignatureFromURL(response, setting.getPlatformCertPath());
            if (verified) {
                return ResponseData.error("微信应答验签失败");
            }

            JsonNode body = objectMapper.readTree(response.getBody());
            String prepayId = body.path("prepay_id").asText(null);
            if (!StringUtils.hasText(prepayId)) {
                return ResponseData.error("微信返回prepay_id为空");
            }

            Map<String, String> payParams = WxPayUtils.jsApiCreateSign(setting.getAppId(), prepayId, setting.getKeyPemPath());
            payParams.put("id", dto.getNumber());

            OrderPay orderPay = orderPayRepository.findByOutTradeNoAndDeleted(dto.getNumber(), 0).orElse(null);
            if (orderPay == null) {
                orderPay = new OrderPay();
                orderPay.setId(idWorker.nextIds());
                orderPay.setBizOrderNo(dto.getNumber());
                orderPay.setOutTradeNo(dto.getNumber());
                orderPay.setOpenId(dto.getOpenId());
                orderPay.setAppId(setting.getAppId());
                orderPay.setMchId(setting.getMchId());
                orderPay.setDescription(StringUtils.hasText(dto.getDescription()) ? dto.getDescription() : "及刻救援");
                orderPay.setTotalAmount(BigDecimalUtils.toPenny(dto.getPayAmount()));
                orderPay.setStatus("CREATED");
                orderPay.setTimeExpire(LocalDateTime.now().plusMinutes(timeRemaining));
                orderPay.setCreateTime(LocalDateTime.now());
                orderPay.setUpdateTime(LocalDateTime.now());
                orderPay.setDeleted(0);
            } else {
                orderPay.setBizOrderNo(dto.getNumber());
                orderPay.setOpenId(dto.getOpenId());
                orderPay.setAppId(setting.getAppId());
                orderPay.setMchId(setting.getMchId());
                orderPay.setDescription(StringUtils.hasText(dto.getDescription()) ? dto.getDescription() : "及刻救援");
                orderPay.setTotalAmount(BigDecimalUtils.toPenny(dto.getPayAmount()));
                orderPay.setStatus("CREATED");
                orderPay.setTimeExpire(LocalDateTime.now().plusMinutes(timeRemaining));
                orderPay.setUpdateTime(LocalDateTime.now());
            }
            orderPayRepository.save(orderPay);

            return ResponseData.ok(payParams);
        } catch (Exception e) {
            log.error("jsApiPay error", e);
            return ResponseData.error("支付下单异常：" + e.getMessage());
        }
    }

    @Override
    public ResponseData<?> refundPay(RefundPayDTO dto) {
        try {
            if (dto == null) {
                return ResponseData.error("请求参数不能为空");
            }
            if (!StringUtils.hasText(dto.getOutTradeNo()) && !StringUtils.hasText(dto.getBizOrderNo())) {
                return ResponseData.error("outTradeNo或bizOrderNo不能为空");
            }

            OrderPay orderPay = null;
            if (StringUtils.hasText(dto.getOutTradeNo())) {
                orderPay = orderPayRepository.findByOutTradeNoAndDeleted(dto.getOutTradeNo(), 0).orElse(null);
            }
            if (orderPay == null && StringUtils.hasText(dto.getBizOrderNo())) {
                orderPay = orderPayRepository.findTopByBizOrderNoAndDeletedOrderByCreateTimeDesc(dto.getBizOrderNo(), 0).orElse(null);
            }
            if (orderPay == null) {
                return ResponseData.error("支付记录不存在");
            }

            if (!StringUtils.hasText(dto.getOutTradeNo())) {
                dto.setOutTradeNo(orderPay.getOutTradeNo());
            }

            Order order = null;
            if (StringUtils.hasText(orderPay.getBizOrderNo())) {
                order = orderRepository.findByOrderNo(orderPay.getBizOrderNo()).orElse(null);
            }
            if (order == null && StringUtils.hasText(dto.getBizOrderNo())) {
                order = orderRepository.findByOrderNo(dto.getBizOrderNo()).orElse(null);
            }
            if (order == null) {
                return ResponseData.error("业务订单不存在");
            }

            String status = order.getStatus();
            if (!("PAID".equals(status) || "ACCEPTED".equals(status) || "DEPARTED".equals(status))) {
                return ResponseData.error("当前订单状态不允许退款");
            }

            BigDecimal paidYuan = BigDecimalUtils.toBig(orderPay.getTotalAmount());
            BigDecimal expectedRefundYuan = "DEPARTED".equals(status)
                    ? paidYuan.multiply(new BigDecimal("0.80")).setScale(2, RoundingMode.HALF_UP)
                    : paidYuan;

            Integer totalFen = orderPay.getTotalAmount();
            Integer refundFen = BigDecimalUtils.toPenny(expectedRefundYuan);

            if (dto.getRefundAmount() != null) {
                Integer inputRefundFen = BigDecimalUtils.toPenny(dto.getRefundAmount());
                if (!refundFen.equals(inputRefundFen)) {
                    return ResponseData.error("退款金额与订单规则不一致");
                }
            }

            String appId = orderPay.getAppId();
            if (!StringUtils.hasText(appId)) {
                appId = payAppIdConfig.getByClientType(dto.getClientType());
            }
            PaySettingQuery setting = paySettingService.queryPaySetting(appId, "MINI_PROGRAM");

            RefundRecord record = new RefundRecord();
            record.setId(idWorker.nextIds());
            record.setRefundNo(dto.getOutRefundNo());
            record.setBizOrderNo(order.getOrderNo());
            record.setOutTradeNo(orderPay.getOutTradeNo());
            record.setRefundAmount(refundFen);
            record.setTotalAmount(totalFen);
            record.setReason(dto.getReason());
            record.setOpenId(orderPay.getOpenId());
            record.setAppId(appId);
            record.setStatus("APPLYING");
            record.setApplyTime(LocalDateTime.now());
            record.setCreateTime(LocalDateTime.now());
            record.setUpdateTime(LocalDateTime.now());
            record.setDeleted(0);
            record.setOrderStatusBeforeRefund(order.getStatus());
            refundRecordRepository.save(record);

            Map<String, Object> req = new LinkedHashMap<>();
            req.put("out_trade_no", orderPay.getOutTradeNo());
            req.put("out_refund_no", dto.getOutRefundNo());
            req.put("reason", dto.getReason());
            req.put("notify_url", setting.getRefundNotifyUrl());

            Map<String, Object> amount = new HashMap<>();
            amount.put("refund", refundFen);
            amount.put("total", totalFen);
            amount.put("currency", "CNY");
            req.put("amount", amount);

            String requestBody = objectMapper.writeValueAsString(req);
            HttpResponse response = wxPayApi.v3(appId, RequestMethod.POST, WX_DOMAIN, API_REFUND, requestBody);

            if (response == null || !(response.getStatus() == 200 || response.getStatus() == 201)) {
                log.warn("[refundPay] invalid response status={}, body={}",
                        response == null ? null : response.getStatus(),
                        response == null ? null : response.getBody());
                record.setStatus("ABNORMAL");
                record.setFailReason("微信退款请求失败");
                record.setUpdateTime(LocalDateTime.now());
                refundRecordRepository.save(record);
                return ResponseData.error("退款请求失败");
            }

            boolean verified = WxPayUtils.verifySignatureFromURL(response, setting.getPlatformCertPath());
            if (verified) {
                record.setStatus("ABNORMAL");
                record.setFailReason("退款应答验签失败");
                record.setUpdateTime(LocalDateTime.now());
                refundRecordRepository.save(record);
                return ResponseData.error("退款应答验签失败");
            }

            JsonNode body = objectMapper.readTree(response.getBody());
            String refundId = body.path("refund_id").asText(null);
            if (StringUtils.hasText(refundId)) {
                record.setRefundId(refundId);
                record.setUpdateTime(LocalDateTime.now());
                refundRecordRepository.save(record);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("status", body.path("status").asText());
            result.put("refundId", refundId);
            result.put("refundAmount", expectedRefundYuan);
            result.put("rule", "DEPARTED状态扣20%，其余允许状态全额");
            return ResponseData.ok(result);
        } catch (Exception e) {
            log.error("refundPay error", e);
            return ResponseData.error("退款异常：" + e.getMessage());
        }
    }

    @Override
    public ResponseData<?> createWithdraw(WithdrawCreateDTO dto) {
        try {
            if (dto == null) {
                return ResponseData.error("请求参数不能为空");
            }
            if (!StringUtils.hasText(dto.getMasterId())) {
                return ResponseData.error("masterId不能为空");
            }

            Integer amountFen = resolveAmountFen(dto);
            if (amountFen == null || amountFen <= 0) {
                return ResponseData.error("amount必须大于0");
            }
            if (amountFen < 1000) {
                return ResponseData.error("最低提现金额为10元");
            }
            if (amountFen > 200000) {
                return ResponseData.error("单笔提现金额不能超过2000元");
            }

            if (StringUtils.hasText(dto.getRequestId())) {
                WithdrawRecord exist = withdrawRecordRepository.findByRequestIdAndDeleted(dto.getRequestId(), 0).orElse(null);
                if (exist != null) {
                    return ResponseData.ok(buildWithdrawCreateView(exist));
                }
            }

            walletAccountService.ensureAccount(dto.getMasterId());
            java.util.Optional<MasterAccount> accountOpt =
                    masterAccountRepository.findByMasterIdAndDeleted(dto.getMasterId(), 0);
            if (!accountOpt.isPresent()) {
                return ResponseData.error("钱包账户不存在");
            }

            int balance = accountOpt.get().getBalanceAmount() == null ? 0 : accountOpt.get().getBalanceAmount();
            int frozen = accountOpt.get().getFrozenAmount() == null ? 0 : accountOpt.get().getFrozenAmount();
            int available = balance - frozen;
            if (available < amountFen) {
                return ResponseData.error("可提现余额不足");
            }

            WithdrawRecord record = new WithdrawRecord();
            record.setId(idWorker.nextIds());
            record.setApplyNo(idWorker.nextIds());
            record.setRequestId(dto.getRequestId());
            record.setBizOrderNo(dto.getBizOrderNo());
            record.setMasterId(dto.getMasterId());
            record.setTransferAmount(amountFen);
            record.setStatus("INIT");
            record.setCreateTime(LocalDateTime.now());
            record.setUpdateTime(LocalDateTime.now());
            record.setDeleted(0);
            try {
                withdrawRecordRepository.save(record);
            } catch (org.springframework.dao.DataIntegrityViolationException dupEx) {
                if (StringUtils.hasText(dto.getRequestId())) {
                    WithdrawRecord exist = withdrawRecordRepository.findByRequestIdAndDeleted(dto.getRequestId(), 0).orElse(null);
                    if (exist != null) {
                        return ResponseData.ok(buildWithdrawCreateView(exist));
                    }
                }
                throw dupEx;
            }

            return ResponseData.ok(buildWithdrawCreateView(record));
        } catch (Exception e) {
            log.error("createWithdraw error", e);
            return ResponseData.error("创建提现单异常：" + e.getMessage());
        }
    }

    @Override
    public ResponseData<?> transferApply(TransferAccountsApiPayDTO dto) {
        try {
            if (dto == null || !StringUtils.hasText(dto.getApplyNo())) {
                return ResponseData.error("applyNo不能为空");
            }

            WithdrawRecord record = withdrawRecordRepository.findByApplyNoAndDeleted(dto.getApplyNo(), 0).orElse(null);
            if (record == null) {
                return ResponseData.error("提现申请单不存在");
            }
            if (!"INIT".equals(record.getStatus()) && !"ABNORMAL".equals(record.getStatus())) {
                return ResponseData.error("当前提现单状态不允许再次发起");
            }

            Master master = masterRepository.findById(record.getMasterId()).orElse(null);
            if (master == null || !StringUtils.hasText(master.getOpenId())) {
                return ResponseData.error("技师信息不完整，无法发起提现");
            }
            if (record.getTransferAmount() == null || record.getTransferAmount() <= 0) {
                return ResponseData.error("提现金额不合法");
            }

            String appId = payAppIdConfig.getByClientType("shifu");
            PaySettingQuery setting = paySettingService.queryPaySetting(appId, "MINI_PROGRAM");
            String outBillNo = StringUtils.hasText(record.getOutBillNo()) ? record.getOutBillNo() : idWorker.nextIds();
            String transferNotifyUrl = StringUtils.hasText(setting.getTransferNotifyUrl())
                    ? setting.getTransferNotifyUrl()
                    : setting.getRefundNotifyUrl();

            Map<String, Object> req = new LinkedHashMap<>();
            req.put("appid", setting.getAppId());
            req.put("out_bill_no", outBillNo);
            req.put("transfer_scene_id", "1000");
            req.put("openid", master.getOpenId());
            req.put("transfer_amount", record.getTransferAmount());
            req.put("transfer_remark", "提现申请:" + record.getApplyNo());
            req.put("notify_url", transferNotifyUrl);

            walletAccountService.freezeForWithdraw(record.getMasterId(), record.getApplyNo(), record.getTransferAmount());

            record.setOutBillNo(outBillNo);
            record.setOpenId(master.getOpenId());
            record.setAppId(appId);
            record.setStatus("APPLYING");
            record.setApplyTime(LocalDateTime.now());
            record.setFailReason(null);
            record.setUpdateTime(LocalDateTime.now());
            withdrawRecordRepository.save(record);

            String requestBody = objectMapper.writeValueAsString(req);
            HttpResponse response = wxPayApi.v3(appId, RequestMethod.POST, WX_DOMAIN, API_TRANSFER, requestBody);
            log.info("[transferApply] wx transfer request applyNo={}, outBillNo={}, url={}, requestBody={}, responseStatus={}, responseBody={}",
                    record.getApplyNo(), outBillNo, API_TRANSFER, requestBody,
                    response == null ? null : response.getStatus(), response == null ? null : response.getBody());

            if (response == null || (response.getStatus() != 200 && response.getStatus() != 202)) {
                walletAccountService.rollbackWithdraw(record.getMasterId(), record.getApplyNo(), record.getTransferAmount(), "微信提现请求失败");
                record.setStatus("ABNORMAL");
                record.setFailReason("微信提现请求失败");
                record.setUpdateTime(LocalDateTime.now());
                withdrawRecordRepository.save(record);
                return ResponseData.error("提现请求失败");
            }

            boolean verified = WxPayUtils.verifySignatureFromURL(response, setting.getPlatformCertPath());
            log.info("[transferApply] wx response signature verified={}, applyNo={}, outBillNo={}", verified, record.getApplyNo(), outBillNo);
            if (verified) {
                walletAccountService.rollbackWithdraw(record.getMasterId(), record.getApplyNo(), record.getTransferAmount(), "提现应答验签失败");
                record.setStatus("ABNORMAL");
                record.setFailReason("提现应答验签失败");
                record.setUpdateTime(LocalDateTime.now());
                withdrawRecordRepository.save(record);
                return ResponseData.error("提现应答验签失败");
            }

            JsonNode body = objectMapper.readTree(response.getBody());
            String state = body.path("state").asText("ACCEPTED");
            String transferBillNo = body.path("transfer_bill_no").asText(null);
            log.info("[transferApply] wx response body parsed applyNo={}, outBillNo={}, state={}, transferBillNo={}",
                    record.getApplyNo(), outBillNo, state, transferBillNo);

            record.setTransferBillNo(transferBillNo);
            record.setStatus(mapTransferState(state));
            log.info("[transferApply] withdraw record mapped status={}, applyNo={}, outBillNo={}, sourceState={}",
                    record.getStatus(), record.getApplyNo(), outBillNo, state);
            if ("SUCCESS".equals(record.getStatus())) {
                walletAccountService.confirmWithdrawSuccess(record.getMasterId(), record.getApplyNo(), record.getTransferAmount());
                record.setSuccessTime(LocalDateTime.now());
                record.setFailReason(null);
            } else if ("CLOSED".equals(record.getStatus()) || "ABNORMAL".equals(record.getStatus())) {
                walletAccountService.rollbackWithdraw(record.getMasterId(), record.getApplyNo(), record.getTransferAmount(), state);
            }
            record.setUpdateTime(LocalDateTime.now());
            withdrawRecordRepository.save(record);

            return ResponseData.ok(buildWithdrawView(record));
        } catch (Exception e) {
            log.error("transferApply error", e);
            return ResponseData.error("提现异常：" + e.getMessage());
        }
    }

    @Override
    public ResponseData<?> getOrderStatus(String bizOrderNo) {
        if (!StringUtils.hasText(bizOrderNo)) {
            return ResponseData.error("bizOrderNo不能为空");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("bizOrderNo", bizOrderNo);
        result.put("status", "UNKNOWN");
        return ResponseData.ok(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String handlePayNotify(Map<String, String> headers, String body) {
        try {
            Map<String, String> safeHeaders = headers == null ? Map.of() : headers;
            String serialNo = getHeaderIgnoreCase(safeHeaders, "Wechatpay-Serial");
            String signature = getHeaderIgnoreCase(safeHeaders, "Wechatpay-Signature");
            String nonce = getHeaderIgnoreCase(safeHeaders, "Wechatpay-Nonce");
            String timestamp = getHeaderIgnoreCase(safeHeaders, "Wechatpay-Timestamp");

            if (!StringUtils.hasText(serialNo) || !StringUtils.hasText(signature)
                    || !StringUtils.hasText(nonce) || !StringUtils.hasText(timestamp)
                    || !StringUtils.hasText(body)) {
                log.warn("[pay-notify] invalid notify headers/body, headers={}, body={}", safeHeaders, body);
                return "{\"code\":\"FAIL\",\"message\":\"参数不完整\"}";
            }

            String appId = payAppIdConfig.getByClientType("user");
            PaySettingQuery setting = paySettingService.queryPaySetting(appId, "MINI_PROGRAM");

            String plain = WxPayUtils.verifyNotifyFromURL(
                    serialNo,
                    body,
                    signature,
                    nonce,
                    timestamp,
                    setting.getApiV3Key(),
                    setting.getPlatformCertPath()
            );

            JsonNode notify = objectMapper.readTree(plain);
            String outTradeNo = notify.path("out_trade_no").asText(null);
            String transactionId = notify.path("transaction_id").asText(null);
            String tradeState = notify.path("trade_state").asText(null);

            if (!StringUtils.hasText(outTradeNo)) {
                log.warn("[pay-notify] out_trade_no missing, plain={}", plain);
                return "{\"code\":\"FAIL\",\"message\":\"订单号缺失\"}";
            }

            OrderPay orderPay = orderPayRepository.findByOutTradeNoAndDeleted(outTradeNo, 0).orElse(null);
            if (orderPay == null) {
                log.warn("[pay-notify] order not found, outTradeNo={}", outTradeNo);
                return "{\"code\":\"FAIL\",\"message\":\"订单不存在\"}";
            }

            if ("SUCCESS".equals(orderPay.getStatus())) {
                log.info("[pay-notify] idempotent hit, outTradeNo={}", outTradeNo);
                return "{\"code\":\"SUCCESS\",\"message\":\"成功\"}";
            }

            orderPay.setNotifyRaw(plain);
            orderPay.setNotifyTime(LocalDateTime.now());
            orderPay.setUpdateTime(LocalDateTime.now());

            String mappedPayStatus;
            if ("SUCCESS".equals(tradeState)) {
                orderPay.setStatus("SUCCESS");
                orderPay.setTransactionId(transactionId);
                orderPay.setPayTime(LocalDateTime.now());
                orderPay.setFailReason(null);
                mappedPayStatus = "SUCCESS";
            } else {
                orderPay.setStatus("FAILED");
                orderPay.setFailReason(tradeState);
                mappedPayStatus = "FAILED";
            }
            log.info("[pay-notify] payment status mapped, outTradeNo={}, tradeState={}, mappedStatus={}",
                    outTradeNo, tradeState, mappedPayStatus);

            orderPayRepository.save(orderPay);

            if ("SUCCESS".equals(orderPay.getStatus()) && StringUtils.hasText(orderPay.getBizOrderNo())) {
                eventPublisher.publishEvent(new PaymentSucceededEvent(orderPay.getBizOrderNo(), outTradeNo));
            }

            return "{\"code\":\"SUCCESS\",\"message\":\"成功\"}";
        } catch (Exception e) {
            log.error("[pay-notify] handle error", e);
            return "{\"code\":\"FAIL\",\"message\":\"失败\"}";
        }
    }

    @Override
    public String handleRefundNotify(Map<String, String> headers, String body) {
        try {
            Map<String, String> safeHeaders = headers == null ? Map.of() : headers;
            log.info("[refund-notify] 接收到退款回调, headers={}, body={}", safeHeaders, body);
            String serialNo = getHeaderIgnoreCase(safeHeaders, "Wechatpay-Serial");
            String signature = getHeaderIgnoreCase(safeHeaders, "Wechatpay-Signature");
            String nonce = getHeaderIgnoreCase(safeHeaders, "Wechatpay-Nonce");
            String timestamp = getHeaderIgnoreCase(safeHeaders, "Wechatpay-Timestamp");

            if (!StringUtils.hasText(serialNo) || !StringUtils.hasText(signature)
                    || !StringUtils.hasText(nonce) || !StringUtils.hasText(timestamp)
                    || !StringUtils.hasText(body)) {
                log.warn("[refund-notify] 参数不完整, serialNo={}, signature={}, nonce={}, timestamp={}, body={} ", serialNo, signature, nonce, timestamp, body);
                return "{\"code\":\"FAIL\",\"message\":\"参数不完整\"}";
            }

            String appId = payAppIdConfig.getByClientType("user");
            PaySettingQuery setting = paySettingService.queryPaySetting(appId, "MINI_PROGRAM");

            log.info("[refund-notify] 开始解密回调, serialNo={}, signature={}, nonce={}, timestamp={}", serialNo, signature, nonce, timestamp);
            String plain = null;
            try {
                plain = WxPayUtils.verifyNotifyFromURL(
                        serialNo,
                        body,
                        signature,
                        nonce,
                        timestamp,
                        setting.getApiV3Key(),
                        setting.getPlatformCertPath()
                );
                log.info("[refund-notify] 解密成功, plain={}", plain);
            } catch (Exception ex) {
                log.error("[refund-notify] 解密失败, exception=", ex);
                return "{\"code\":\"FAIL\",\"message\":\"解密失败\"}";
            }

            JsonNode notify = null;
            try {
                notify = objectMapper.readTree(plain);
            } catch (Exception ex) {
                log.error("[refund-notify] JSON解析失败, plain={}, exception=", plain, ex);
                return "{\"code\":\"FAIL\",\"message\":\"JSON解析失败\"}";
            }
            log.info("[refund-notify] 解析回调内容成功, notify={}", notify);
            String result = processRefundNotify(notify, plain);
            log.info("[refund-notify] 业务处理结果: {}", result);
            return result;
        } catch (Exception e) {
            log.error("[refund-notify] handle error", e);
            return "{\"code\":\"FAIL\",\"message\":\"失败\"}";
        }
    }

    @Override
    public String handleRefundNotifyWithoutVerify(String body) {
        try {
            if (!StringUtils.hasText(body)) {
                log.warn("[refund-notify-test] invalid notify body, body={}", body);
                return "{\"code\":\"FAIL\",\"message\":\"参数不完整\"}";
            }
            JsonNode notify = objectMapper.readTree(body);
            String plain = decryptRefundNotifyBody(notify);
            if (!StringUtils.hasText(plain)) {
                return "{\"code\":\"FAIL\",\"message\":\"解析失败\"}";
            }
            JsonNode plainNotify = objectMapper.readTree(plain);
            return processRefundNotify(plainNotify, plain);
        } catch (Exception e) {
            log.error("[refund-notify-test] handle error", e);
            return "{\"code\":\"FAIL\",\"message\":\"失败\"}";
        }
    }

    private String decryptRefundNotifyBody(JsonNode notify) throws Exception {
        JsonNode resource = notify.path("resource");
        if (resource.isMissingNode() || resource.isNull()) {
            throw new RuntimeException("resource字段缺失");
        }

        String cipherText = resource.path("ciphertext").asText(null);
        String nonce = resource.path("nonce").asText(null);
        String associatedData = resource.path("associated_data").asText(null);
        if (!StringUtils.hasText(cipherText) || !StringUtils.hasText(nonce)) {
            throw new RuntimeException("resource参数不完整");
        }

        String appId = payAppIdConfig.getByClientType("user");
        PaySettingQuery setting = paySettingService.queryPaySetting(appId, "MINI_PROGRAM");
        AesUtils aesUtil = new AesUtils(setting.getApiV3Key().getBytes(StandardCharsets.UTF_8));
        return aesUtil.decryptToString(
                associatedData == null ? new byte[0] : associatedData.getBytes(StandardCharsets.UTF_8),
                nonce.getBytes(StandardCharsets.UTF_8),
                cipherText
        );
    }

    private String processRefundNotify(JsonNode notify, String plain) {
        try {
            String outRefundNo = notify.path("out_refund_no").asText(null);
            if (!StringUtils.hasText(outRefundNo)) {
                return "{\"code\":\"FAIL\",\"message\":\"退款单号缺失\"}";
            }

            RefundRecord record = refundRecordRepository.findByRefundNoAndDeleted(outRefundNo, 0).orElse(null);
            if (record == null) {
                log.warn("[refund-notify] refund record not found, outRefundNo={}", outRefundNo);
                return "{\"code\":\"FAIL\",\"message\":\"退款单不存在\"}";
            }

            String refundId = notify.path("refund_id").asText(null);
            String refundStatus = notify.path("refund_status").asText(null);

            if ("SUCCESS".equals(record.getStatus())) {
                log.info("[refund-notify] idempotent hit, outRefundNo={}", outRefundNo);
                return "{\"code\":\"SUCCESS\",\"message\":\"成功\"}";
            }

            record.setNotifyRaw(plain);
            record.setNotifyTime(LocalDateTime.now());
            record.setUpdateTime(LocalDateTime.now());
            record.setRefundId(refundId);

            String mappedRefundStatus;
            if ("SUCCESS".equals(refundStatus)) {
                record.setStatus("SUCCESS");
                record.setSuccessTime(LocalDateTime.now());
                record.setFailReason(null);
                mappedRefundStatus = "SUCCESS";
            } else if ("CLOSED".equals(refundStatus)) {
                record.setStatus("CLOSED");
                record.setFailReason(refundStatus);
                mappedRefundStatus = "CLOSED";
            } else {
                record.setStatus("ABNORMAL");
                record.setFailReason(refundStatus);
                mappedRefundStatus = "ABNORMAL";
            }
            log.info("[refund-notify] refund status mapped, outRefundNo={}, refundStatus={}, mappedStatus={}",
                    outRefundNo, refundStatus, mappedRefundStatus);

            refundRecordRepository.save(record);

            if (record.getBizOrderNo() != null) {
                Order order = orderRepository.findByOrderNo(record.getBizOrderNo()).orElse(null);
                if (order != null && "REFUND".equals(order.getStatus())) {
                    if ("SUCCESS".equals(record.getStatus())) {
                        order.setStatus("CANCELLED");
                        order.setUpdateTime(LocalDateTime.now());
                        orderRepository.save(order);
                    } else if (record.getOrderStatusBeforeRefund() != null) {
                        order.setStatus(record.getOrderStatusBeforeRefund());
                        order.setUpdateTime(LocalDateTime.now());
                        orderRepository.save(order);
                    }
                }
            }

            return "{\"code\":\"SUCCESS\",\"message\":\"成功\"}";
        } catch (Exception e) {
            log.error("[refund-notify] process error", e);
            return "{\"code\":\"FAIL\",\"message\":\"失败\"}";
        }
    }

    @Override
    public String handleTransferNotify(Map<String, String> headers, String body) {
        try {
            Map<String, String> safeHeaders = headers == null ? Map.of() : headers;
            String serialNo = getHeaderIgnoreCase(safeHeaders, "Wechatpay-Serial");
            String signature = getHeaderIgnoreCase(safeHeaders, "Wechatpay-Signature");
            String nonce = getHeaderIgnoreCase(safeHeaders, "Wechatpay-Nonce");
            String timestamp = getHeaderIgnoreCase(safeHeaders, "Wechatpay-Timestamp");

            if (!StringUtils.hasText(serialNo) || !StringUtils.hasText(signature)
                    || !StringUtils.hasText(nonce) || !StringUtils.hasText(timestamp)
                    || !StringUtils.hasText(body)) {
                log.warn("[transfer-notify] invalid notify headers/body, headers={}, body={}", safeHeaders, body);
                return "{\"code\":\"FAIL\",\"message\":\"参数不完整\"}";
            }

            String appId = payAppIdConfig.getByClientType("shifu");
            PaySettingQuery setting = paySettingService.queryPaySetting(appId, "MINI_PROGRAM");

            String plain = WxPayUtils.verifyNotifyFromURL(
                    serialNo,
                    body,
                    signature,
                    nonce,
                    timestamp,
                    setting.getApiV3Key(),
                    setting.getPlatformCertPath()
            );

            JsonNode notify = objectMapper.readTree(plain);
            String outBillNo = notify.path("out_bill_no").asText(null);
            String transferBillNo = notify.path("transfer_bill_no").asText(null);
            String state = notify.path("state").asText(null);
            String failReason = notify.path("fail_reason").asText(null);

            if (!StringUtils.hasText(outBillNo)) {
                log.warn("[transfer-notify] out_bill_no missing, plain={}", plain);
                return "{\"code\":\"FAIL\",\"message\":\"提现单号缺失\"}";
            }

            WithdrawRecord record = withdrawRecordRepository.findByOutBillNoAndDeleted(outBillNo, 0).orElse(null);
            if (record == null) {
                log.warn("[transfer-notify] withdraw record not found, outBillNo={}", outBillNo);
                return "{\"code\":\"FAIL\",\"message\":\"提现单不存在\"}";
            }

            if ("SUCCESS".equals(record.getStatus()) || "CLOSED".equals(record.getStatus())) {
                log.info("[transfer-notify] idempotent hit, outBillNo={}, state={}", outBillNo, record.getStatus());
                return "{\"code\":\"SUCCESS\",\"message\":\"成功\"}";
            }

            record.setTransferBillNo(StringUtils.hasText(transferBillNo) ? transferBillNo : record.getTransferBillNo());
            record.setStatus(mapTransferState(state));
            record.setFailReason("SUCCESS".equals(record.getStatus()) ? null : failReason);
            if ("SUCCESS".equals(record.getStatus())) {
                walletAccountService.confirmWithdrawSuccess(record.getMasterId(), record.getApplyNo(), record.getTransferAmount());
                record.setSuccessTime(LocalDateTime.now());
            } else if ("CLOSED".equals(record.getStatus()) || "ABNORMAL".equals(record.getStatus())) {
                walletAccountService.rollbackWithdraw(record.getMasterId(), record.getApplyNo(), record.getTransferAmount(), failReason);
            }
            record.setNotifyRaw(plain);
            record.setNotifyTime(LocalDateTime.now());
            record.setUpdateTime(LocalDateTime.now());
            withdrawRecordRepository.save(record);

            log.info("[transfer-notify] outBillNo={}, transferBillNo={}, state={}, mappedStatus={}",
                    outBillNo, transferBillNo, state, record.getStatus());

            return "{\"code\":\"SUCCESS\",\"message\":\"成功\"}";
        } catch (Exception e) {
            log.error("[transfer-notify] handle error", e);
            return "{\"code\":\"FAIL\",\"message\":\"失败\"}";
        }
    }

    @Override
    public ResponseData<?> getWithdrawRecord(String applyNo) {
        if (!StringUtils.hasText(applyNo)) {
            return ResponseData.error("applyNo不能为空");
        }
        WithdrawRecord record = withdrawRecordRepository.findByApplyNoAndDeleted(applyNo, 0).orElse(null);
        if (record == null) {
            return ResponseData.error("提现单不存在");
        }
        return ResponseData.ok(buildWithdrawView(record));
    }

    @Override
    public ResponseData<?> listWithdrawRecords(String masterId) {
        try {
            if (!StringUtils.hasText(masterId)) {
                log.warn("[withdraw-reconcile] invalid param, masterId is blank");
                return ResponseData.error("masterId不能为空");
            }

            log.info("[withdraw-reconcile] start, masterId={}", masterId);

            java.util.List<Map<String, Object>> list = withdrawRecordRepository
                    .findByMasterIdAndDeletedOrderByCreateTimeDesc(masterId, 0)
                    .stream().map(this::buildWithdrawView).toList();

            log.info("[withdraw-reconcile] records loaded, masterId={}, listSize={}", masterId, list.size());

            walletAccountService.ensureAccount(masterId);
            MasterAccount account = masterAccountRepository.findByMasterIdAndDeleted(masterId, 0).orElse(null);
            int balanceFen = account == null || account.getBalanceAmount() == null ? 0 : account.getBalanceAmount();
            int frozenFen = account == null || account.getFrozenAmount() == null ? 0 : account.getFrozenAmount();
            int availableFen = Math.max(balanceFen - frozenFen, 0);

            log.info("[withdraw-reconcile] account summary, masterId={}, accountExists={}, balanceFen={}, frozenFen={}, availableFen={}",
                    masterId, account != null, balanceFen, frozenFen, availableFen);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("availableBalanceYuan", BigDecimalUtils.toBig(availableFen));
            data.put("freezeBalanceYuan", BigDecimalUtils.toBig(frozenFen));
            data.put("list", list);
            return ResponseData.ok(data);
        } catch (Exception e) {
            log.error("[withdraw-reconcile] failed, masterId={}", masterId, e);
            return ResponseData.error("钱包对账查询失败");
        }
    }

    private Integer resolveAmountFen(WithdrawCreateDTO dto) {
        if (dto.getAmountFen() != null) {
            return dto.getAmountFen();
        }
        if (dto.getAmountYuan() != null) {
            return BigDecimalUtils.toPenny(dto.getAmountYuan());
        }
        return null;
    }

    private Map<String, Object> buildWithdrawCreateView(WithdrawRecord record) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("applyNo", record.getApplyNo());
        row.put("status", "PENDING");
        row.put("transferAmountYuan", record.getTransferAmount() == null ? null : BigDecimalUtils.toBig(record.getTransferAmount()));
        row.put("feeYuan", BigDecimal.ZERO);
        row.put("netAmountYuan", record.getTransferAmount() == null ? null : BigDecimalUtils.toBig(record.getTransferAmount()));
        return row;
    }

    private Map<String, Object> buildWithdrawView(WithdrawRecord record) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("applyNo", record.getApplyNo());
        row.put("masterId", record.getMasterId());
        row.put("bizOrderNo", record.getBizOrderNo());
        row.put("outBillNo", record.getOutBillNo());
        row.put("transferBillNo", record.getTransferBillNo());
        row.put("status", record.getStatus());
        row.put("transferAmountFen", record.getTransferAmount());
        row.put("transferAmountYuan", record.getTransferAmount() == null ? null : BigDecimalUtils.toBig(record.getTransferAmount()));
        row.put("openId", record.getOpenId());
        row.put("applyTime", record.getApplyTime());
        row.put("successTime", record.getSuccessTime());
        row.put("failReason", record.getFailReason());
        row.put("notifyTime", record.getNotifyTime());
        return row;
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

    private String getHeaderIgnoreCase(Map<String, String> headers, String target) {
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(target)) {
                return entry.getValue();
            }
        }
        return null;
    }
}
