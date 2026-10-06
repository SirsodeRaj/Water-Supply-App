package com.example.watersupply;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;

import java.util.HashMap;
import java.util.Map;

public class MonthlyPackageActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private Spinner spinnerWaterType;
    private EditText etQuantity;
    private TextView tvPrice;
    private TextView tvDailyAmount;
    private TextView tvEstimatedMonthlyAmount;

    private RadioGroup rgDuration;
    private RadioButton rbAnytime;
    private RadioButton rbCustomise;

    private LinearLayout customTimeContainer;

    private EditText etStartTime;
    private EditText etEndTime;

    private Spinner spinnerStartAmPm;
    private Spinner spinnerEndAmPm;

    private Button btnActivatePackage;

    private double pricePerUnit = 25.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monthly_package);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        spinnerWaterType = findViewById(R.id.spinnerWaterType);
        etQuantity = findViewById(R.id.etQuantity);
        tvPrice = findViewById(R.id.tvPrice);
        tvDailyAmount = findViewById(R.id.tvDailyAmount);
        tvEstimatedMonthlyAmount =
                findViewById(R.id.tvEstimatedMonthlyAmount);

        rgDuration = findViewById(R.id.rgDuration);
        rbAnytime = findViewById(R.id.rbAnytime);
        rbCustomise = findViewById(R.id.rbCustomise);

        customTimeContainer =
                findViewById(R.id.customTimeContainer);

        etStartTime = findViewById(R.id.etStartTime);
        etEndTime = findViewById(R.id.etEndTime);

        spinnerStartAmPm =
                findViewById(R.id.spinnerStartAmPm);

        spinnerEndAmPm =
                findViewById(R.id.spinnerEndAmPm);

        btnActivatePackage =
                findViewById(R.id.btnActivatePackage);

        // Back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        setupWaterTypeSpinner();
        setupAmPmSpinners();
        setupDuration();
        setupQuantityCalculation();

        btnActivatePackage.setOnClickListener(
                v -> activatePackage()
        );
    }

    private void setupWaterTypeSpinner() {

        String[] waterTypes = {
                "Normal Water",
                "Cold Water"
        };

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        waterTypes
                );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinnerWaterType.setAdapter(adapter);

        spinnerWaterType.setOnItemSelectedListener(
                new android.widget.AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            android.widget.AdapterView<?> parent,
                            View view,
                            int position,
                            long id) {

                        if (position == 0) {
                            pricePerUnit = 25.0;
                        } else {
                            pricePerUnit = 30.0;
                        }

                        tvPrice.setText(
                                "Price per unit: ₹" +
                                        (int) pricePerUnit
                        );

                        calculateAmount();
                    }

                    @Override
                    public void onNothingSelected(
                            android.widget.AdapterView<?> parent) {
                    }
                }
        );
    }

    private void setupAmPmSpinners() {

        String[] amPm = {
                "AM",
                "PM"
        };

        ArrayAdapter<String> startAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        amPm
                );

        startAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinnerStartAmPm.setAdapter(startAdapter);

        ArrayAdapter<String> endAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        amPm
                );

        endAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinnerEndAmPm.setAdapter(endAdapter);
    }

    private void setupDuration() {

        rgDuration.setOnCheckedChangeListener(
                (group, checkedId) -> {

                    if (checkedId == R.id.rbCustomise) {

                        customTimeContainer.setVisibility(
                                View.VISIBLE
                        );

                    } else {

                        customTimeContainer.setVisibility(
                                View.GONE
                        );
                    }
                }
        );
    }

    private void setupQuantityCalculation() {

        etQuantity.addTextChangedListener(
                new android.text.TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after) {
                    }

                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count) {

                        calculateAmount();
                    }

                    @Override
                    public void afterTextChanged(
                            android.text.Editable s) {
                    }
                }
        );
    }

    private void calculateAmount() {

        String quantityText =
                etQuantity.getText().toString().trim();

        if (TextUtils.isEmpty(quantityText)) {

            tvDailyAmount.setText(
                    "Daily Amount: ₹0"
            );

            tvEstimatedMonthlyAmount.setText(
                    "Estimated Monthly Amount: ₹0"
            );

            return;
        }

        try {

            int quantity =
                    Integer.parseInt(quantityText);

            if (quantity <= 0) {
                throw new NumberFormatException();
            }

            double dailyAmount =
                    quantity * pricePerUnit;

            double monthlyAmount =
                    dailyAmount * 30;

            tvDailyAmount.setText(
                    "Daily Amount: ₹" +
                            (int) dailyAmount
            );

            tvEstimatedMonthlyAmount.setText(
                    "Estimated Monthly Amount: ₹" +
                            (int) monthlyAmount
            );

        } catch (NumberFormatException e) {

            tvDailyAmount.setText(
                    "Daily Amount: ₹0"
            );

            tvEstimatedMonthlyAmount.setText(
                    "Estimated Monthly Amount: ₹0"
            );
        }
    }

    private void activatePackage() {

        if (auth.getCurrentUser() == null) {

            Toast.makeText(
                    this,
                    "Please login again.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String quantityText =
                etQuantity.getText().toString().trim();

        if (TextUtils.isEmpty(quantityText)) {

            etQuantity.setError(
                    "Enter quantity"
            );

            return;
        }

        int quantity;

        try {

            quantity =
                    Integer.parseInt(quantityText);

            if (quantity <= 0) {
                throw new NumberFormatException();
            }

        } catch (NumberFormatException e) {

            etQuantity.setError(
                    "Enter a valid quantity"
            );

            return;
        }

        String waterType =
                spinnerWaterType.getSelectedItem().toString();

        String durationType;

        if (rbCustomise.isChecked()) {

            durationType = "Customise";

            if (TextUtils.isEmpty(
                    etStartTime.getText().toString().trim())) {

                etStartTime.setError(
                        "Enter start time"
                );

                return;
            }

            if (TextUtils.isEmpty(
                    etEndTime.getText().toString().trim())) {

                etEndTime.setError(
                        "Enter end time"
                );

                return;
            }

        } else {

            durationType = "Anytime";
        }

        String deliveryTime;

        if (durationType.equals("Anytime")) {

            deliveryTime = "Anytime";

        } else {

            deliveryTime =
                    etStartTime.getText().toString().trim()
                            + " "
                            + spinnerStartAmPm
                            .getSelectedItem().toString()
                            + " - "
                            + etEndTime.getText().toString().trim()
                            + " "
                            + spinnerEndAmPm
                            .getSelectedItem().toString();
        }

        String customerId =
                auth.getCurrentUser().getUid();

        Map<String, Object> packageData =
                new HashMap<>();

        packageData.put(
                "customerId",
                customerId
        );

        packageData.put(
                "waterType",
                waterType
        );

        packageData.put(
                "quantity",
                quantity
        );

        packageData.put(
                "pricePerUnit",
                pricePerUnit
        );

        packageData.put(
                "durationType",
                durationType
        );

        packageData.put(
                "deliveryTime",
                deliveryTime
        );

        packageData.put(
                "packageStatus",
                "Active"
        );

        packageData.put(
                "createdAt",
                FieldValue.serverTimestamp()
        );

        packageData.put(
                "updatedAt",
                FieldValue.serverTimestamp()
        );

        db.collection("monthlyPackages")
                .add(packageData)
                .addOnSuccessListener(documentReference -> {

                    Toast.makeText(
                            this,
                            "Monthly package activated successfully!",
                            Toast.LENGTH_LONG
                    ).show();

                    finish();
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            this,
                            "Failed to activate monthly package.",
                            Toast.LENGTH_LONG
                    ).show();
                });
    }
}