package com.example.watersupply;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class SessionRouterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_session_router);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        prefs = getSharedPreferences(
                "WaterSupplyPrefs",
                MODE_PRIVATE
        );

        checkSession();
    }

    private void checkSession() {

        // No Firebase user logged in
        if (mAuth.getCurrentUser() == null) {
            openLogin();
            return;
        }

        String uid = mAuth.getCurrentUser().getUid();

        boolean adminRemembered =
                prefs.getBoolean("adminRememberMe", false);

        boolean customerRemembered =
                prefs.getBoolean("customerRememberMe", false);

        // Remember Me was not selected
        if (!adminRemembered && !customerRemembered) {

            mAuth.signOut();

            openLogin();
            return;
        }

        // Find user's profile
        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(document -> {

                    if (!document.exists()) {

                        mAuth.signOut();
                        clearPreferences();
                        openLogin();

                        return;
                    }

                    String role =
                            document.getString("role");

                    // ADMIN
                    if ("admin".equals(role)
                            && adminRemembered) {

                        openAdminDashboard();

                    }

                    // CUSTOMER
                    else if ("customer".equals(role)
                            && customerRemembered) {

                        /*
                         * Customer Dashboard will be created
                         * in the next customer-portal step.
                         */
                        openLogin();

                    }

                    // Something doesn't match
                    else {

                        mAuth.signOut();
                        clearPreferences();
                        openLogin();
                    }
                })
                .addOnFailureListener(e -> {

                    mAuth.signOut();
                    clearPreferences();
                    openLogin();
                });
    }

    private void openAdminDashboard() {

        Intent intent = new Intent(
                SessionRouterActivity.this,
                AdminDashboardActivity.class
        );

        intent.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
        finish();
    }

    private void openLogin() {

        Intent intent = new Intent(
                SessionRouterActivity.this,
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

    private void clearPreferences() {

        prefs.edit()
                .remove("adminRememberMe")
                .remove("adminEmail")
                .remove("customerRememberMe")
                .remove("customerEmail")
                .apply();
    }
}