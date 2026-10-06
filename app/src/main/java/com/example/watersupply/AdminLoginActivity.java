package com.example.watersupply;

import android.content.Intent;
import android.content.SharedPreferences;
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

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_admin_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        prefs = getSharedPreferences(
                "WaterSupplyPrefs",
                MODE_PRIVATE
        );

        etAdminEmail = findViewById(R.id.etAdminEmail);
        etAdminPassword = findViewById(R.id.etAdminPassword);

        cbAdminRememberMe = findViewById(R.id.cbAdminRememberMe);
        btnAdminLogin = findViewById(R.id.btnAdminLogin);

        tvAdminForgotPassword =
                findViewById(R.id.tvAdminForgotPassword);

        btnBack = findViewById(R.id.btnBack);

        // Password eye button
        setupPasswordEye(etAdminPassword);

        // Restore saved credentials
        loadRememberedCredentials();

        // Login
        btnAdminLogin.setOnClickListener(
                v -> loginAdmin()
        );

        // Forgot password
        tvAdminForgotPassword.setOnClickListener(
                v -> resetAdminPassword()
        );

        // Back
        btnBack.setOnClickListener(
                v -> finish()
        );
    }

    private void loadRememberedCredentials() {

        boolean rememberMe =
                prefs.getBoolean(
                        "adminRememberMe",
                        false
                );

        if (rememberMe) {

            String savedEmail =
                    prefs.getString(
                            "adminEmail",
                            ""
                    );

            String savedPassword =
                    prefs.getString(
                            "adminPassword",
                            ""
                    );

            etAdminEmail.setText(savedEmail);
            etAdminPassword.setText(savedPassword);

            cbAdminRememberMe.setChecked(true);
        }
    }

    private void loginAdmin() {

        String email =
                etAdminEmail.getText()
                        .toString()
                        .trim();

        String password =
                etAdminPassword.getText()
                        .toString();

        if (email.isEmpty()) {

            etAdminEmail.setError(
                    "Enter admin email"
            );

            etAdminEmail.requestFocus();

            return;
        }

        if (password.isEmpty()) {

            etAdminPassword.setError(
                    "Enter password"
            );

            etAdminPassword.requestFocus();

            return;
        }

        btnAdminLogin.setEnabled(false);
        btnAdminLogin.setText("LOGGING IN...");

        mAuth.signInWithEmailAndPassword(
                        email,
                        password
                )
                .addOnCompleteListener(
                        this,
                        task -> {

                            if (!task.isSuccessful()) {

                                btnAdminLogin.setEnabled(true);
                                btnAdminLogin.setText(
                                        "ADMIN LOGIN"
                                );

                                Toast.makeText(
                                        AdminLoginActivity.this,
                                        "Invalid admin email or password.",
                                        Toast.LENGTH_LONG
                                ).show();

                                return;
                            }

                            if (mAuth.getCurrentUser() == null) {

                                showError(
                                        "Unable to verify account."
                                );

                                return;
                            }

                            String uid =
                                    mAuth.getCurrentUser()
                                            .getUid();

                            // Check admin profile
                            db.collection("users")
                                    .document(uid)
                                    .get()
                                    .addOnSuccessListener(
                                            document -> {

                                                if (!document.exists()) {

                                                    mAuth.signOut();

                                                    showError(
                                                            "Admin profile not found."
                                                    );

                                                    return;
                                                }

                                                String role =
                                                        document.getString(
                                                                "role"
                                                        );

                                                if ("admin".equals(role)) {

                                                    saveRememberedCredentials(
                                                            email,
                                                            password
                                                    );

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

                                                    mAuth.signOut();

                                                    showError(
                                                            "Access denied. Admin account required."
                                                    );
                                                }
                                            }
                                    )
                                    .addOnFailureListener(
                                            e -> {

                                                mAuth.signOut();

                                                showError(
                                                        "Unable to verify admin account."
                                                );
                                            }
                                    );
                        }
                );
    }

    private void saveRememberedCredentials(
            String email,
            String password
    ) {

        if (cbAdminRememberMe.isChecked()) {

            prefs.edit()
                    .putBoolean(
                            "adminRememberMe",
                            true
                    )
                    .putString(
                            "adminEmail",
                            email
                    )
                    .putString(
                            "adminPassword",
                            password
                    )
                    .apply();

        } else {

            prefs.edit()
                    .remove("adminRememberMe")
                    .remove("adminEmail")
                    .remove("adminPassword")
                    .apply();
        }
    }

    private void resetAdminPassword() {

        String email =
                etAdminEmail.getText()
                        .toString()
                        .trim();

        if (email.isEmpty()) {

            etAdminEmail.setError(
                    "Enter your admin email first"
            );

            etAdminEmail.requestFocus();

            return;
        }

        mAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(
                        unused -> {

                            Toast.makeText(
                                    AdminLoginActivity.this,
                                    "Password reset email sent!",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                )
                .addOnFailureListener(
                        e -> {

                            Toast.makeText(
                                    AdminLoginActivity.this,
                                    "Unable to send password reset email.",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }

    private void showError(String message) {

        btnAdminLogin.setEnabled(true);
        btnAdminLogin.setText("ADMIN LOGIN");

        Toast.makeText(
                AdminLoginActivity.this,
                message,
                Toast.LENGTH_LONG
        ).show();
    }

    private void setupPasswordEye(
            EditText passwordField
    ) {

        passwordField.setOnTouchListener(
                (v, event) -> {

                    if (event.getAction()
                            == MotionEvent.ACTION_UP) {

                        int drawableWidth =
                                passwordField
                                        .getCompoundDrawables()[2]
                                        .getBounds()
                                        .width();

                        if (event.getX() >=
                                passwordField.getWidth()
                                        - passwordField.getPaddingEnd()
                                        - drawableWidth) {

                            if (passwordField
                                    .getTransformationMethod()
                                    instanceof PasswordTransformationMethod) {

                                passwordField
                                        .setTransformationMethod(
                                                null
                                        );

                            } else {

                                passwordField
                                        .setTransformationMethod(
                                                PasswordTransformationMethod
                                                        .getInstance()
                                        );
                            }

                            passwordField.setSelection(
                                    passwordField
                                            .getText()
                                            .length()
                            );

                            return true;
                        }
                    }

                    return false;
                }
        );
    }
}