package com.tongyi.rescue_api.controller;

import com.tongyi.rescue_api.common.Result;
import com.tongyi.rescue_api.domain.dto.OrderCreateDTO;
import com.tongyi.rescue_api.domain.entity.Order;
import com.tongyi.rescue_api.domain.entity.OrderLog;
import com.tongyi.rescue_api.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private Environment env;

    @PostMapping("/precreate")
    public Result<Order> preCreate(@RequestBody OrderCreateDTO dto) {
        return Result.success(orderService.preCreate(dto));
    }

    @GetMapping("/list")
    public Result<List<Map<String, Object>>> list(@RequestParam(required = false) String customerPhone,
                                                   @RequestParam(required = false) String operatorId) {
        if (operatorId != null && !operatorId.isBlank()) {
            return Result.success(orderService.getOrderListByOperator(operatorId));
        }
        if (customerPhone != null && !customerPhone.isBlank()) {
            return Result.success(orderService.getOrderList(customerPhone));
        }
        return Result.success(java.util.List.of());
    }

    @GetMapping("/detail")
    public Result<Map<String, Object>> detail(@RequestParam String orderId) {
        return Result.success(orderService.getOrderDetail(orderId));
    }

    @GetMapping("/offers")
    public Result<List<Map<String, Object>>> offers(@RequestParam String agencyid) {
        return Result.success(orderService.getOffers(agencyid));
    }

    @PostMapping("/pay-callback")
    public Result<String> payCallback(@RequestParam String payOrderId) {
        orderService.handlePayCallback(payOrderId);
        return Result.success("ok", null);
    }

    @PostMapping("/test/pay-callback")
    public Result<String> testPayCallback(@RequestParam String payOrderId) {
        String activeProfiles = env.getProperty("spring.profiles.active", "");
        if (!activeProfiles.contains("test")) {
            return Result.error("仅测试环境可用");
        }
        orderService.handlePayCallback(payOrderId);
        return Result.success("ok", null);
    }

    @PostMapping("/cancel")
    public Result<String> cancel(@RequestBody Map<String, Object> payload) {
        if (payload == null || payload.get("orderId") == null || payload.get("orderId").toString().isBlank()) {
            return Result.error("orderId不能为空");
        }
        String orderId = payload.get("orderId").toString();
        String operatorId = payload.get("operatorId") == null ? null : payload.get("operatorId").toString();
        orderService.cancelOrder(orderId, operatorId);
        return Result.success("ok", null);
    }

    @PostMapping("/close-expired")
    public Result<String> closeExpired() {
        orderService.closeExpiredOrders();
        return Result.success("ok", null);
    }

    @PostMapping("/accept")
    public Result<Order> accept(@RequestBody Map<String, String> payload) {
        String orderId = payload.get("orderId");
        String operatorId = payload.get("operatorId");
        return Result.success(orderService.acceptOrder(orderId, operatorId));
    }

    @PostMapping("/dispatch/create")
    public Result<Map<String, Object>> createDispatch(@RequestBody Map<String, Object> payload) {
        String orderId = String.valueOf(payload.get("orderId"));
        String operatorId = String.valueOf(payload.get("operatorId"));
        Integer commissionAmount = payload.get("commissionAmount") == null ? null : Integer.parseInt(String.valueOf(payload.get("commissionAmount")));
        return Result.success(orderService.createDispatch(orderId, operatorId, commissionAmount));
    }

    @GetMapping("/dispatch/preview")
    public Result<Map<String, Object>> previewDispatch(@RequestParam String token) {
        return Result.success(orderService.dispatchPreview(token));
    }

    @PostMapping("/dispatch/accept")
    public Result<Order> acceptDispatch(@RequestBody Map<String, String> payload) {
        return Result.success(orderService.acceptDispatch(payload.get("token"), payload.get("operatorId")));
    }

    @PostMapping("/dispatch/revoke")
    public Result<Order> revokeDispatch(@RequestBody Map<String, String> payload) {
        return Result.success(orderService.revokeDispatch(payload.get("orderId"), payload.get("operatorId")));
    }

    @PostMapping("/complete")
    public Result<Order> complete(@RequestBody Map<String, String> payload) {
        return Result.success(orderService.completeOrder(payload.get("orderId"), payload.get("operatorId")));
    }

    @PostMapping("/status")
    public Result<Order> updateStatus(@RequestBody Map<String, String> payload) {
        String orderId = payload.get("orderId");
        String status = payload.get("status");
        String operatorId = payload.get("operatorId");
        return Result.success(orderService.updateOrderStatus(orderId, status, operatorId));
    }

    @GetMapping("/logs")
    public Result<List<Map<String, Object>>> logs(@RequestParam String orderId) {
        List<OrderLog> logs = orderService.getOrderLogs(orderId);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        List<Map<String, Object>> data = logs.stream().map(log -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("status", log.getStatus());
            row.put("operateTime", log.getOperateTime() == null ? null : log.getOperateTime().format(formatter));
            return row;
        }).toList();
        return Result.success(data);
    }
}
