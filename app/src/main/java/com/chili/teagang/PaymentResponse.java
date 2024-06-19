package com.chili.teagang;

import com.google.gson.annotations.SerializedName;

public class PaymentResponse {
    @SerializedName("data")
    private Data data;

    public Data getData() {
        return data;
    }

    public static class Data {
        @SerializedName("id")
        private String id;

        @SerializedName("attributes")
        private Attributes attributes;

        public String getId() {
            return id;
        }

        public Attributes getAttributes() {
            return attributes;
        }

        public static class Attributes {
            @SerializedName("amount")
            private int amount;

            @SerializedName("currency")
            private String currency;

            @SerializedName("description")
            private String description;

            @SerializedName("status")
            private String status;

            @SerializedName("client_key")
            private String clientKey;

            @SerializedName("created_at")
            private long createdAt;

            @SerializedName("updated_at")
            private long updatedAt;

            @SerializedName("payment_method_allowed")
            private String[] paymentMethodAllowed;

            @SerializedName("payments")
            private Object[] payments;

            @SerializedName("next_action")
            private Object nextAction;

            @SerializedName("payment_method_options")
            private PaymentMethodOptions paymentMethodOptions;

            @SerializedName("metadata")
            private Metadata metadata;

            public static class PaymentMethodOptions {
                @SerializedName("card")
                private Card card;

                public static class Card {
                    @SerializedName("request_three_d_secure")
                    private String requestThreeDSecure;
                }
            }

            public static class Metadata {
                @SerializedName("yet_another_metadata")
                private String yetAnotherMetadata;

                @SerializedName("reference_number")
                private String referenceNumber;
            }
        }
    }
}
