package vn.humg.hai.event_ticket_booking_app.view;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import androidx.activity.result.ActivityResultLauncher;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
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
    private final List<Booking> displayList = new ArrayList<>();
    private final List<Booking> fullBookingList = new ArrayList<>();
    private final BookingController bookingController = new BookingController();
    private final EventController eventController = new EventController();
    private final vn.humg.hai.event_ticket_booking_app.controller.UserController userController = new vn.humg.hai.event_ticket_booking_app.controller.UserController();
    private String currentAdminId;
    // Phase B: trạng thái filter hiện tại
    private String currentFilter = "ALL";
    private FloatingActionButton fabScan;

    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(),
            result -> {
                if (result.getContents() == null) {
                    Toast.makeText(AdminManageBookingsActivity.this, "Đã hủy quét mã QR", Toast.LENGTH_SHORT).show();
                } else {
                    handleScannedBookingId(result.getContents());
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage_bookings);

        currentAdminId = FirebaseAuth.getInstance().getUid();

        initViews();
        setupRecyclerView();
        loadAllBookings();
    }

    private void initViews() {
        findViewById(R.id.btn_back_custom).setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        recyclerView = findViewById(R.id.recycler_admin_all_bookings);

        // Phase B: Ánh xạ ChipGroup và lắng nghe thay đổi filter
        ChipGroup chipGroup = findViewById(R.id.chip_group_admin_booking_status);
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if (id == R.id.chip_admin_bookings_refund_pending) {
                currentFilter = "Refund Pending";
            } else if (id == R.id.chip_admin_bookings_confirmed) {
                currentFilter = "Confirmed";
            } else if (id == R.id.chip_admin_bookings_cancelled) {
                currentFilter = "Cancelled";
            } else {
                currentFilter = "ALL";
            }
            applyFilter();
        });

        fabScan = findViewById(R.id.fab_scan_checkin);
        if (fabScan != null) {
            fabScan.setOnClickListener(v -> {
                ScanOptions options = new ScanOptions();
                options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
                options.setPrompt("Đặt mã QR vé của khách vào khung để quét check-in");
                options.setCameraId(0);
                options.setBeepEnabled(true);
                options.setBarcodeImageEnabled(true);
                options.setOrientationLocked(false);
                barcodeLauncher.launch(options);
            });
        }
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminBookingAdapter(displayList, new AdminBookingAdapter.OnBookingActionListener() {
            @Override
            public void onApproveRefund(Booking booking) {
                // Phase B: Admin phê duyệt → Cancelled + hoàn trả kho vé
                showApproveRefundDialog(booking);
            }

            @Override
            public void onRejectRefund(Booking booking) {
                // Phase B: Admin từ chối → khôi phục sang Confirmed
                showRejectRefundDialog(booking);
            }

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

    private void loadAllBookings() {
        if (currentAdminId == null) return;
        userController.getAdminById(currentAdminId, admin -> {
            int accessLevel = (admin != null) ? admin.getAccessLevel() : 1;
            
            if (accessLevel == 3) {
                // Developer: tải tất cả giao dịch trong hệ thống
                bookingController.getAllBookings(bookings -> {
                    runOnUiThread(() -> {
                        fullBookingList.clear();
                        fullBookingList.addAll(bookings);
                        applyFilter();
                    });
                }, error -> runOnUiThread(() ->
                        Toast.makeText(this, "Lỗi tải đơn hàng: " + error, Toast.LENGTH_SHORT).show()));
            } else {
                // Staff (Cấp 1) và Manager (Cấp 2): Chỉ tải các giao dịch thuộc sự kiện của admin này
                bookingController.getBookingsBySeller(currentAdminId, bookings -> {
                    runOnUiThread(() -> {
                        fullBookingList.clear();
                        fullBookingList.addAll(bookings);
                        applyFilter();
                    });
                }, error -> runOnUiThread(() ->
                        Toast.makeText(this, "Lỗi tải đơn hàng: " + error, Toast.LENGTH_SHORT).show()));
            }
        }, err -> {
            // Mặc định lọc theo sellerId nếu xảy ra lỗi profile
            bookingController.getBookingsBySeller(currentAdminId, bookings -> {
                runOnUiThread(() -> {
                    fullBookingList.clear();
                    fullBookingList.addAll(bookings);
                    applyFilter();
                });
            }, error -> runOnUiThread(() ->
                    Toast.makeText(this, "Lỗi tải đơn hàng: " + error, Toast.LENGTH_SHORT).show()));
        });
    }

    // Phase B: Lọc danh sách theo filter đang chọn
    private void applyFilter() {
        displayList.clear();
        for (Booking b : fullBookingList) {
            if ("ALL".equals(currentFilter) || currentFilter.equalsIgnoreCase(b.getStatus())) {
                displayList.add(b);
            }
        }
        adapter.notifyDataSetChanged();
    }

    // Phase B: Dialog xác nhận phê duyệt hoàn tiền
    private void showApproveRefundDialog(Booking booking) {
        new AlertDialog.Builder(this)
                .setTitle("Phê duyệt hoàn tiền")
                .setMessage("Xác nhận đã hoàn tiền cho khách?\nĐơn vé sẽ chuyển sang trạng thái Đã hủy và kho vé sẽ được cộng trả lại.")
                .setPositiveButton("Xác nhận đã hoàn tiền", (d, w) -> {
                    // 1. Cộng trả kho vé tổng
                    eventController.getEventById(booking.getEventId(), event -> {
                        if (event != null) {
                            String tierId = booking.getTierId();

                            Runnable updateOverall = () ->
                                eventController.updateRemainingTicket(event.getEventId(), -booking.getQuantity(), () -> {
                                    // 2. Sau khi cập nhật kho → đổi trạng thái sang Cancelled
                                    updateStatus(booking, "Cancelled", "Đã phê duyệt hoàn tiền thành công");
                                }, err -> updateStatus(booking, "Cancelled", "Đã phê duyệt hoàn tiền thành công"));

                            if (tierId != null && !tierId.isEmpty() && event.hasTiers()) {
                                // Hoàn trả kho hạng vé cụ thể
                                eventController.updateTierRemainingTicket(event.getEventId(), tierId, -booking.getQuantity(),
                                        updateOverall::run, err -> updateOverall.run());
                            } else {
                                updateOverall.run();
                            }
                        } else {
                            updateStatus(booking, "Cancelled", "Đã phê duyệt hoàn tiền thành công");
                        }
                    }, err -> updateStatus(booking, "Cancelled", "Đã phê duyệt hoàn tiền thành công"));
                })
                .setNegativeButton("Quay lại", null)
                .show();
    }

    // Phase B: Dialog từ chối hoàn tiền — khôi phục vé sang Confirmed
    private void showRejectRefundDialog(Booking booking) {
        new AlertDialog.Builder(this)
                .setTitle("Từ chối hoàn tiền")
                .setMessage("Từ chối yêu cầu hoàn tiền? Đơn vé sẽ được khôi phục về trạng thái Đã xác nhận.")
                .setPositiveButton("Từ chối & Khôi phục vé", (d, w) ->
                        updateStatus(booking, "Confirmed", "Đã từ chối hoàn tiền, vé được khôi phục"))
                .setNegativeButton("Quay lại", null)
                .show();
    }

    private void updateStatus(Booking booking, String newStatus, String message) {
        booking.setStatus(newStatus);
        booking.setProcessedBy(currentAdminId);
        booking.setUpdatedAt(Timestamp.now());

        bookingController.saveBooking(booking, () -> {
            if ("Completed".equalsIgnoreCase(newStatus)) {
                userController.incrementAdminBookingCount(currentAdminId, booking.getQuantity(), updatedAdmin -> {
                    runOnUiThread(() -> {
                        // Kiểm tra nếu admin được nâng lên Cấp 2 ngay sau giao dịch này
                        if (updatedAdmin.getAccessLevel() == 2 && updatedAdmin.getBookingsConfirmedCount() >= 10 && (updatedAdmin.getBookingsConfirmedCount() - booking.getQuantity() < 10)) {
                            Toast.makeText(this, "🎉 Chúc mừng! Bạn đã bán đủ " + updatedAdmin.getBookingsConfirmedCount() + " vé và được nâng lên Admin Cấp 2!", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(this, message + " (+ " + booking.getQuantity() + " vé)", Toast.LENGTH_SHORT).show();
                        }
                        loadAllBookings();
                    });
                }, err -> runOnUiThread(() -> {
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                    loadAllBookings();
                }));
            } else {
                runOnUiThread(() -> {
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                    loadAllBookings();
                });
            }
        }, error -> runOnUiThread(() ->
                Toast.makeText(this, "Lỗi cập nhật: " + error, Toast.LENGTH_SHORT).show()));
    }

    private void showCancelDialog(Booking booking) {
        new AlertDialog.Builder(this)
                .setTitle("Hủy đơn hàng")
                .setMessage("Bạn có chắc chắn muốn hủy đơn hàng này không?")
                .setPositiveButton("Hủy đơn", (dialog, which) ->
                        updateStatus(booking, "Cancelled", "Đã hủy đơn hàng thành công"))
                .setNegativeButton("Quay lại", null)
                .show();
    }

    private void handleScannedBookingId(String bookingId) {
        if (bookingId == null || bookingId.trim().isEmpty()) {
            Toast.makeText(this, "Mã vé quét được không hợp lệ!", Toast.LENGTH_SHORT).show();
            return;
        }

        bookingController.getBookingById(bookingId, booking -> {
            if (booking == null) {
                runOnUiThread(() -> Toast.makeText(this, "Không tìm thấy thông tin vé trên hệ thống!", Toast.LENGTH_LONG).show());
                return;
            }

            // Kiểm tra phân quyền: Staff/Manager chỉ được check-in cho sự kiện của họ.
            // Cấp 3 (Developer) được check-in mọi sự kiện.
            userController.getAdminById(currentAdminId, admin -> {
                int accessLevel = (admin != null) ? admin.getAccessLevel() : 1;
                if (accessLevel < 3 && !currentAdminId.equals(booking.getSellerId())) {
                    runOnUiThread(() -> Toast.makeText(this, "Bạn không có quyền check-in vé của sự kiện từ tài khoản khác!", Toast.LENGTH_LONG).show());
                    return;
                }

                runOnUiThread(() -> processCheckIn(booking));
            }, err -> {
                if (!currentAdminId.equals(booking.getSellerId())) {
                    runOnUiThread(() -> Toast.makeText(this, "Bạn không có quyền check-in vé của sự kiện từ tài khoản khác!", Toast.LENGTH_LONG).show());
                } else {
                    runOnUiThread(() -> processCheckIn(booking));
                }
            });

        }, error -> runOnUiThread(() -> Toast.makeText(this, "Lỗi truy vấn vé: " + error, Toast.LENGTH_SHORT).show()));
    }

    private void processCheckIn(Booking booking) {
        String status = booking.getStatus() != null ? booking.getStatus() : "";
        
        // 1. Cảnh báo vé đã sử dụng / check-in trùng lặp
        if (booking.isCheckedIn() || "Completed".equalsIgnoreCase(status)) {
            String checkInTimeStr = "";
            if (booking.getCheckInAt() != null) {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm dd/MM/yyyy", java.util.Locale.getDefault());
                checkInTimeStr = sdf.format(booking.getCheckInAt().toDate());
            }
            new AlertDialog.Builder(this)
                .setTitle("CẢNH BÁO: VÉ ĐĂ ĐƯỢC SỬ DỤNG ⚠️")
                .setMessage("Vé này đã được check-in trước đó!\n" +
                            "• Thời gian: " + checkInTimeStr + "\n" +
                            "• Người quét: " + (booking.getCheckInBy() != null ? "Admin/Staff ID: " + booking.getCheckInBy().substring(0, 8).toUpperCase() : "Hệ thống") + "\n" +
                            "• Khách hàng: " + booking.getUserId())
                .setPositiveButton("Đóng", null)
                .show();
            return;
        }

        // 2. Cảnh báo vé bị hủy hoặc lỗi thanh toán
        if ("Cancelled".equalsIgnoreCase(status) || "Refund Pending".equalsIgnoreCase(status) || "Pending Payment".equalsIgnoreCase(status)) {
            new AlertDialog.Builder(this)
                .setTitle("CẢNH BÁO: VÉ KHÔNG HỢP LỆ ❌")
                .setMessage("Vé này không đủ điều kiện check-in!\n" +
                            "• Trạng thái vé: " + status + "\n" +
                            "• Khách hàng: " + booking.getUserId())
                .setPositiveButton("Đóng", null)
                .show();
            return;
        }

        // 3. Tiến hành check-in vé hợp lệ
        if ("Confirmed".equalsIgnoreCase(status)) {
            eventController.getEventById(booking.getEventId(), event -> {
                String eventName = event != null ? event.getTitle() : "Sự kiện";
                
                runOnUiThread(() -> {
                    new AlertDialog.Builder(this)
                        .setTitle("Xác nhận Check-in vé 🎟️")
                        .setMessage("Thông tin khách hàng:\n" +
                                    "• Sự kiện: " + eventName + "\n" +
                                    "• Hạng vé: " + (booking.getTierName() != null ? booking.getTierName() : "Standard") + "\n" +
                                    "• Số lượng: " + booking.getQuantity() + " vé\n" +
                                    "• Tổng chi tiêu: " + String.format(java.util.Locale.getDefault(), "%,.0fđ", booking.getTotalPrice()))
                        .setPositiveButton("Xác nhận Check-in", (dialog, which) -> {
                            booking.setCheckedIn(true);
                            booking.setCheckInAt(Timestamp.now());
                            booking.setCheckInBy(currentAdminId);
                            updateStatus(booking, "Completed", "Check-in thành công");
                        })
                        .setNegativeButton("Hủy bỏ", null)
                        .show();
                });
            }, err -> {
                runOnUiThread(() -> {
                    booking.setCheckedIn(true);
                    booking.setCheckInAt(Timestamp.now());
                    booking.setCheckInBy(currentAdminId);
                    updateStatus(booking, "Completed", "Check-in thành công");
                });
            });
        }
    }
}
