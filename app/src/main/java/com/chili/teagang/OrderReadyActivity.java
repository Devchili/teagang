package com.chili.teagang;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class OrderReadyActivity extends AppCompatActivity {

    private static final int NAV_HOME_ID = R.id.nav_home;
    private static final int NAV_ORDERS_ID = R.id.nav_orders;
    private static final int NAV_ACCOUNT_ID = R.id.nav_account;

    private RecyclerView recyclerViewOrderReady;
    private ItemAdapter itemAdapter;
    private List<CartItem> itemList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_ready);

        recyclerViewOrderReady = findViewById(R.id.recyclerViewOrderReady);
        recyclerViewOrderReady.setLayoutManager(new LinearLayoutManager(this));

        itemList = new ArrayList<>();
        itemAdapter = new ItemAdapter(itemList, null, false); // Pass false to hide the "Done" button
        recyclerViewOrderReady.setAdapter(itemAdapter);

        db = FirebaseFirestore.getInstance();
        loadOrderReadyItems();

        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setOnNavigationItemSelectedListener(this::onNavigationItemSelected);
    }

    private void loadOrderReadyItems() {
        CollectionReference orderReadyRef = db.collection("order_ready");
        orderReadyRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                itemList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    CartItem item = document.toObject(CartItem.class);
                    itemList.add(item);
                }
                itemAdapter.notifyDataSetChanged();
            }
        });
    }

    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == NAV_HOME_ID) {
            Intent intent = new Intent(this, DisplayItemsActivity.class); // Replace with your actual Home activity
            startActivity(intent);
            return true;
        } else if (itemId == NAV_ORDERS_ID) {
            // Current activity, do nothing
            return true;
        } else if (itemId == NAV_ACCOUNT_ID) {
            Intent intent = new Intent(this, AccountActivity.class); // Replace with your actual Account activity
            startActivity(intent);
            return true;
        }
        return false;
    }
}
