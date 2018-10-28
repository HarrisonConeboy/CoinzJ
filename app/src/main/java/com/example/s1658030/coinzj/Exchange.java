package com.example.s1658030.coinzj;

public class Exchange {

    private String currency;
    private Double value;

    public Exchange(String currency, Double value) {
        this.currency = currency;
        this.value = value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getCurrency() {
        return currency;
    }

    public Double getValue() {
        return value;
    }
}
