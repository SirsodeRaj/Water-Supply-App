package com.example.watersupply;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class AssignDeliveryManActivity extends AppCompatActivity {

    private Spinner spDeliveryMan;
    private Button btnAssign;
    private Button btnBack;
    private TextView tvOrderInfo;

    private FirebaseFirestore db;

    private String orderId;

    private final List<String> deliveryManNames = new ArrayList<>();
    private final List<String> deliveryManIds = new ArrayList<>();

    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assign_delivery_man);

        db = FirebaseFirestore.getInstance();

        spDeliveryMan = findViewById(R.id.spDeliveryMan);
        btnAssign = findViewById(R.id.btnAssignDeliveryMan);
        btnBack = findViewById(R.id.btnBack);
        tvOrderInfo = findViewById(R.id.tvOrderInfo);

        orderId = getIntent().getStringExtra("orderId");

        if (orderId == null || orderId.trim().isEmpty()) {
            Toast.makeText(
                    this,
                    "Order ID not found.",
                    Toast.LENGTH_LONG
            ).show();

            finish();
            return;
        }

        btnBack.setOnClickListener(v -> finish());

        adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                deliveryManNames
        );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spDeliveryMan.setAdapter(adapter);

        loadOrderDetails();
        loadDeliveryMen();

        btnAssign.setOnClickListener(v -> assignDeliveryMan());
    }

    private void loadOrderDetails() {

        db.collection("orders")
                .document(orderId)
                .get()
                .addOnSuccessListener(document -> {

                    if (!document.exists()) {
                        tvOrderInfo.setText("Order not found.");
                        return;
                    }

                    String customerName =
                            document.getString("customerName");

                    String waterType =
                            document.getString("waterType");

                    Long quantity =
                            document.getLong("quantity");

                    String currentDeliveryMan =
                            document.getString("deliveryManName");

                    String info =
                            "Customer: "
                                    + safe(customerName)
                                    + "\nWater: "
                                    + safe(waterType)
                                    + " × "
                                    + (quantity != null
                                    ? quantity
                                    : 0);

                    if (currentDeliveryMan != null
                            && !currentDeliveryMan.trim().isEmpty()) {

                        info +=
                                "\n\nCurrently Assigned: "
                                        + currentDeliveryMan;
                    }

                    tvOrderInfo.setText(info);
                });
    }

    private void loadDeliveryMen() {

        db.collection("users")
                .whereEqualTo("role", "deliveryMan")
                .orderBy("name", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    deliveryManNames.clear();
                    deliveryManIds.clear();

                    for (DocumentSnapshot document
                            : querySnapshot.getDocuments()) {

                        String name =
                                document.getString("name");

                        if (name == null
                                || name.trim().isEmpty()) {

                            name = "Delivery Man";
                        }

                        deliveryManNames.add(name);
                        deliveryManIds.add(document.getId());
                    }

                    adapter.notifyDataSetChanged();

                    if (deliveryManNames.isEmpty()) {

                        Toast.makeText(
                                this,
                                "No delivery men found.",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            this,
                            "Failed to load delivery men.",
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private void assignDeliveryMan() {

        if (deliveryManIds.isEmpty()) {

            Toast.makeText(
                    this,
                    "No delivery man available.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        int position =
                spDeliveryMan.getSelectedItemPosition();

        if (position < 0
                || position >= deliveryManIds.size()) {

            Toast.makeText(
                    this,
                    "Please select a delivery man.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String deliveryManId =
                deliveryManIds.get(position);

        String deliveryManName =
                deliveryManNames.get(position);

        btnAssign.setEnabled(false);

        db.collection("orders")
                .document(orderId)
                .update(
                        "deliveryManId",
                        deliveryManId,
                        "deliveryManName",
                        deliveryManName
                )
                .addOnSuccessListener(unused -> {

                    Toast.makeText(
                            this,
                            "Delivery Man assigned successfully!",
                            Toast.LENGTH_LONG
                    ).show();

                    finish();
                })
                .addOnFailureListener(e -> {

                    btnAssign.setEnabled(true);

                    Toast.makeText(
                            this,
                            "Failed to assign Delivery Man.",
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private String safe(String value) {

        return value == null
                || value.trim().isEmpty()
                ? "Not provided"
                : value;
    }
}