package com.example.watersupply;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CustomerOrdersActivity
        extends AppCompatActivity {


    private FirebaseAuth mAuth;

    private FirebaseFirestore db;


    private LinearLayout ordersContainer;

    private ProgressBar progressBar;

    private TextView tvNoOrders;


    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {

        super.onCreate(
                savedInstanceState
        );


        setContentView(
                R.layout.activity_customer_orders
        );


        // ==========================================
        // FIREBASE
        // ==========================================

        mAuth =
                FirebaseAuth.getInstance();

        db =
                FirebaseFirestore.getInstance();


        // ==========================================
        // VIEWS
        // ==========================================

        ordersContainer =
                findViewById(
                        R.id.ordersContainer
                );

        progressBar =
                findViewById(
                        R.id.progressBar
                );

        tvNoOrders =
                findViewById(
                        R.id.tvNoOrders
                );


        // ==========================================
        // BACK
        // ==========================================

        findViewById(
                R.id.btnBack
        ).setOnClickListener(
                v -> finish()
        );


        // ==========================================
        // LOAD
        // ==========================================

        loadCustomerOrders();
    }


    // ==========================================
    // LOAD CUSTOMER ORDERS
    // ==========================================

    private void loadCustomerOrders() {

        if (
                mAuth.getCurrentUser() == null
        ) {

            Toast.makeText(
                    this,
                    "Session expired. Please login again.",
                    Toast.LENGTH_LONG
            ).show();

            finish();

            return;
        }


        String customerId =
                mAuth.getCurrentUser()
                        .getUid();


        progressBar.setVisibility(
                View.VISIBLE
        );


        db.collection("orders")
                .whereEqualTo(
                        "customerId",
                        customerId
                )
                .orderBy(
                        "timestamp",
                        Query.Direction.DESCENDING
                )
                .addSnapshotListener(
                        (snapshots, error) -> {


                            progressBar.setVisibility(
                                    View.GONE
                            );


                            // ======================================
                            // ERROR
                            // ======================================

                            if (error != null) {

                                Toast.makeText(
                                        this,
                                        "Failed to load your orders.",
                                        Toast.LENGTH_LONG
                                ).show();

                                return;
                            }


                            ordersContainer.removeAllViews();


                            // ======================================
                            // EMPTY
                            // ======================================

                            if (
                                    snapshots == null
                                            || snapshots.isEmpty()
                            ) {

                                tvNoOrders.setVisibility(
                                        View.VISIBLE
                                );

                                return;
                            }


                            tvNoOrders.setVisibility(
                                    View.GONE
                            );


                            // ======================================
                            // DISPLAY
                            // ======================================

                            for (
                                    DocumentSnapshot document :
                                    snapshots.getDocuments()
                            ) {

                                addOrderCard(
                                        document
                                );
                            }
                        }
                );
    }


    // ==========================================
    // CREATE ORDER CARD
    // ==========================================

    private void addOrderCard(
            DocumentSnapshot document
    ) {

        View orderView =
                getLayoutInflater()
                        .inflate(
                                R.layout.item_customer_order,
                                ordersContainer,
                                false
                        );


        // ==========================================
        // VIEWS
        // ==========================================

        TextView tvOrderId =
                orderView.findViewById(
                        R.id.tvOrderId
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

        TextView tvStatus =
                orderView.findViewById(
                        R.id.tvStatus
                );

        Button btnCancelOrder =
                orderView.findViewById(
                        R.id.btnCancelOrder
                );


        // ==========================================
        // DATA
        // ==========================================

        String orderId =
                document.getId();


        String waterType =
                document.getString(
                        "waterType"
                );


        String paymentMethod =
                document.getString(
                        "paymentMethod"
                );


        String paymentStatus =
                document.getString(
                        "paymentStatus"
                );


        String orderType =
                document.getString(
                        "orderType"
                );


        String orderStatus =
                document.getString(
                        "orderStatus"
                );


        String deliveryTime =
                document.getString(
                        "deliveryTime"
                );


        Long quantity =
                document.getLong(
                        "quantity"
                );


        Long totalAmount =
                document.getLong(
                        "totalAmount"
                );


        // ==========================================
        // ORDER ID
        // ==========================================

        String shortOrderId =
                orderId;


        if (
                orderId.length() > 8
        ) {

            shortOrderId =
                    orderId.substring(
                            0,
                            8
                    );
        }


        tvOrderId.setText(
                "Order ID: #"
                        + shortOrderId
        );


        // ==========================================
        // WATER
        // ==========================================

        tvWater.setText(
                safe(waterType)
                        + " Water × "
                        + (
                        quantity != null
                                ? quantity
                                : 0
                )
        );


        // ==========================================
        // TOTAL
        // ==========================================

        tvTotal.setText(
                "Total: ₹"
                        + (
                        totalAmount != null
                                ? totalAmount
                                : 0
                )
        );


        // ==========================================
        // PAYMENT
        // ==========================================

        tvPayment.setText(
                "Payment: "
                        + safe(paymentMethod)
                        + " ("
                        + safe(paymentStatus)
                        + ")"
        );


        // ==========================================
        // ORDER TYPE
        // ==========================================

        tvOrderType.setText(
                "Order Type: "
                        + safe(orderType)
        );


        // ==========================================
        // DATE
        // ==========================================

        if (
                document.getTimestamp(
                        "timestamp"
                ) != null
        ) {

            Date date =
                    document.getTimestamp(
                            "timestamp"
                    ).toDate();


            SimpleDateFormat formatter =
                    new SimpleDateFormat(
                            "dd MMM yyyy, hh:mm a",
                            Locale.getDefault()
                    );


            tvDate.setText(
                    "Date: "
                            + formatter.format(
                            date
                    )
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
                deliveryTime != null
                        && !deliveryTime.trim().isEmpty()
        ) {

            tvDate.setText(
                    tvDate.getText()
                            + "\nDelivery Time: "
                            + deliveryTime
            );
        }


        // ==========================================
        // STATUS
        // ==========================================

        tvStatus.setText(
                "Status: "
                        + safe(orderStatus)
        );


        // ==========================================
        // STATUS COLOR
        // ==========================================

        setStatusColor(
                tvStatus,
                orderStatus
        );


        // ==========================================
        // CANCEL BUTTON
        // ==========================================

        if (
                "Pending".equals(
                        orderStatus
                )
        ) {

            btnCancelOrder.setVisibility(
                    View.VISIBLE
            );


            btnCancelOrder.setOnClickListener(
                    v ->
                            showCancelConfirmation(
                                    document.getId()
                            )
            );

        } else {

            btnCancelOrder.setVisibility(
                    View.GONE
            );
        }


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
            TextView tvStatus,
            String status
    ) {

        if (status == null) {

            tvStatus.setTextColor(
                    Color.DKGRAY
            );

            return;
        }


        switch (status) {

            case "Accepted":

                tvStatus.setTextColor(
                        Color.rgb(
                                25,
                                118,
                                210
                        )
                );

                break;


            case "Preparing":

                tvStatus.setTextColor(
                        Color.rgb(
                                245,
                                124,
                                0
                        )
                );

                break;


            case "Out for Delivery":

                tvStatus.setTextColor(
                        Color.rgb(
                                123,
                                31,
                                162
                        )
                );

                break;


            case "Delivered":

                tvStatus.setTextColor(
                        Color.rgb(
                                46,
                                125,
                                50
                        )
                );

                break;


            case "Cancelled":

                tvStatus.setTextColor(
                        Color.rgb(
                                211,
                                47,
                                47
                        )
                );

                break;


            case "Rejected":

                tvStatus.setTextColor(
                        Color.rgb(
                                211,
                                47,
                                47
                        )
                );

                break;


            default:

                tvStatus.setTextColor(
                        Color.DKGRAY
                );

                break;
        }
    }


    // ==========================================
    // CANCEL CONFIRMATION
    // ==========================================

    private void showCancelConfirmation(
            String orderId
    ) {

        new AlertDialog.Builder(
                this
        )
                .setTitle(
                        "Cancel Order"
                )
                .setMessage(
                        "Are you sure you want to cancel this order?"
                )
                .setPositiveButton(
                        "CANCEL ORDER",
                        (dialog, which) ->
                                cancelOrder(
                                        orderId
                                )
                )
                .setNegativeButton(
                        "KEEP ORDER",
                        null
                )
                .show();
    }


    // ==========================================
    // CANCEL ORDER
    // ==========================================

    private void cancelOrder(
            String orderId
    ) {

        if (
                mAuth.getCurrentUser() == null
        ) {

            Toast.makeText(
                    this,
                    "Session expired. Please login again.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }


        String customerId =
                mAuth.getCurrentUser()
                        .getUid();


        db.collection("orders")
                .document(orderId)
                .get()
                .addOnSuccessListener(
                        documentSnapshot -> {


                            if (
                                    !documentSnapshot.exists()
                            ) {

                                Toast.makeText(
                                        this,
                                        "Order not found.",
                                        Toast.LENGTH_LONG
                                ).show();

                                return;
                            }


                            String orderCustomerId =
                                    documentSnapshot.getString(
                                            "customerId"
                                    );


                            String currentStatus =
                                    documentSnapshot.getString(
                                            "orderStatus"
                                    );


                            // ======================================
                            // SECURITY CHECK
                            // ======================================

                            if (
                                    !customerId.equals(
                                            orderCustomerId
                                    )
                            ) {

                                Toast.makeText(
                                        this,
                                        "You cannot cancel this order.",
                                        Toast.LENGTH_LONG
                                ).show();

                                return;
                            }


                            // ======================================
                            // ONLY PENDING
                            // ======================================

                            if (
                                    !"Pending".equals(
                                            currentStatus
                                    )
                            ) {

                                Toast.makeText(
                                        this,
                                        "This order can no longer be cancelled.",
                                        Toast.LENGTH_LONG
                                ).show();

                                return;
                            }


                            // ======================================
                            // CANCEL
                            // ======================================

                            db.collection("orders")
                                    .document(orderId)
                                    .update(
                                            "orderStatus",
                                            "Cancelled",

                                            "statusUpdatedBy",
                                            "customer"
                                    )
                                    .addOnSuccessListener(
                                            unused -> {

                                                Toast.makeText(
                                                        this,
                                                        "Order cancelled successfully!",
                                                        Toast.LENGTH_LONG
                                                ).show();

                                            }
                                    )
                                    .addOnFailureListener(
                                            e -> {

                                                Toast.makeText(
                                                        this,
                                                        "Failed to cancel order.",
                                                        Toast.LENGTH_LONG
                                                ).show();

                                            }
                                    );

                        }
                )
                .addOnFailureListener(
                        e -> {

                            Toast.makeText(
                                    this,
                                    "Unable to verify order.",
                                    Toast.LENGTH_LONG
                            ).show();

                        }
                );
    }


    // ==========================================
    // SAFE STRING
    // ==========================================

    private String safe(
            String value
    ) {

        return value == null
                || value.trim().isEmpty()
                ? "Not provided"
                : value;
    }
}