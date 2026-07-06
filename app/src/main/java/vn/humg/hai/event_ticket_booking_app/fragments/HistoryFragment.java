package vn.humg.hai.event_ticket_booking_app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.chip.ChipGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Collections;
import vn.humg.hai.event_ticket_booking_app.R;
import vn.humg.hai.event_ticket_booking_app.adapter.TransactionAdapter;
import vn.humg.hai.event_ticket_booking_app.controller.BookingController;
import vn.humg.hai.event_ticket_booking_app.model.Booking;

public class HistoryFragment extends Fragment {

    private final BookingController bookingController = new BookingController();
    private final List<Booking> transactionList = new ArrayList<>();
    private TransactionAdapter transactionAdapter;

    private RecyclerView recyclerTransactions;
    private ChipGroup chipGroupStatus;
    private View btnViewPastMonths;
    private List<Booking> allBookings = new ArrayList<>();

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
        chipGroupStatus = view.findViewById(R.id.chip_group_status);
        btnViewPastMonths = view.findViewById(R.id.btn_view_past_months);
        
        View btnMenuDrawer = view.findViewById(R.id.btn_menu_drawer);
        if (btnMenuDrawer != null) {
            btnMenuDrawer.setOnClickListener(v -> {
                if (getActivity() instanceof vn.humg.hai.event_ticket_booking_app.view.MainActivity) {
                    ((vn.humg.hai.event_ticket_booking_app.view.MainActivity) getActivity()).openDrawer();
                }
            });
        }

        chipGroupStatus.setOnCheckedChangeListener((group, checkedId) -> filterTransactions(checkedId));

        if (btnViewPastMonths != null) {
            btnViewPastMonths.setOnClickListener(v -> showPastMonthsStatsDialog());
        }
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
                allBookings.clear();
                if (bookings != null) {
                    allBookings.addAll(bookings);
                }
                filterTransactions(chipGroupStatus.getCheckedChipId());
            });
        }, error -> {
            if (getActivity() == null || !isAdded()) return;
            getActivity().runOnUiThread(() -> 
                Toast.makeText(getContext(), "Lỗi nạp lịch sử: " + error, Toast.LENGTH_SHORT).show()
            );
        });
    }

    private void filterTransactions(int checkedId) {
        transactionList.clear();
        for (Booking b : allBookings) {
            String status = b.getStatus() != null ? b.getStatus() : "";
            boolean isCompleted = status.equalsIgnoreCase("Completed") || status.equalsIgnoreCase("Hoàn thành") || status.equalsIgnoreCase("Confirmed");
            boolean isCancelled = status.equalsIgnoreCase("Cancelled") || status.equalsIgnoreCase("Đã hủy");
            boolean isProcessing = status.equalsIgnoreCase("Pending Payment") || status.equalsIgnoreCase("Pending");

            if (checkedId == R.id.chip_completed && isCompleted) {
                transactionList.add(b);
            } else if (checkedId == R.id.chip_cancelled && isCancelled) {
                transactionList.add(b);
            } else if (checkedId == R.id.chip_processing && isProcessing) {
                transactionList.add(b);
            } else if (checkedId == R.id.chip_all || checkedId == View.NO_ID) {
                transactionList.add(b);
            }
        }
        transactionAdapter.notifyDataSetChanged();
    }



    /** Hiển thị hộp thoại chứa dữ liệu thống kê chi tiêu lưu lại của các tháng trước */
    private void showPastMonthsStatsDialog() {
        if (getContext() == null || allBookings.isEmpty()) {
            Toast.makeText(getContext(), "Không có lịch sử chi tiêu", Toast.LENGTH_SHORT).show();
            return;
        }

        // Nhóm các giao dịch thành công theo Tháng/Năm
        java.util.Map<String, MonthlyRecord> recordsMap = new java.util.HashMap<>();

        for (Booking b : allBookings) {
            String status = b.getStatus() != null ? b.getStatus() : "";
            boolean isCompleted = status.equalsIgnoreCase("Completed") || status.equalsIgnoreCase("Hoàn thành") || status.equalsIgnoreCase("Confirmed");
            if (isCompleted && b.getBookingDate() != null) {
                java.util.Calendar bCal = java.util.Calendar.getInstance();
                bCal.setTime(b.getBookingDate().toDate());
                int m = bCal.get(java.util.Calendar.MONTH); // 0-indexed
                int y = bCal.get(java.util.Calendar.YEAR);

                String key = String.format(Locale.getDefault(), "%02d/%d", m + 1, y);
                int sortKey = y * 100 + m;

                if (!recordsMap.containsKey(key)) {
                    MonthlyRecord rec = new MonthlyRecord();
                    rec.title = "Tháng " + (m + 1) + "/" + y;
                    rec.spent = 0;
                    rec.saved = 0;
                    rec.sortKey = sortKey;
                    recordsMap.put(key, rec);
                }

                MonthlyRecord rec = recordsMap.get(key);
                if (rec != null) {
                    rec.spent += b.getTotalPrice();
                    rec.saved += b.getDiscount();
                }
            }
        }

        List<MonthlyRecord> sortedRecords = new ArrayList<>(recordsMap.values());
        // Sắp xếp các tháng giảm dần (mới nhất lên đầu)
        Collections.sort(sortedRecords, (r1, r2) -> Integer.compare(r2.sortKey, r1.sortKey));

        if (sortedRecords.isEmpty()) {
            Toast.makeText(getContext(), "Không có lịch sử chi tiêu các tháng trước", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo chuỗi HTML hiển thị báo cáo chi tiết
        StringBuilder sb = new StringBuilder();
        sb.append("<font color='#5A647A'>Báo cáo chi tiêu & tiết kiệm đã lưu qua từng tháng:</font><br/><br/>");
        for (MonthlyRecord r : sortedRecords) {
            sb.append("<b><font color='#0E1322'>• ").append(r.title).append("</font></b><br/>");
            sb.append("&nbsp;&nbsp;&nbsp;&nbsp;Tổng chi tiêu: <font color='#0E1322'><b>").append(formatVND(r.spent)).append("</b></font><br/>");
            sb.append("&nbsp;&nbsp;&nbsp;&nbsp;Voucher tiết kiệm: <font color='#10B981'><b>").append(formatVND(r.saved)).append("</b></font><br/><br/>");
        }

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
        builder.setTitle("Lịch sử chi tiêu theo tháng");
        builder.setIcon(R.drawable.ic_chart);

        android.widget.ScrollView scrollView = new android.widget.ScrollView(getContext());
        TextView tvContent = new TextView(getContext());
        tvContent.setPadding(48, 24, 48, 24);
        tvContent.setTextSize(14f);
        tvContent.setTextColor(0xFF0E1322);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            tvContent.setText(android.text.Html.fromHtml(sb.toString(), android.text.Html.FROM_HTML_MODE_LEGACY));
        } else {
            tvContent.setText(android.text.Html.fromHtml(sb.toString()));
        }

        scrollView.addView(tvContent);
        builder.setView(scrollView);
        builder.setPositiveButton("Đóng", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private String formatVND(double amount) {
        return String.format(Locale.getDefault(), "%,.0fđ", amount);
    }

    /** Lớp phụ lưu trữ bản ghi chi tiêu theo tháng */
    private static class MonthlyRecord {
        String title;
        double spent;
        double saved;
        int sortKey;
    }
}
