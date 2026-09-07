package com.example.watersupply;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etRegisterEmail, etPhone, etAddress;
    private EditText etRegisterPassword, etConfirmPassword;
    private Button btnRegister;
    private TextView tvBackToLogin;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Connect UI elements
        etName = findViewById(R.id.etName);
        etRegisterEmail = findViewById(R.id.etRegisterEmail);
        etPhone = findViewById(R.id.etPhone);
        etAddress = findViewById(R.id.etAddress);
        etRegisterPassword = findViewById(R.id.etRegisterPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        btnRegister = findViewById(R.id.btnRegister);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);

        // Password eye buttons
        setupPasswordEye(etRegisterPassword);
        setupPasswordEye(etConfirmPassword);

        // Register button
        btnRegister.setOnClickListener(v -> registerCustomer());

        // Back to Login
        tvBackToLogin.setOnClickListener(v -> {
            finish();
        });
    }

    private void registerCustomer() {

        String name = etName.getText().toString().trim();
        String email = etRegisterEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String password = etRegisterPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        // Validation
        if (name.isEmpty()) {
            etName.setError("Enter your name");
            etName.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            etRegisterEmail.setError("Enter your email");
            etRegisterEmail.requestFocus();
            return;
        }

        if (phone.isEmpty() || phone.length() != 10) {
            etPhone.setError("Enter a valid 10-digit phone number");
            etPhone.requestFocus();
            return;
        }

        if (address.isEmpty()) {
            etAddress.setError("Enter your delivery address");
            etAddress.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etRegisterPassword.setError("Enter a password");
            etRegisterPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etRegisterPassword.setError("Password must be at least 6 characters");
            etRegisterPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }

        btnRegister.setEnabled(false);
        btnRegister.setText("CREATING ACCOUNT...");

        // Create Firebase Authentication account
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {

                    if (task.isSuccessful()) {

                        FirebaseUser user = mAuth.getCurrentUser();

                        if (user == null) {
                            btnRegister.setEnabled(true);
                            btnRegister.setText("REGISTER");
                            Toast.makeText(
                                    this,
                                    "Registration failed",
                                    Toast.LENGTH_SHORT
                            ).show();
                            return;
                        }

                        String userId = user.getUid();

                        // Customer information for Firestore
                        Map<String, Object> customer = new HashMap<>();
                        customer.put("name", name);
                        customer.put("email", email);
                        customer.put("phone", phone);
                        customer.put("address", address);
                        customer.put("role", "customer");

                        // Save customer profile
                        db.collection("users")
                                .document(userId)
                                .set(customer)
                                .addOnSuccessListener(unused -> {

                                    Toast.makeText(
                                            RegisterActivity.this,
                                            "Account created successfully!",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                    Intent intent = new Intent(
                                            RegisterActivity.this,
                                            MainActivity.class
                                    );

                                    intent.addFlags(
                                            Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                                    Intent.FLAG_ACTIVITY_NEW_TASK
                                    );

                                    startActivity(intent);
                                    finish();

                                })
                                .addOnFailureListener(e -> {

                                    btnRegister.setEnabled(true);
                                    btnRegister.setText("REGISTER");

                                    Toast.makeText(
                                            RegisterActivity.this,
                                            "Account created, but profile could not be saved",
                                            Toast.LENGTH_LONG
                                    ).show();
                                });

                    } else {

                        btnRegister.setEnabled(true);
                        btnRegister.setText("REGISTER");

                        String errorMessage = task.getException() != null
                                ? task.getException().getMessage()
                                : "Registration failed";

                        Toast.makeText(
                                RegisterActivity.this,
                                errorMessage,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    private void setupPasswordEye(EditText passwordField) {

        passwordField.setOnTouchListener((v, event) -> {

            if (event.getAction() == MotionEvent.ACTION_UP) {

                int drawableWidth =
                        passwordField.getCompoundDrawables()[2]
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
                                PasswordTransformationMethod.getInstance()
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