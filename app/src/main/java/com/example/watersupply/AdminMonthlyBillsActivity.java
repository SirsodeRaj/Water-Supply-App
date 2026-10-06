package com.example.watersupply;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminMonthlyBillsActivity extends AppCompatActivity {

    private FirebaseFirestore db;

    private RecyclerView recyclerView;
    private Button btnBack;

    private final List<BillItem> billList =
            new ArrayList<>();

    private BillAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_monthly_bills);

        db = FirebaseFirestore.getInstance();

        recyclerView =
                findViewById(R.id.recyclerBills);

        btnBack =
                findViewById(R.id.btnBack);

        recyclerView.setLayoutManager(
                new LinearLayoutManager(this)
        );

        adapter = new BillAdapter(billList);

        recyclerView.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());

        loadBills();
    }

    private void loadBills() {

        db.collection("monthlyBills")
                .orderBy(
                        "timestamp",
                        Query.Direction.DESCENDING
                )
                .addSnapshotListener(
                        (querySnapshot, error) -> {

                            if (error != null) {

                                Toast.makeText(
                                        this,
                                        "Unable to load monthly bills.",
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

                                String customerName =
                                        document.getString(
                                                "customerName"
                                        );

                                String month =
                                        document.getString(
                                                "month"
                                        );

                                Double totalAmount =
                                        document.getDouble(
                                                "totalAmount"
                                        );

                                String paymentStatus =
                                        document.getString(
                                                "paymentStatus"
                                        );

                                String billStatus =
                                        document.getString(
                                                "billStatus"
                                        );

                                Long totalDeliveryDays =
                                        document.getLong(
                                                "totalDeliveryDays"
                                        );

                                Long deliveredDays =
                                        document.getLong(
                                                "deliveredDays"
                                        );

                                if (customerName == null) {
                                    customerName =
                                            "Unknown Customer";
                                }

                                if (month == null) {
                                    month = "N/A";
                                }

                                if (totalAmount == null) {
                                    totalAmount = 0.0;
                                }

                                if (paymentStatus == null) {
                                    paymentStatus = "Pending";
                                }

                                if (billStatus == null) {
                                    billStatus = "N/A";
                                }

                                if (totalDeliveryDays == null) {
                                    totalDeliveryDays = 0L;
                                }

                                if (deliveredDays == null) {
                                    deliveredDays = 0L;
                                }

                                billList.add(
                                        new BillItem(
                                                document.getId(),
                                                customerName,
                                                month,
                                                totalAmount,
                                                paymentStatus,
                                                billStatus,
                                                totalDeliveryDays,
                                                deliveredDays
                                        )
                                );
                            }

                            adapter.notifyDataSetChanged();

                            if (billList.isEmpty()) {

                                Toast.makeText(
                                        this,
                                        "No monthly bills available.",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        }
                );
    }

    private void markBillAsPaid(
            BillItem bill
    ) {

        db.collection("monthlyBills")
                .document(bill.billId)
                .update(
                        "paymentStatus",
                        "Paid",
                        "billStatus",
                        "Paid"
                )
                .addOnSuccessListener(unused -> {

                    Toast.makeText(
                            this,
                            "Bill marked as paid successfully!",
                            Toast.LENGTH_SHORT
                    ).show();
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            this,
                            "Failed to update bill.",
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    public static class BillItem {

        String billId;
        String customerName;
        String month;
        double totalAmount;
        String paymentStatus;
        String billStatus;
        long totalDeliveryDays;
        long deliveredDays;

        public BillItem(
                String billId,
                String customerName,
                String month,
                double totalAmount,
                String paymentStatus,
                String billStatus,
                long totalDeliveryDays,
                long deliveredDays
        ) {

            this.billId = billId;
            this.customerName = customerName;
            this.month = month;
            this.totalAmount = totalAmount;
            this.paymentStatus = paymentStatus;
            this.billStatus = billStatus;
            this.totalDeliveryDays = totalDeliveryDays;
            this.deliveredDays = deliveredDays;
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
                            R.layout.item_admin_monthly_bill,
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

            holder.tvCustomerName.setText(
                    "Customer: " + bill.customerName
            );

            holder.tvMonth.setText(
                    "Month: " + bill.month
            );

            holder.tvDeliveryDays.setText(
                    "Delivery Days: "
                            + bill.totalDeliveryDays
            );

            holder.tvDeliveredDays.setText(
                    "Delivered Days: "
                            + bill.deliveredDays
            );

            holder.tvAmount.setText(
                    String.format(
                            Locale.getDefault(),
                            "Amount: ₹%.2f",
                            bill.totalAmount
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

            if ("Paid".equalsIgnoreCase(
                    bill.paymentStatus
            )) {

                holder.btnMarkPaid.setText(
                        "PAID"
                );

                holder.btnMarkPaid.setEnabled(
                        false
                );

            } else {

                holder.btnMarkPaid.setText(
                        "MARK AS PAID"
                );

                holder.btnMarkPaid.setEnabled(
                        true
                );

                holder.btnMarkPaid.setOnClickListener(
                        v -> markBillAsPaid(bill)
                );
            }
        }

        @Override
        public int getItemCount() {
            return bills.size();
        }

        class BillViewHolder
                extends RecyclerView.ViewHolder {

            android.widget.TextView tvCustomerName;
            android.widget.TextView tvMonth;
            android.widget.TextView tvDeliveryDays;
            android.widget.TextView tvDeliveredDays;
            android.widget.TextView tvAmount;
            android.widget.TextView tvPaymentStatus;
            android.widget.TextView tvBillStatus;
            android.widget.Button btnMarkPaid;

            BillViewHolder(
                    android.view.View itemView
            ) {

                super(itemView);

                tvCustomerName =
                        itemView.findViewById(
                                R.id.tvCustomerName
                        );

                tvMonth =
                        itemView.findViewById(
                                R.id.tvMonth
                        );

                tvDeliveryDays =
                        itemView.findViewById(
                                R.id.tvDeliveryDays
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

                btnMarkPaid =
                        itemView.findViewById(
                                R.id.btnMarkPaid
                        );
            }
        }
    }
}