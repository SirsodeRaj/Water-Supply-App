package com.example.watersupply;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CustomerNotificationsActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private LinearLayout notificationsContainer;
    private ProgressBar progressBar;
    private TextView tvNoNotifications;
    private TextView tvNotificationCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_notifications);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        notificationsContainer =
                findViewById(R.id.notificationsContainer);

        progressBar =
                findViewById(R.id.progressBar);

        tvNoNotifications =
                findViewById(R.id.tvNoNotifications);

        tvNotificationCount =
                findViewById(R.id.tvNotificationCount);

        // Back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        loadNotifications();
    }

    private void loadNotifications() {

        if (auth.getCurrentUser() == null) {

            tvNoNotifications.setText(
                    "Please login again."
            );

            tvNoNotifications.setVisibility(View.VISIBLE);

            return;
        }

        String customerId =
                auth.getCurrentUser().getUid();

        progressBar.setVisibility(View.VISIBLE);
        tvNoNotifications.setVisibility(View.GONE);

        db.collection("notifications")
                .whereEqualTo("customerId", customerId)
                .orderBy(
                        "timestamp",
                        Query.Direction.DESCENDING
                )
                .addSnapshotListener((value, error) -> {

                    progressBar.setVisibility(View.GONE);
                    notificationsContainer.removeAllViews();

                    if (error != null) {

                        tvNotificationCount.setText("0");

                        tvNoNotifications.setText(
                                "Failed to load notifications."
                        );

                        tvNoNotifications.setVisibility(
                                View.VISIBLE
                        );

                        return;
                    }

                    if (value == null || value.isEmpty()) {

                        tvNotificationCount.setText("0");

                        tvNoNotifications.setText(
                                "No notifications."
                        );

                        tvNoNotifications.setVisibility(
                                View.VISIBLE
                        );

                        return;
                    }

                    int count = 0;

                    for (DocumentSnapshot document :
                            value.getDocuments()) {

                        count++;

                        View notificationView =
                                LayoutInflater.from(this).inflate(
                                        R.layout.item_customer_notification,
                                        notificationsContainer,
                                        false
                                );

                        TextView tvType =
                                notificationView.findViewById(
                                        R.id.tvNotificationType
                                );

                        TextView tvTitle =
                                notificationView.findViewById(
                                        R.id.tvNotificationTitle
                                );

                        TextView tvMessage =
                                notificationView.findViewById(
                                        R.id.tvNotificationMessage
                                );

                        TextView tvDate =
                                notificationView.findViewById(
                                        R.id.tvNotificationDate
                                );

                        String type =
                                document.getString("type");

                        String title =
                                document.getString("title");

                        String message =
                                document.getString("message");

                        Date timestamp =
                                document.getDate("timestamp");

                        if (type == null || type.isEmpty()) {
                            type = "ORDER";
                        }

                        if (title == null || title.isEmpty()) {
                            title = "Notification";
                        }

                        if (message == null || message.isEmpty()) {
                            message = "No message available.";
                        }

                        tvType.setText(
                                type.toUpperCase(
                                        Locale.getDefault()
                                )
                        );

                        tvTitle.setText(title);
                        tvMessage.setText(message);

                        if (timestamp != null) {

                            SimpleDateFormat dateFormat =
                                    new SimpleDateFormat(
                                            "dd MMM yyyy, hh:mm a",
                                            Locale.getDefault()
                                    );

                            tvDate.setText(
                                    dateFormat.format(timestamp)
                            );

                        } else {

                            tvDate.setText(
                                    "Date: Not available"
                            );
                        }

                        notificationsContainer.addView(
                                notificationView
                        );
                    }

                    tvNotificationCount.setText(
                            String.valueOf(count)
                    );
                });
    }
}