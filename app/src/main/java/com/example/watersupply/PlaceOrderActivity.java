package com.example.watersupply;

import android.os.Bundle;
import android.view.View;
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
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class PlaceOrderActivity extends AppCompatActivity {

    // Customer details
    private EditText etCustomerName;
    private EditText etPhone;
    private EditText etAddress;
    private EditText etQuantity;

    // Water type
    private RadioGroup rgWaterType;
    private RadioButton rbNormal;
    private RadioButton rbCold;

    // Payment
    private RadioGroup rgPayment;
    private RadioButton rbCash;
    private RadioButton rbOnline;

    // Order type
    private RadioGroup rgOrderType;
    private RadioButton rbSingle;
    private RadioButton rbMonthly;

    // Delivery time
    private RadioGroup rgDeliveryTime;
    private RadioButton rbAnytime;
    private RadioButton rbCustomise;

    private LinearLayout layoutCustomTime;

    private Spinner spStartTime;
    private Spinner spEndTime;

    // Total and button
    private TextView tvTotal;
    private Button btnPlaceOrder;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // Price
    private int pricePerUnit = 25;

    // Delivery time data
    private String deliveryTimeType = "Anytime";
    private String startTime = "";
    private String endTime = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_place_order);

        // Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();


        // ================= CUSTOMER DETAILS =================

        etCustomerName = findViewById(R.id.etCustomerName);
        etPhone = findViewById(R.id.etPhone);
        etAddress = findViewById(R.id.etAddress);
        etQuantity = findViewById(R.id.etQuantity);


        // ================= WATER TYPE =================

        rgWaterType = findViewById(R.id.rgWaterType);

        rbNormal = findViewById(R.id.rbNormal);
        rbCold = findViewById(R.id.rbCold);


        // ================= PAYMENT =================

        rgPayment = findViewById(R.id.rgPayment);

        rbCash = findViewById(R.id.rbCash);
        rbOnline = findViewById(R.id.rbOnline);


        // ================= ORDER TYPE =================

        rgOrderType = findViewById(R.id.rgOrderType);

        rbSingle = findViewById(R.id.rbSingle);
        rbMonthly = findViewById(R.id.rbMonthly);


        // ================= TOTAL =================

        tvTotal = findViewById(R.id.tvTotal);
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);


        // ================= DELIVERY TIME =================

        rgDeliveryTime = findViewById(R.id.rgDeliveryTime);

        rbAnytime = findViewById(R.id.rbAnytime);
        rbCustomise = findViewById(R.id.rbCustomise);

        layoutCustomTime = findViewById(R.id.layoutCustomTime);

        spStartTime = findViewById(R.id.spStartTime);
        spEndTime = findViewById(R.id.spEndTime);


        // ================= DEFAULT VALUES =================

        rbNormal.setChecked(true);
        rbCash.setChecked(true);
        rbSingle.setChecked(true);

        // Delivery time default = Anytime
        rbAnytime.setChecked(true);
        layoutCustomTime.setVisibility(View.GONE);


        // ================= LOAD CUSTOMER =================

        loadCustomerDetails();


        // ================= BACK BUTTON =================

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());


        // ================= WATER SELECTION =================

        rgWaterType.setOnCheckedChangeListener((group, checkedId) -> {

            if (checkedId == R.id.rbCold) {

                pricePerUnit = 30;

            } else {

                pricePerUnit = 25;
            }

            calculateTotal();
        });


        // ================= QUANTITY =================

        etQuantity.setOnKeyListener((v, keyCode, event) -> {

            calculateTotal();

            return false;
        });


        // ================= DELIVERY TIME =================

        rgDeliveryTime.setOnCheckedChangeListener(
                (group, checkedId) -> {

                    if (checkedId == R.id.rbAnytime) {

                        // Anytime selected
                        deliveryTimeType = "Anytime";

                        layoutCustomTime.setVisibility(
                                View.GONE
                        );

                        startTime = "";
                        endTime = "";

                    } else if (checkedId == R.id.rbCustomise) {

                        // Customise selected
                        deliveryTimeType = "Customise";

                        layoutCustomTime.setVisibility(
                                View.VISIBLE
                        );
                    }
                }
        );


        // ================= PLACE ORDER =================

        btnPlaceOrder.setOnClickListener(v -> placeOrder());


        // Calculate initial total
        calculateTotal();
    }


    // =========================================================
    // LOAD CUSTOMER DETAILS
    // =========================================================

    private void loadCustomerDetails() {

        if (mAuth.getCurrentUser() == null) {
            return;
        }

        String uid = mAuth.getCurrentUser().getUid();

        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    if (documentSnapshot.exists()) {

                        String name =
                                documentSnapshot.getString("name");

                        String phone =
                                documentSnapshot.getString("phone");

                        String address =
                                documentSnapshot.getString("address");


                        if (name != null) {
                            etCustomerName.setText(name);
                        }

                        if (phone != null) {
                            etPhone.setText(phone);
                        }

                        if (address != null) {
                            etAddress.setText(address);
                        }
                    }
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            this,
                            "Failed to load customer details.",
                            Toast.LENGTH_SHORT
                    ).show();

                });
    }


    // =========================================================
    // CALCULATE TOTAL
    // =========================================================

    private void calculateTotal() {

        int quantity = getQuantity();

        int total = pricePerUnit * quantity;

        tvTotal.setText(
                "Total: ₹" + total
        );
    }


    // =========================================================
    // GET QUANTITY
    // =========================================================

    private int getQuantity() {

        String quantityText =
                etQuantity.getText().toString().trim();

        if (quantityText.isEmpty()) {
            return 1;
        }

        try {

            return Integer.parseInt(quantityText);

        } catch (NumberFormatException e) {

            return 1;
        }
    }


    // =========================================================
    // CHECK TIME RANGE
    // =========================================================

    private boolean isValidTimeRange(
            String start,
            String end) {

        try {

            SimpleDateFormat format =
                    new SimpleDateFormat(
                            "hh:mm a",
                            Locale.US
                    );

            Date startDate =
                    format.parse(start);

            Date endDate =
                    format.parse(end);


            if (startDate == null || endDate == null) {

                return false;
            }


            return endDate.after(startDate);


        } catch (Exception e) {

            return false;
        }
    }


    // =========================================================
    // PLACE ORDER
    // =========================================================

    private void placeOrder() {

        // Check login
        if (mAuth.getCurrentUser() == null) {

            Toast.makeText(
                    this,
                    "Please login again.",
                    Toast.LENGTH_LONG
            ).show();

            finish();

            return;
        }


        // ================= GET CUSTOMER DATA =================

        String customerName =
                etCustomerName.getText()
                        .toString()
                        .trim();

        String phone =
                etPhone.getText()
                        .toString()
                        .trim();

        String address =
                etAddress.getText()
                        .toString()
                        .trim();

        String quantityText =
                etQuantity.getText()
                        .toString()
                        .trim();


        // ================= VALIDATION =================

        if (customerName.isEmpty()) {

            etCustomerName.setError(
                    "Enter your name"
            );

            etCustomerName.requestFocus();

            return;
        }


        if (phone.isEmpty()) {

            etPhone.setError(
                    "Enter phone number"
            );

            etPhone.requestFocus();

            return;
        }


        if (phone.length() != 10) {

            etPhone.setError(
                    "Enter a valid 10-digit phone number"
            );

            etPhone.requestFocus();

            return;
        }


        if (address.isEmpty()) {

            etAddress.setError(
                    "Enter delivery address"
            );

            etAddress.requestFocus();

            return;
        }


        if (quantityText.isEmpty()) {

            etQuantity.setError(
                    "Enter quantity"
            );

            etQuantity.requestFocus();

            return;
        }


        int quantity;

        try {

            quantity =
                    Integer.parseInt(quantityText);

        } catch (NumberFormatException e) {

            etQuantity.setError(
                    "Enter a valid quantity"
            );

            etQuantity.requestFocus();

            return;
        }


        if (quantity <= 0) {

            etQuantity.setError(
                    "Quantity must be greater than 0"
            );

            etQuantity.requestFocus();

            return;
        }


        // ================= DELIVERY TIME =================

        if (deliveryTimeType.equals("Customise")) {

            startTime =
                    spStartTime
                            .getSelectedItem()
                            .toString();

            endTime =
                    spEndTime
                            .getSelectedItem()
                            .toString();


            // Check start and end time
            if (!isValidTimeRange(
                    startTime,
                    endTime)) {

                Toast.makeText(
                        this,
                        "End time must be after start time.",
                        Toast.LENGTH_LONG
                ).show();

                return;
            }

        } else {

            // Anytime
            startTime = "";
            endTime = "";
        }


        // ================= WATER TYPE =================

        String waterType;

        if (rbCold.isChecked()) {

            waterType = "Cold";

            pricePerUnit = 30;

        } else {

            waterType = "Normal";

            pricePerUnit = 25;
        }


        // ================= PAYMENT =================

        String paymentMethod;

        if (rbOnline.isChecked()) {

            paymentMethod = "Online";

        } else {

            paymentMethod = "Cash";
        }


        // ================= PAYMENT STATUS =================

        String paymentStatus;

        if (paymentMethod.equals("Cash")) {

            paymentStatus = "Cash Pending";

        } else {

            // Online payment will be added later
            paymentStatus = "Pending";
        }


        // ================= ORDER TYPE =================

        String orderType;

        if (rbMonthly.isChecked()) {

            orderType = "Monthly";

        } else {

            orderType = "Single";
        }


        // ================= TOTAL =================

        int totalAmount =
                pricePerUnit * quantity;


        // ================= CUSTOMER ID =================

        String customerId =
                mAuth.getCurrentUser().getUid();


        // ================= CREATE ORDER =================

        Map<String, Object> order =
                new HashMap<>();


        order.put(
                "customerId",
                customerId
        );

        order.put(
                "customerName",
                customerName
        );

        order.put(
                "phone",
                phone
        );

        order.put(
                "address",
                address
        );


        // Water
        order.put(
                "waterType",
                waterType
        );

        order.put(
                "quantity",
                quantity
        );

        order.put(
                "pricePerUnit",
                pricePerUnit
        );

        order.put(
                "totalAmount",
                totalAmount
        );


        // Payment
        order.put(
                "paymentMethod",
                paymentMethod
        );

        order.put(
                "paymentStatus",
                paymentStatus
        );


        // Order
        order.put(
                "orderStatus",
                "Pending"
        );

        order.put(
                "orderType",
                orderType
        );


        // Delivery Time
        order.put(
                "deliveryTimeType",
                deliveryTimeType
        );

        order.put(
                "startTime",
                startTime
        );

        order.put(
                "endTime",
                endTime
        );


        // Timestamp
        order.put(
                "timestamp",
                FieldValue.serverTimestamp()
        );


        // ================= DISABLE BUTTON =================

        btnPlaceOrder.setEnabled(false);

        btnPlaceOrder.setText(
                "PLACING ORDER..."
        );


        // ================= SAVE TO FIRESTORE =================

        db.collection("orders")
                .add(order)
                .addOnSuccessListener(documentReference -> {

                    NotificationHelper notificationHelper =
                            new NotificationHelper();

                    notificationHelper.sendAdminNotification(
                            "New Order",
                            "New Order Received",
                            "A new order has been placed by "
                                    + customerName
                                    + "."
                    );

                    Toast.makeText(
                            this,
                            "Order placed successfully!",
                            Toast.LENGTH_SHORT
                    ).show();

                    finish();
                })
                .addOnFailureListener(
                        e -> {

                            btnPlaceOrder.setEnabled(true);

                            btnPlaceOrder.setText(
                                    "PLACE ORDER"
                            );

                            Toast.makeText(
                                    PlaceOrderActivity.this,
                                    "Order failed. Please try again.",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }
}