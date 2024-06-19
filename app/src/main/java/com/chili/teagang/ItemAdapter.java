package com.chili.teagang;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    private List<CartItem> itemList;
    private OnItemDoneListener onItemDoneListener;
    private boolean showDoneButton;

    public ItemAdapter(List<CartItem> itemList, OnItemDoneListener onItemDoneListener, boolean showDoneButton) {
        this.itemList = itemList;
        this.onItemDoneListener = onItemDoneListener;
        this.showDoneButton = showDoneButton;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_view, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        CartItem item = itemList.get(position);
        holder.textViewName.setText(item.getName());
        holder.textViewSize.setText(item.getSize());
        holder.textViewQuantity.setText(String.valueOf(item.getQuantity()));
        holder.textViewPrice.setText(String.valueOf(item.getPrice()));
        if (showDoneButton) {
            holder.buttonDone.setVisibility(View.VISIBLE);
            holder.buttonDone.setOnClickListener(v -> onItemDoneListener.onItemDone(item));
        } else {
            holder.buttonDone.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView textViewName, textViewSize, textViewQuantity, textViewPrice;
        Button buttonDone;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewSize = itemView.findViewById(R.id.textViewSize);
            textViewQuantity = itemView.findViewById(R.id.textViewQuantity);
            textViewPrice = itemView.findViewById(R.id.textViewPrice);
            buttonDone = itemView.findViewById(R.id.buttonDone);
        }
    }

    public interface OnItemDoneListener {
        void onItemDone(CartItem item);
    }
}
