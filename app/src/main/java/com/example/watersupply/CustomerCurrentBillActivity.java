package com.example.watersupply;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Locale;

public class CustomerCurrentBillActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private TextView tvMonth;
    private TextView tvPackage;
    private TextView tvDeliveryDays;
    private TextView tvDeliveredDays;
    private TextView tvTotalAmount;
    private TextView tvPaymentStatus;
    private TextView tvBillStatus;

    private Button btnBack;
    private Button btnBillHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_current_bill);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        tvMonth = findViewById(R.id.tvMonth);
        tvPackage = findViewById(R.id.tvPackage);
        tvDeliveryDays = findViewById(R.id.tvDeliveryDays);
        tvDeliveredDays = findViewById(R.id.tvDeliveredDays);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        tvPaymentStatus = findViewById(R.id.tvPaymentStatus);
        tvBillStatus = findViewById(R.id.tvBillStatus);

        btnBack = findViewById(R.id.btnBack);
        btnBillHistory = findViewById(R.id.btnBillHistory);

        btnBack.setOnClickListener(v -> finish());

        btnBillHistory.setOnClickListener(v -> {

            Intent intent = new Intent(
                    CustomerCurrentBillActivity.this,
                    CustomerBillHistoryActivity.class
            );

            startActivity(intent);
        });

        loadCurrentBill();
    }

    private void loadCurrentBill() {

        if (mAuth.getCurrentUser() == null) {

            Toast.makeText(
                    this,
                    "Session expired. Please login again.",
                    Toast.LENGTH_LONG
            ).show();

            finish();
            return;
        }

        String customerId =
                mAuth.getCurrentUser().getUid();

        db.collection("monthlyBills")
                .whereEqualTo(
                        "customerId",
                        customerId
                )
                .orderBy(
                        "timestamp",
                        Query.Direction.DESCENDING
                )
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    if (querySnapshot.isEmpty()) {

                        showNoBill();

                        return;
                    }

                    com.google.firebase.firestore.DocumentSnapshot document =
                            querySnapshot.getDocuments().get(0);

                    displayBill(document);
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            this,
                            "Unable to load current bill.",
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private void displayBill(
            com.google.firebase.firestore.DocumentSnapshot document
    ) {

        String month =
                document.getString("month");

        String paymentStatus =
                document.getString("paymentStatus");

        String billStatus =
                document.getString("billStatus");

        String packageId =
                document.getString("monthlyPackageId");

        Long totalDeliveryDays =
                document.getLong("totalDeliveryDays");

        Long deliveredDays =
                document.getLong("deliveredDays");

        Double totalAmount =
                document.getDouble("totalAmount");

        if (month == null) {
            month = "N/A";
        }

        if (paymentStatus == null) {
            paymentStatus = "Pending";
        }

        if (billStatus == null) {
            billStatus = "N/A";
        }

        if (packageId == null) {
            packageId = "N/A";
        }

        if (totalDeliveryDays == null) {
            totalDeliveryDays = 0L;
        }

        if (deliveredDays == null) {
            deliveredDays = 0L;
        }

        if (totalAmount == null) {
            totalAmount = 0.0;
        }

        tvMonth.setText(
                "Month: " + month
        );

        tvPackage.setText(
                "Monthly Package: " + packageId
        );

        tvDeliveryDays.setText(
                "Scheduled Delivery Days: "
                        + totalDeliveryDays
        );

        tvDeliveredDays.setText(
                "Delivered Days: "
                        + deliveredDays
        );

        tvTotalAmount.setText(
                String.format(
                        Locale.getDefault(),
                        "₹%.2f",
                        totalAmount
                )
        );

        tvPaymentStatus.setText(
                "Payment Status: "
                        + paymentStatus
        );

        tvBillStatus.setText(
                "Bill Status: "
                        + billStatus
        );
    }

    private void showNoBill() {

        tvMonth.setText(
                "Month: No bill available"
        );

        tvPackage.setText(
                "Monthly Package: -"
        );

        tvDeliveryDays.setText(
                "Scheduled Delivery Days: 0"
        );

        tvDeliveredDays.setText(
                "Delivered Days: 0"
        );

        tvTotalAmount.setText(
                "₹0.00"
        );

        tvPaymentStatus.setText(
                "Payment Status: No Bill"
        );

        tvBillStatus.setText(
                "Bill Status: Not Generated"
        );
    }
}