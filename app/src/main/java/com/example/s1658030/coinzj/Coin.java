package com.example.s1658030.coinzj;

public class Coin {

    private String id;
    private Float value;
    private String currency;


    public Coin(String id, Float value, String currency) {
        this.id = id;
        this.value = value;
        this.currency = currency;
    }

    //Getters & Setters
    public String getId() {
        return id;
    }

    public Float getValue() {
        return value;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setValue(Float value) {
        this.value = value;
    }

}
