package com.ruiyun.jvppeteer.entities;

public class CreditCard {
    private String number;
    private String name;
    private String expiryMonth;
    private String expiryYear;
    private String cvc;

    public CreditCard() {
    }

    public CreditCard(String number, String name, String expiryYear, String expiryMonth, String cvc) {
        this.number = number;
        this.cvc = cvc;
        this.expiryYear = expiryYear;
        this.expiryMonth = expiryMonth;
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExpiryMonth() {
        return expiryMonth;
    }

    public void setExpiryMonth(String expiryMonth) {
        this.expiryMonth = expiryMonth;
    }

    public String getExpiryYear() {
        return expiryYear;
    }

    public void setExpiryYear(String expiryYear) {
        this.expiryYear = expiryYear;
    }

    public String getCvc() {
        return cvc;
    }

    public void setCvc(String cvc) {
        this.cvc = cvc;
    }
}
