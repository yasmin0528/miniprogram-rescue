package com.tongyi.rescue_api.service.impl;

import com.tongyi.rescue_api.common.ResponseData;
import com.tongyi.rescue_api.domain.dto.OrderCreateDTO;
import com.tongyi.rescue_api.domain.dto.RefundPayDTO;
import com.tongyi.rescue_api.domain.entity.AgencyPrice;
import com.tongyi.rescue_api.domain.entity.Order;
import com.tongyi.rescue_api.domain.entity.OrderLog;
import com.tongyi.rescue_api.domain.entity.OrderPay;
import com.tongyi.rescue_api.repository.AgencyPriceRepository;
import com.tongyi.rescue_api.repository.MasterRepository;
import com.tongyi.rescue_api.repository.OrderLogRepository;
import com.tongyi.rescue_api.repository.OrderPayRepository;
import com.tongyi.rescue_api.repository.OrderRepository;
import com.tongyi.rescue_api.service.OrderService;
import com.tongyi.rescue_api.service.PayService;
import com.tongyi.rescue_api.service.VoiceNotifyService;
import com.tongyi.rescue_api.service.WalletAccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import com.tongyi.rescue_api.common.utils.wx.BigDecimalUtils;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import com.tongyi.rescue_api.common.IdWorker;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private AgencyPriceRepository agencyPriceRepository;

    @Autowired
    private MasterRepository masterRepository;

    @Autowired
    private OrderLogRepository orderLogRepository;

    @Autowired
    private OrderPayRepository orderPayRepository;

    @Autowired
    private VoiceNotifyService voiceNotifyService;

    @Autowired
    private WalletAccountService walletAccountService;

    @Autowired
    private PayService payService;

    private static final AtomicLong orderSeq = new AtomicLong(0);
    private static final String ORDER_EXPIRE_OPERATOR = "system-expire-job";
    private static final String ORDER_PAY_CALLBACK_OPERATOR = "system-pay-callback";
    private final IdWorker idWorker = new IdWorker();

    @Value("${order.pay-timeout-minutes:30}")
    private long payTimeoutMinutes;

    @Override
    @Transactional
    public Order preCreate(OrderCreateDTO dto) {
        if (!StringUtils.hasText(dto.getServiceType())) {
            throw new RuntimeException("serviceType不能为空");
        }

        boolean isAgencyOrder = StringUtils.hasText(dto.getAgencyId());
        if (dto.getAgencyOrderType() != null) {
            isAgencyOrder = dto.getAgencyOrderType() == 1;
        }

        if (isAgencyOrder) {
            masterRepository.findById(dto.getAgencyId())
                    .orElseThrow(() -> new RuntimeException("服务商不存在"));
        }

        Integer orderType = dto.getOrderType();
        if (orderType == null) {
            orderType = 1; // 默认即刻单
        }
        if (orderType == 2 && dto.getAppointmentTime() == null) {
            throw new RuntimeException("预约单必须传appointmentTime");
        }

        Order order = new Order();
        order.setId(idWorker.nextIds());

        // 生成可读订单号 ROyyyyMMddHHmmssSSS + 随机两位，避免重启后重复
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        String seq = String.format("%02d", (orderSeq.incrementAndGet() % 100));
        order.setOrderNo("RO" + date + seq);

        order.setOrderType(orderType);
        order.setAppointmentTime(dto.getAppointmentTime());

        order.setCustomerId(dto.getCustomerId());
        order.setCustomerName(dto.getCustomerName());
        order.setCustomerPhone(dto.getCustomerPhone());

        order.setServiceType(dto.getServiceType());

        order.setPlateNo(dto.getPlateNo());
        order.setAddress(dto.getAddress());
        order.setLat(dto.getLat());
        order.setLng(dto.getLng());
        order.setRemark(dto.getRemark());

        order.setAgencyId(dto.getAgencyId());
        order.setAgencyOrderType(isAgencyOrder ? 1 : 0);

        BigDecimal rawPrice;
        BigDecimal rawRatio;
        if (isAgencyOrder) {
            // 从服务商报价表匹配当前serviceType，回填价格与抽成
            List<AgencyPrice> prices = agencyPriceRepository.findByAgencyidAndStatusOrderByIdAsc(dto.getAgencyId(), 1);
            AgencyPrice matched = prices.stream()
                    .filter(p -> dto.getServiceType().equals(p.getService()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("该服务商未配置此服务类型报价"));

            rawPrice = matched.getPrice() == null ? BigDecimal.ZERO : matched.getPrice();
            rawRatio = matched.getRatio() == null ? BigDecimal.ZERO : matched.getRatio();
        } else {
            rawPrice = dto.getPrice() == null ? BigDecimal.ZERO : dto.getPrice();
            rawRatio = dto.getRatio() == null ? BigDecimal.ZERO : dto.getRatio();
        }
        order.setPrice(rawPrice);
        order.setRatio(rawRatio);

        int netPriceFen = toFen(calcNetPrice(rawPrice, rawRatio));
        order.setNetPrice(netPriceFen);
        order.setMasterIncomeAmount(netPriceFen);
        order.setProviderIncomeAmount(0);
        order.setIsDispatch(0);
        order.setDispatchId(null);
        order.setDispatchToken(null);
        order.setSettlementStatus("UNSETTLED");

        order.setStatus("CREATED");

        order.setIsDeleted(0);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());

        Order saved = orderRepository.save(order);
        insertOrderLog(saved);

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getOrderList(String customerPhone) {
        List<Order> orders;
        if (StringUtils.hasText(customerPhone)) {
            orders = orderRepository.findByCustomerPhoneOrderByCreateTimeDesc(customerPhone);
        } else {
            orders = orderRepository.findAllByOrderByCreateTimeDesc();
        }
        List<Order> filtered = orders.stream()
                .filter(o -> !Integer.valueOf(1).equals(o.getIsDeleted()))
                .filter(o -> StringUtils.hasText(o.getStatus()))
                .toList();
        return filtered.stream().map(this::toOrderWithNetPrice).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getOrderListByOperator(String operatorId) {
        if (!StringUtils.hasText(operatorId)) {
            throw new RuntimeException("operatorId不能为空");
        }
        List<Order> allOrders = orderRepository.findAllByOrderByCreateTimeDesc();
        List<Order> orders = allOrders.stream()
                .filter(o -> !Integer.valueOf(1).equals(o.getIsDeleted()))
                .filter(o -> operatorId.equals(o.getOrderReceivingId()) || operatorId.equals(o.getAgencyId()) || operatorId.equals(o.getDispatchId()))
                .filter(o -> StringUtils.hasText(o.getStatus()))
                .filter(o -> !"CREATED".equals(o.getStatus()))
                .filter(o -> !"CANCELLED".equals(o.getStatus()))
                .filter(o -> !"REFUND".equals(o.getStatus()))
                .filter(o -> !"PAID".equals(o.getStatus()))
                .toList();
        return orders.stream().map(this::toOrderWithNetPrice).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getOrderDetail(String orderId) {
        Order order = orderRepository.findById(orderId)
                .or(() -> orderRepository.findByOrderNo(orderId))
                .orElseThrow(() -> new RuntimeException("订单不存在"));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("order", order);

        orderPayRepository.findTopByBizOrderNoAndDeletedOrderByCreateTimeDesc(order.getOrderNo(), 0)
                .ifPresent(orderPay -> {
                    String thirdPartyNumber = StringUtils.hasText(orderPay.getTransactionId())
                            ? orderPay.getTransactionId()
                            : orderPay.getOutTradeNo();
                    result.put("thirdPartyNumber", thirdPartyNumber);
                    result.put("outTradeNo", orderPay.getOutTradeNo());
                    result.put("transactionId", orderPay.getTransactionId());
                    result.put("realPayAmount", orderPay.getTotalAmount() == null ? null : BigDecimalUtils.toBig(orderPay.getTotalAmount()));
                    result.put("payStatus", orderPay.getStatus());
                });

        return result;
    }

    @Override
    @Transactional
    public void handlePayCallback(String payOrderId) {
        if (!StringUtils.hasText(payOrderId)) {
            throw new RuntimeException("payOrderId不能为空");
        }

        Order order = orderRepository.findById(payOrderId)
                .or(() -> orderRepository.findByOrderNo(payOrderId))
                .orElseThrow(() -> new RuntimeException("订单不存在"));

        LocalDateTime now = LocalDateTime.now();
        int affectedRows = orderRepository.updateStatusIfCurrent(
                order.getId(),
                "CREATED",
                "PAID",
                now,
                ORDER_PAY_CALLBACK_OPERATOR
        );

        if (affectedRows == 1) {
            Order paidOrder = orderRepository.findById(order.getId())
                    .orElseThrow(() -> new RuntimeException("订单不存在"));
            insertOrderLog(paidOrder, ORDER_PAY_CALLBACK_OPERATOR);
            log.info("[pay-callback] paid success, orderId={}, orderNo={}", paidOrder.getId(), paidOrder.getOrderNo());

            OrderPay orderPay = orderPayRepository.findTopByBizOrderNoAndDeletedOrderByCreateTimeDesc(paidOrder.getOrderNo(), 0).orElse(null);
            if (orderPay == null) {
                orderPay = new OrderPay();
                orderPay.setId(idWorker.nextIds());
                orderPay.setBizOrderNo(paidOrder.getOrderNo());
                orderPay.setOutTradeNo(paidOrder.getOrderNo());
                orderPay.setTotalAmount(paidOrder.getPrice() == null ? 0 : BigDecimalUtils.toPenny(paidOrder.getPrice()));
                orderPay.setStatus("SUCCESS");
                orderPay.setCreateTime(LocalDateTime.now());
                orderPay.setUpdateTime(LocalDateTime.now());
                orderPay.setDeleted(0);
                orderPayRepository.save(orderPay);
            } else if (!"SUCCESS".equals(orderPay.getStatus())) {
                orderPay.setStatus("SUCCESS");
                orderPay.setUpdateTime(LocalDateTime.now());
                orderPayRepository.save(orderPay);
            }

            if (Integer.valueOf(1).equals(paidOrder.getAgencyOrderType())) {
                voiceNotifyService.notifyAgencyOnOrderCreated(paidOrder);
            }
            return;
        }

        Order latestOrder = orderRepository.findById(order.getId())
                .orElseThrow(() -> new RuntimeException("订单不存在"));
        if ("PAID".equals(latestOrder.getStatus())) {
            log.info("[pay-callback] idempotent hit, orderId={}, orderNo={}", latestOrder.getId(), latestOrder.getOrderNo());
            return;
        }
        if ("CANCELLED".equals(latestOrder.getStatus())) {
            log.warn("[pay-callback] skipped because order already cancelled, orderId={}, orderNo={}", latestOrder.getId(), latestOrder.getOrderNo());
            return;
        }

        log.info("[pay-callback] skipped due to concurrent status change, orderId={}, orderNo={}, status={}",
                latestOrder.getId(), latestOrder.getOrderNo(), latestOrder.getStatus());
    }

    @Override
    @Transactional
    public void cancelOrder(String orderId, String operatorId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));

        if ("COMPLETED".equals(order.getStatus())) {
            throw new RuntimeException("已完成订单不可取消");
        }

        String currentStatus = order.getStatus();
        if ("CANCELLED".equals(currentStatus) || "REFUND".equals(currentStatus)) {
            return;
        }

        List<String> cancellableStatuses = List.of("PAID", "ACCEPTED", "DEPARTED");
        if (!cancellableStatuses.contains(currentStatus)) {
            throw new RuntimeException("当前订单状态不允许取消退款");
        }

        RefundPayDTO refundDTO = new RefundPayDTO();
        refundDTO.setBizOrderNo(order.getOrderNo());
        refundDTO.setOutRefundNo("RF" + idWorker.nextIds());
        refundDTO.setReason("订单取消自动退款");
        refundDTO.setClientType("user");

        orderPayRepository.findTopByBizOrderNoAndDeletedOrderByCreateTimeDesc(order.getOrderNo(), 0)
                .ifPresent(orderPay -> refundDTO.setOutTradeNo(orderPay.getOutTradeNo()));

        ResponseData<?> refundResp = payService.refundPay(refundDTO);
        if (refundResp == null || !Boolean.TRUE.equals(refundResp.getSuccess())) {
            throw new RuntimeException("自动退款失败:" + (refundResp == null ? "未知错误" : refundResp.getMsg()));
        }

        int updated = orderRepository.updateStatusIfCurrentIn(orderId, cancellableStatuses, "REFUND", LocalDateTime.now(), operatorId);
        if (updated == 0) {
            throw new RuntimeException("订单状态已变更，取消失败，请稍后刷新后重试");
        }

        Order saved = new Order();
        saved.setId(orderId);
        saved.setStatus("REFUND");
        insertOrderLog(saved, operatorId);
    }

    private void insertOrderLog(Order order) {
        insertOrderLog(order, null);
    }

    private void insertOrderLog(Order order, String operatorId) {
        OrderLog log = new OrderLog();
        log.setId(idWorker.nextIds());
        log.setOrderId(order.getId());
        log.setStatus(order.getStatus());
        log.setOperateTime(LocalDateTime.now());
        log.setIsDeleted(0);
        log.setCreateTime(LocalDateTime.now());
        log.setUpdateTime(LocalDateTime.now());
        log.setCreateBy(operatorId);
        log.setUpdateBy(operatorId);
        orderLogRepository.save(log);
    }

    @Override
    @Transactional
    public void closeExpiredOrders() {
        LocalDateTime expireBefore = LocalDateTime.now().minusMinutes(payTimeoutMinutes);
        log.info("[order-expire] scan start, payTimeoutMinutes={}, expireBefore={}", payTimeoutMinutes, expireBefore);

        List<Order> candidates = orderRepository.findByStatusAndIsDeletedAndCreateTimeLessThanEqual("CREATED", 0, expireBefore);
        if (candidates.isEmpty()) {
            log.info("[order-expire] scan finish, candidateCount=0, closedCount=0, skippedCount=0");
            return;
        }

        int closedCount = 0;
        int skippedCount = 0;
        for (Order candidate : candidates) {
            try {
                LocalDateTime now = LocalDateTime.now();
                int affectedRows = orderRepository.updateStatusIfCurrent(
                        candidate.getId(),
                        "CREATED",
                        "CANCELLED",
                        now,
                        ORDER_EXPIRE_OPERATOR
                );

                if (affectedRows == 1) {
                    Order cancelledOrder = orderRepository.findById(candidate.getId())
                            .orElseThrow(() -> new RuntimeException("订单不存在"));
                    insertOrderLog(cancelledOrder, ORDER_EXPIRE_OPERATOR);
                    closedCount++;
                    log.info("[order-expire] closed success, orderId={}, orderNo={}", cancelledOrder.getId(), cancelledOrder.getOrderNo());
                } else {
                    skippedCount++;
                    log.info("[order-expire] skipped by concurrent update, orderId={}, orderNo={}", candidate.getId(), candidate.getOrderNo());
                }
            } catch (Exception e) {
                log.error("[order-expire] close one failed, orderId={}, orderNo={}", candidate.getId(), candidate.getOrderNo(), e);
            }
        }

        log.info("[order-expire] scan finish, candidateCount={}, closedCount={}, skippedCount={}",
                candidates.size(), closedCount, skippedCount);
    }

    @Override
    @Transactional
    public Order acceptOrder(String orderId, String operatorId) {
        if (!StringUtils.hasText(orderId)) {
            throw new RuntimeException("orderId不能为空");
        }
        if (!StringUtils.hasText(operatorId)) {
            throw new RuntimeException("operatorId不能为空");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));

        if (!"PAID".equals(order.getStatus())) {
            throw new RuntimeException("当前订单不可接单");
        }

        order.setStatus("ACCEPTED");
        order.setOrderReceivingId(operatorId);
        order.setDispatchId(operatorId);
        order.setUpdateTime(LocalDateTime.now());
        order.setUpdateBy(operatorId);
        Order saved = orderRepository.save(order);
        insertOrderLog(saved, operatorId);
        return saved;
    }

    @Override
    @Transactional
    public Order acceptDispatch(String token, String operatorId) {
        if (!StringUtils.hasText(token)) {
            throw new RuntimeException("token不能为空");
        }
        if (!StringUtils.hasText(operatorId)) {
            throw new RuntimeException("operatorId不能为空");
        }

        Order order = orderRepository.findByDispatchToken(token)
                .orElseThrow(() -> new RuntimeException("派单不存在或已失效"));

        if (operatorId.equals(order.getAgencyId())) {
            throw new RuntimeException("派单人不能接自己派出的单");
        }

        if (operatorId.equals(order.getDispatchId())) {
                return order;
            }
        if (StringUtils.hasText(order.getDispatchId()) && !operatorId.equals(order.getDispatchId())) {
            throw new RuntimeException("派单已被其他师傅接单");
        }
        if (!Integer.valueOf(1).equals(order.getIsDispatch())) {
            throw new RuntimeException("当前派单不可接单");
        }

        order.setStatus("ACCEPTED");
        order.setDispatchId(operatorId);
        order.setUpdateTime(LocalDateTime.now());
        order.setUpdateBy(operatorId);
        Order saved = orderRepository.save(order);
        insertOrderLog(saved, operatorId);
        return saved;
    }

    @Override
    @Transactional
    public Order updateOrderStatus(String orderId, String status, String operatorId) {
        if ("COMPLETED".equals(status)) {
            return completeOrder(orderId, operatorId);
        }
        if (!StringUtils.hasText(orderId)) {
            throw new RuntimeException("orderId不能为空");
        }
        if (!StringUtils.hasText(status)) {
            throw new RuntimeException("status不能为空");
        }
        if (!StringUtils.hasText(operatorId)) {
            throw new RuntimeException("operatorId不能为空");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));

        String current = order.getStatus();
        if (!isValidNextStatus(current, status)) {
            throw new RuntimeException("非法的状态流转");
        }

        order.setStatus(status);
        order.setUpdateTime(LocalDateTime.now());
        order.setUpdateBy(operatorId);
        Order saved = orderRepository.save(order);
        insertOrderLog(saved, operatorId);

        return saved;
    }

    @Override
    @Transactional
    public Map<String, Object> createDispatch(String orderId, String operatorId, Integer commissionAmountFen) {
        if (!StringUtils.hasText(orderId) || !StringUtils.hasText(operatorId)) {
            throw new RuntimeException("orderId/operatorId不能为空");
        }
        if (commissionAmountFen == null || commissionAmountFen < 0) {
            throw new RuntimeException("commissionAmount不能为空且不能为负数");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));

        if (!operatorId.equals(order.getAgencyId())) {
            throw new RuntimeException("仅服务商可派单");
        }

        int netPriceFen = getOrInitNetPriceFen(order);
        if (commissionAmountFen > netPriceFen) {
            throw new RuntimeException("commissionAmount必须在[0,netPrice]范围内");
        }

        int masterIncomeFen = netPriceFen - commissionAmountFen;
        order.setMasterIncomeAmount(masterIncomeFen);
        order.setProviderIncomeAmount(commissionAmountFen);
        order.setIsDispatch(1);
        order.setDispatchId(null);
        order.setDispatchToken(UUID.randomUUID().toString().replace("-", ""));
        order.setUpdateTime(LocalDateTime.now());
        order.setUpdateBy(operatorId);
        Order saved = orderRepository.save(order);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("orderId", saved.getId());
        resp.put("token", saved.getDispatchToken());
        resp.put("sharePath", "/pages/order-detail/order-detail?dispatchToken=" + saved.getDispatchToken() + "&from=dispatch");
        return resp;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> dispatchPreview(String token) {
        if (!StringUtils.hasText(token)) {
            throw new RuntimeException("token不能为空");
        }
        Order order = orderRepository.findByDispatchToken(token)
                .orElseThrow(() -> new RuntimeException("派单不存在或已失效"));

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("orderId", order.getId());
        resp.put("orderNo", order.getOrderNo());
        resp.put("serviceType", order.getServiceType());
        resp.put("address", order.getAddress());
        resp.put("customerPhone", order.getCustomerPhone());
        resp.put("masterIncomeAmount", order.getMasterIncomeAmount() == null ? 0 : order.getMasterIncomeAmount());
        resp.put("isDispatch", order.getIsDispatch() == null ? 0 : order.getIsDispatch());
        return resp;
    }

    @Override
    @Transactional
    public Order revokeDispatch(String orderId, String operatorId) {
        if (!StringUtils.hasText(orderId) || !StringUtils.hasText(operatorId)) {
            throw new RuntimeException("orderId/operatorId不能为空");
        }
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));

        if (!operatorId.equals(order.getAgencyId())) {
            throw new RuntimeException("仅服务商可撤回派单");
        }

        if (!Integer.valueOf(1).equals(order.getIsDispatch())) {
            throw new RuntimeException("当前状态不可撤回派单");
        }

        order.setIsDispatch(0);
        order.setDispatchToken(null);
        order.setDispatchId(null);
        order.setMasterIncomeAmount(null);
        order.setProviderIncomeAmount(null);
        order.setUpdateTime(LocalDateTime.now());
        order.setUpdateBy(operatorId);
        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public Order completeOrder(String orderId, String operatorId) {
        if (!StringUtils.hasText(orderId) || !StringUtils.hasText(operatorId)) {
            throw new RuntimeException("orderId/operatorId不能为空");
        }
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));

        if ("COMPLETED".equals(order.getStatus()) && "SETTLED".equals(order.getSettlementStatus())) {
            return order;
        }
        if (!"ARRIVED".equals(order.getStatus()) && !"COMPLETED".equals(order.getStatus())) {
            throw new RuntimeException("当前状态不可完成");
        }

        settleMasterWallet(order, operatorId);

        order.setStatus("COMPLETED");
        order.setSettlementStatus("SETTLED");
        order.setUpdateTime(LocalDateTime.now());
        order.setUpdateBy(operatorId);
        Order saved = orderRepository.save(order);
        insertOrderLog(saved, operatorId);
        return saved;
    }

    private boolean isValidNextStatus(String current, String next) {
        if (!StringUtils.hasText(current)) {
            return false;
        }
        if (current.equals(next)) {
            return false;
        }
        return switch (current) {
            case "ACCEPTED" -> "DEPARTED".equals(next);
            case "DEPARTED" -> "ARRIVED".equals(next);
            case "ARRIVED" -> "COMPLETED".equals(next);
            default -> false;
        };
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getOffers(String agencyid) {
        if (!StringUtils.hasText(agencyid)) {
            throw new RuntimeException("agencyid不能为空");
        }

        masterRepository.findById(agencyid)
                .orElseThrow(() -> new RuntimeException("服务商不存在"));

        List<AgencyPrice> prices = agencyPriceRepository.findByAgencyidAndStatusOrderByIdAsc(agencyid, 1);
        return prices.stream().map(item -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", String.valueOf(item.getId()));
            row.put("agencyid", item.getAgencyid());
            row.put("companyname", item.getCompanyname());
            row.put("service", item.getService());
            row.put("price", item.getPrice());
            row.put("ratio", item.getRatio());
            row.put("status", item.getStatus());
            return row;
        }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getShifuHallOrders(String userId, Double lat, Double lng, Double radiusKm, String visibleStatuses, String excludeStatus) {
        if (!StringUtils.hasText(userId)) {
            throw new RuntimeException("userId不能为空");
        }
        if (lat == null || lng == null) {
            throw new RuntimeException("定位不能为空");
        }

        double radius = radiusKm == null ? 10d : radiusKm;

        List<Order> allOrders = orderRepository.findAllByOrderByCreateTimeDesc();
        if (allOrders.isEmpty()) {
            return List.of();
        }

        Set<String> visibleSet = parseStatusSet(visibleStatuses);
        Set<String> excludeSet = parseStatusSet(excludeStatus);

        List<String> orderIds = allOrders.stream().map(Order::getId).toList();
        List<OrderLog> logs = orderLogRepository.findByOrderIdInOrderByOperateTimeDesc(orderIds);

        Map<String, OrderLog> latestLogMap = new HashMap<>();
        for (OrderLog log : logs) {
            if (!latestLogMap.containsKey(log.getOrderId())) {
                latestLogMap.put(log.getOrderId(), log);
            }
        }

        List<Order> filtered = allOrders.stream()
                .filter(order -> !Integer.valueOf(1).equals(order.getIsDeleted()))
                // 仅保留当前用户相关订单：服务商本人订单或被派单给当前用户的订单
                // 若 agencyId 为空，则按距离筛选（附近大厅单）
                .filter(order -> {
                    boolean relatedToUser = userId.equals(order.getAgencyId()) || userId.equals(order.getDispatchId());
                    if (relatedToUser) {
                        return true;
                    }
                    if (!StringUtils.hasText(order.getAgencyId())) {
                        if (order.getLat() == null || order.getLng() == null) {
                            return false;
                        }
                        return calcDistanceKm(lat, lng, order.getLat(), order.getLng()) <= radius;
                    }
                    return false;
                })
                // 固定过滤掉待支付创建态和取消态
                .filter(order -> {
                    OrderLog latest = latestLogMap.get(order.getId());
                    String status = latest != null ? latest.getStatus() : order.getStatus();
                    if (!StringUtils.hasText(status)) {
                        return false;
                    }
                    return !"CREATED".equals(status) && !"CANCELLED".equals(status);
                })
                // 兼容保留可见状态/排除状态参数
                .filter(order -> {
                    OrderLog latest = latestLogMap.get(order.getId());
                    String status = latest != null ? latest.getStatus() : order.getStatus();
                    if (StringUtils.hasText(status)) {
                        if (excludeSet.contains(status)) {
                            return false;
                        }
                        if (!visibleSet.isEmpty()) {
                            return visibleSet.contains(status);
                        }
                    }
                    return visibleSet.isEmpty();
                })
                .collect(Collectors.toList());

        log.info("[hallOrders] userId={}, lat={}, lng={}, radiusKm={}, visibleStatuses={}, excludeStatus={}, total={}, filtered={}",
                userId, lat, lng, radius, visibleStatuses, excludeStatus, allOrders.size(), filtered.size());
        if (!filtered.isEmpty()) {
            String summary = filtered.stream()
                    .map(order -> String.format("id=%s,status=%s,agencyId=%s,lat=%s,lng=%s", order.getId(), order.getStatus(), order.getAgencyId(), order.getLat(), order.getLng()))
                    .collect(Collectors.joining(" | "));
            log.info("[hallOrders] filtered orders: {}", summary);
        }

        return filtered.stream().map(order -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("order", order);
            row.put("netPrice", calcNetPrice(order.getPrice(), order.getRatio()));
            return row;
        }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getShifuOrderDetail(String orderId) {
        if (!StringUtils.hasText(orderId)) {
            throw new RuntimeException("orderId不能为空");
        }

        Order order = orderRepository.findById(orderId)
                .or(() -> orderRepository.findByOrderNo(orderId))
                .orElseThrow(() -> new RuntimeException("订单不存在"));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("order", order);
        result.put("netPrice", calcNetPrice(order.getPrice(), order.getRatio()));

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderLog> getOrderLogs(String orderId) {
        if (!StringUtils.hasText(orderId)) {
            throw new RuntimeException("orderId不能为空");
        }
        return orderLogRepository.findByOrderIdOrderByOperateTimeAsc(orderId);
    }

    private Set<String> parseStatusSet(String raw) {
        if (!StringUtils.hasText(raw)) {
            return Set.of();
        }
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
    }

    private Map<String, Object> toOrderWithNetPrice(Order order) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("order", order);
        Integer netFen = getOrInitNetPriceFen(order);
        row.put("netPrice", BigDecimalUtils.toBig(netFen));
        return row;
    }

    private int getOrInitNetPriceFen(Order order) {
        if (order.getNetPrice() != null && order.getNetPrice() >= 0) {
            return order.getNetPrice();
        }
        int netPriceFen = toFen(calcNetPrice(order.getPrice(), order.getRatio()));
        order.setNetPrice(netPriceFen);
        return netPriceFen;
    }

    private int toFen(BigDecimal amountYuan) {
        if (amountYuan == null) {
            return 0;
        }
        return amountYuan.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).intValue();
    }

    private BigDecimal calcNetPrice(BigDecimal price, BigDecimal ratio) {
        if (price == null) {
            price = BigDecimal.ZERO;
        }
        if (ratio == null) {
            ratio = BigDecimal.ZERO;
        }
        BigDecimal hundred = BigDecimal.valueOf(100);
        BigDecimal rate = BigDecimal.ONE.subtract(ratio.divide(hundred, 6, RoundingMode.HALF_UP));
        return price.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }

    private void settleMasterWallet(Order order, String operatorId) {
        if (order == null) {
            return;
        }
        String settleMasterId = StringUtils.hasText(order.getDispatchId()) ? order.getDispatchId() : order.getOrderReceivingId();
        if (!StringUtils.hasText(settleMasterId)) {
            log.warn("[wallet-settle] skip, order has no receiver, orderNo={}", order.getOrderNo());
            return;
        }
        if (!StringUtils.hasText(order.getAgencyId())) {
            log.warn("[wallet-settle] skip, order has no agencyId, orderNo={}", order.getOrderNo());
            return;
        }

        int netPriceFen = getOrInitNetPriceFen(order);
        int providerFen = order.getProviderIncomeAmount() == null ? 0 : order.getProviderIncomeAmount();
        if (providerFen < 0 || providerFen > netPriceFen) {
            throw new RuntimeException("订单抽成金额非法");
        }
        int masterFen = order.getMasterIncomeAmount() == null ? (netPriceFen - providerFen) : order.getMasterIncomeAmount();
        if (masterFen != netPriceFen - providerFen) {
            masterFen = netPriceFen - providerFen;
        }

        order.setMasterIncomeAmount(masterFen);
        order.setProviderIncomeAmount(providerFen);

        if (masterFen > 0) {
            walletAccountService.creditOrderIncome(
                    settleMasterId,
                    order.getOrderNo(),
                    masterFen,
                    "订单" + order.getOrderNo() + "完单入账(师傅)"
            );
        }
        if (providerFen > 0) {
            walletAccountService.creditOrderIncome(
                    order.getAgencyId(),
                    order.getOrderNo() + "-PROVIDER",
                    providerFen,
                    "订单" + order.getOrderNo() + "完单入账(服务商)"
            );
        }

        log.info("[wallet-settle] credited, orderNo={}, settleMasterId={}, orderReceivingId={}, dispatchId={}, providerId={}, masterFen={}, providerFen={}, operatorId={}",
                order.getOrderNo(), settleMasterId, order.getOrderReceivingId(), order.getDispatchId(), order.getAgencyId(), masterFen, providerFen, operatorId);
    }

    private double calcDistanceKm(double lat1, double lng1, double lat2, double lng2) {
        final double earthRadius = 6371d;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c;
    }
}
