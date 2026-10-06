package com.example.watersupply;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminCustomersActivity extends AppCompatActivity {

    private FirebaseFirestore db;

    private LinearLayout customersContainer;
    private ProgressBar progressBar;
    private TextView tvNoCustomers;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_admin_customers
        );


        // ==========================================
        // FIREBASE
        // ==========================================

        db =
                FirebaseFirestore.getInstance();


        // ==========================================
        // VIEWS
        // ==========================================

        customersContainer =
                findViewById(
                        R.id.customersContainer
                );

        progressBar =
                findViewById(
                        R.id.progressBar
                );

        tvNoCustomers =
                findViewById(
                        R.id.tvNoCustomers
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
        // LOAD CUSTOMERS
        // ==========================================

        loadCustomers();
    }


    // ==========================================
    // LOAD CUSTOMERS
    // ==========================================

    private void loadCustomers() {

        progressBar.setVisibility(
                View.VISIBLE
        );


        db.collection("users")
                .whereEqualTo(
                        "role",
                        "customer"
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
                                        "Failed to load customers.",
                                        Toast.LENGTH_LONG
                                ).show();

                                return;
                            }


                            // ======================================
                            // CLEAR OLD LIST
                            // ======================================

                            customersContainer.removeAllViews();


                            // ======================================
                            // NO CUSTOMERS
                            // ======================================

                            if (
                                    snapshots == null
                                            || snapshots.isEmpty()
                            ) {

                                tvNoCustomers.setVisibility(
                                        View.VISIBLE
                                );

                                return;
                            }


                            tvNoCustomers.setVisibility(
                                    View.GONE
                            );


                            // ======================================
                            // ADD CUSTOMERS
                            // ======================================

                            for (
                                    DocumentSnapshot document :
                                    snapshots.getDocuments()
                            ) {

                                addCustomerCard(
                                        document
                                );
                            }
                        }
                );
    }


    // ==========================================
    // CUSTOMER CARD
    // ==========================================

    private void addCustomerCard(
            DocumentSnapshot document
    ) {

        View customerView =
                getLayoutInflater()
                        .inflate(
                                R.layout.item_admin_customer,
                                customersContainer,
                                false
                        );


        // ==========================================
        // VIEWS
        // ==========================================

        TextView tvCustomerName =
                customerView.findViewById(
                        R.id.tvCustomerName
                );

        TextView tvEmail =
                customerView.findViewById(
                        R.id.tvEmail
                );

        TextView tvPhone =
                customerView.findViewById(
                        R.id.tvPhone
                );

        TextView tvAddress =
                customerView.findViewById(
                        R.id.tvAddress
                );

        TextView tvAccountType =
                customerView.findViewById(
                        R.id.tvAccountType
                );


        // ==========================================
        // FIRESTORE DATA
        // ==========================================

        String name =
                document.getString(
                        "name"
                );

        String email =
                document.getString(
                        "email"
                );

        String phone =
                document.getString(
                        "phone"
                );

        String address =
                document.getString(
                        "address"
                );

        String role =
                document.getString(
                        "role"
                );


        // ==========================================
        // DISPLAY
        // ==========================================

        tvCustomerName.setText(
                safe(name)
        );


        tvEmail.setText(
                "Email: " +
                        safe(email)
        );


        tvPhone.setText(
                "Phone: " +
                        safe(phone)
        );


        tvAddress.setText(
                "Address: " +
                        safe(address)
        );


        tvAccountType.setText(
                "Account Type: " +
                        safe(role)
        );


        // ==========================================
        // ADD CARD
        // ==========================================

        customersContainer.addView(
                customerView
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