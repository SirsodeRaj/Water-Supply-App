package com.example.watersupply;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MonthlyOrderGenerator {

    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    public MonthlyOrderGenerator() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    public void generateTodayOrder(
            OnOrderGenerationListener listener) {

        if (auth.getCurrentUser() == null) {
            listener.onFailure(
                    "Please login again."
            );
            return;
        }

        String customerId =
                auth.getCurrentUser().getUid();

        // Today's date
        SimpleDateFormat dateFormat =
                new SimpleDateFormat(
                        "yyyy-MM-dd",
                        Locale.getDefault()
                );

        String today =
                dateFormat.format(new Date());

        // Find active monthly package
        db.collection("monthlyPackages")
                .whereEqualTo(
                        "customerId",
                        customerId
                )
                .whereEqualTo(
                        "packageStatus",
                        "Active"
                )
                .limit(1)
                .get()
                .addOnSuccessListener(packageSnapshot -> {

                    if (packageSnapshot.isEmpty()) {

                        listener.onNoPackage();

                        return;
                    }

                    DocumentSnapshot packageDocument =
                            packageSnapshot
                                    .getDocuments()
                                    .get(0);

                    String packageId =
                            packageDocument.getId();

                    checkDeliverySchedule(
                            customerId,
                            packageId,
                            packageDocument,
                            today,
                            listener
                    );
                })
                .addOnFailureListener(e ->
                        listener.onFailure(
                                "Failed to load monthly package."
                        )
                );
    }

    private void checkDeliverySchedule(
            String customerId,
            String packageId,
            DocumentSnapshot packageDocument,
            String today,
            OnOrderGenerationListener listener) {

        String scheduleId =
                packageId + "_" + today;

        db.collection("monthlyPackageSchedules")
                .document(scheduleId)
                .get()
                .addOnSuccessListener(scheduleDocument -> {

                    if (scheduleDocument.exists()) {

                        Boolean delivery =
                                scheduleDocument.getBoolean(
                                        "delivery"
                                );

                        if (delivery != null &&
                                !delivery) {

                            listener.onDeliveryOff();

                            return;
                        }
                    }

                    // No OFF record means delivery is ON
                    createTodayOrder(
                            customerId,
                            packageId,
                            packageDocument,
                            today,
                            listener
                    );
                })
                .addOnFailureListener(e ->
                        listener.onFailure(
                                "Failed to check delivery schedule."
                        )
                );
    }

    private void createTodayOrder(
            String customerId,
            String packageId,
            DocumentSnapshot packageDocument,
            String today,
            OnOrderGenerationListener listener) {

        String orderId =
                packageId + "_" + today;

        db.collection("orders")
                .document(orderId)
                .get()
                .addOnSuccessListener(existingOrder -> {

                    // Prevent duplicate order
                    if (existingOrder.exists()) {

                        listener.onAlreadyExists();

                        return;
                    }

                    String waterType =
                            packageDocument.getString(
                                    "waterType"
                            );

                    Long quantity =
                            packageDocument.getLong(
                                    "quantity"
                            );

                    Double pricePerUnit =
                            packageDocument.getDouble(
                                    "pricePerUnit"
                            );

                    String deliveryTime =
                            packageDocument.getString(
                                    "deliveryTime"
                            );

                    if (waterType == null) {
                        waterType = "Normal Water";
                    }

                    if (quantity == null) {
                        quantity = 0L;
                    }

                    if (pricePerUnit == null) {
                        pricePerUnit = 0.0;
                    }

                    if (deliveryTime == null ||
                            deliveryTime.isEmpty()) {

                        deliveryTime = "Anytime";
                    }

                    double totalAmount =
                            quantity * pricePerUnit;

                    loadCustomerAndCreateOrder(
                            customerId,
                            packageId,
                            orderId,
                            waterType,
                            quantity,
                            pricePerUnit,
                            totalAmount,
                            deliveryTime,
                            today,
                            listener
                    );
                })
                .addOnFailureListener(e ->
                        listener.onFailure(
                                "Failed to check existing order."
                        )
                );
    }

    private void loadCustomerAndCreateOrder(
            String customerId,
            String packageId,
            String orderId,
            String waterType,
            Long quantity,
            Double pricePerUnit,
            Double totalAmount,
            String deliveryTime,
            String today,
            OnOrderGenerationListener listener) {

        db.collection("users")
                .document(customerId)
                .get()
                .addOnSuccessListener(userDocument -> {

                    if (!userDocument.exists()) {

                        listener.onFailure(
                                "Customer profile not found."
                        );

                        return;
                    }

                    String customerName =
                            userDocument.getString(
                                    "name"
                            );

                    String phone =
                            userDocument.getString(
                                    "phone"
                            );

                    String address =
                            userDocument.getString(
                                    "address"
                            );

                    if (customerName == null) {
                        customerName = "Customer";
                    }

                    if (phone == null) {
                        phone = "";
                    }

                    if (address == null) {
                        address = "";
                    }

                    Map<String, Object> order =
                            new HashMap<>();

                    order.put(
                            "customerId",
                            customerId
                    );

                    order.put(
                            "customerName",
                            customerName
                    );

                    order.put(
                            "phone",
                            phone
                    );

                    order.put(
                            "address",
                            address
                    );

                    order.put(
                            "waterType",
                            waterType
                    );

                    order.put(
                            "quantity",
                            quantity
                    );

                    order.put(
                            "pricePerUnit",
                            pricePerUnit
                    );

                    order.put(
                            "totalAmount",
                            totalAmount
                    );

                    order.put(
                            "paymentMethod",
                            "Monthly Package"
                    );

                    order.put(
                            "paymentStatus",
                            "Pending"
                    );

                    order.put(
                            "orderStatus",
                            "Pending"
                    );

                    order.put(
                            "orderType",
                            "Monthly Package"
                    );

                    order.put(
                            "deliveryTime",
                            deliveryTime
                    );

                    order.put(
                            "deliveryDate",
                            today
                    );

                    order.put(
                            "monthlyPackageId",
                            packageId
                    );

                    order.put(
                            "timestamp",
                            FieldValue.serverTimestamp()
                    );

                    db.collection("orders")
                            .document(orderId)
                            .set(order)
                            .addOnSuccessListener(v ->
                                    listener.onCreated()
                            )
                            .addOnFailureListener(e ->
                                    listener.onFailure(
                                            "Failed to create today's order."
                                    )
                            );
                })
                .addOnFailureListener(e ->
                        listener.onFailure(
                                "Failed to load customer profile."
                        )
                );
    }

    public interface OnOrderGenerationListener {

        void onCreated();

        void onAlreadyExists();

        void onDeliveryOff();

        void onNoPackage();

        void onFailure(String message);
    }
}