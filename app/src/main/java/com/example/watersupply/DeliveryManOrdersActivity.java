package com.example.watersupply;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class DeliveryManOrdersActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private Button btnBack;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private final List<OrderModel> orderList = new ArrayList<>();
    private OrderAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_man_orders);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        recyclerView = findViewById(R.id.recyclerViewDeliveryOrders);
        tvEmpty = findViewById(R.id.tvEmpty);
        btnBack = findViewById(R.id.btnBack);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new OrderAdapter(orderList);
        recyclerView.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());

        loadAssignedOrders();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAssignedOrders();
    }

    private void loadAssignedOrders() {

        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this,
                    "Please login again.",
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String deliveryManId = mAuth.getCurrentUser().getUid();

        db.collection("orders")
                .whereEqualTo("deliveryManId", deliveryManId)
                .whereIn("orderStatus", java.util.Arrays.asList(
                        "Accepted",
                        "Preparing",
                        "Out for Delivery"
                ))
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    orderList.clear();

                    for (DocumentSnapshot document : queryDocumentSnapshots) {

                        OrderModel order = new OrderModel();

                        order.orderId = document.getId();
                        order.customerId =
                                document.getString("customerId");

                        order.customerName =
                                document.getString("customerName");

                        order.phone =
                                document.getString("phone");

                        order.address =
                                document.getString("address");

                        order.waterType =
                                document.getString("waterType");

                        order.orderStatus =
                                document.getString("orderStatus");

                        order.paymentStatus =
                                document.getString("paymentStatus");

                        order.paymentMethod =
                                document.getString("paymentMethod");

                        order.deliveryTime =
                                document.getString("deliveryTime");

                        Long quantity =
                                document.getLong("quantity");

                        Double total =
                                document.getDouble("totalAmount");

                        order.quantity =
                                quantity != null ? quantity : 0;

                        order.totalAmount =
                                total != null ? total : 0;

                        orderList.add(order);
                    }

                    adapter.notifyDataSetChanged();

                    if (orderList.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        tvEmpty.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(this,
                            "Failed to load assigned orders.",
                            Toast.LENGTH_LONG).show();
                });
    }

    private void markDelivered(OrderModel order) {

        db.collection("orders")
                .document(order.orderId)
                .update(
                        "orderStatus", "Delivered",
                        "deliveredAt",
                        com.google.firebase.firestore.FieldValue.serverTimestamp()
                )
                .addOnSuccessListener(unused -> {

                    NotificationHelper notificationHelper =
                            new NotificationHelper();

                    notificationHelper.sendCustomerNotification(
                            order.customerId,
                            "Order Delivered",
                            "Order Delivered",
                            "Your water order has been delivered successfully."
                    );

                    Toast.makeText(this,
                            "Order marked as delivered!",
                            Toast.LENGTH_SHORT).show();

                    loadAssignedOrders();
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(this,
                            "Failed to mark order as delivered.",
                            Toast.LENGTH_LONG).show();
                });
    }

    // ---------------------------------------------------------
    // ORDER MODEL
    // ---------------------------------------------------------

    private static class OrderModel {

        String orderId;
        String customerId;
        String customerName;
        String phone;
        String address;
        String waterType;
        String orderStatus;
        String paymentStatus;
        String paymentMethod;
        String deliveryTime;

        long quantity;
        double totalAmount;
    }

    // ---------------------------------------------------------
    // ADAPTER
    // ---------------------------------------------------------

    private class OrderAdapter
            extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

        private final List<OrderModel> orders;

        OrderAdapter(List<OrderModel> orders) {
            this.orders = orders;
        }

        @NonNull
        @Override
        public OrderViewHolder onCreateViewHolder(
                @NonNull ViewGroup parent,
                int viewType) {

            View view = LayoutInflater.from(parent.getContext())
                    .inflate(
                            R.layout.item_delivery_man_order,
                            parent,
                            false
                    );

            return new OrderViewHolder(view);
        }

        @Override
        public void onBindViewHolder(
                @NonNull OrderViewHolder holder,
                int position) {

            OrderModel order = orders.get(position);

            holder.tvCustomer.setText(
                    "Customer: " +
                            safe(order.customerName)
            );

            holder.tvPhone.setText(
                    "Phone: " +
                            safe(order.phone)
            );

            holder.tvAddress.setText(
                    "Address: " +
                            safe(order.address)
            );

            holder.tvWater.setText(
                    "Water: " +
                            safe(order.waterType)
            );

            holder.tvQuantity.setText(
                    "Quantity: " +
                            order.quantity
            );

            holder.tvAmount.setText(
                    "Amount: ₹" +
                            String.format("%.2f", order.totalAmount)
            );

            holder.tvPayment.setText(
                    "Payment: " +
                            safe(order.paymentMethod) +
                            " (" +
                            safe(order.paymentStatus) +
                            ")"
            );

            holder.tvDeliveryTime.setText(
                    "Delivery Time: " +
                            safe(order.deliveryTime)
            );

            holder.tvStatus.setText(
                    "Status: " +
                            safe(order.orderStatus)
            );

            holder.btnDelivered.setOnClickListener(v ->
                    markDelivered(order)
            );
        }

        @Override
        public int getItemCount() {
            return orders.size();
        }

        private String safe(String value) {
            return value == null || value.trim().isEmpty()
                    ? "Not available"
                    : value;
        }

        class OrderViewHolder extends RecyclerView.ViewHolder {

            TextView tvCustomer;
            TextView tvPhone;
            TextView tvAddress;
            TextView tvWater;
            TextView tvQuantity;
            TextView tvAmount;
            TextView tvPayment;
            TextView tvDeliveryTime;
            TextView tvStatus;
            Button btnDelivered;

            OrderViewHolder(@NonNull View itemView) {
                super(itemView);

                tvCustomer =
                        itemView.findViewById(R.id.tvDeliveryCustomer);

                tvPhone =
                        itemView.findViewById(R.id.tvDeliveryPhone);

                tvAddress =
                        itemView.findViewById(R.id.tvDeliveryAddress);

                tvWater =
                        itemView.findViewById(R.id.tvDeliveryWater);

                tvQuantity =
                        itemView.findViewById(R.id.tvDeliveryQuantity);

                tvAmount =
                        itemView.findViewById(R.id.tvDeliveryAmount);

                tvPayment =
                        itemView.findViewById(R.id.tvDeliveryPayment);

                tvDeliveryTime =
                        itemView.findViewById(R.id.tvDeliveryTime);

                tvStatus =
                        itemView.findViewById(R.id.tvDeliveryStatus);

                btnDelivered =
                        itemView.findViewById(R.id.btnMarkDelivered);
            }
        }
    }
}