package com.tongyi.rescue_api.domain.dto;

import java.math.BigDecimal;

public class OrderCreateDTO {
    private String description;
    private BigDecimal amount;

    public OrderCreateDTO() {
    }

    public OrderCreateDTO(String description, BigDecimal amount) {
        this.description = description;
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
