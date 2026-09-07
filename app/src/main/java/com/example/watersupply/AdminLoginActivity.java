package com.example.watersupply;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminLoginActivity extends AppCompatActivity {

    private EditText etAdminEmail;
    private EditText etAdminPassword;

    private CheckBox cbAdminRememberMe;
    private Button btnAdminLogin;

    private TextView tvAdminForgotPassword;
    private ImageButton btnBack;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        // Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // UI
        etAdminEmail = findViewById(R.id.etAdminEmail);
        etAdminPassword = findViewById(R.id.etAdminPassword);

        cbAdminRememberMe = findViewById(R.id.cbAdminRememberMe);

        btnAdminLogin = findViewById(R.id.btnAdminLogin);

        tvAdminForgotPassword =
                findViewById(R.id.tvAdminForgotPassword);

        btnBack = findViewById(R.id.btnBack);

        // Password show/hide
        setupPasswordEye(etAdminPassword);

        // Admin Login
        btnAdminLogin.setOnClickListener(v -> loginAdmin());

        // Forgot Password
        tvAdminForgotPassword.setOnClickListener(
                v -> resetAdminPassword()
        );

        // Back
        btnBack.setOnClickListener(v -> finish());
    }

    private void loginAdmin() {

        String email =
                etAdminEmail.getText().toString().trim();

        String password =
                etAdminPassword.getText().toString();

        if (email.isEmpty()) {
            etAdminEmail.setError("Enter admin email");
            etAdminEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etAdminPassword.setError("Enter password");
            etAdminPassword.requestFocus();
            return;
        }

        btnAdminLogin.setEnabled(false);
        btnAdminLogin.setText("LOGGING IN...");

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {

                    if (!task.isSuccessful()) {

                        btnAdminLogin.setEnabled(true);
                        btnAdminLogin.setText("ADMIN LOGIN");

                        Toast.makeText(
                                AdminLoginActivity.this,
                                "Invalid admin email or password.",
                                Toast.LENGTH_LONG
                        ).show();

                        return;
                    }

                    if (mAuth.getCurrentUser() == null) {

                        btnAdminLogin.setEnabled(true);
                        btnAdminLogin.setText("ADMIN LOGIN");

                        Toast.makeText(
                                AdminLoginActivity.this,
                                "Unable to verify account.",
                                Toast.LENGTH_LONG
                        ).show();

                        return;
                    }

                    String uid =
                            mAuth.getCurrentUser().getUid();

                    // Check user's role
                    db.collection("users")
                            .document(uid)
                            .get()
                            .addOnSuccessListener(documentSnapshot -> {

                                if (!documentSnapshot.exists()) {

                                    mAuth.signOut();

                                    showLoginError(
                                            "Admin profile not found."
                                    );

                                    return;
                                }

                                String role =
                                        documentSnapshot.getString("role");

                                if ("admin".equals(role)) {

                                    // Remember Me
                                    getSharedPreferences(
                                            "WaterSupplyPrefs",
                                            MODE_PRIVATE
                                    )
                                            .edit()
                                            .putBoolean(
                                                    "adminRememberMe",
                                                    cbAdminRememberMe.isChecked()
                                            )
                                            .apply();

                                    Toast.makeText(
                                            AdminLoginActivity.this,
                                            "Login successful!",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                    Intent intent =
                                            new Intent(
                                                    AdminLoginActivity.this,
                                                    AdminDashboardActivity.class
                                            );

                                    intent.addFlags(
                                            Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                                    Intent.FLAG_ACTIVITY_NEW_TASK |
                                                    Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    );

                                    startActivity(intent);
                                    finish();

                                } else {

                                    // Not an admin
                                    mAuth.signOut();

                                    showLoginError(
                                            "Access denied. Admin account required."
                                    );
                                }
                            })
                            .addOnFailureListener(e -> {

                                mAuth.signOut();

                                showLoginError(
                                        "Unable to verify admin account."
                                );
                            });
                });
    }

    private void resetAdminPassword() {

        String email =
                etAdminEmail.getText().toString().trim();

        if (email.isEmpty()) {

            etAdminEmail.setError(
                    "Enter your admin email first"
            );

            etAdminEmail.requestFocus();

            return;
        }

        mAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(unused -> {

                    Toast.makeText(
                            AdminLoginActivity.this,
                            "Password reset email sent!",
                            Toast.LENGTH_LONG
                    ).show();

                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            AdminLoginActivity.this,
                            "Unable to send password reset email.",
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private void showLoginError(String message) {

        btnAdminLogin.setEnabled(true);
        btnAdminLogin.setText("ADMIN LOGIN");

        Toast.makeText(
                AdminLoginActivity.this,
                message,
                Toast.LENGTH_LONG
        ).show();
    }

    private void setupPasswordEye(EditText passwordField) {

        passwordField.setOnTouchListener((v, event) -> {

            if (event.getAction() == MotionEvent.ACTION_UP) {

                int drawableWidth =
                        passwordField
                                .getCompoundDrawables()[2]
                                .getBounds()
                                .width();

                if (event.getX() >=
                        passwordField.getWidth()
                                - passwordField.getPaddingEnd()
                                - drawableWidth) {

                    if (passwordField.getTransformationMethod()
                            instanceof PasswordTransformationMethod) {

                        passwordField.setTransformationMethod(null);

                    } else {

                        passwordField.setTransformationMethod(
                                PasswordTransformationMethod
                                        .getInstance()
                        );
                    }

                    passwordField.setSelection(
                            passwordField.getText().length()
                    );

                    return true;
                }
            }

            return false;
        });
    }
}