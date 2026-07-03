package vn.humg.hai.event_ticket_booking_app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import vn.humg.hai.event_ticket_booking_app.R;
import vn.humg.hai.event_ticket_booking_app.adapter.TransactionAdapter;
import vn.humg.hai.event_ticket_booking_app.controller.BookingController;
import vn.humg.hai.event_ticket_booking_app.model.Booking;

public class HistoryFragment extends Fragment {

    private final BookingController bookingController = new BookingController();
    private final List<Booking> transactionList = new ArrayList<>();
    private TransactionAdapter transactionAdapter;

    private RecyclerView recyclerTransactions;
    private TextView tvTotalSpent;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        initViews(view);
        setupRecyclerView();
        loadTransactionHistory();
        return view;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            loadTransactionHistory(); // Tải lại khi người dùng chuyển sang tab Lịch sử
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTransactionHistory(); // Tải lại khi quay lại từ màn hình thanh toán thành công
    }

    private void initViews(View view) {
        recyclerTransactions = view.findViewById(R.id.recycler_transactions);
        tvTotalSpent = view.findViewById(R.id.tv_total_spent);
    }

    private void setupRecyclerView() {
        recyclerTransactions.setLayoutManager(new LinearLayoutManager(getContext()));
        transactionAdapter = new TransactionAdapter(transactionList);
        recyclerTransactions.setAdapter(transactionAdapter);
    }

    private void loadTransactionHistory() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) return;

        bookingController.getBookingsByUser(userId, bookings -> {
            if (getActivity() == null || !isAdded()) return;
            getActivity().runOnUiThread(() -> {
                transactionList.clear();
                double total = 0;
                for (Booking b : bookings) {
                    String status = b.getStatus();
                    // Hiển thị cả giao dịch vừa "Xác nhận" và "Hoàn thành" vào lịch sử chi tiêu
                    if ("Completed".equalsIgnoreCase(status) || "Hoàn thành".equalsIgnoreCase(status) || "Confirmed".equalsIgnoreCase(status)) {
                        transactionList.add(b);
                        total += b.getTotalPrice();
                    } else if ("Cancelled".equalsIgnoreCase(status) || "Đã hủy".equalsIgnoreCase(status)) {
                        transactionList.add(b);
                    }
                }
                
                transactionAdapter.notifyDataSetChanged();
                tvTotalSpent.setText(String.format(Locale.getDefault(), "%,.0fđ", total));
            });
        }, error -> {
            if (getActivity() == null || !isAdded()) return;
            getActivity().runOnUiThread(() -> 
                Toast.makeText(getContext(), "Lỗi nạp lịch sử: " + error, Toast.LENGTH_SHORT).show()
            );
        });
    }
}
