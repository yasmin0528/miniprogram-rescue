package com.tongyi.rescue_api.controller;

import com.tongyi.rescue_api.service.PayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/pay/notify")
@RequiredArgsConstructor
public class PayNotifyController {

    private final PayService payService;
    private final Environment env;

    @PostMapping("/pay")
    public String payNotify(HttpServletRequest request, @RequestBody(required = false) String body) {
        return payService.handlePayNotify(extractHeaders(request), body == null ? "" : body);
    }

    @PostMapping("/refund")
    public String refundNotify(HttpServletRequest request, @RequestBody(required = false) String body) {
        return payService.handleRefundNotify(extractHeaders(request), body == null ? "" : body);
    }

    @PostMapping("/test/refund")
    public String refundNotifyTest(@RequestBody(required = false) String body) {
        String activeProfiles = env.getProperty("spring.profiles.active", "");
        if (!activeProfiles.contains("test")) {
            return "{\"code\":\"FAIL\",\"message\":\"仅测试环境可用\"}";
        }
        return payService.handleRefundNotifyWithoutVerify(body == null ? "" : body);
    }

    @PostMapping("/transfer")
    public String transferNotify(HttpServletRequest request, @RequestBody(required = false) String body) {
        return payService.handleTransferNotify(extractHeaders(request), body == null ? "" : body);
    }

    private Map<String, String> extractHeaders(HttpServletRequest request) {
        if (request == null) {
            return Collections.emptyMap();
        }
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> names = request.getHeaderNames();
        while (names != null && names.hasMoreElements()) {
            String name = names.nextElement();
            headers.put(name, request.getHeader(name));
        }
        return headers;
    }
}
