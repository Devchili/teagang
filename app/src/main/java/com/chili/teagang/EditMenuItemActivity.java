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

public class EditMenuItemActivity extends AppCompatActivity {

    private Spinner spinnerCategory;
    private Spinner spinnerOutOfStock;
    private EditText editTextName, editTextSmallSizePrice, editTextMediumSizePrice, editTextLargeSizePrice;
    private Button buttonSaveChanges;
    private MenuItem menuItem;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_menu_item);

        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerOutOfStock = findViewById(R.id.spinnerOutOfStock);
        editTextName = findViewById(R.id.editTextName);
        editTextSmallSizePrice = findViewById(R.id.editTextSmallSizePrice);
        editTextMediumSizePrice = findViewById(R.id.editTextMediumSizePrice);
        editTextLargeSizePrice = findViewById(R.id.editTextLargeSizePrice);
        buttonSaveChanges = findViewById(R.id.buttonSaveChanges);

        // Set up the Spinners with the categories and out-of-stock status
        ArrayAdapter<MenuCategory> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getFilteredCategories());
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        ArrayAdapter<String> outOfStockAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"In Stock", "Out of Stock"});
        outOfStockAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerOutOfStock.setAdapter(outOfStockAdapter);

        db = FirebaseFirestore.getInstance();

        // Retrieve the menu item from the intent
        menuItem = (MenuItem) getIntent().getSerializableExtra("menuItem");
        if (menuItem != null) {
            populateFields(menuItem);
        }

        buttonSaveChanges.setOnClickListener(v -> saveChangesToFirestore());
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

    private void populateFields(MenuItem menuItem) {
        MenuCategory category = MenuCategory.valueOf(menuItem.getCategory());
        spinnerCategory.setSelection(getFilteredCategories().indexOf(category));
        editTextName.setText(menuItem.getName());
        Map<String, Double> sizes = menuItem.getSizes();
        editTextSmallSizePrice.setText(String.valueOf(sizes.get("SMALL")));
        editTextMediumSizePrice.setText(String.valueOf(sizes.get("MEDIUM")));
        if (sizes.containsKey("LARGE")) {
            editTextLargeSizePrice.setText(String.valueOf(sizes.get("LARGE")));
        }

        String outOfStockStatus = menuItem.isOutOfStock() ? "Out of Stock" : "In Stock";
        spinnerOutOfStock.setSelection(outOfStockStatus.equals("Out of Stock") ? 1 : 0);
    }

    private void saveChangesToFirestore() {
        MenuCategory selectedCategory = (MenuCategory) spinnerCategory.getSelectedItem();
        String name = editTextName.getText().toString().trim();
        String smallPriceStr = editTextSmallSizePrice.getText().toString().trim();
        String mediumPriceStr = editTextMediumSizePrice.getText().toString().trim();
        String largePriceStr = editTextLargeSizePrice.getText().toString().trim();
        String outOfStockStatus = (String) spinnerOutOfStock.getSelectedItem();

        if (selectedCategory == null || TextUtils.isEmpty(name) || TextUtils.isEmpty(smallPriceStr) || TextUtils.isEmpty(mediumPriceStr)) {
            Toast.makeText(this, "Please fill out all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double smallPrice = Double.parseDouble(smallPriceStr);
            double mediumPrice = Double.parseDouble(mediumPriceStr);
            Double largePrice = TextUtils.isEmpty(largePriceStr) ? null : Double.parseDouble(largePriceStr);

            Map<String, Double> sizes = new HashMap<>();
            sizes.put("SMALL", smallPrice);
            sizes.put("MEDIUM", mediumPrice);
            if (largePrice != null) {
                sizes.put("LARGE", largePrice);
            }

            Map<String, Object> updatedMenuItem = new HashMap<>();
            updatedMenuItem.put("category", selectedCategory.name());
            updatedMenuItem.put("name", name);
            updatedMenuItem.put("sizes", sizes);
            updatedMenuItem.put("outOfStock", "Out of Stock".equals(outOfStockStatus));

            buttonSaveChanges.setEnabled(false); // Disable button to prevent multiple clicks
            db.collection("menu_items").document(menuItem.getId())
                    .update(updatedMenuItem)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Item updated successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to update item: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        buttonSaveChanges.setEnabled(true); // Re-enable button if failed
                    });
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid prices", Toast.LENGTH_SHORT).show();
        }
    }
}
