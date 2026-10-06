package com.example.watersupply;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.text.method.TransformationMethod;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private EditText etEmail;
    private EditText etPassword;

    private Button btnLogin;
    private CheckBox cbRememberMe;

    private TextView tvForgotPassword;
    private TextView tvRegister;
    private TextView tvAdminPortal;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // SharedPreferences
        prefs = getSharedPreferences(
                "CustomerLoginPrefs",
                MODE_PRIVATE
        );

        // Find views
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        btnLogin = findViewById(R.id.btnLogin);
        cbRememberMe = findViewById(R.id.cbRememberMe);

        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvRegister = findViewById(R.id.tvRegister);
        tvAdminPortal = findViewById(R.id.tvAdminPortal);

        // Load saved login details
        loadRememberedLogin();

        // Show / Hide Password
        etPassword.setOnTouchListener((v, event) -> {

            if (event.getAction() == MotionEvent.ACTION_UP) {

                if (event.getX() >=
                        (etPassword.getWidth()
                                - etPassword.getCompoundDrawables()[2]
                                .getBounds().width()
                                - etPassword.getPaddingEnd())) {

                    TransformationMethod transformationMethod =
                            etPassword.getTransformationMethod();

                    if (transformationMethod instanceof PasswordTransformationMethod) {

                        etPassword.setTransformationMethod(null);

                    } else {

                        etPassword.setTransformationMethod(
                                PasswordTransformationMethod.getInstance()
                        );
                    }

                    etPassword.setSelection(
                            etPassword.getText().length()
                    );

                    return true;
                }
            }

            return false;
        });

        // Customer Login
        btnLogin.setOnClickListener(v -> loginCustomer());

        // Forgot Password
        tvForgotPassword.setOnClickListener(v -> resetPassword());

        // Register
        tvRegister.setOnClickListener(v -> {

            Intent intent = new Intent(
                    MainActivity.this,
                    RegisterActivity.class
            );

            startActivity(intent);
        });

        // Admin Portal
        tvAdminPortal.setOnClickListener(v -> {

            Intent intent = new Intent(
                    MainActivity.this,
                    AdminLoginActivity.class
            );

            startActivity(intent);
        });
    }


    // ==========================================
    // CUSTOMER LOGIN
    // ==========================================

    private void loginCustomer() {

        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validate email
        if (email.isEmpty()) {

            etEmail.setError("Enter your email");
            etEmail.requestFocus();
            return;
        }

        // Validate password
        if (password.isEmpty()) {

            etPassword.setError("Enter your password");
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {

            etPassword.setError(
                    "Password must be at least 6 characters"
            );

            etPassword.requestFocus();
            return;
        }

        // Disable button while logging in
        btnLogin.setEnabled(false);
        btnLogin.setText("LOGGING IN...");

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {

                    btnLogin.setEnabled(true);
                    btnLogin.setText("LOGIN");

                    if (task.isSuccessful()) {

                        String uid = mAuth.getCurrentUser().getUid();

                        // Check customer profile
                        db.collection("users")
                                .document(uid)
                                .get()
                                .addOnSuccessListener(documentSnapshot -> {

                                    if (!documentSnapshot.exists()) {

                                        Toast.makeText(
                                                MainActivity.this,
                                                "Customer profile not found.",
                                                Toast.LENGTH_LONG
                                        ).show();

                                        mAuth.signOut();
                                        return;
                                    }

                                    String role =
                                            documentSnapshot.getString("role");

                                    if (!"customer".equals(role)) {

                                        Toast.makeText(
                                                MainActivity.this,
                                                "Access denied. Customer account required.",
                                                Toast.LENGTH_LONG
                                        ).show();

                                        mAuth.signOut();
                                        return;
                                    }

                                    // Remember Me
                                    if (cbRememberMe.isChecked()) {

                                        prefs.edit()
                                                .putBoolean(
                                                        "customerRememberMe",
                                                        true
                                                )
                                                .putString(
                                                        "customerEmail",
                                                        email
                                                )
                                                .putString(
                                                        "customerPassword",
                                                        password
                                                )
                                                .apply();

                                    } else {

                                        prefs.edit()
                                                .clear()
                                                .apply();
                                    }

                                    Toast.makeText(
                                            MainActivity.this,
                                            "Login successful!",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                    // Customer Dashboard
                                    Intent intent = new Intent(
                                            MainActivity.this,
                                            CustomerDashboardActivity.class
                                    );

                                    startActivity(intent);
                                    finish();

                                })
                                .addOnFailureListener(e -> {

                                    Toast.makeText(
                                            MainActivity.this,
                                            "Failed to load customer profile.",
                                            Toast.LENGTH_LONG
                                    ).show();

                                    mAuth.signOut();
                                });

                    } else {

                        Toast.makeText(
                                MainActivity.this,
                                "Invalid email or password.",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }


    // ==========================================
    // FORGOT PASSWORD
    // ==========================================

    private void resetPassword() {

        String email = etEmail.getText().toString().trim();

        if (email.isEmpty()) {

            etEmail.setError(
                    "Enter your email first"
            );

            etEmail.requestFocus();
            return;
        }

        mAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(unused -> {

                    Toast.makeText(
                            MainActivity.this,
                            "Password reset email sent!",
                            Toast.LENGTH_LONG
                    ).show();

                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            MainActivity.this,
                            "Failed to send password reset email.",
                            Toast.LENGTH_LONG
                    ).show();
                });
    }


    // ==========================================
    // REMEMBER ME
    // ==========================================

    private void loadRememberedLogin() {

        boolean rememberMe = prefs.getBoolean(
                "customerRememberMe",
                false
        );

        if (rememberMe) {

            String savedEmail =
                    prefs.getString(
                            "customerEmail",
                            ""
                    );

            String savedPassword =
                    prefs.getString(
                            "customerPassword",
                            ""
                    );

            etEmail.setText(savedEmail);
            etPassword.setText(savedPassword);

            cbRememberMe.setChecked(true);
        }
    }
}