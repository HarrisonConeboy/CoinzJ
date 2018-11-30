package com.example.s1658030.coinzj;

//This Java class is used to create Coin objects throughout the app

public class Coin {

    private String id;
    private Double value;
    private String currency;


    Coin(String id, Double value, String currency) {
        this.id = id;
        this.value = value;
        this.currency = currency;
    }

    //Getters & Setters
    public String getId() {
        return id;
    }

    public Double getValue() {
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

    public void setValue(Double value) {
        this.value = value;
    }

}
