package com.chili.teagang;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ViewMenuItemsActivity extends AppCompatActivity {

    private ImageButton buttonCategory1;
    private ImageButton buttonCategory2;
    private ImageButton buttonCategory3;
    private ImageButton buttonCategory4;
    private ImageButton buttonCategory5;
    private RecyclerView recyclerViewMenuItems;
    private MenuItemAdapter menuItemAdapter;
    private List<MenuItem> menuItems;
    private FirebaseFirestore db;
    private ImageButton selectedButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_menu_items);

        buttonCategory1 = findViewById(R.id.buttonCategory1);
        buttonCategory2 = findViewById(R.id.buttonCategory2);
        buttonCategory3 = findViewById(R.id.buttonCategory3);
        buttonCategory4 = findViewById(R.id.buttonCategory4);
        buttonCategory5 = findViewById(R.id.buttonCategory5);
        recyclerViewMenuItems = findViewById(R.id.recyclerViewMenuItems);

        recyclerViewMenuItems.setLayoutManager(new LinearLayoutManager(this));
        menuItems = new ArrayList<>();
        menuItemAdapter = new MenuItemAdapter(menuItems, this::editMenuItem, this::deleteMenuItem);
        recyclerViewMenuItems.setAdapter(menuItemAdapter);

        db = FirebaseFirestore.getInstance();

        buttonCategory1.setOnClickListener(view -> selectCategory(buttonCategory1, MenuCategory.COFFEE));
        buttonCategory2.setOnClickListener(view -> selectCategory(buttonCategory2, MenuCategory.NON_COFFEE));
        buttonCategory3.setOnClickListener(view -> selectCategory(buttonCategory3, MenuCategory.FRUIT_TEA));
        buttonCategory4.setOnClickListener(view -> selectCategory(buttonCategory4, MenuCategory.MILK_TEA));
        buttonCategory5.setOnClickListener(view -> selectCategory(buttonCategory5, MenuCategory.FRUIT_TEA));  // Assuming this is intentional
    }

    private void selectCategory(ImageButton button, MenuCategory category) {
        if (selectedButton != null) {
            selectedButton.setBackgroundResource(0); // Reset the background of the previously selected button
        }
        selectedButton = button;
        selectedButton.setBackgroundResource(R.drawable.button_selected); // Highlight the selected button
        loadMenuItems(category);
    }

    private void loadMenuItems(MenuCategory category) {
        CollectionReference menuItemsRef = db.collection("menu_items");
        menuItemsRef.whereEqualTo("category", category.name())
                .get()
                .addOnSuccessListener(snapshots -> {
                    menuItems.clear();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        MenuItem menuItem = doc.toObject(MenuItem.class);
                        if (menuItem != null) {
                            menuItem.setId(doc.getId());
                            menuItems.add(menuItem);
                        }
                    }
                    menuItemAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(ViewMenuItemsActivity.this, "Error loading menu items", Toast.LENGTH_SHORT).show());
    }

    private void editMenuItem(MenuItem menuItem) {
        Intent intent = new Intent(this, EditMenuItemActivity.class);
        intent.putExtra("menuItem", menuItem);
        startActivity(intent);
    }

    private void deleteMenuItem(MenuItem menuItem) {
        db.collection("menu_items").document(menuItem.getId())
                .delete()
                .addOnSuccessListener(aVoid -> Toast.makeText(ViewMenuItemsActivity.this, "Item deleted successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(ViewMenuItemsActivity.this, "Failed to delete item", Toast.LENGTH_SHORT).show());
    }
}
