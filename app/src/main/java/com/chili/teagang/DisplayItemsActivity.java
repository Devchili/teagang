package com.chili.teagang;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class DisplayItemsActivity extends AppCompatActivity {

    private static final int NAV_HOME_ID = R.id.nav_home;
    private static final int NAV_ORDERS_ID = R.id.nav_orders;
    private static final int NAV_ACCOUNT_ID = R.id.nav_account;

    private RecyclerView recyclerViewItems;
    private ItemAdapter itemAdapter;
    private List<CartItem> itemList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_items2);

        recyclerViewItems = findViewById(R.id.recyclerViewItems);
        recyclerViewItems.setLayoutManager(new LinearLayoutManager(this));

        itemList = new ArrayList<>();
        itemAdapter = new ItemAdapter(itemList, this::markAsDone, true);
        recyclerViewItems.setAdapter(itemAdapter);

        db = FirebaseFirestore.getInstance();
        loadItems();

        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setOnNavigationItemSelectedListener(this::onNavigationItemSelected);
    }

    private void loadItems() {
        CollectionReference itemsRef = db.collection("data");
        itemsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                itemList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    CartItem item = document.toObject(CartItem.class);
                    item.setId(document.getId());
                    itemList.add(item);
                }
                itemAdapter.notifyDataSetChanged();
            }
        });
    }

    private void markAsDone(CartItem item) {
        db.collection("order_ready").add(item)
                .addOnSuccessListener(documentReference -> {
                    db.collection("data").document(item.getId()).delete()
                            .addOnSuccessListener(aVoid -> {
                                itemList.remove(item);
                                itemAdapter.notifyDataSetChanged();
                            });
                });
    }

    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == NAV_HOME_ID) {
            return true;
        } else if (itemId == NAV_ORDERS_ID) {
            // Current activity, do nothing
            Intent intent = new Intent(this, OrderReadyActivity.class); // Replace with your actual Home activity
            startActivity(intent);
            return true;
        } else if (itemId == NAV_ACCOUNT_ID) {
            Intent intent = new Intent(this, AccountActivity.class); // Replace with your actual Account activity
            startActivity(intent);
            return true;
        }
        return false;
    }
}
