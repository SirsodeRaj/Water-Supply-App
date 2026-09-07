package com.example.watersupply;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.text.method.TransformationMethod;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText etPassword;
    private TextView tvRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etPassword = findViewById(R.id.etPassword);
        tvRegister = findViewById(R.id.tvRegister);

        // Show / Hide Password
        etPassword.setOnTouchListener((v, event) -> {

            if (event.getAction() == android.view.MotionEvent.ACTION_UP) {

                if (event.getX() >=
                        (etPassword.getWidth() -
                                etPassword.getCompoundDrawables()[2].getBounds().width() -
                                etPassword.getPaddingEnd())) {

                    TransformationMethod transformationMethod =
                            etPassword.getTransformationMethod();

                    if (transformationMethod instanceof PasswordTransformationMethod) {
                        etPassword.setTransformationMethod(null);
                    } else {
                        etPassword.setTransformationMethod(
                                PasswordTransformationMethod.getInstance()
                        );
                    }

                    etPassword.setSelection(etPassword.getText().length());

                    return true;
                }
            }

            return false;
        });

        // Open Register screen
        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    RegisterActivity.class
            );
            startActivity(intent);
        });
    }
}