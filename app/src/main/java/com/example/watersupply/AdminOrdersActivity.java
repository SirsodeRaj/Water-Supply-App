package com.example.watersupply;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AdminOrdersActivity extends AppCompatActivity {

    private FirebaseFirestore db;

    private LinearLayout ordersContainer;
    private ProgressBar progressBar;
    private TextView tvNoOrders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_admin_orders
        );

        db = FirebaseFirestore.getInstance();

        ordersContainer =
                findViewById(R.id.ordersContainer);

        progressBar =
                findViewById(R.id.progressBar);

        tvNoOrders =
                findViewById(R.id.tvNoOrders);

        findViewById(R.id.btnBack)
                .setOnClickListener(v -> finish());

        loadAllOrders();
    }

    // ==========================================
    // LOAD ALL ORDERS
    // ==========================================

    private void loadAllOrders() {

        progressBar.setVisibility(View.VISIBLE);

        db.collection("orders")
                .orderBy(
                        "timestamp",
                        Query.Direction.DESCENDING
                )
                .addSnapshotListener((snapshots, error) -> {

                    progressBar.setVisibility(View.GONE);

                    if (error != null) {

                        Toast.makeText(
                                this,
                                "Failed to load orders.",
                                Toast.LENGTH_LONG
                        ).show();

                        return;
                    }

                    ordersContainer.removeAllViews();

                    if (
                            snapshots == null ||
                                    snapshots.isEmpty()
                    ) {

                        tvNoOrders.setVisibility(
                                View.VISIBLE
                        );

                        return;
                    }

                    tvNoOrders.setVisibility(
                            View.GONE
                    );

                    for (
                            DocumentSnapshot document :
                            snapshots.getDocuments()
                    ) {

                        addOrderCard(document);
                    }
                });
    }

    // ==========================================
    // ADD ORDER CARD
    // ==========================================

    private void addOrderCard(
            DocumentSnapshot document
    ) {

        View orderView =
                getLayoutInflater().inflate(
                        R.layout.item_admin_all_order,
                        ordersContainer,
                        false
                );

        TextView tvOrderId =
                orderView.findViewById(
                        R.id.tvOrderId
                );

        TextView tvCustomer =
                orderView.findViewById(
                        R.id.tvCustomer
                );

        TextView tvPhone =
                orderView.findViewById(
                        R.id.tvPhone
                );

        TextView tvAddress =
                orderView.findViewById(
                        R.id.tvAddress
                );

        TextView tvWater =
                orderView.findViewById(
                        R.id.tvWater
                );

        TextView tvTotal =
                orderView.findViewById(
                        R.id.tvTotal
                );

        TextView tvPayment =
                orderView.findViewById(
                        R.id.tvPayment
                );

        TextView tvOrderType =
                orderView.findViewById(
                        R.id.tvOrderType
                );

        TextView tvDate =
                orderView.findViewById(
                        R.id.tvDate
                );

        TextView tvDeliveryTime =
                orderView.findViewById(
                        R.id.tvDeliveryTime
                );

        TextView tvStatus =
                orderView.findViewById(
                        R.id.tvStatus
                );

        // ==========================================
        // FIRESTORE DATA
        // ==========================================

        String orderId =
                document.getId();

        String customerName =
                document.getString("customerName");

        String phone =
                document.getString("phone");

        String address =
                document.getString("address");

        String waterType =
                document.getString("waterType");

        String paymentMethod =
                document.getString("paymentMethod");

        String paymentStatus =
                document.getString("paymentStatus");

        String orderType =
                document.getString("orderType");

        String orderStatus =
                document.getString("orderStatus");

        String deliveryTime =
                document.getString("deliveryTime");

        Long quantity =
                document.getLong("quantity");

        Long totalAmount =
                document.getLong("totalAmount");

        // ==========================================
        // ORDER ID
        // ==========================================

        String shortOrderId = orderId;

        if (orderId.length() > 8) {

            shortOrderId =
                    orderId.substring(0, 8);
        }

        tvOrderId.setText(
                "Order ID: #" + shortOrderId
        );

        // ==========================================
        // CUSTOMER
        // ==========================================

        tvCustomer.setText(
                "Customer: " +
                        safe(customerName)
        );

        // ==========================================
        // PHONE
        // ==========================================

        tvPhone.setText(
                "Phone: " +
                        safe(phone)
        );

        // ==========================================
        // ADDRESS
        // ==========================================

        tvAddress.setText(
                "Address: " +
                        safe(address)
        );

        // ==========================================
        // WATER
        // ==========================================

        tvWater.setText(
                safe(waterType) +
                        " Water × " +
                        (
                                quantity != null
                                        ? quantity
                                        : 0
                        )
        );

        // ==========================================
        // TOTAL
        // ==========================================

        tvTotal.setText(
                "Total: ₹" +
                        (
                                totalAmount != null
                                        ? totalAmount
                                        : 0
                        )
        );

        // ==========================================
        // PAYMENT
        // ==========================================

        tvPayment.setText(
                "Payment: " +
                        safe(paymentMethod) +
                        " (" +
                        safe(paymentStatus) +
                        ")"
        );

        // ==========================================
        // ORDER TYPE
        // ==========================================

        tvOrderType.setText(
                "Order Type: " +
                        safe(orderType)
        );

        // ==========================================
        // DATE
        // ==========================================

        if (
                document.getTimestamp("timestamp")
                        != null
        ) {

            Date date =
                    document
                            .getTimestamp("timestamp")
                            .toDate();

            SimpleDateFormat formatter =
                    new SimpleDateFormat(
                            "dd MMM yyyy, hh:mm a",
                            Locale.getDefault()
                    );

            tvDate.setText(
                    "Date: " +
                            formatter.format(date)
            );

        } else {

            tvDate.setText(
                    "Date: Processing..."
            );
        }

        // ==========================================
        // DELIVERY TIME
        // ==========================================

        if (
                deliveryTime != null &&
                        !deliveryTime.trim().isEmpty()
        ) {

            tvDeliveryTime.setText(
                    "Delivery Time: " +
                            deliveryTime
            );

        } else {

            tvDeliveryTime.setText(
                    "Delivery Time: Anytime"
            );
        }

        // ==========================================
        // STATUS
        // ==========================================

        tvStatus.setText(
                "Status: " +
                        safe(orderStatus)
        );

        setStatusColor(
                tvStatus,
                orderStatus
        );

        // ==========================================
        // ADD CARD
        // ==========================================

        ordersContainer.addView(
                orderView
        );
    }

    // ==========================================
    // STATUS COLOR
    // ==========================================

    private void setStatusColor(
            TextView textView,
            String status
    ) {

        if (status == null) {

            textView.setTextColor(
                    Color.DKGRAY
            );

            return;
        }

        switch (status) {

            case "Pending":

                textView.setTextColor(
                        Color.rgb(245, 124, 0)
                );

                break;

            case "Accepted":

                textView.setTextColor(
                        Color.rgb(25, 118, 210)
                );

                break;

            case "Preparing":

                textView.setTextColor(
                        Color.rgb(123, 31, 162)
                );

                break;

            case "Out for Delivery":

                textView.setTextColor(
                        Color.rgb(2, 119, 189)
                );

                break;

            case "Delivered":

                textView.setTextColor(
                        Color.rgb(46, 125, 50)
                );

                break;

            case "Cancelled":
            case "Rejected":

                textView.setTextColor(
                        Color.rgb(211, 47, 47)
                );

                break;

            default:

                textView.setTextColor(
                        Color.DKGRAY
                );
        }
    }

    // ==========================================
    // SAFE STRING
    // ==========================================

    private String safe(String value) {

        return value == null ||
                value.trim().isEmpty()
                ? "Not provided"
                : value;
    }
}