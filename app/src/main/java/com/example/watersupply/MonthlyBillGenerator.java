package com.example.watersupply;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MonthlyBillGenerator {

    private final FirebaseAuth mAuth;
    private final FirebaseFirestore db;

    public MonthlyBillGenerator() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    public interface OnBillGenerationListener {

        void onBillGenerated(double totalAmount);

        void onAlreadyExists(double totalAmount);

        void onNoPackage();

        void onFailure(String message);
    }

    public void generateCurrentMonthBill(
            OnBillGenerationListener listener
    ) {

        if (mAuth.getCurrentUser() == null) {
            listener.onFailure("Customer is not logged in.");
            return;
        }

        String customerId =
                mAuth.getCurrentUser().getUid();

        String month = new SimpleDateFormat(
                "yyyy-MM",
                Locale.getDefault()
        ).format(new Date());

        // Only generate the bill after the current month ends.
        Calendar today = Calendar.getInstance();

        int currentDay =
                today.get(Calendar.DAY_OF_MONTH);

        int lastDay =
                today.getActualMaximum(Calendar.DAY_OF_MONTH);

        if (currentDay < lastDay) {
            listener.onFailure(
                    "Current month is not complete yet."
            );
            return;
        }

        findActivePackage(
                customerId,
                month,
                listener
        );
    }

    private void findActivePackage(
            String customerId,
            String month,
            OnBillGenerationListener listener
    ) {

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
                .addOnSuccessListener(querySnapshot -> {

                    if (querySnapshot.isEmpty()) {
                        listener.onNoPackage();
                        return;
                    }

                    DocumentSnapshot packageDocument =
                            querySnapshot.getDocuments().get(0);

                    calculateBill(
                            customerId,
                            month,
                            packageDocument,
                            listener
                    );
                })
                .addOnFailureListener(e ->
                        listener.onFailure(
                                "Unable to find monthly package."
                        )
                );
    }

    private void calculateBill(
            String customerId,
            String month,
            DocumentSnapshot packageDocument,
            OnBillGenerationListener listener
    ) {

        String packageId =
                packageDocument.getId();

        String billId =
                packageId + "_" + month;

        // Prevent duplicate bill creation.
        db.collection("monthlyBills")
                .document(billId)
                .get()
                .addOnSuccessListener(existingBill -> {

                    if (existingBill.exists()) {

                        Double existingAmount =
                                existingBill.getDouble(
                                        "totalAmount"
                                );

                        if (existingAmount == null) {
                            existingAmount = 0.0;
                        }

                        listener.onAlreadyExists(
                                existingAmount
                        );

                        return;
                    }

                    loadMonthlyOrders(
                            customerId,
                            month,
                            packageId,
                            listener
                    );
                })
                .addOnFailureListener(e ->
                        listener.onFailure(
                                "Unable to check existing bill."
                        )
                );
    }

    private void loadMonthlyOrders(
            String customerId,
            String month,
            String packageId,
            OnBillGenerationListener listener
    ) {

        Calendar calendar =
                Calendar.getInstance();

        int year =
                calendar.get(Calendar.YEAR);

        int currentMonth =
                calendar.get(Calendar.MONTH);

        Calendar startCalendar =
                Calendar.getInstance();

        startCalendar.set(
                year,
                currentMonth,
                1,
                0,
                0,
                0
        );

        startCalendar.set(
                Calendar.MILLISECOND,
                0
        );

        Calendar endCalendar =
                Calendar.getInstance();

        endCalendar.set(
                year,
                currentMonth,
                calendar.getActualMaximum(
                        Calendar.DAY_OF_MONTH
                ),
                23,
                59,
                59
        );

        endCalendar.set(
                Calendar.MILLISECOND,
                999
        );

        Date startDate =
                startCalendar.getTime();

        Date endDate =
                endCalendar.getTime();

        db.collection("orders")
                .whereEqualTo(
                        "customerId",
                        customerId
                )
                .whereEqualTo(
                        "monthlyPackageId",
                        packageId
                )
                .whereGreaterThanOrEqualTo(
                        "timestamp",
                        startDate
                )
                .whereLessThanOrEqualTo(
                        "timestamp",
                        endDate
                )
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    double totalAmount = 0.0;

                    int totalDeliveryDays = 0;

                    int deliveredDays = 0;

                    for (DocumentSnapshot order :
                            querySnapshot.getDocuments()) {

                        totalDeliveryDays++;

                        String orderStatus =
                                order.getString(
                                        "orderStatus"
                                );

                        if ("Delivered".equals(
                                orderStatus
                        )) {

                            deliveredDays++;

                            Double amount =
                                    order.getDouble(
                                            "totalAmount"
                                    );

                            if (amount != null) {
                                totalAmount += amount;
                            }
                        }
                    }

                    createBill(
                            customerId,
                            month,
                            packageId,
                            totalAmount,
                            totalDeliveryDays,
                            deliveredDays,
                            listener
                    );
                })
                .addOnFailureListener(e ->
                        listener.onFailure(
                                "Unable to calculate monthly bill."
                        )
                );
    }

    private void createBill(
            String customerId,
            String month,
            String packageId,
            double totalAmount,
            int totalDeliveryDays,
            int deliveredDays,
            OnBillGenerationListener listener
    ) {

        db.collection("users")
                .document(customerId)
                .get()
                .addOnSuccessListener(userDocument -> {

                    String customerName =
                            userDocument.getString("name");

                    if (customerName == null) {
                        customerName = "";
                    }

                    Map<String, Object> bill =
                            new HashMap<>();

                    bill.put(
                            "customerId",
                            customerId
                    );

                    bill.put(
                            "customerName",
                            customerName
                    );

                    bill.put(
                            "month",
                            month
                    );

                    bill.put(
                            "monthlyPackageId",
                            packageId
                    );

                    bill.put(
                            "totalAmount",
                            totalAmount
                    );

                    bill.put(
                            "totalDeliveryDays",
                            totalDeliveryDays
                    );

                    bill.put(
                            "deliveredDays",
                            deliveredDays
                    );

                    bill.put(
                            "paymentStatus",
                            "Pending"
                    );

                    bill.put(
                            "billStatus",
                            "Generated"
                    );

                    bill.put(
                            "timestamp",
                            FieldValue.serverTimestamp()
                    );

                    db.collection("monthlyBills")
                            .document(
                                    packageId + "_" + month
                            )
                            .set(bill)
                            .addOnSuccessListener(unused ->
                                    listener.onBillGenerated(
                                            totalAmount
                                    )
                            )
                            .addOnFailureListener(e ->
                                    listener.onFailure(
                                            "Unable to create monthly bill."
                                    )
                            );
                })
                .addOnFailureListener(e ->
                        listener.onFailure(
                                "Unable to load customer details."
                        )
                );
    }
}