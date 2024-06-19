package com.chili.teagang;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddMenuItemActivity extends AppCompatActivity {

    private Spinner spinnerCategory;
    private EditText editTextName, editTextSmallSizePrice, editTextMediumSizePrice, editTextLargeSizePrice;
    private Button buttonAddItem;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_menu_item);

        spinnerCategory = findViewById(R.id.spinnerCategory);
        editTextName = findViewById(R.id.editTextName);
        editTextSmallSizePrice = findViewById(R.id.editTextSmallSizePrice);
        editTextMediumSizePrice = findViewById(R.id.editTextMediumSizePrice);
        editTextLargeSizePrice = findViewById(R.id.editTextLargeSizePrice);
        buttonAddItem = findViewById(R.id.buttonAddItem);

        // Set up the Spinner with the filtered categories
        ArrayAdapter<MenuCategory> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getFilteredCategories());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        buttonAddItem.setOnClickListener(v -> addItemToFirestore());
    }

    private List<MenuCategory> getFilteredCategories() {
        List<MenuCategory> filteredCategories = new ArrayList<>();
        for (MenuCategory category : MenuCategory.values()) {
            if (!category.name().equals("RECOMMENDATIONS")) {
                filteredCategories.add(category);
            }
        }
        return filteredCategories;
    }

    private void addItemToFirestore() {
        MenuCategory selectedCategory = (MenuCategory) spinnerCategory.getSelectedItem();
        String name = editTextName.getText().toString().trim();
        String smallPriceStr = editTextSmallSizePrice.getText().toString().trim();
        String mediumPriceStr = editTextMediumSizePrice.getText().toString().trim();
        String largePriceStr = editTextLargeSizePrice.getText().toString().trim();

        if (selectedCategory == null || TextUtils.isEmpty(name) || TextUtils.isEmpty(smallPriceStr) || TextUtils.isEmpty(mediumPriceStr)) {
            Toast.makeText(this, "Please fill out all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double smallPrice = Double.parseDouble(smallPriceStr);
        double mediumPrice = Double.parseDouble(mediumPriceStr);
        Double largePrice = TextUtils.isEmpty(largePriceStr) ? null : Double.parseDouble(largePriceStr);

        Map<String, Double> sizes = new HashMap<>();
        sizes.put("SMALL", smallPrice);
        sizes.put("MEDIUM", mediumPrice);
        if (largePrice != null) {
            sizes.put("LARGE", largePrice);
        }

        Map<String, Object> menuItem = new HashMap<>();
        menuItem.put("category", selectedCategory.name());
        menuItem.put("name", name);
        menuItem.put("sizes", sizes);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("menu_items")
                .add(menuItem)
                .addOnSuccessListener(documentReference -> Toast.makeText(this, "Item added successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to add item", Toast.LENGTH_SHORT).show());
    }
}
