package com.example.watersupply;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class CustomerProfileActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private static final String CHANNEL_ID = "water_supply_orders";
    private static final int NOTIFICATION_PERMISSION_CODE = 1001;

    private TextView tvName;
    private TextView tvEmail;
    private TextView tvPhone;
    private TextView tvAddress;
    private TextView tvRole;

    private Button btnEditProfile;
    private Button btnDeleteAccount;
    private Button btnLogout;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_customer_profile);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        tvPhone = findViewById(R.id.tvPhone);
        tvAddress = findViewById(R.id.tvAddress);
        tvRole = findViewById(R.id.tvRole);

        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount);
        btnLogout = findViewById(R.id.btnLogout);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        loadProfile();

        btnEditProfile.setOnClickListener(v -> {

            Toast.makeText(
                    this,
                    "Profile editing coming next.",
                    Toast.LENGTH_SHORT
            ).show();

        });

        btnDeleteAccount.setOnClickListener(v -> showDeleteConfirmation());

        btnLogout.setOnClickListener(v -> logout());
    }

    private void loadProfile() {

        if (auth.getCurrentUser() == null) {
            return;
        }

        String uid = auth.getCurrentUser().getUid();

        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    if (documentSnapshot.exists()) {

                        String name =
                                documentSnapshot.getString("name");

                        String email =
                                documentSnapshot.getString("email");

                        String phone =
                                documentSnapshot.getString("phone");

                        String address =
                                documentSnapshot.getString("address");

                        String role =
                                documentSnapshot.getString("role");

                        tvName.setText(
                                name != null ? name : "Not available"
                        );

                        tvEmail.setText(
                                email != null ? email : "Not available"
                        );

                        tvPhone.setText(
                                phone != null ? phone : "Not available"
                        );

                        tvAddress.setText(
                                address != null ? address : "Not available"
                        );

                        tvRole.setText(
                                role != null ? role : "Customer"
                        );

                    } else {

                        Toast.makeText(
                                this,
                                "Customer profile not found.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }

                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            this,
                            "Failed to load profile.",
                            Toast.LENGTH_SHORT
                    ).show();

                });
    }

    private void showDeleteConfirmation() {

        new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage(
                        "Are you sure you want to permanently delete your account?"
                )
                .setNegativeButton(
                        "CANCEL",
                        null
                )
                .setPositiveButton(
                        "DELETE",
                        (dialog, which) -> deleteAccount()
                )
                .show();
    }

    private void deleteAccount() {

        if (auth.getCurrentUser() == null) {
            return;
        }

        String uid = auth.getCurrentUser().getUid();

        db.collection("users")
                .document(uid)
                .delete()
                .addOnSuccessListener(unused -> {

                    if (auth.getCurrentUser() != null) {

                        auth.getCurrentUser()
                                .delete()
                                .addOnSuccessListener(unused2 -> {

                                    Toast.makeText(
                                            this,
                                            "Account deleted successfully!",
                                            Toast.LENGTH_LONG
                                    ).show();

                                    Intent intent =
                                            new Intent(
                                                    CustomerProfileActivity.this,
                                                    MainActivity.class
                                            );

                                    intent.addFlags(
                                            Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                                    Intent.FLAG_ACTIVITY_NEW_TASK |
                                                    Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    );

                                    startActivity(intent);

                                })
                                .addOnFailureListener(e -> {

                                    Toast.makeText(
                                            this,
                                            "Account deletion requires recent login. Please login again and try again.",
                                            Toast.LENGTH_LONG
                                    ).show();

                                });
                    }

                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            this,
                            "Failed to delete account.",
                            Toast.LENGTH_LONG
                    ).show();

                });
    }

    private void logout() {

        auth.signOut();

        Toast.makeText(
                this,
                "Logout successful!",
                Toast.LENGTH_SHORT
        ).show();

        Intent intent =
                new Intent(
                        CustomerProfileActivity.this,
                        MainActivity.class
                );

        intent.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
    }
}