package com.example.watersupply;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class DeliveryManDashboardActivity extends AppCompatActivity {

    private Button btnOrders, btnLogout, btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_man_dashboard);

        btnOrders = findViewById(R.id.btnDeliveryOrders);
        btnLogout = findViewById(R.id.btnDeliveryLogout);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        btnOrders.setOnClickListener(v -> {
            Intent intent = new Intent(
                    DeliveryManDashboardActivity.this,
                    DeliveryManOrdersActivity.class
            );
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            com.google.firebase.auth.FirebaseAuth
                    .getInstance()
                    .signOut();

            Intent intent = new Intent(
                    DeliveryManDashboardActivity.this,
                    MainActivity.class
            );

            intent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
            );

            startActivity(intent);
            finish();
        });
    }
}