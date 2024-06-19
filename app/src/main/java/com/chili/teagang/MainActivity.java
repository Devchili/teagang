package com.chili.teagang;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private MenuViewModel menuViewModel;
    private ScrollView sidebar;
    private ScrollView cartContainer;
    private LinearLayout cartItemsContainer;
    private MenuCategory currentCategory;
    private TextView categoryTitle;
    private FirebaseAuth mAuth;
    private ImageButton btnClearCart;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            navigateToLogin();
            return;
        } else {
            String email = currentUser.getEmail();
            if ("admin@gmail.com".equals(email)) {
                navigateToDisplayItems();
                return;
            }
        }

        initializeViews();

        menuViewModel = new ViewModelProvider(this).get(MenuViewModel.class);

        setupSidebarButtons();
        observeViewModel();
        setupCheckoutButton();
        setupClearCartButton();
        setupBottomNavigation();

        // Fetch and observe recommendations at the start
        menuViewModel.fetchRecommendations();
        observeRecommendations();
    }

    private void initializeViews() {
        sidebar = findViewById(R.id.sidebar);
        cartContainer = findViewById(R.id.cartContainer);
        cartItemsContainer = findViewById(R.id.cartItemsContainer);
        categoryTitle = findViewById(R.id.categoryTitle);
        btnClearCart = findViewById(R.id.btnClearCart);
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void navigateToDisplayItems() {
        Intent intent = new Intent(this, DisplayItemsActivity.class);
        startActivity(intent);
        finish();
    }

    private void setupSidebarButtons() {
        initializeButton(R.id.btnCoffee, MenuCategory.COFFEE);
        initializeButton(R.id.btnNonCoffee, MenuCategory.NON_COFFEE);
        initializeButton(R.id.btnMilkTea, MenuCategory.MILK_TEA);
        initializeButton(R.id.btnFruitTea, MenuCategory.FRUIT_TEA);
        initializeButton(R.id.btnCreamCheese, MenuCategory.CREAM_CHEESE);
        initializeButton(R.id.btnRecommendations, null); // Pass null for recommendations
    }

    private void initializeButton(int buttonId, MenuCategory category) {
        ImageButton button = findViewById(buttonId);
        button.setOnClickListener(v -> {
            if (category == null) {
                observeRecommendations();
            } else {
                updateCategory(category);
            }
        });
    }

    private void observeRecommendations() {
        // Clear the current menu items
        menuViewModel.clearMenuItems();

        // Observe and update with recommended items
        menuViewModel.getRecommendedItems().observe(this, recommendations -> {
            List<String> recommendationTexts = new ArrayList<>();
            for (Recommendation recommendation : recommendations) {
                recommendationTexts.add(recommendation.getDrinkname() + ": " + recommendation.getAnswer());
            }
            updateMenu(recommendationTexts, "Recommendations");
            updateCategoryTitle("Recommendations");
        });
    }

    private void updateCategory(MenuCategory category) {
        currentCategory = category;
        updateCategoryTitle();
        menuViewModel.setCategory(currentCategory);
    }

    private void observeViewModel() {
        menuViewModel.getMenuItems().observe(this, this::updateMenu);
        menuViewModel.getCartItems().observe(this, this::updateCart);
    }

    private void setupCheckoutButton() {
        ImageButton btnCheckout = findViewById(R.id.btnCheckout);
        btnCheckout.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CheckoutActivity.class);

            List<CartItem> cartItems = menuViewModel.getCartItems().getValue();
            if (cartItems == null) {
                cartItems = new ArrayList<>();
            }

            double totalPrice = calculateTotalPrice(cartItems);

            Bundle extras = new Bundle();
            extras.putParcelableArrayList("cartItems", new ArrayList<>(cartItems));
            extras.putDouble("totalPrice", totalPrice);
            intent.putExtras(extras);

            startActivity(intent);
        });
    }

    private void setupClearCartButton() {
        btnClearCart.setOnClickListener(v -> {
            menuViewModel.clearCart();
            Log.d("MainActivity", "Cart cleared");
        });
    }

    private void updateMenu(List<String> menuItems) {
        updateMenu(menuItems, currentCategory != null ? currentCategory.name() : "Recommendations");
    }

    private void updateMenu(List<String> menuItems, String category) {
        Fragment menuFragment = MenuFragment.newInstance(menuItems, category);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.menuFragmentContainer, menuFragment)
                .commit();
    }

    private void updateCategoryTitle(String title) {
        categoryTitle.setText(title);
    }

    private void updateCategoryTitle() {
        categoryTitle.setText(currentCategory.name());
    }

    private void updateCart(List<CartItem> cartItems) {
        Log.d("MainActivity", "Updating cart: " + cartItems.size() + " items");
        cartItemsContainer.removeAllViews();
        double totalPrice = 0.0;
        for (CartItem item : cartItems) {
            totalPrice += addItemToCartView(item);
        }
        addTotalPriceView(totalPrice);
    }

    private double addItemToCartView(CartItem item) {
        LinearLayout cartItemLayout = new LinearLayout(this);
        cartItemLayout.setOrientation(LinearLayout.HORIZONTAL);
        cartItemLayout.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        TextView textView = new TextView(this);
        double itemPrice = item.getPrice() * item.getQuantity();
        textView.setText(String.format("%s - %s: %d (₱%.2f)", item.getName(), item.getSize(), item.getQuantity(), itemPrice));
        LinearLayout.LayoutParams textViewParams = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1.0f
        );
        textViewParams.setMargins(0, 0, 0, 16);
        textView.setLayoutParams(textViewParams);
        cartItemLayout.addView(textView);

        ImageButton removeButton = new ImageButton(this);
        removeButton.setImageResource(android.R.drawable.ic_delete);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(40, 40);
        params.setMargins(16, 0, 0, 16);
        removeButton.setLayoutParams(params);
        removeButton.setOnClickListener(v -> menuViewModel.removeFromCart(item));
        cartItemLayout.addView(removeButton);

        cartItemsContainer.addView(cartItemLayout);

        return itemPrice;
    }

    private void addTotalPriceView(double totalPrice) {
        TextView totalTextView = new TextView(this);
        totalTextView.setText(String.format("Total: ₱%.2f", totalPrice));
        LinearLayout.LayoutParams totalTextParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        totalTextParams.setMargins(0, 16, 0, 0);
        totalTextView.setLayoutParams(totalTextParams);
        cartItemsContainer.addView(totalTextView);
    }

    private double calculateTotalPrice(List<CartItem> cartItems) {
        double totalPrice = 0.0;
        for (CartItem item : cartItems) {
            totalPrice += item.getPrice() * item.getQuantity();
        }
        return totalPrice;
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView != null) {
            bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int id = item.getItemId();
                    if (id == R.id.navigation_order) {
                        return true;
                    } else if (id == R.id.navigation_profile) {
                        Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                        startActivity(intent);
                        return true;
                    }
                    return false;
                }
            });
        }
    }
}
