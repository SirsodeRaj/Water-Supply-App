package com.example.watersupply;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class NotificationHelper {

    private final FirebaseFirestore db;

    public NotificationHelper() {
        db = FirebaseFirestore.getInstance();
    }

    // ---------------------------------------------------------
    // CUSTOMER NOTIFICATION
    // ---------------------------------------------------------

    public void sendCustomerNotification(
            String customerId,
            String type,
            String title,
            String message
    ) {

        if (customerId == null ||
                customerId.trim().isEmpty()) {
            return;
        }

        Map<String, Object> notification =
                new HashMap<>();

        notification.put(
                "customerId",
                customerId
        );

        notification.put(
                "type",
                type
        );

        notification.put(
                "title",
                title
        );

        notification.put(
                "message",
                message
        );

        notification.put(
                "timestamp",
                FieldValue.serverTimestamp()
        );

        db.collection("notifications")
                .add(notification);
    }


    // ---------------------------------------------------------
    // ADMIN NOTIFICATION
    // ---------------------------------------------------------

    public void sendAdminNotification(
            String type,
            String title,
            String message
    ) {

        Map<String, Object> notification =
                new HashMap<>();

        notification.put(
                "type",
                type
        );

        notification.put(
                "title",
                title
        );

        notification.put(
                "message",
                message
        );

        notification.put(
                "timestamp",
                FieldValue.serverTimestamp()
        );

        db.collection("notifications")
                .add(notification);
    }
}