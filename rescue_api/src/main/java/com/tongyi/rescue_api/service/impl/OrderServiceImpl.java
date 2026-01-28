package com.tongyi.rescue_api.service.impl;

import com.tongyi.rescue_api.domain.entity.Order;
import com.tongyi.rescue_api.domain.vo.OrderVO;
import com.tongyi.rescue_api.repository.OrderRepository;
import com.tongyi.rescue_api.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderRepository orderRepository;

    @Override
    public Order getOrderById(Long id) {
        return orderRepository.findById(id).orElse(null);
    }

    @Override
    public Order getOrderByOrderNo(String orderNo) {
        return orderRepository.findByOrderNo(orderNo);
    }

    @Override
    public List<Order> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    @Override
    public Order createOrder(Order order) {
        return orderRepository.save(order);
    }

    @Override
    public Order updateOrder(Order order) {
        return orderRepository.save(order);
    }

    @Override
    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }

    @Override
    public OrderVO getOrderVO(Long id) {
        Order order = getOrderById(id);
        if (order == null) {
            return null;
        }
        OrderVO orderVO = new OrderVO();
        orderVO.setId(order.getId());
        orderVO.setOrderNo(order.getOrderNo());
        orderVO.setUserId(order.getUserId());
        orderVO.setStatus(order.getStatus());
        orderVO.setAmount(order.getAmount());
        orderVO.setDescription(order.getDescription());
        orderVO.setCreatedTime(order.getCreatedTime());
        orderVO.setUpdatedTime(order.getUpdatedTime());
        return orderVO;
    }
}
