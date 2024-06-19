package com.chili.teagang;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class ReceiptActivity extends AppCompatActivity {

    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    private MenuViewModel menuViewModel;
    private boolean shareAfterCapture;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt);

        menuViewModel = new ViewModelProvider(this).get(MenuViewModel.class);

        String paymentId = getIntent().getStringExtra("paymentId");
        String checkoutUrl = getIntent().getStringExtra("checkoutUrl");
        double amount = getIntent().getDoubleExtra("amount", 0.0);
        List<CartItem> cartItems = getIntent().getParcelableArrayListExtra("cartItems");

        TextView tvReceiptDetails = findViewById(R.id.tvReceiptDetails);
        StringBuilder receiptDetails = new StringBuilder();
        receiptDetails.append(String.format("Payment ID: %s\nAmount: ₱%.2f\nCheckout URL: %s\n\n", paymentId, amount, checkoutUrl));
        receiptDetails.append("Items:\n");
        for (CartItem item : cartItems) {
            receiptDetails.append(String.format("%s - %s: %d\n", item.getName(), item.getSize(), item.getQuantity()));
        }
        tvReceiptDetails.setText(receiptDetails.toString());

        Button btnSave = findViewById(R.id.btnSave);
        btnSave.setOnClickListener(v -> {
            shareAfterCapture = false;
            handleCaptureReceipt();
        });

        Button btnShare = findViewById(R.id.btnShare);
        btnShare.setOnClickListener(v -> {
            shareAfterCapture = true;
            handleCaptureReceipt();
        });

        ImageButton btnClose = findViewById(R.id.btnClose);
        btnClose.setOnClickListener(v -> {
            Intent intent = new Intent(ReceiptActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void handleCaptureReceipt() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10 and above don't require WRITE_EXTERNAL_STORAGE
            captureReceiptAsImage();
        } else {
            // For Android versions below 10, request WRITE_EXTERNAL_STORAGE permission
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE);
            } else {
                captureReceiptAsImage();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                captureReceiptAsImage();
            } else {
                Snackbar.make(findViewById(android.R.id.content), "Permission denied", Snackbar.LENGTH_LONG).show();
            }
        }
    }

    private void captureReceiptAsImage() {
        View cardViewReceipt = findViewById(R.id.cardViewReceipt);
        cardViewReceipt.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(cardViewReceipt.getDrawingCache());
        cardViewReceipt.setDrawingCacheEnabled(false);

        try {
            Uri imageUri = saveImageToMediaStore(bitmap);
            if (shareAfterCapture) {
                shareImage(imageUri);
            } else {
                Snackbar.make(findViewById(android.R.id.content), "Receipt saved successfully", Snackbar.LENGTH_LONG).show();
            }

        } catch (IOException e) {
            e.printStackTrace();
            Snackbar.make(findViewById(android.R.id.content), "Failed to save receipt", Snackbar.LENGTH_LONG).show();
        }
    }

    private Uri saveImageToMediaStore(Bitmap bitmap) throws IOException {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, "receipt.png");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");

        Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        if (uri != null) {
            try (OutputStream out = getContentResolver().openOutputStream(uri)) {
                if (out != null) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                }
            }
        }
        return uri;
    }

    private void shareImage(Uri imageUri) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
        shareIntent.setType("image/png");
        startActivity(Intent.createChooser(shareIntent, "Share Receipt"));
    }
}
