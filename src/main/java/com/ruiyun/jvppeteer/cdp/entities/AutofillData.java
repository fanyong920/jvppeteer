package com.ruiyun.jvppeteer.cdp.entities;

public class AutofillData {

    private CreditCard creditCard;

    public AutofillData() {
    }

    public AutofillData(CreditCard creditCard) {
        this.creditCard = creditCard;
    }

    public CreditCard getCreditCard() {
        return creditCard;
    }

    public void setCreditCard(CreditCard creditCard) {
        this.creditCard = creditCard;
    }
}
