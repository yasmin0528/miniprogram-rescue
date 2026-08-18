package com.tongyi.rescue_api.controller;

import com.tongyi.rescue_api.common.Result;
import com.tongyi.rescue_api.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/shifu")
public class ShifuOrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/orders/hall")
    public Result<List<java.util.Map<String, Object>>> hallOrders(@RequestParam String userId,
                                                                  @RequestParam Double lat,
                                                                  @RequestParam Double lng,
                                                                  @RequestParam(required = false) Double radiusKm,
                                                                  @RequestParam(required = false) String visibleStatuses,
                                                                  @RequestParam(required = false) String excludeStatus) {
        return Result.success(orderService.getShifuHallOrders(userId, lat, lng, radiusKm, visibleStatuses, excludeStatus));
    }

    @GetMapping("/orders/detail")
    public Result<java.util.Map<String, Object>> detail(@RequestParam String orderId) {
        return Result.success(orderService.getShifuOrderDetail(orderId));
    }
}
