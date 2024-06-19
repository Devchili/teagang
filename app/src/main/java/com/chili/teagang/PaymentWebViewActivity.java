package com.chili.teagang;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PaymentWebViewActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private static final String TAG = "PaymentWebViewActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_web_view);

        db = FirebaseFirestore.getInstance();

        WebView webView = findViewById(R.id.webView);
        String checkoutUrl = getIntent().getStringExtra("checkoutUrl");
        String paymentId = getIntent().getStringExtra("paymentId");
        double amount = getIntent().getDoubleExtra("amount", 0.0);
        List<CartItem> cartItems = getIntent().getParcelableArrayListExtra("cartItems");

        if (checkoutUrl == null || paymentId == null || cartItems == null) {
            Toast.makeText(this, "Invalid payment details", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        webView.getSettings().setJavaScriptEnabled(true); // Enable JavaScript

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                // Assuming that success URL contains "success" and failure URL contains "cancel"
                if (uri.toString().contains("success")) {
                    storeItemsInFirestore(cartItems, amount);
                    Intent intent = new Intent(PaymentWebViewActivity.this, ReceiptActivity.class);
                    intent.putExtra("paymentId", paymentId);
                    intent.putExtra("checkoutUrl", checkoutUrl);
                    intent.putExtra("amount", amount);
                    intent.putParcelableArrayListExtra("cartItems", new ArrayList<>(cartItems));
                    startActivity(intent);
                    finish();
                    return true;
                } else if (uri.toString().contains("cancel")) {
                    Intent intent = new Intent(PaymentWebViewActivity.this, CancelledActivity.class);
                    startActivity(intent);
                    finish();
                    return true;
                }
                return super.shouldOverrideUrlLoading(view, request);
            }
        });

        webView.loadUrl(checkoutUrl);
    }

    private void storeItemsInFirestore(List<CartItem> cartItems, double amount) {
        for (CartItem item : cartItems) {
            Map<String, Object> data = new HashMap<>();
            data.put("name", item.getName());
            data.put("size", item.getSize());
            data.put("quantity", item.getQuantity());
            data.put("amount", amount);

            db.collection("data")
                    .add(data)
                    .addOnSuccessListener(documentReference -> {
                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                    })
                    .addOnFailureListener(e -> {
                        Log.w(TAG, "Error adding document", e);
                    });
        }
    }
}
