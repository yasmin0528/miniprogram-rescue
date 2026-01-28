package com.tongyi.rescue_api;

import com.tongyi.rescue_api.domain.entity.Order;
import com.tongyi.rescue_api.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class OrderServiceTest {
    @Autowired
    private OrderService orderService;

    @Test
    public void testCreateOrder() {
        Order order = new Order();
        order.setOrderNo("ORD20240128001");
        order.setUserId(1L);
        order.setStatus("pending");
        order.setDescription("Test Order");
        order.setCreatedTime(new java.util.Date());

        Order savedOrder = orderService.createOrder(order);
        assertNotNull(savedOrder);
        assertNotNull(savedOrder.getId());
    }

    @Test
    public void testGetOrderById() {
        Order order = orderService.getOrderById(1L);
        if (order != null) {
            assertEquals(1L, order.getId());
        }
    }
}
