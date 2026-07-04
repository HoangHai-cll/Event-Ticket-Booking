package vn.humg.hai.event_ticket_booking_app.view;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import java.util.ArrayList;
import java.util.List;
import vn.humg.hai.event_ticket_booking_app.R;
import vn.humg.hai.event_ticket_booking_app.adapter.AdminBookingAdapter;
import vn.humg.hai.event_ticket_booking_app.controller.BookingController;
import vn.humg.hai.event_ticket_booking_app.controller.EventController;
import vn.humg.hai.event_ticket_booking_app.model.Booking;

public class AdminManageBookingsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AdminBookingAdapter adapter;
    private final List<Booking> bookingList = new ArrayList<>();
    private final BookingController bookingController = new BookingController();
    private final EventController eventController = new EventController();
    private String currentAdminId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage_bookings);

        currentAdminId = FirebaseAuth.getInstance().getUid();

        initViews();
        setupRecyclerView();
        loadFilteredBookings();
    }

    private void initViews() {
        findViewById(R.id.btn_back_custom).setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        recyclerView = findViewById(R.id.recycler_admin_all_bookings);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminBookingAdapter(bookingList, new AdminBookingAdapter.OnBookingActionListener() {
            @Override
            public void onConfirm(Booking booking) {
                updateStatus(booking, "Completed", "Đã xác nhận hoàn thành giao dịch");
            }

            @Override
            public void onCancel(Booking booking) {
                showCancelDialog(booking);
            }
        });
        recyclerView.setAdapter(adapter);
    }

    private void loadFilteredBookings() {
        bookingController.getBookingsBySeller(currentAdminId, bookings -> {
            runOnUiThread(() -> {
                bookingList.clear();
                bookingList.addAll(bookings);
                adapter.notifyDataSetChanged();
            });
        }, error -> runOnUiThread(() -> Toast.makeText(this, "Lỗi tải đơn hàng: " + error, Toast.LENGTH_SHORT).show()));
    }

    private void updateStatus(Booking booking, String newStatus, String message) {
        // Cập nhật thêm thông tin người xử lý để kiểm soát
        booking.setStatus(newStatus);
        booking.setProcessedBy(currentAdminId);
        booking.setUpdatedAt(Timestamp.now());

        // Gọi hàm cập nhật (Cần đảm bảo BookingController có hàm updateBooking hoặc dùng saveBooking)
        bookingController.saveBooking(booking, () -> {
            runOnUiThread(() -> {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                loadFilteredBookings(); 
            });
        }, error -> runOnUiThread(() -> Toast.makeText(this, "Lỗi cập nhật: " + error, Toast.LENGTH_SHORT).show()));
    }

    private void showCancelDialog(Booking booking) {
        new AlertDialog.Builder(this)
                .setTitle("Hủy đơn hàng")
                .setMessage("Bạn có chắc chắn muốn hủy đơn hàng này không?")
                .setPositiveButton("Hủy đơn", (dialog, which) -> {
                    updateStatus(booking, "Cancelled", "Đã hủy đơn hàng thành công");
                })
                .setNegativeButton("Quay lại", null)
                .show();
    }
}
