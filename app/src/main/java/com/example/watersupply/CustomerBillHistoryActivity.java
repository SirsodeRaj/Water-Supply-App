package com.example.watersupply;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CustomerBillHistoryActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private RecyclerView recyclerView;
    private Button btnBack;

    private final List<BillItem> billList =
            new ArrayList<>();

    private BillAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_bill_history);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        recyclerView =
                findViewById(R.id.recyclerBills);

        btnBack =
                findViewById(R.id.btnBack);

        recyclerView.setLayoutManager(
                new LinearLayoutManager(this)
        );

        adapter =
                new BillAdapter(billList);

        recyclerView.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());

        loadBillHistory();
    }

    private void loadBillHistory() {

        if (mAuth.getCurrentUser() == null) {

            Toast.makeText(
                    this,
                    "Session expired. Please login again.",
                    Toast.LENGTH_LONG
            ).show();

            finish();
            return;
        }

        String customerId =
                mAuth.getCurrentUser().getUid();

        db.collection("monthlyBills")
                .whereEqualTo(
                        "customerId",
                        customerId
                )
                .orderBy(
                        "timestamp",
                        Query.Direction.DESCENDING
                )
                .addSnapshotListener(
                        (querySnapshot, error) -> {

                            if (error != null) {

                                Toast.makeText(
                                        this,
                                        "Unable to load bill history.",
                                        Toast.LENGTH_LONG
                                ).show();

                                return;
                            }

                            if (querySnapshot == null) {
                                return;
                            }

                            billList.clear();

                            for (var document :
                                    querySnapshot.getDocuments()) {

                                String month =
                                        document.getString("month");

                                Double amount =
                                        document.getDouble(
                                                "totalAmount"
                                        );

                                Long deliveredDays =
                                        document.getLong(
                                                "deliveredDays"
                                        );

                                String paymentStatus =
                                        document.getString(
                                                "paymentStatus"
                                        );

                                String billStatus =
                                        document.getString(
                                                "billStatus"
                                        );

                                if (month == null) {
                                    month = "N/A";
                                }

                                if (amount == null) {
                                    amount = 0.0;
                                }

                                if (deliveredDays == null) {
                                    deliveredDays = 0L;
                                }

                                if (paymentStatus == null) {
                                    paymentStatus = "Pending";
                                }

                                if (billStatus == null) {
                                    billStatus = "N/A";
                                }

                                billList.add(
                                        new BillItem(
                                                month,
                                                amount,
                                                deliveredDays,
                                                paymentStatus,
                                                billStatus
                                        )
                                );
                            }

                            adapter.notifyDataSetChanged();

                            if (billList.isEmpty()) {

                                Toast.makeText(
                                        this,
                                        "No bill history available.",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        }
                );
    }

    private static class BillItem {

        String month;
        double amount;
        long deliveredDays;
        String paymentStatus;
        String billStatus;

        BillItem(
                String month,
                double amount,
                long deliveredDays,
                String paymentStatus,
                String billStatus
        ) {

            this.month = month;
            this.amount = amount;
            this.deliveredDays = deliveredDays;
            this.paymentStatus = paymentStatus;
            this.billStatus = billStatus;
        }
    }

    private class BillAdapter
            extends RecyclerView.Adapter<BillAdapter.BillViewHolder> {

        private final List<BillItem> bills;

        BillAdapter(List<BillItem> bills) {
            this.bills = bills;
        }

        @Override
        public BillViewHolder onCreateViewHolder(
                android.view.ViewGroup parent,
                int viewType
        ) {

            android.view.View view =
                    getLayoutInflater().inflate(
                            R.layout.item_customer_bill,
                            parent,
                            false
                    );

            return new BillViewHolder(view);
        }

        @Override
        public void onBindViewHolder(
                BillViewHolder holder,
                int position
        ) {

            BillItem bill =
                    bills.get(position);

            holder.tvMonth.setText(
                    "Month: " + bill.month
            );

            holder.tvDeliveredDays.setText(
                    "Delivered Days: "
                            + bill.deliveredDays
            );

            holder.tvAmount.setText(
                    String.format(
                            Locale.getDefault(),
                            "Amount: ₹%.2f",
                            bill.amount
                    )
            );

            holder.tvPaymentStatus.setText(
                    "Payment: "
                            + bill.paymentStatus
            );

            holder.tvBillStatus.setText(
                    "Bill: "
                            + bill.billStatus
            );
        }

        @Override
        public int getItemCount() {
            return bills.size();
        }

        class BillViewHolder
                extends RecyclerView.ViewHolder {

            android.widget.TextView tvMonth;
            android.widget.TextView tvDeliveredDays;
            android.widget.TextView tvAmount;
            android.widget.TextView tvPaymentStatus;
            android.widget.TextView tvBillStatus;

            BillViewHolder(
                    android.view.View itemView
            ) {

                super(itemView);

                tvMonth =
                        itemView.findViewById(
                                R.id.tvMonth
                        );

                tvDeliveredDays =
                        itemView.findViewById(
                                R.id.tvDeliveredDays
                        );

                tvAmount =
                        itemView.findViewById(
                                R.id.tvAmount
                        );

                tvPaymentStatus =
                        itemView.findViewById(
                                R.id.tvPaymentStatus
                        );

                tvBillStatus =
                        itemView.findViewById(
                                R.id.tvBillStatus
                        );
            }
        }
    }
}