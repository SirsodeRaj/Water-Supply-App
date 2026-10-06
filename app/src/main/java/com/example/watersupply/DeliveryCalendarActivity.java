package com.example.watersupply;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class DeliveryCalendarActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private LinearLayout calendarContainer;

    private TextView tvMonthYear;
    private TextView tvSelectedDate;

    private Button btnToggleDelivery;
    private Button btnPreviousMonth;
    private Button btnNextMonth;

    private Calendar currentMonth;
    private Calendar selectedDate;

    private String packageId;

    private final Map<String, Boolean> deliveryStatus =
            new HashMap<>();

    private final SimpleDateFormat dateKeyFormat =
            new SimpleDateFormat(
                    "yyyy-MM-dd",
                    Locale.getDefault()
            );

    private final SimpleDateFormat monthFormat =
            new SimpleDateFormat(
                    "MMMM yyyy",
                    Locale.getDefault()
            );

    private final SimpleDateFormat displayDateFormat =
            new SimpleDateFormat(
                    "dd MMMM yyyy",
                    Locale.getDefault()
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_calendar);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        calendarContainer =
                findViewById(R.id.calendarContainer);

        tvMonthYear =
                findViewById(R.id.tvMonthYear);

        tvSelectedDate =
                findViewById(R.id.tvSelectedDate);

        btnToggleDelivery =
                findViewById(R.id.btnToggleDelivery);

        btnPreviousMonth =
                findViewById(R.id.btnPreviousMonth);

        btnNextMonth =
                findViewById(R.id.btnNextMonth);

        findViewById(R.id.btnBack)
                .setOnClickListener(v -> finish());

        currentMonth = Calendar.getInstance();

        currentMonth.set(
                Calendar.DAY_OF_MONTH,
                1
        );

        selectedDate = Calendar.getInstance();

        btnPreviousMonth.setOnClickListener(v -> {

            currentMonth.add(
                    Calendar.MONTH,
                    -1
            );

            loadMonthSchedule();
        });

        btnNextMonth.setOnClickListener(v -> {

            currentMonth.add(
                    Calendar.MONTH,
                    1
            );

            loadMonthSchedule();
        });

        btnToggleDelivery.setOnClickListener(
                v -> toggleSelectedDate()
        );

        findPackage();
    }

    private void findPackage() {

        if (auth.getCurrentUser() == null) {

            Toast.makeText(
                    this,
                    "Please login again.",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        String customerId =
                auth.getCurrentUser().getUid();

        db.collection("monthlyPackages")
                .whereEqualTo(
                        "customerId",
                        customerId
                )
                .whereEqualTo(
                        "packageStatus",
                        "Active"
                )
                .limit(1)
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (snapshot.isEmpty()) {

                        Toast.makeText(
                                this,
                                "No active monthly package found.",
                                Toast.LENGTH_LONG
                        ).show();

                        finish();
                        return;
                    }

                    DocumentSnapshot document =
                            snapshot.getDocuments().get(0);

                    packageId =
                            document.getId();

                    loadMonthSchedule();
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            this,
                            "Failed to load monthly package.",
                            Toast.LENGTH_LONG
                    ).show();

                    finish();
                });
    }

    private void loadMonthSchedule() {

        if (packageId == null) {
            return;
        }

        tvMonthYear.setText(
                monthFormat.format(
                        currentMonth.getTime()
                )
        );

        db.collection("monthlyPackageSchedules")
                .whereEqualTo(
                        "packageId",
                        packageId
                )
                .whereEqualTo(
                        "month",
                        new SimpleDateFormat(
                                "yyyy-MM",
                                Locale.getDefault()
                        ).format(
                                currentMonth.getTime()
                        )
                )
                .get()
                .addOnSuccessListener(snapshot -> {

                    deliveryStatus.clear();

                    for (DocumentSnapshot document :
                            snapshot.getDocuments()) {

                        String date =
                                document.getString("date");

                        Boolean delivery =
                                document.getBoolean(
                                        "delivery"
                                );

                        if (date != null &&
                                delivery != null) {

                            deliveryStatus.put(
                                    date,
                                    delivery
                            );
                        }
                    }

                    buildCalendar();
                })
                .addOnFailureListener(e -> {

                    deliveryStatus.clear();

                    buildCalendar();

                    Toast.makeText(
                            this,
                            "Unable to load delivery schedule.",
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }

    private void buildCalendar() {

        calendarContainer.removeAllViews();

        String[] days = {
                "SUN",
                "MON",
                "TUE",
                "WED",
                "THU",
                "FRI",
                "SAT"
        };

        LinearLayout header =
                new LinearLayout(this);

        header.setOrientation(
                LinearLayout.HORIZONTAL
        );

        for (String day : days) {

            TextView textView =
                    createCalendarCell();

            textView.setText(day);
            textView.setTextSize(12);
            textView.setTextColor(
                    Color.DKGRAY
            );

            header.addView(textView);
        }

        calendarContainer.addView(header);

        Calendar firstDay =
                (Calendar) currentMonth.clone();

        firstDay.set(
                Calendar.DAY_OF_MONTH,
                1
        );

        int firstDayOfWeek =
                firstDay.get(
                        Calendar.DAY_OF_WEEK
                ) - 1;

        int daysInMonth =
                currentMonth.getActualMaximum(
                        Calendar.DAY_OF_MONTH
                );

        LinearLayout week = null;

        for (int i = 0;
             i < firstDayOfWeek;
             i++) {

            if (i == 0) {

                week = new LinearLayout(this);

                week.setOrientation(
                        LinearLayout.HORIZONTAL
                );

                calendarContainer.addView(
                        week
                );
            }

            TextView empty =
                    createCalendarCell();

            week.addView(empty);
        }

        for (int day = 1;
             day <= daysInMonth;
             day++) {

            if ((firstDayOfWeek + day - 1) % 7 == 0) {

                week = new LinearLayout(this);

                week.setOrientation(
                        LinearLayout.HORIZONTAL
                );

                calendarContainer.addView(
                        week
                );
            }

            Calendar date =
                    (Calendar) currentMonth.clone();

            date.set(
                    Calendar.DAY_OF_MONTH,
                    day
            );

            String key =
                    dateKeyFormat.format(
                            date.getTime()
                    );

            Boolean delivery =
                    deliveryStatus.get(key);

            boolean isDeliveryOn =
                    delivery == null || delivery;

            TextView cell =
                    createCalendarCell();

            cell.setText(
                    String.valueOf(day)
            );

            updateCellAppearance(
                    cell,
                    isDeliveryOn
            );

            Calendar selected =
                    (Calendar) selectedDate.clone();

            if (sameDate(date, selected)) {

                cell.setTypeface(
                        null,
                        android.graphics.Typeface.BOLD
                );

                cell.setTextSize(17);
            }

            cell.setOnClickListener(v -> {

                selectedDate =
                        (Calendar) date.clone();

                updateSelectedDate();

                buildCalendar();
            });

            week.addView(cell);
        }

        updateSelectedDate();
    }

    private TextView createCalendarCell() {

        TextView cell =
                new TextView(this);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        0,
                        52
                );

        params.weight = 1;

        params.setMargins(
                3,
                3,
                3,
                3
        );

        cell.setLayoutParams(params);

        cell.setGravity(
                Gravity.CENTER
        );

        cell.setTextSize(15);

        return cell;
    }

    private void updateCellAppearance(
            TextView cell,
            boolean deliveryOn) {

        GradientDrawable background =
                new GradientDrawable();

        background.setCornerRadius(12);

        if (deliveryOn) {

            background.setColor(
                    Color.rgb(
                            220,
                            245,
                            220
                    )
            );

            cell.setTextColor(
                    Color.rgb(
                            27,
                            100,
                            45
                    )
            );

        } else {

            background.setColor(
                    Color.rgb(
                            255,
                            225,
                            225
                    )
            );

            cell.setTextColor(
                    Color.rgb(
                            180,
                            30,
                            30
                    )
            );
        }

        cell.setBackground(
                background
        );
    }

    private void updateSelectedDate() {

        String key =
                dateKeyFormat.format(
                        selectedDate.getTime()
                );

        Boolean delivery =
                deliveryStatus.get(key);

        boolean deliveryOn =
                delivery == null || delivery;

        tvSelectedDate.setText(
                displayDateFormat.format(
                        selectedDate.getTime()
                )
                        + " — "
                        + (deliveryOn
                        ? "Delivery ON"
                        : "Delivery OFF")
        );

        btnToggleDelivery.setEnabled(true);

        if (deliveryOn) {

            btnToggleDelivery.setText(
                    "TURN DELIVERY OFF"
            );

        } else {

            btnToggleDelivery.setText(
                    "TURN DELIVERY ON"
            );
        }
    }

    private void toggleSelectedDate() {

        if (packageId == null) {
            return;
        }

        String date =
                dateKeyFormat.format(
                        selectedDate.getTime()
                );

        Boolean current =
                deliveryStatus.get(date);

        boolean newStatus =
                current == null || !current;

        String month =
                new SimpleDateFormat(
                        "yyyy-MM",
                        Locale.getDefault()
                ).format(
                        selectedDate.getTime()
                );

        String documentId =
                packageId + "_" + date;

        Map<String, Object> schedule =
                new HashMap<>();

        schedule.put(
                "packageId",
                packageId
        );

        schedule.put(
                "customerId",
                auth.getCurrentUser().getUid()
        );

        schedule.put(
                "date",
                date
        );

        schedule.put(
                "month",
                month
        );

        schedule.put(
                "delivery",
                newStatus
        );

        schedule.put(
                "updatedAt",
                FieldValue.serverTimestamp()
        );

        db.collection("monthlyPackageSchedules")
                .document(documentId)
                .set(schedule)
                .addOnSuccessListener(v -> {

                    deliveryStatus.put(
                            date,
                            newStatus
                    );

                    updateSelectedDate();
                    buildCalendar();

                    Toast.makeText(
                            this,
                            newStatus
                                    ? "Delivery turned ON."
                                    : "Delivery turned OFF.",
                            Toast.LENGTH_SHORT
                    ).show();
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            this,
                            "Failed to update delivery day.",
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private boolean sameDate(
            Calendar first,
            Calendar second) {

        return first.get(Calendar.YEAR)
                == second.get(Calendar.YEAR)
                &&
                first.get(Calendar.DAY_OF_YEAR)
                        == second.get(Calendar.DAY_OF_YEAR);
    }
}