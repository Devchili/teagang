package com.chili.teagang;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView userName;
    private TextView currentemail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        userName = findViewById(R.id.userName);
        currentemail = findViewById(R.id.currentemail);
        Button btnLogout = findViewById(R.id.btnLogout);
        Button btnAddMenuItem = findViewById(R.id.btnAddMenuItem);
        Button btnViewMenuItems = findViewById(R.id.btnViewMenuItems); // New button

        // Set up logout button
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        // Set up add menu item button
        btnAddMenuItem.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, AddMenuItemActivity.class);
            startActivity(intent);
        });

        // Set up view menu items button
        btnViewMenuItems.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, ViewMenuItemsActivity.class);
            startActivity(intent);
        });

        // Retrieve and display user's name
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            getUserInfo(currentUser.getUid());
        }

        // Setup bottom navigation
        setupBottomNavigation();
    }

    private void getUserInfo(String userId) {
        DocumentReference userRef = db.collection("users").document(userId);

        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String name = documentSnapshot.getString("name");
                String email = documentSnapshot.getString("email");
                if (name != null && !name.isEmpty()) {
                    userName.setText(name);
                } else {
                    userName.setText("Anonymous User");
                }
                if (email != null && !email.isEmpty()) {
                    currentemail.setText(email);
                }
            } else {
                userName.setText("User not found");
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(ProfileActivity.this, "Error getting user data", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView != null) {
            bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int id = item.getItemId();
                    if (id == R.id.navigation_order) {
                        // Start MainActivity
                        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                        return true;
                    } else if (id == R.id.navigation_profile) {
                        // Stay in ProfileActivity
                        return true;
                    }
                    return false;
                }
            });
        }
    }
}
