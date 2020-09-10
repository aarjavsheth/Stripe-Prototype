package com.Stripe;

class Charge {
    String network;
    String chargeID;
    String merchantID;
    long amount;
    double networkFee;
    boolean confirm;

    public Charge() {
        network = "";
        chargeID = "";
        merchantID = "";
        amount = 0;
        networkFee = 0;
        confirm = false;
    }
}