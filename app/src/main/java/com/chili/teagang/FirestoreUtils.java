package com.chili.teagang;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class FirestoreUtils {

    public static void loadItems(FirebaseFirestore db, OnItemsLoadedListener listener) {
        CollectionReference itemsRef = db.collection("data");
        itemsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<CartItem> items = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    CartItem item = document.toObject(CartItem.class);
                    item.setId(document.getId());
                    items.add(item);
                }
                listener.onItemsLoaded(items);
            } else {
                listener.onError(task.getException());
            }
        });
    }

    public static void markItemAsDone(FirebaseFirestore db, CartItem item, OnItemMarkedDoneListener listener) {
        db.collection("order_ready").add(item)
                .addOnSuccessListener(documentReference -> {
                    db.collection("data").document(item.getId()).delete()
                            .addOnSuccessListener(aVoid -> listener.onItemMarkedDone())
                            .addOnFailureListener(listener::onError);
                })
                .addOnFailureListener(listener::onError);
    }

    public interface OnItemsLoadedListener {
        void onItemsLoaded(List<CartItem> items);
        void onError(Exception e);
    }

    public interface OnItemMarkedDoneListener {
        void onItemMarkedDone();
        void onError(Exception e);
    }
}
