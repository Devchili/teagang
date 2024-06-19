package com.chili.teagang;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

// CartAdapter.java
public class CartAdapter extends RecyclerView.Adapter<ItemCartViewHolder> {
    private List<CartItem> cartItems;

    public CartAdapter(List<CartItem> cartItems) {
        this.cartItems = cartItems;
    }

    @NonNull
    @Override
    public ItemCartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new ItemCartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemCartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);
        holder.tvItemName.setText(item.getName());
        // Convert the price to pesos
        double priceInPeso = item.getPrice(); // Assuming 1 dollar = 50 pesos, adjust this according to your exchange rate
        holder.tvQuantityPrice.setText(item.getQuantity() + " x ₱" + priceInPeso); // Display the price in pesos
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }
}
