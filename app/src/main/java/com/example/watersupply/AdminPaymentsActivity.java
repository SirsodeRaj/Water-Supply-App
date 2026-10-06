package com.example.watersupply;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AdminPaymentsActivity extends AppCompatActivity {

    private FirebaseFirestore db;

    private LinearLayout paymentsContainer;
    private ProgressBar progressBar;
    private TextView tvNoPayments;
    private TextView tvPaymentCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_payments);

        db = FirebaseFirestore.getInstance();

        paymentsContainer = findViewById(R.id.paymentsContainer);
        progressBar = findViewById(R.id.progressBar);
        tvNoPayments = findViewById(R.id.tvNoPayments);
        tvPaymentCount = findViewById(R.id.tvPaymentCount);

        // Back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        loadPayments();
    }

    private void loadPayments() {

        progressBar.setVisibility(View.VISIBLE);
        tvNoPayments.setVisibility(View.GONE);

        db.collection("orders")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {

                    progressBar.setVisibility(View.GONE);
                    paymentsContainer.removeAllViews();

                    if (error != null) {
                        tvPaymentCount.setText("0 Payments");
                        tvNoPayments.setText("Failed to load payments.");
                        tvNoPayments.setVisibility(View.VISIBLE);
                        return;
                    }

                    if (value == null || value.isEmpty()) {
                        tvPaymentCount.setText("0 Payments");
                        tvNoPayments.setText("No payments found.");
                        tvNoPayments.setVisibility(View.VISIBLE);
                        return;
                    }

                    int paymentCount = 0;

                    for (DocumentSnapshot document : value.getDocuments()) {

                        // Payment information exists in the order document.
                        String paymentMethod =
                                document.getString("paymentMethod");

                        String paymentStatus =
                                document.getString("paymentStatus");

                        // If payment fields don't exist, skip the document.
                        if (paymentMethod == null && paymentStatus == null) {
                            continue;
                        }

                        paymentCount++;

                        View paymentView = LayoutInflater.from(this)
                                .inflate(
                                        R.layout.item_admin_payment,
                                        paymentsContainer,
                                        false
                                );

                        TextView tvCustomerName =
                                paymentView.findViewById(
                                        R.id.tvCustomerName
                                );

                        TextView tvOrderId =
                                paymentView.findViewById(
                                        R.id.tvOrderId
                                );

                        TextView tvWater =
                                paymentView.findViewById(
                                        R.id.tvWater
                                );

                        TextView tvTotal =
                                paymentView.findViewById(
                                        R.id.tvTotal
                                );

                        TextView tvPaymentMethod =
                                paymentView.findViewById(
                                        R.id.tvPaymentMethod
                                );

                        TextView tvPaymentStatus =
                                paymentView.findViewById(
                                        R.id.tvPaymentStatus
                                );

                        TextView tvOrderStatus =
                                paymentView.findViewById(
                                        R.id.tvOrderStatus
                                );

                        TextView tvDate =
                                paymentView.findViewById(
                                        R.id.tvDate
                                );

                        String customerName =
                                document.getString("customerName");

                        String waterType =
                                document.getString("waterType");

                        String orderStatus =
                                document.getString("orderStatus");

                        Long quantity =
                                document.getLong("quantity");

                        Double totalAmount =
                                document.getDouble("totalAmount");

                        Date timestamp =
                                document.getDate("timestamp");

                        if (customerName == null ||
                                customerName.isEmpty()) {
                            customerName = "Unknown Customer";
                        }

                        if (waterType == null ||
                                waterType.isEmpty()) {
                            waterType = "Water";
                        }

                        if (orderStatus == null ||
                                orderStatus.isEmpty()) {
                            orderStatus = "Unknown";
                        }

                        if (paymentMethod == null ||
                                paymentMethod.isEmpty()) {
                            paymentMethod = "Not specified";
                        }

                        if (paymentStatus == null ||
                                paymentStatus.isEmpty()) {
                            paymentStatus = "Pending";
                        }

                        tvCustomerName.setText(customerName);

                        String shortOrderId = document.getId();

                        if (shortOrderId.length() > 8) {
                            shortOrderId =
                                    shortOrderId.substring(0, 8);
                        }

                        tvOrderId.setText(
                                "Order ID: #" + shortOrderId
                        );

                        if (quantity != null) {
                            tvWater.setText(
                                    waterType + " × " + quantity
                            );
                        } else {
                            tvWater.setText(waterType);
                        }

                        if (totalAmount != null) {
                            tvTotal.setText(
                                    "Amount: ₹" +
                                            String.format(
                                                    Locale.getDefault(),
                                                    "%.0f",
                                                    totalAmount
                                            )
                            );
                        } else {
                            tvTotal.setText("Amount: ₹0");
                        }

                        tvPaymentMethod.setText(
                                "Method: " + paymentMethod
                        );

                        tvPaymentStatus.setText(
                                "Payment: " + paymentStatus
                        );

                        tvOrderStatus.setText(
                                "Order: " + orderStatus
                        );

                        if (timestamp != null) {

                            SimpleDateFormat dateFormat =
                                    new SimpleDateFormat(
                                            "dd MMM yyyy, hh:mm a",
                                            Locale.getDefault()
                                    );

                            tvDate.setText(
                                    "Date: " +
                                            dateFormat.format(timestamp)
                            );

                        } else {
                            tvDate.setText(
                                    "Date: Not available"
                            );
                        }

                        // Payment status appearance
                        if (paymentStatus.equalsIgnoreCase("Paid")) {

                            tvPaymentStatus.setTextColor(
                                    getResources().getColor(
                                            android.R.color.holo_green_dark
                                    )
                            );

                        } else if (
                                paymentStatus.equalsIgnoreCase("Failed")
                        ) {

                            tvPaymentStatus.setTextColor(
                                    getResources().getColor(
                                            android.R.color.holo_red_dark
                                    )
                            );

                        } else {

                            tvPaymentStatus.setTextColor(
                                    getResources().getColor(
                                            android.R.color.holo_orange_dark
                                    )
                            );
                        }

                        paymentsContainer.addView(paymentView);
                    }

                    tvPaymentCount.setText(
                            paymentCount + " Payments"
                    );

                    if (paymentCount == 0) {
                        tvNoPayments.setText(
                                "No payment records found."
                        );
                        tvNoPayments.setVisibility(View.VISIBLE);
                    }
                });
    }
}