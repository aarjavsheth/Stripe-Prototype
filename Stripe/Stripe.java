package com.Stripe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Stripe {

    private static HashMap<String, Double> cardFees; // To store network & its fees
    private static List<Charge> charges; // To store all the charges
    private static List<Merchant> merchants; // To store payout balance for each merchant
    private static final double STRIPE_FEE = 2; // Stripe processing fee

    public Stripe() {
        cardFees = new HashMap<>();
        charges = new ArrayList<>();
        merchants = new ArrayList<>();
    }

    // Function to check whether given input is a charge
    private boolean isCharge(String s) {
        return s.split("\\?")[0].equals("/charge");
    }

    // Function to check whether given input is a confirmed charge
    private boolean isConfirm(String s) {
        return s.split("\\?")[0].equals("/confirm");
    }

    // Function to check whether given input is a refunded charge
    private boolean isRefund(String s) {
        return s.split("\\?")[0].equals("/refund");
    }

    // Function to check whether given input is a payout action
    private boolean isPayout(String s) {
        return s.split("\\?")[0].equals("/payout");
    }

    // Function to create a charge
    private Charge getCharge(String s) {
        String[] sArray =  s.split("&");

        Charge charge = new Charge();
        Merchant merchant = new Merchant();
        for (String value : sArray) {
            switch (value.split("=")[0]) {
                case "network":
                    charge.network = value.split("=")[1];
                    charge.networkFee = cardFees.get((value.split("=")[1]));
                    break;
                case "amount":
                    charge.amount = Long.parseLong(value.split("=")[1]);
                    break;
                case "merchant_id":
                    charge.merchantID = value.split("=")[1];
                    merchant.merchantID = value.split("=")[1];
                    int count = 0;
                    for (Merchant merchant1 : merchants) {
                        if (merchant1.merchantID.equals(charge.merchantID)) {
                            count++;
                        }
                    }
                    if (count == 0) {
                        merchants.add(merchant);
                    }
                    break;
                case "charge_id":
                    charge.chargeID = value.split("=")[1];
                    break;
            }
        }

        return charge;
    }

    // Function to confirm a charge
    private void confirm(String s) {

        String chargeID = s.split("=")[1];
        for(Charge charge : charges) {
            for(Merchant merchant : merchants) {
                if(charge.chargeID.equals(chargeID) && charge.merchantID.equals(merchant.merchantID)) {
                    charge.confirm = true;
                    double processingFees = charge.amount * (STRIPE_FEE / 100);
                    double networkFees = charge.amount * (charge.networkFee / 100);

                    merchant.payoutBal += charge.amount - processingFees - networkFees;
                }
            }
        }
    }

    // Function to process a payout
    private void payout(String s) {
        for(Merchant merchant : merchants) {
            if(s.split("=")[1].equals(merchant.merchantID)) {
                System.out.println(merchant.merchantID+", "+(long) Math.ceil(merchant.payoutBal));
                merchant.payoutBal = 0;
            }
        }
    }

    // Function to refund a charge
    private void refund(String s) {
        for(Charge charge : charges) {
            for(Merchant merchant : merchants) {
                if(charge.chargeID.equals(s.split("=")[1]) && !charge.confirm && charge.merchantID.equals(merchant.merchantID)) {
                    double networkFees = charge.amount * (charge.networkFee / 100);
                    merchant.payoutBal -= networkFees;
                }
            }
        }
    }

    public static void process_actions(List<String> input_lines) {
        Stripe stripe = new Stripe();

        for(int i = 1; i <= Integer.parseInt(input_lines.get(0)); i++) {
            cardFees.put(input_lines.get(i).split(" ")[0], Double.parseDouble(input_lines.get(i).split(" ")[1]));
        }


        for(int i = Integer.parseInt(input_lines.get(0)) + 2;
            i < Integer.parseInt(input_lines.get(Integer.parseInt(input_lines.get(0)) + 1) ) + Integer.parseInt(input_lines.get(0)) + 2;
            i++) {
            if(stripe.isCharge(input_lines.get(i))) {
                charges.add(stripe.getCharge(input_lines.get(i).split("\\?")[1]));
            }
            else if(stripe.isConfirm(input_lines.get(i))) {
                stripe.confirm(input_lines.get(i).split("\\?")[1]);
            }
            else if(stripe.isRefund(input_lines.get(i))) {
                stripe.refund(input_lines.get(i).split("\\?")[1]);
            }
            else if(stripe.isPayout(input_lines.get(i))) {
                stripe.payout(input_lines.get(i).split("\\?")[1]);
            }
        }

    }

    public static void main(String[] args) {

        List<String> input_lines = new ArrayList<>();
        input_lines.add("3");
        input_lines.add("visa 2.2");
        input_lines.add("mastercard 2.5");
        input_lines.add("amex 3.7");
        input_lines.add("10");
        input_lines.add("/charge?network=visa&merchant_id=m001&charge_id=c001&amount=67800");
        input_lines.add("/confirm?charge_id=c001");
        input_lines.add("/charge?network=visa&charge_id=c002&amount=5520&merchant_id=m002");
        input_lines.add("/confirm?charge_id=c002");
        input_lines.add("/charge?network=amex&amount=9000&charge_id=c003&merchant_id=m001");
        input_lines.add("/confirm?charge_id=c003");
        input_lines.add("/charge?network=mastercard&amount=7777&charge_id=c004&merchant_id=m002");
        input_lines.add("/refund?charge_id=c004");
        input_lines.add("/payout?merchant_id=m001");
        input_lines.add("/payout?merchant_id=m002");

        process_actions(input_lines);
    }

}