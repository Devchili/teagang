package com.chili.teagang;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

// ItemCartViewHolder.java
public class ItemCartViewHolder extends RecyclerView.ViewHolder {
    TextView tvItemName;
    TextView tvQuantityPrice;

    public ItemCartViewHolder(@NonNull View itemView) {
        super(itemView);
        tvItemName = itemView.findViewById(R.id.tvItemName);
        tvQuantityPrice = itemView.findViewById(R.id.tvQuantityPrice);
    }
}
