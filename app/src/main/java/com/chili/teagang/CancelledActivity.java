package com.chili.teagang;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class CancelledActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cancelled);

        TextView tvCancelled = findViewById(R.id.tvCancelled);
        tvCancelled.setText("Order Cancelled. Please try again.");
    }
}
