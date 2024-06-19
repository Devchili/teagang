package com.chili.teagang;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MenuItemAdapter extends RecyclerView.Adapter<MenuItemAdapter.MenuItemViewHolder> {

    private List<MenuItem> menuItems;
    private OnEditClickListener editClickListener;
    private OnDeleteClickListener deleteClickListener;

    public MenuItemAdapter(List<MenuItem> menuItems, OnEditClickListener editClickListener, OnDeleteClickListener deleteClickListener) {
        this.menuItems = menuItems;
        this.editClickListener = editClickListener;
        this.deleteClickListener = deleteClickListener;
    }

    @NonNull
    @Override
    public MenuItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_menu, parent, false);
        return new MenuItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuItemViewHolder holder, int position) {
        MenuItem menuItem = menuItems.get(position);
        holder.textViewName.setText(menuItem.getName());

        holder.buttonEdit.setOnClickListener(v -> editClickListener.onEditClick(menuItem));
        holder.buttonDelete.setOnClickListener(v -> deleteClickListener.onDeleteClick(menuItem));
    }

    @Override
    public int getItemCount() {
        return menuItems.size();
    }

    public static class MenuItemViewHolder extends RecyclerView.ViewHolder {

        TextView textViewName;
        Button buttonEdit, buttonDelete;

        public MenuItemViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewName);
            buttonEdit = itemView.findViewById(R.id.buttonEdit);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }
    }

    public interface OnEditClickListener {
        void onEditClick(MenuItem menuItem);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(MenuItem menuItem);
    }
}
