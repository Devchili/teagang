package com.chili.teagang;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MenuFragment extends Fragment {

    private static final String ARG_MENU_ITEMS = "menu_items";
    private static final String ARG_CATEGORY_NAME = "category_name";
    private MenuViewModel menuViewModel;

    public static MenuFragment newInstance(List<String> menuItems, String categoryName) {
        MenuFragment fragment = new MenuFragment();
        Bundle args = new Bundle();
        args.putStringArrayList(ARG_MENU_ITEMS, new ArrayList<>(menuItems));
        args.putString(ARG_CATEGORY_NAME, categoryName);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_menu, container, false);
        LinearLayout menuContainer = view.findViewById(R.id.menuContainer);

        menuViewModel = new ViewModelProvider(requireActivity()).get(MenuViewModel.class);

        List<String> menuItems = getArguments().getStringArrayList(ARG_MENU_ITEMS);
        String categoryName = getArguments().getString(ARG_CATEGORY_NAME);

        for (final String item : menuItems) {
            View menuItemView = inflater.inflate(R.layout.menu_item, menuContainer, false);
            TextView tvMenuItemName = menuItemView.findViewById(R.id.tvMenuItemName);
            LinearLayout pricesContainer = menuItemView.findViewById(R.id.pricesContainer);

            tvMenuItemName.setText(item);

            if (!"Recommendations".equals(categoryName)) {
                Map<String, Double> sizePriceMap = menuViewModel.getPrices(categoryName, item);
                for (Map.Entry<String, Double> entry : sizePriceMap.entrySet()) {
                    TextView priceView = new TextView(getContext());
                    priceView.setText(String.format("%s: ₱%.2f", entry.getKey(), entry.getValue()));
                    priceView.setTextSize(14);
                    priceView.setTextColor(getResources().getColor(android.R.color.black));
                    pricesContainer.addView(priceView);
                }

                menuItemView.setOnClickListener(v -> showSizeQuantityDialog(item, sizePriceMap));
            }

            menuContainer.addView(menuItemView);
        }

        return view;
    }

    private void showSizeQuantityDialog(final String item, Map<String, Double> sizePriceMap) {
        final View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_size_quantity, null);
        AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                .setTitle("Select Size and Quantity")
                .setView(dialogView)
                .setPositiveButton("Add to Cart", null) // We'll override the onClick later
                .setNegativeButton("Cancel", null)
                .show();

        EditText etSmall = dialogView.findViewById(R.id.etSmall);
        EditText etMedium = dialogView.findViewById(R.id.etMedium);
        EditText etLarge = dialogView.findViewById(R.id.etLarge);

        String categoryName = getArguments().getString(ARG_CATEGORY_NAME);

        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
            int smallQuantity = etSmall.getText().toString().isEmpty() ? 0 : Integer.parseInt(etSmall.getText().toString());
            int mediumQuantity = etMedium.getText().toString().isEmpty() ? 0 : Integer.parseInt(etMedium.getText().toString());
            int largeQuantity = etLarge.getText().toString().isEmpty() ? 0 : Integer.parseInt(etLarge.getText().toString());

            if (sizePriceMap.containsKey("SMALL") && smallQuantity > 0) {
                double smallPrice = sizePriceMap.get("SMALL");
                menuViewModel.addToCart(new CartItem(item, "Small", smallQuantity, smallPrice));
            }
            if (sizePriceMap.containsKey("MEDIUM") && mediumQuantity > 0) {
                double mediumPrice = sizePriceMap.get("MEDIUM");
                menuViewModel.addToCart(new CartItem(item, "Medium", mediumQuantity, mediumPrice));
            }
            if (sizePriceMap.containsKey("LARGE") && largeQuantity > 0) {
                double largePrice = sizePriceMap.get("LARGE");
                menuViewModel.addToCart(new CartItem(item, "Large", largeQuantity, largePrice));
            }

            alertDialog.dismiss();
        });
    }
}
