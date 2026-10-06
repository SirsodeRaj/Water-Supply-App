package com.example.watersupply;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminProfileActivity extends AppCompatActivity {

    private TextView tvProfileName;
    private TextView tvProfileEmail;
    private TextView tvProfilePhone;
    private TextView tvProfileAddress;

    private Button btnEditProfile;
    private Button btnDeleteAccount;
    private Button btnLogout;

    private ImageButton btnProfileBack;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        prefs = getSharedPreferences(
                "WaterSupplyPrefs",
                MODE_PRIVATE
        );

        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        tvProfilePhone = findViewById(R.id.tvProfilePhone);
        tvProfileAddress = findViewById(R.id.tvProfileAddress);

        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount);
        btnLogout = findViewById(R.id.btnLogout);

        btnProfileBack = findViewById(R.id.btnProfileBack);

        loadAdminProfile();

        btnProfileBack.setOnClickListener(v -> finish());

        btnEditProfile.setOnClickListener(v -> {

            Toast.makeText(
                    AdminProfileActivity.this,
                    "Profile editing coming next",
                    Toast.LENGTH_SHORT
            ).show();

        });

        btnDeleteAccount.setOnClickListener(
                v -> showDeleteConfirmation()
        );

        btnLogout.setOnClickListener(
                v -> logoutAdmin()
        );
    }

    private void loadAdminProfile() {

        if (mAuth.getCurrentUser() == null) {
            logoutAdmin();
            return;
        }

        String uid =
                mAuth.getCurrentUser().getUid();

        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(document -> {

                    if (document.exists()) {

                        tvProfileName.setText(
                                document.getString("name")
                        );

                        tvProfileEmail.setText(
                                document.getString("email")
                        );

                        tvProfilePhone.setText(
                                document.getString("phone")
                        );

                        tvProfileAddress.setText(
                                document.getString("address")
                        );
                    }
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            AdminProfileActivity.this,
                            "Unable to load profile.",
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }

    private void showDeleteConfirmation() {

        new AlertDialog.Builder(this)
                .setTitle("Delete Admin Account?")
                .setMessage(
                        "This will permanently delete your admin " +
                                "profile and authentication account. " +
                                "This action cannot be undone."
                )
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .setPositiveButton(
                        "Delete",
                        (dialog, which) -> deleteAccount()
                )
                .show();
    }

    private void deleteAccount() {

        if (mAuth.getCurrentUser() == null) {
            return;
        }

        String uid =
                mAuth.getCurrentUser().getUid();

        db.collection("users")
                .document(uid)
                .delete()
                .addOnSuccessListener(unused -> {

                    mAuth.getCurrentUser()
                            .delete()
                            .addOnSuccessListener(unused2 -> {

                                prefs.edit()
                                        .clear()
                                        .apply();

                                Toast.makeText(
                                        AdminProfileActivity.this,
                                        "Account deleted successfully!",
                                        Toast.LENGTH_LONG
                                ).show();

                                Intent intent =
                                        new Intent(
                                                AdminProfileActivity.this,
                                                MainActivity.class
                                        );

                                intent.addFlags(
                                        Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                                Intent.FLAG_ACTIVITY_NEW_TASK |
                                                Intent.FLAG_ACTIVITY_CLEAR_TASK
                                );

                                startActivity(intent);
                                finish();

                            })
                            .addOnFailureListener(e -> {

                                Toast.makeText(
                                        AdminProfileActivity.this,
                                        "Profile deleted, but authentication account could not be deleted. Please sign in again and retry.",
                                        Toast.LENGTH_LONG
                                ).show();
                            });
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            AdminProfileActivity.this,
                            "Unable to delete account.",
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private void logoutAdmin() {

        mAuth.signOut();

        prefs.edit()
                .remove("adminRememberMe")
                .remove("adminEmail")
                .remove("adminPassword")
                .apply();

        Toast.makeText(
                AdminProfileActivity.this,
                "Logout successful!",
                Toast.LENGTH_SHORT
        ).show();

        Intent intent = new Intent(
                AdminProfileActivity.this,
                MainActivity.class
        );

        intent.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);

        finish();
    }
}