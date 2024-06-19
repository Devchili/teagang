package com.chili.teagang;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CheckoutActivity extends AppCompatActivity {

    private static final String PAYMONGO_API_URL = "https://api.paymongo.com/v1/links";
    private static final String PAYMONGO_API_KEY = "sk_test_WVjBZAByDueqnDmz77dFK7eC";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private OkHttpClient client;
    private Gson gson;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.checkout_activity);

        client = new OkHttpClient();
        gson = new Gson();

        TextView tvTotal = findViewById(R.id.tvTotal);
        Button btnPay = findViewById(R.id.btnPay);
        RecyclerView recyclerViewItems = findViewById(R.id.recyclerViewItems);
        progressBar = findViewById(R.id.progressBar);

        double totalPrice = getIntent().getDoubleExtra("totalPrice", 0.0);
        tvTotal.setText("Total: ₱" + totalPrice);

        List<CartItem> cartItems = getIntent().getParcelableArrayListExtra("cartItems");
        setupRecyclerView(recyclerViewItems, cartItems);

        btnPay.setOnClickListener(v -> {
            int totalAmountInCents = (int) (totalPrice * 100);
            if (totalAmountInCents < 10000) {
                Toast.makeText(CheckoutActivity.this, "Total amount must be at least ₱100.00", Toast.LENGTH_SHORT).show();
                return;
            }
            String description = generateDescription(cartItems);
            createCheckoutSession(totalAmountInCents, "PHP", description, cartItems, totalPrice);
        });
    }

    private void setupRecyclerView(RecyclerView recyclerView, List<CartItem> cartItems) {
        CartAdapter adapter = new CartAdapter(cartItems);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private String generateDescription(List<CartItem> cartItems) {
        StringBuilder description = new StringBuilder("Order details: ");
        for (CartItem item : cartItems) {
            description.append(item.getName())
                    .append(" (")
                    .append(item.getSize())
                    .append(" x")
                    .append(item.getQuantity())
                    .append("), ");
        }
        if (description.length() > 2) {
            description.setLength(description.length() - 2);
        }
        return description.toString();
    }

    private void createCheckoutSession(int amount, String currency, String description, List<CartItem> cartItems, double totalPrice) {
        progressBar.setVisibility(View.VISIBLE);
        List<LineItem> lineItems = new ArrayList<>();
        for (CartItem item : cartItems) {
            lineItems.add(new LineItem(item.getName(), item.getQuantity(), (int) (item.getPrice() * 100), currency));
        }

        PaymentRequest paymentRequest = new PaymentRequest(amount, currency, description, lineItems);
        String json = gson.toJson(paymentRequest);

        Log.d("PaymentRequest", json);

        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(PAYMONGO_API_URL)
                .post(body)
                .addHeader("accept", "application/json")
                .addHeader("Content-Type", "application/json")
                .addHeader("authorization", "Basic " + android.util.Base64.encodeToString((PAYMONGO_API_KEY + ":").getBytes(), android.util.Base64.NO_WRAP))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                handleFailure(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    handleErrorResponse(response);
                    return;
                }

                handleSuccessResponse(response, totalPrice);
            }
        });
    }

    private void handleFailure(IOException e) {
        e.printStackTrace();
        runOnUiThread(() -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(CheckoutActivity.this, "Payment Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void handleErrorResponse(Response response) throws IOException {
        String errorBody = response.body().string();
        Log.e("PaymentError", "Error response: " + errorBody);
        runOnUiThread(() -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(CheckoutActivity.this, "Payment Failed: " + errorBody, Toast.LENGTH_SHORT).show();
        });
    }

    private void handleSuccessResponse(Response response, double totalPrice) throws IOException {
        PaymentResponse paymentResponse = gson.fromJson(response.body().string(), PaymentResponse.class);
        String paymentId = paymentResponse.getData().getId();
        String checkoutUrl = paymentResponse.getData().getAttributes().getCheckoutUrl();

        runOnUiThread(() -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(CheckoutActivity.this, "Redirecting....", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(CheckoutActivity.this, PaymentWebViewActivity.class);
            intent.putExtra("checkoutUrl", checkoutUrl);
            intent.putExtra("paymentId", paymentId);
            intent.putExtra("amount", totalPrice);

            // Pass the cart items
            List<CartItem> cartItems = getIntent().getParcelableArrayListExtra("cartItems");
            intent.putParcelableArrayListExtra("cartItems", new ArrayList<>(cartItems));

            startActivity(intent);
        });
    }

    private static class LineItem {
        @SerializedName("name")
        private String name;

        @SerializedName("quantity")
        private int quantity;

        @SerializedName("amount")
        private int amount;

        @SerializedName("currency")
        private String currency;

        public LineItem(String name, int quantity, int amount, String currency) {
            this.name = name;
            this.quantity = quantity;
            this.amount = amount;
            this.currency = currency;
        }
    }

    private static class PaymentRequest {
        @SerializedName("data")
        private Data data;

        public PaymentRequest(int amount, String currency, String description, List<LineItem> lineItems) {
            this.data = new Data(amount, currency, description, lineItems);
        }

        private static class Data {
            @SerializedName("attributes")
            private Attributes attributes;

            public Data(int amount, String currency, String description, List<LineItem> lineItems) {
                this.attributes = new Attributes(amount, currency, description, lineItems);
            }

            private static class Attributes {
                @SerializedName("send_email_receipt")
                private boolean sendEmailReceipt;

                @SerializedName("show_description")
                private boolean showDescription;

                @SerializedName("show_line_items")
                private boolean showLineItems;

                @SerializedName("amount")
                private int amount;

                @SerializedName("currency")
                private String currency;

                @SerializedName("description")
                private String description;

                @SerializedName("line_items")
                private List<LineItem> lineItems;

                @SerializedName("payment_method_types")
                private List<String> paymentMethodTypes;

                public Attributes(int amount, String currency, String description, List<LineItem> lineItems) {
                    this.sendEmailReceipt = false;
                    this.showDescription = true;
                    this.showLineItems = true;
                    this.amount = amount;
                    this.currency = currency;
                    this.description = description;
                    this.lineItems = lineItems;
                    this.paymentMethodTypes = Collections.singletonList("gcash");
                }
            }
        }
    }

    private static class PaymentResponse {
        @SerializedName("data")
        private Data data;

        public Data getData() {
            return data;
        }

        private static class Data {
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
        }

        private static class Attributes {
            @SerializedName("checkout_url")
            private String checkoutUrl;

            public String getCheckoutUrl() {
                return checkoutUrl;
            }
        }
    }
}
