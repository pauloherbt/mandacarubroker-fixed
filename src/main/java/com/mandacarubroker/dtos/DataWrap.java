package com.mandacarubroker.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class DataWrap {

    @Positive(message = "Amount cannot be negative")
    @NotNull(message = "Amount cannot be null")
    private Double amount;

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
