package com.example.watersupply;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class AdminDashboardActivity
        extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private ImageButton btnProfile;
    private ImageButton btnNotifications;

    private LinearLayout cardNewOrders;
    private LinearLayout cardCustomers;
    private LinearLayout cardOrders;
    private LinearLayout cardBills;
    private LinearLayout cardPayments;


    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_admin_dashboard
        );


        // ==========================================
        // FIREBASE
        // ==========================================

        mAuth =
                FirebaseAuth.getInstance();


        // ==========================================
        // TOP BAR
        // ==========================================

        btnProfile =
                findViewById(
                        R.id.btnProfile
                );

        btnNotifications =
                findViewById(
                        R.id.btnNotifications
                );


        // ==========================================
        // DASHBOARD CARDS
        // ==========================================

        cardNewOrders =
                findViewById(
                        R.id.cardNewOrders
                );

        cardCustomers =
                findViewById(
                        R.id.cardCustomers
                );

        cardOrders =
                findViewById(
                        R.id.cardOrders
                );

        cardBills =
                findViewById(
                        R.id.cardBills
                );

        cardPayments =
                findViewById(
                        R.id.cardPayments
                );


        // ==========================================
        // PROFILE
        // ==========================================

        btnProfile.setOnClickListener(v -> {

            startActivity(
                    new Intent(
                            AdminDashboardActivity.this,
                            AdminProfileActivity.class
                    )
            );

        });


        // ==========================================
        // NOTIFICATIONS
        // ==========================================

        btnNotifications.setOnClickListener(v -> {
            Intent intent = new Intent(
                    AdminDashboardActivity.this,
                    AdminNotificationsActivity.class
            );

            startActivity(intent);
        });


        // ==========================================
        // NEW ORDERS
        // ==========================================

        cardNewOrders.setOnClickListener(v -> {

            startActivity(
                    new Intent(
                            AdminDashboardActivity.this,
                            NewOrdersActivity.class
                    )
            );

        });


        // ==========================================
        // CUSTOMERS
        // ==========================================

        cardCustomers.setOnClickListener(v -> {

            startActivity(
                    new Intent(
                            AdminDashboardActivity.this,
                            AdminCustomersActivity.class
                    )
            );

        });


        // ==========================================
        // ALL ORDERS
        // ==========================================

        cardOrders.setOnClickListener(v -> {

            startActivity(
                    new Intent(
                            AdminDashboardActivity.this,
                            AdminOrdersActivity.class
                    )
            );

        });


        // ==========================================
        // MONTHLY BILLS
        // ==========================================

        cardBills.setOnClickListener(v -> {
            Intent intent = new Intent(
                    AdminDashboardActivity.this,
                    AdminMonthlyBillsActivity.class
            );
            startActivity(intent);
        });


        // ==========================================
        // PAYMENTS
        // ==========================================

        cardPayments.setOnClickListener(v -> {
            Intent intent = new Intent(
                    AdminDashboardActivity.this,
                    AdminPaymentsActivity.class
            );
            startActivity(intent);
        });
    }
}