package vn.humg.hai.event_ticket_booking_app.view;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
    private Toolbar toolbar;
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
        toolbar = findViewById(R.id.toolbar_admin_bookings);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

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
        // 1. Lấy danh sách sự kiện của Admin này trước
        eventController.getAllEvents(events -> {
            Set<String> myEventIds = new HashSet<>();
            for (vn.humg.hai.event_ticket_booking_app.model.Event e : events) {
                if (currentAdminId != null && currentAdminId.equals(e.getCreatedByAdminId())) {
                    myEventIds.add(e.getEventId());
                }
            }

            // 2. Tải tất cả đơn hàng và lọc theo sự kiện của mình
            bookingController.getAllBookings(bookings -> {
                runOnUiThread(() -> {
                    bookingList.clear();
                    for (Booking b : bookings) {
                        if (myEventIds.contains(b.getEventId())) {
                            bookingList.add(b);
                        }
                    }
                    adapter.notifyDataSetChanged();
                });
            }, error -> runOnUiThread(() -> Toast.makeText(this, "Lỗi tải đơn hàng", Toast.LENGTH_SHORT).show()));

        }, error -> runOnUiThread(() -> Toast.makeText(this, "Lỗi tải dữ liệu sự kiện", Toast.LENGTH_SHORT).show()));
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
