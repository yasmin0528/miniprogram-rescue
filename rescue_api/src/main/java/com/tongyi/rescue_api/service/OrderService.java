package com.tongyi.rescue_api.service;

import com.tongyi.rescue_api.domain.dto.OrderCreateDTO;
import com.tongyi.rescue_api.domain.entity.Order;

public interface OrderService {
    /**
     * 预下单
     */
    Order preCreate(OrderCreateDTO dto);

    /**
     * 查询订单列表（可按手机号过滤）
     */
    java.util.List<java.util.Map<String, Object>> getOrderList(String customerPhone);

    /**
     * 查询与当前用户相关订单（接单人或服务商）
     */
    java.util.List<java.util.Map<String, Object>> getOrderListByOperator(String operatorId);

    /**
     * 查询订单详情（含支付信息）
     */
    java.util.Map<String, Object> getOrderDetail(String orderId);

    /**
     * 支付成功回调
     */
    void handlePayCallback(String payOrderId);

    /**
     * 取消订单
     */
    void cancelOrder(String orderId, String operatorId);

    /**
     * 自动关闭超时未支付订单
     */
    void closeExpiredOrders();

    /**
     * 师傅接单
     */
    Order acceptOrder(String orderId, String operatorId);

    /**
     * 基于派单 token 接单
     */
    Order acceptDispatch(String token, String operatorId);

    /**
     * 更新订单状态
     */
    Order updateOrderStatus(String orderId, String status, String operatorId);

    /**
     * 查询服务商报价列表
     */
    java.util.List<java.util.Map<String, Object>> getOffers(String agencyid);

    /**
     * 师傅接单大厅：过滤状态 + 半径 + 服务商订单
     */
    java.util.List<java.util.Map<String, Object>> getShifuHallOrders(String userId, Double lat, Double lng, Double radiusKm, String visibleStatuses, String excludeStatus);

    /**
     * 师傅端订单详情（含抽成后价格）
     */
    java.util.Map<String, Object> getShifuOrderDetail(String orderId);

    /**
     * 派单创建
     */
    java.util.Map<String, Object> createDispatch(String orderId, String operatorId, Integer commissionAmountFen);

    /**
     * 派单预览（师傅仅看可得金额）
     */
    java.util.Map<String, Object> dispatchPreview(String token);

    /**
     * 撤回派单（支持 DISPATCHED/ACCEPTED）
     */
    Order revokeDispatch(String orderId, String operatorId);

    /**
     * 完成订单并分账入钱包（幂等）
     */
    Order completeOrder(String orderId, String operatorId);

    /**
     * 订单日志时间轴
     */
    java.util.List<com.tongyi.rescue_api.domain.entity.OrderLog> getOrderLogs(String orderId);
}
