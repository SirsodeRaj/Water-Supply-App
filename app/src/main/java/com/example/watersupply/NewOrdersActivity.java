package com.example.watersupply;

import android.content.Intent;
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

public class NewOrdersActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private LinearLayout ordersContainer;
    private ProgressBar progressBar;
    private TextView tvNoOrders;

    private NotificationHelper notificationHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_new_orders);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        notificationHelper = new NotificationHelper();

        ordersContainer = findViewById(R.id.ordersContainer);
        progressBar = findViewById(R.id.progressBar);
        tvNoOrders = findViewById(R.id.tvNoOrders);

        findViewById(R.id.btnBack)
                .setOnClickListener(v -> finish());

        loadOrders();
    }

    private void loadOrders() {

        if (auth.getCurrentUser() == null) {

            Toast.makeText(
                    this,
                    "Admin session expired. Please login again.",
                    Toast.LENGTH_LONG
            ).show();

            finish();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        db.collection("orders")
                .whereIn(
                        "orderStatus",
                        java.util.Arrays.asList(
                                "Pending",
                                "Accepted",
                                "Preparing",
                                "Out for Delivery"
                        )
                )
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

                    if (snapshots == null
                            || snapshots.isEmpty()) {

                        tvNoOrders.setVisibility(View.VISIBLE);
                        return;
                    }

                    tvNoOrders.setVisibility(View.GONE);

                    for (DocumentSnapshot document
                            : snapshots.getDocuments()) {

                        addOrderCard(document);
                    }
                });
    }

    private void addOrderCard(DocumentSnapshot document) {

        View orderView =
                getLayoutInflater().inflate(
                        R.layout.item_admin_order,
                        ordersContainer,
                        false
                );

        TextView tvCustomerName =
                orderView.findViewById(R.id.tvCustomerName);

        TextView tvPhone =
                orderView.findViewById(R.id.tvPhone);

        TextView tvAddress =
                orderView.findViewById(R.id.tvAddress);

        TextView tvWater =
                orderView.findViewById(R.id.tvWater);

        TextView tvTotal =
                orderView.findViewById(R.id.tvTotal);

        TextView tvPayment =
                orderView.findViewById(R.id.tvPayment);

        TextView tvOrderStatus =
                orderView.findViewById(R.id.tvOrderStatus);

        TextView tvDeliveryMan =
                orderView.findViewById(R.id.tvDeliveryMan);

        Button btnAssignDeliveryMan =
                orderView.findViewById(
                        R.id.btnAssignDeliveryMan
                );

        LinearLayout layoutPendingButtons =
                orderView.findViewById(
                        R.id.layoutPendingButtons
                );

        Button btnAccept =
                orderView.findViewById(R.id.btnAccept);

        Button btnReject =
                orderView.findViewById(R.id.btnReject);

        Button btnNextStatus =
                orderView.findViewById(
                        R.id.btnNextStatus
                );

        // ------------------------------------------
        // FIRESTORE DATA
        // ------------------------------------------

        String customerId =
                document.getString("customerId");

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

        String orderStatus =
                document.getString("orderStatus");

        String deliveryManName =
                document.getString("deliveryManName");

        Long quantity =
                document.getLong("quantity");

        Long totalAmount =
                document.getLong("totalAmount");

        // ------------------------------------------
        // DISPLAY
        // ------------------------------------------

        tvCustomerName.setText(
                "Customer: " + safe(customerName)
        );

        tvPhone.setText(
                "Phone: " + safe(phone)
        );

        tvAddress.setText(
                "Address: " + safe(address)
        );

        tvWater.setText(
                "Water: "
                        + safe(waterType)
                        + " × "
                        + (quantity != null
                        ? quantity
                        : 0)
        );

        tvTotal.setText(
                "Total: ₹"
                        + (totalAmount != null
                        ? totalAmount
                        : 0)
        );

        tvPayment.setText(
                "Payment: "
                        + safe(paymentMethod)
                        + " ("
                        + safe(paymentStatus)
                        + ")"
        );

        tvOrderStatus.setText(
                "Status: "
                        + safe(orderStatus)
        );

        tvDeliveryMan.setText(
                "Delivery Man: "
                        + safe(deliveryManName)
        );

        // ------------------------------------------
        // ASSIGN DELIVERY MAN
        // ------------------------------------------

        if (deliveryManName == null
                || deliveryManName.trim().isEmpty()) {

            btnAssignDeliveryMan.setText(
                    "ASSIGN DELIVERY MAN"
            );

        } else {

            btnAssignDeliveryMan.setText(
                    "CHANGE DELIVERY MAN"
            );
        }

        btnAssignDeliveryMan.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            NewOrdersActivity.this,
                            AssignDeliveryManActivity.class
                    );

            intent.putExtra(
                    "orderId",
                    document.getId()
            );

            startActivity(intent);
        });

        // ------------------------------------------
        // PENDING
        // ------------------------------------------

        if ("Pending".equals(orderStatus)) {

            layoutPendingButtons.setVisibility(
                    View.VISIBLE
            );

            btnNextStatus.setVisibility(
                    View.GONE
            );

            btnAccept.setOnClickListener(v ->
                    updateOrderStatus(
                            document.getId(),
                            "Accepted",
                            customerId
                    )
            );

            btnReject.setOnClickListener(v ->
                    updateOrderStatus(
                            document.getId(),
                            "Rejected",
                            customerId
                    )
            );
        }

        // ------------------------------------------
        // OTHER STATUSES
        // ------------------------------------------

        else {

            layoutPendingButtons.setVisibility(
                    View.GONE
            );

            // Accepted → Preparing
            if ("Accepted".equals(orderStatus)) {

                btnNextStatus.setVisibility(
                        View.VISIBLE
                );

                btnNextStatus.setText(
                        "START PREPARING"
                );

                btnNextStatus.setOnClickListener(v ->
                        updateOrderStatus(
                                document.getId(),
                                "Preparing",
                                customerId
                        )
                );
            }

            // Preparing → Out for Delivery
            else if ("Preparing".equals(orderStatus)) {

                btnNextStatus.setVisibility(
                        View.VISIBLE
                );

                btnNextStatus.setText(
                        "OUT FOR DELIVERY"
                );

                btnNextStatus.setOnClickListener(v -> {

                    String deliveryManId =
                            document.getString(
                                    "deliveryManId"
                            );

                    if (deliveryManId == null
                            || deliveryManId.trim().isEmpty()) {

                        Toast.makeText(
                                this,
                                "Please assign a Delivery Man first.",
                                Toast.LENGTH_LONG
                        ).show();

                        return;
                    }

                    updateOrderStatus(
                            document.getId(),
                            "Out for Delivery",
                            customerId
                    );
                });
            }

            // Out for Delivery → Delivered
            else if ("Out for Delivery"
                    .equals(orderStatus)) {

                btnNextStatus.setVisibility(
                        View.VISIBLE
                );

                btnNextStatus.setText(
                        "MARK DELIVERED"
                );

                btnNextStatus.setOnClickListener(v ->
                        updateOrderStatus(
                                document.getId(),
                                "Delivered",
                                customerId
                        )
                );
            }
        }

        ordersContainer.addView(orderView);
    }

    private void updateOrderStatus(
            String orderId,
            String status,
            String customerId) {

        db.collection("orders")
                .document(orderId)
                .update(
                        "orderStatus",
                        status,
                        "statusUpdatedBy",
                        "admin"
                )
                .addOnSuccessListener(unused -> {

                    String message;

                    switch (status) {

                        case "Accepted":
                            message =
                                    "Order accepted successfully!";
                            break;

                        case "Rejected":
                            message =
                                    "Order rejected successfully!";
                            break;

                        case "Preparing":
                            message =
                                    "Order moved to Preparing!";
                            break;

                        case "Out for Delivery":
                            message =
                                    "Order is out for delivery!";
                            break;

                        case "Delivered":
                            message =
                                    "Order delivered successfully!";
                            break;

                        default:
                            message =
                                    "Order status updated!";
                    }

                    if (customerId != null
                            && !customerId.trim().isEmpty()) {

                        notificationHelper
                                .sendCustomerNotification(
                                        customerId,
                                        "Order Status",
                                        "Order " + status,
                                        "Your water order is now "
                                                + status
                                                + "."
                                );
                    }

                    Toast.makeText(
                            this,
                            message,
                            Toast.LENGTH_SHORT
                    ).show();
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            this,
                            "Failed to update order status.",
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private String safe(String value) {

        return value == null
                || value.trim().isEmpty()
                ? "Not assigned"
                : value;
    }
}