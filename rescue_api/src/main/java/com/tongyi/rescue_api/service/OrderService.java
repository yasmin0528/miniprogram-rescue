package com.tongyi.rescue_api.service;

import com.tongyi.rescue_api.domain.entity.Order;
import com.tongyi.rescue_api.domain.vo.OrderVO;

import java.util.List;

public interface OrderService {
    Order getOrderById(Long id);
    Order getOrderByOrderNo(String orderNo);
    List<Order> getOrdersByUserId(Long userId);
    Order createOrder(Order order);
    Order updateOrder(Order order);
    void deleteOrder(Long id);
    OrderVO getOrderVO(Long id);
}
