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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class MyMonthlyPackageActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private ProgressBar progressBar;

    private LinearLayout noPackageContainer;
    private LinearLayout packageContainer;

    private TextView tvWaterType;
    private TextView tvQuantity;
    private TextView tvPricePerUnit;
    private TextView tvDailyAmount;
    private TextView tvDeliveryTime;
    private TextView tvPackageStatus;
    private TextView tvMonthlyAmount;

    private Button btnCreatePackage;
    private Button btnManageDeliveryDays;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_monthly_package);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        progressBar = findViewById(R.id.progressBar);

        noPackageContainer =
                findViewById(R.id.noPackageContainer);

        packageContainer =
                findViewById(R.id.packageContainer);

        tvWaterType =
                findViewById(R.id.tvWaterType);

        tvQuantity =
                findViewById(R.id.tvQuantity);

        tvPricePerUnit =
                findViewById(R.id.tvPricePerUnit);

        tvDailyAmount =
                findViewById(R.id.tvDailyAmount);

        tvDeliveryTime =
                findViewById(R.id.tvDeliveryTime);

        tvPackageStatus =
                findViewById(R.id.tvPackageStatus);

        tvMonthlyAmount =
                findViewById(R.id.tvMonthlyAmount);

        btnCreatePackage =
                findViewById(R.id.btnCreatePackage);

        btnManageDeliveryDays =
                findViewById(R.id.btnManageDeliveryDays);

        // Back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Create package
        btnCreatePackage.setOnClickListener(v -> {

            Intent intent = new Intent(
                    MyMonthlyPackageActivity.this,
                    MonthlyPackageActivity.class
            );

            startActivity(intent);
        });

        // Step 20.3 will implement this
        btnManageDeliveryDays.setOnClickListener(v -> {

            Intent intent = new Intent(
                    MyMonthlyPackageActivity.this,
                    DeliveryCalendarActivity.class
            );

            startActivity(intent);

        });

        loadActivePackage();
    }

    @Override
    protected void onResume() {
        super.onResume();

        loadActivePackage();
    }

    private void loadActivePackage() {

        if (auth.getCurrentUser() == null) {

            showNoPackage();

            Toast.makeText(
                    this,
                    "Please login again.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String customerId =
                auth.getCurrentUser().getUid();

        progressBar.setVisibility(View.VISIBLE);

        noPackageContainer.setVisibility(View.GONE);
        packageContainer.setVisibility(View.GONE);

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

                    progressBar.setVisibility(View.GONE);

                    if (querySnapshot.isEmpty()) {

                        showNoPackage();

                        return;
                    }

                    displayPackage(
                            querySnapshot
                                    .getDocuments()
                                    .get(0)
                    );
                })
                .addOnFailureListener(e -> {

                    progressBar.setVisibility(View.GONE);

                    showNoPackage();

                    Toast.makeText(
                            this,
                            "Failed to load monthly package.",
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private void displayPackage(
            com.google.firebase.firestore.DocumentSnapshot document) {

        String waterType =
                document.getString("waterType");

        Long quantity =
                document.getLong("quantity");

        Double pricePerUnit =
                document.getDouble("pricePerUnit");

        String deliveryTime =
                document.getString("deliveryTime");

        String packageStatus =
                document.getString("packageStatus");

        if (waterType == null || waterType.isEmpty()) {
            waterType = "Not available";
        }

        if (quantity == null) {
            quantity = 0L;
        }

        if (pricePerUnit == null) {
            pricePerUnit = 0.0;
        }

        if (deliveryTime == null || deliveryTime.isEmpty()) {
            deliveryTime = "Anytime";
        }

        if (packageStatus == null || packageStatus.isEmpty()) {
            packageStatus = "Active";
        }

        double dailyAmount =
                quantity * pricePerUnit;

        double monthlyAmount =
                dailyAmount * 30;

        tvWaterType.setText(waterType);

        tvQuantity.setText(
                quantity + " units per day"
        );

        tvPricePerUnit.setText(
                "₹" + formatAmount(pricePerUnit)
        );

        tvDailyAmount.setText(
                "₹" + formatAmount(dailyAmount)
        );

        tvDeliveryTime.setText(
                deliveryTime
        );

        tvPackageStatus.setText(
                packageStatus
        );

        tvMonthlyAmount.setText(
                "₹" + formatAmount(monthlyAmount)
        );

        noPackageContainer.setVisibility(
                View.GONE
        );

        packageContainer.setVisibility(
                View.VISIBLE
        );
    }

    private void showNoPackage() {

        packageContainer.setVisibility(
                View.GONE
        );

        noPackageContainer.setVisibility(
                View.VISIBLE
        );
    }

    private String formatAmount(double amount) {

        if (amount == Math.floor(amount)) {

            return String.valueOf(
                    (long) amount
            );
        }

        return String.format(
                java.util.Locale.getDefault(),
                "%.2f",
                amount
        );
    }
}