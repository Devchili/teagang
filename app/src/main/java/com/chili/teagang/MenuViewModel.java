package com.chili.teagang;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MenuViewModel extends AndroidViewModel {

    private final MutableLiveData<List<String>> menuItems = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<CartItem>> cartItems = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Recommendation>> recommendedItems = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    private static final Map<MenuCategory, List<MenuItem>> CATEGORY_ITEMS_MAP = new HashMap<>();
    private static final Map<String, Map<String, Double>> PRICE_MAP = new HashMap<>();
    private static boolean isDataLoaded = false;

    public MenuViewModel(@NonNull Application application) {
        super(application);
        if (!isDataLoaded) {
            loadMenuData();
        }
    }

    private void loadMenuData() {
        isLoading.setValue(true);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("menu_items")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            MenuItem menuItem = document.toObject(MenuItem.class);
                            MenuCategory category = MenuCategory.valueOf(menuItem.getCategory());
                            CATEGORY_ITEMS_MAP.computeIfAbsent(category, k -> new ArrayList<>()).add(menuItem);

                            PRICE_MAP.computeIfAbsent(menuItem.getName(), k -> new HashMap<>()).putAll(menuItem.getSizes());
                        }
                        isDataLoaded = true;
                    } else {
                        errorMessage.setValue("Error loading menu data.");
                        Log.w("MenuViewModel", "Error getting documents.", task.getException());
                    }
                    isLoading.setValue(false);
                });
    }

    public void fetchRecommendations() {
        isLoading.setValue(true);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("data")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Map<String, Integer> itemCountMap = new HashMap<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String name = document.getString("name");
                            int quantity = document.getLong("quantity").intValue();
                            itemCountMap.put(name, itemCountMap.getOrDefault(name, 0) + quantity);
                        }
                        List<String> mostOrderedItems = itemCountMap.entrySet().stream()
                                .sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()))
                                .map(Map.Entry::getKey)
                                .limit(1)
                                .collect(Collectors.toList());
                        matchRecommendationsWithJson(mostOrderedItems);
                    } else {
                        errorMessage.setValue("Error fetching recommendations.");
                        Log.w("MenuViewModel", "Error getting documents.", task.getException());
                        isLoading.setValue(false);
                    }
                });
    }

    private void matchRecommendationsWithJson(List<String> mostOrderedItems) {
        try {
            InputStream inputStream = getApplication().getAssets().open("recommendations.json");
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            String json = new String(buffer, "UTF-8");

            Gson gson = new Gson();
            Type type = new TypeToken<RecommendationsResponse>() {}.getType();
            RecommendationsResponse response = gson.fromJson(json, type);

            List<Recommendation> matchedRecommendations = response.getData().stream()
                    .filter(recommendation -> mostOrderedItems.contains(recommendation.getDrinkname()))
                    .collect(Collectors.toList());

            recommendedItems.setValue(matchedRecommendations);
        } catch (IOException e) {
            Log.e("MenuViewModel", "Error reading recommendations.json", e);
            errorMessage.setValue("Error loading recommendations.");
        } finally {
            isLoading.setValue(false);
        }
    }

    public LiveData<List<Recommendation>> getRecommendedItems() {
        return recommendedItems;
    }

    public LiveData<List<String>> getMenuItems() {
        return menuItems;
    }

    public LiveData<List<CartItem>> getCartItems() {
        return cartItems;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void setCategory(MenuCategory category) {
        List<MenuItem> items = CATEGORY_ITEMS_MAP.get(category);
        if (items != null) {
            List<String> itemNames = items.stream()
                    .filter(item -> !item.isOutOfStock())
                    .map(MenuItem::getName)
                    .collect(Collectors.toList());
            menuItems.setValue(itemNames);
        } else {
            menuItems.setValue(new ArrayList<>());
        }
    }

    public Map<String, Double> getPrices(String category, String name) {
        List<MenuItem> items = CATEGORY_ITEMS_MAP.get(MenuCategory.valueOf(category));
        if (items != null) {
            for (MenuItem item : items) {
                if (item.getName().equals(name)) {
                    return item.getSizes();
                }
            }
        }
        return new HashMap<>();
    }

    public void addToCart(CartItem item) {
        List<CartItem> currentCart = new ArrayList<>(cartItems.getValue());
        currentCart.add(item);
        cartItems.setValue(currentCart);
    }

    public void removeFromCart(CartItem item) {
        List<CartItem> currentCart = new ArrayList<>(cartItems.getValue());
        currentCart.remove(item);
        cartItems.setValue(currentCart);
    }

    public void clearCart() {
        Log.d("MenuViewModel", "Clearing cart");
        cartItems.setValue(new ArrayList<>());
    }

    public void clearMenuItems() {
        menuItems.setValue(new ArrayList<>());
    }
}
