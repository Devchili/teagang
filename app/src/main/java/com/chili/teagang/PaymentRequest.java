package com.chili.teagang;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PaymentRequest {
    @SerializedName("data")
    private Data data;

    public PaymentRequest(int amount, String currency, String description, List<String> paymentMethodAllowed) {
        this.data = new Data(amount, currency, description, paymentMethodAllowed);
    }

    private static class Data {
        @SerializedName("attributes")
        private Attributes attributes;

        public Data(int amount, String currency, String description, List<String> paymentMethodAllowed) {
            this.attributes = new Attributes(amount, currency, description, paymentMethodAllowed);
        }

        private static class Attributes {
            @SerializedName("amount")
            private int amount;

            @SerializedName("currency")
            private String currency;

            @SerializedName("description")
            private String description;

            @SerializedName("payment_method_allowed")
            private List<String> paymentMethodAllowed;

            public Attributes(int amount, String currency, String description, List<String> paymentMethodAllowed) {
                this.amount = amount;
                this.currency = currency;
                this.description = description;
                this.paymentMethodAllowed = paymentMethodAllowed;
            }
        }
    }
}
