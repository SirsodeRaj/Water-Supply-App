package com.example.watersupply;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class DeliveryManLoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin, btnBack;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_man_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etEmail = findViewById(R.id.etDeliveryEmail);
        etPassword = findViewById(R.id.etDeliveryPassword);

        btnLogin = findViewById(R.id.btnDeliveryLogin);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        btnLogin.setOnClickListener(v -> loginDeliveryMan());
    }

    private void loginDeliveryMan() {

        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty()) {
            etEmail.setError("Enter email");
            etEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Enter password");
            etPassword.requestFocus();
            return;
        }

        btnLogin.setEnabled(false);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {

                    if (mAuth.getCurrentUser() == null) {
                        btnLogin.setEnabled(true);
                        Toast.makeText(this,
                                "Login failed.",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String uid = mAuth.getCurrentUser().getUid();

                    db.collection("users")
                            .document(uid)
                            .get()
                            .addOnSuccessListener(documentSnapshot -> {

                                btnLogin.setEnabled(true);

                                if (!documentSnapshot.exists()) {
                                    mAuth.signOut();

                                    Toast.makeText(this,
                                            "Delivery man profile not found.",
                                            Toast.LENGTH_LONG).show();
                                    return;
                                }

                                String role =
                                        documentSnapshot.getString("role");

                                if ("deliveryMan".equals(role)) {

                                    Toast.makeText(this,
                                            "Login successful!",
                                            Toast.LENGTH_SHORT).show();

                                    Intent intent = new Intent(
                                            DeliveryManLoginActivity.this,
                                            DeliveryManDashboardActivity.class
                                    );

                                    startActivity(intent);
                                    finish();

                                } else {

                                    mAuth.signOut();

                                    Toast.makeText(this,
                                            "This account is not a Delivery Man account.",
                                            Toast.LENGTH_LONG).show();
                                }
                            })
                            .addOnFailureListener(e -> {

                                btnLogin.setEnabled(true);

                                Toast.makeText(this,
                                        "Failed to verify account.",
                                        Toast.LENGTH_LONG).show();
                            });
                })
                .addOnFailureListener(e -> {

                    btnLogin.setEnabled(true);

                    Toast.makeText(this,
                            "Invalid email or password.",
                            Toast.LENGTH_LONG).show();
                });
    }
}