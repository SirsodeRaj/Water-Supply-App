package com.example.watersupply;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class CustomerDashboardActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private TextView tvWelcome;
    private ImageButton btnProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_dashboard);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        tvWelcome = findViewById(R.id.tvWelcome);

        // Load customer details
        loadCustomerDetails();

        // Profile
        btnProfile = findViewById(R.id.btnProfile);

        btnProfile.setOnClickListener(v -> {

            Intent intent = new Intent(
                    CustomerDashboardActivity.this,
                    CustomerProfileActivity.class
            );

            startActivity(intent);
        });

        // Place New Order
        findViewById(R.id.btnPlaceOrder).setOnClickListener(v -> {

            Intent intent = new Intent(
                    CustomerDashboardActivity.this,
                    PlaceOrderActivity.class
            );

            startActivity(intent);
        });

        // My Orders
        findViewById(R.id.btnOrderHistory).setOnClickListener(v -> {

            Intent intent = new Intent(
                    CustomerDashboardActivity.this,
                    CustomerOrdersActivity.class
            );

            startActivity(intent);
        });

        // Monthly Package
        findViewById(R.id.btnMonthlyPackage).setOnClickListener(v -> {

            Intent intent = new Intent(
                    CustomerDashboardActivity.this,
                    MyMonthlyPackageActivity.class
            );

            startActivity(intent);
        });

        // Bills
        findViewById(R.id.btnBills).setOnClickListener(v -> {

            Intent intent = new Intent(
                    CustomerDashboardActivity.this,
                    CustomerCurrentBillActivity.class
            );

            startActivity(intent);
        });


        // Generate today's monthly package order
        generateMonthlyOrderIfRequired();
    }

    @Override
    protected void onResume() {
        super.onResume();

        generateMonthlyOrderIfRequired();

        checkMonthlyBill();
    }

    private void loadCustomerDetails() {

        if (mAuth.getCurrentUser() == null) {

            Toast.makeText(
                    this,
                    "Session expired. Please login again.",
                    Toast.LENGTH_LONG
            ).show();

            finish();
            return;
        }

        String uid = mAuth.getCurrentUser().getUid();

        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    if (documentSnapshot.exists()) {

                        String name =
                                documentSnapshot.getString("name");

                        if (name != null &&
                                !name.trim().isEmpty()) {

                            tvWelcome.setText(
                                    "Welcome, " + name + "! 👋"
                            );
                        }

                    } else {

                        Toast.makeText(
                                this,
                                "Customer profile not found.",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            this,
                            "Unable to load customer profile.",
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private void generateMonthlyOrderIfRequired() {

        // Make sure a customer is logged in.
        if (mAuth.getCurrentUser() == null) {
            return;
        }

        MonthlyOrderGenerator generator =
                new MonthlyOrderGenerator();

        generator.generateTodayOrder(
                new MonthlyOrderGenerator.OnOrderGenerationListener() {

                    @Override
                    public void onCreated() {

                        // Today's monthly package order
                        // has been created successfully.
                    }

                    @Override
                    public void onAlreadyExists() {

                        // Today's monthly package order
                        // already exists.
                    }

                    @Override
                    public void onDeliveryOff() {

                        // Customer has turned today's
                        // delivery OFF.
                    }

                    @Override
                    public void onNoPackage() {

                        // Customer does not have
                        // an active monthly package.
                    }

                    @Override
                    public void onFailure(String message) {

                        // This is a background operation,
                        // so we don't show an error popup
                        // on the dashboard.
                    }
                }
        );
    }

    private void checkMonthlyBill() {

        if (mAuth.getCurrentUser() == null) {
            return;
        }

        MonthlyBillGenerator generator =
                new MonthlyBillGenerator();

        generator.generateCurrentMonthBill(
                new MonthlyBillGenerator.OnBillGenerationListener() {

                    @Override
                    public void onBillGenerated(
                            double totalAmount
                    ) {

                        // Bill generated successfully.
                    }

                    @Override
                    public void onAlreadyExists(
                            double totalAmount
                    ) {

                        // Bill already exists.
                    }

                    @Override
                    public void onNoPackage() {

                        // No active monthly package.
                    }

                    @Override
                    public void onFailure(
                            String message
                    ) {

                        // Do not interrupt dashboard.
                    }
                }
        );
    }
}