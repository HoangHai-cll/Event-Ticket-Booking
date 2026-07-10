package vn.humg.hai.event_ticket_booking_app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ProgressBar;
import com.google.android.material.chip.ChipGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import vn.humg.hai.event_ticket_booking_app.controller.ConfigController;
import vn.humg.hai.event_ticket_booking_app.controller.UserController;
import android.graphics.Bitmap;
import android.widget.ImageView;
import androidx.appcompat.app.AlertDialog;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import java.util.ArrayList;
import java.util.List;
import vn.humg.hai.event_ticket_booking_app.R;
import vn.humg.hai.event_ticket_booking_app.adapter.TicketAdapter;
import vn.humg.hai.event_ticket_booking_app.controller.BookingController;
import vn.humg.hai.event_ticket_booking_app.controller.EventController;
import vn.humg.hai.event_ticket_booking_app.model.Booking;
import vn.humg.hai.event_ticket_booking_app.model.Event;
import java.util.HashMap;
import java.util.Map;
import java.util.Collections;

public class TicketsFragment extends Fragment {

    private final BookingController bookingController = new BookingController();
    private final EventController eventController = new EventController();
    private final UserController userController = new UserController();
    private final List<Booking> fullBookingList = new ArrayList<>();
    private final List<Booking> displayList = new ArrayList<>();
    private final Map<String, Event> eventCache = new HashMap<>();
    private TicketAdapter ticketAdapter;
    
    private RecyclerView recyclerTickets;
    private TextView tvTicketCount;
    private ChipGroup chipGroupStatus;
    private String currentStatusFilter = "Confirmed";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tickets, container, false);
        initViews(view);
        setupRecyclerView();
        initEvents(view);
        loadMyTickets();
        return view;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            loadMyTickets(); // Tải lại dữ liệu khi tab được hiện lên
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadMyTickets(); // Tải lại dữ liệu khi quay lại từ Activity khác
    }

    private void initViews(View view) {
        recyclerTickets = view.findViewById(R.id.recycler_tickets);
        tvTicketCount = view.findViewById(R.id.tv_ticket_count);
        chipGroupStatus = view.findViewById(R.id.chip_group_status);
    }

    private void setupRecyclerView() {
        recyclerTickets.setLayoutManager(new LinearLayoutManager(getContext()));
        ticketAdapter = new TicketAdapter(displayList, eventCache, this::showQRCodeDialog);
        recyclerTickets.setAdapter(ticketAdapter);
    }

    private void initEvents(View view) {
        View btnMenuDrawer = view.findViewById(R.id.btn_menu_drawer);
        if (btnMenuDrawer != null) {
            btnMenuDrawer.setOnClickListener(v -> {
                if (getActivity() instanceof vn.humg.hai.event_ticket_booking_app.view.MainActivity) {
                    ((vn.humg.hai.event_ticket_booking_app.view.MainActivity) getActivity()).openDrawer();
                }
            });
        }
        
        chipGroupStatus.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chip_active) filterBookings("Confirmed");
            else if (checkedId == R.id.chip_used) filterBookings("Used");
            else if (checkedId == R.id.chip_cancelled) filterBookings("Cancelled");
            else filterBookings("Confirmed");
        });
    }

    private void loadMyTickets() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) return;

        bookingController.getBookingsByUser(userId, bookings -> {
            if (!isAdded() || getActivity() == null) return;

            // Thu thập các ID sự kiện chưa có trong cache
            List<String> missingEventIds = new ArrayList<>();
            if (bookings != null) {
                for (Booking b : bookings) {
                    if (b != null && b.getEventId() != null && !eventCache.containsKey(b.getEventId())) {
                        missingEventIds.add(b.getEventId());
                    }
                }
            }

            Runnable updateUI = () -> {
                fullBookingList.clear();
                if (bookings != null) {
                    fullBookingList.addAll(bookings);
                }
                filterBookings(currentStatusFilter);
            };

            if (missingEventIds.isEmpty()) {
                getActivity().runOnUiThread(updateUI);
            } else {
                // Tải gộp các sự kiện còn thiếu trong 1 truy vấn duy nhất để giải quyết N+1 Query
                eventController.getEventsByIds(missingEventIds, events -> {
                    if (events != null) {
                        for (Event event : events) {
                            if (event != null && event.getEventId() != null) {
                                eventCache.put(event.getEventId(), event);
                            }
                        }
                    }
                    if (isAdded() && getActivity() != null) {
                        getActivity().runOnUiThread(updateUI);
                    }
                }, error -> {
                    if (isAdded() && getActivity() != null) {
                        getActivity().runOnUiThread(updateUI);
                    }
                });
            }
        }, error -> {
            if (!isAdded() || getActivity() == null) return;
            getActivity().runOnUiThread(() -> 
                Toast.makeText(getContext(), getString(R.string.msg_load_error), Toast.LENGTH_SHORT).show()
            );
        });
    }

    private void filterBookings(String status) {
        currentStatusFilter = status;
        displayList.clear();
        for (Booking b : fullBookingList) {
            if (status.equalsIgnoreCase(b.getStatus())) {
                displayList.add(b);
            }
            // Phase B: vé "Refund Pending" cũng hiển thị trong tab "Đã hủy" phía User
            else if ("Cancelled".equalsIgnoreCase(status) && "Refund Pending".equalsIgnoreCase(b.getStatus())) {
                displayList.add(b);
            }
        }
        
        Collections.sort(displayList, (b1, b2) -> {
            Event e1 = eventCache.get(b1.getEventId());
            Event e2 = eventCache.get(b2.getEventId());
            boolean isExpired1 = false;
            boolean isExpired2 = false;
            long currentTime = System.currentTimeMillis() / 1000;
            if (e1 != null && e1.getDate() != null) {
                isExpired1 = e1.getDate().getSeconds() < currentTime;
            }
            if (e2 != null && e2.getDate() != null) {
                isExpired2 = e2.getDate().getSeconds() < currentTime;
            }
            if (isExpired1 && !isExpired2) return 1;
            if (!isExpired1 && isExpired2) return -1;
            return 0;
        });
        
        ticketAdapter.notifyDataSetChanged();
        updateCountText(status);
    }

    private void updateCountText(String status) {
        if (displayList.isEmpty()) {
            tvTicketCount.setText(getString(R.string.msg_no_tickets));
        } else {
            tvTicketCount.setText(getString(R.string.msg_ticket_count, displayList.size()));
        }
    }

    private void showQRCodeDialog(Booking booking) {
        if (getContext() == null) return;

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_ticket_invoice, null);
        
        // --- Mapping Views ---
        ImageView ivQr = dialogView.findViewById(R.id.iv_qr_code);
        TextView tvId = dialogView.findViewById(R.id.tv_qr_booking_id);
        TextView tvLockedMsg = dialogView.findViewById(R.id.tv_qr_locked_message);
        View btnSave = dialogView.findViewById(R.id.btn_save_qr);
        View btnCancel = dialogView.findViewById(R.id.btn_cancel_ticket);
        
        TextView tvEventTitle = dialogView.findViewById(R.id.tv_invoice_event_title);
        TextView tvEventTime = dialogView.findViewById(R.id.tv_invoice_event_time);
        
        TextView tvInvBookingId = dialogView.findViewById(R.id.tv_invoice_booking_id);
        TextView tvInvPriceQty = dialogView.findViewById(R.id.tv_invoice_price_qty);
        TextView tvInvDiscount = dialogView.findViewById(R.id.tv_invoice_discount);
        TextView tvInvTotal = dialogView.findViewById(R.id.tv_invoice_total_paid);
        TextView tvInvPaymentInfo = dialogView.findViewById(R.id.tv_invoice_payment_info);
        TextView tvStatus = dialogView.findViewById(R.id.tv_invoice_checkin_status);

        // Hide Admin Sections
        dialogView.findViewById(R.id.layout_customer_info).setVisibility(View.GONE);
        dialogView.findViewById(R.id.btn_admin_confirm_checkin).setVisibility(View.GONE);

        // --- Populate Invoice Data ---
        Event event = eventCache.get(booking.getEventId());
        if (event != null) {
            tvEventTitle.setText(event.getTitle());
            if (event.getDate() != null) {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("EEEE, dd/MM/yyyy - HH:mm", java.util.Locale.getDefault());
                tvEventTime.setText(sdf.format(event.getDate().toDate()));
            }
        }

        String bId = booking.getBookingId();
        tvInvBookingId.setText("#" + (bId.length() > 12 ? bId.substring(0, 12) : bId).toUpperCase());
        double unitPrice = booking.getPricePerTicket();
        tvInvPriceQty.setText(String.format(java.util.Locale.getDefault(), "%,.0fđ x %d", unitPrice, booking.getQuantity()));
        tvInvDiscount.setText(String.format(java.util.Locale.getDefault(), "-%,.0fđ", booking.getDiscount()));
        tvInvTotal.setText(String.format(java.util.Locale.getDefault(), "%,.0fđ", booking.getTotalPrice()));
        tvInvPaymentInfo.setText("Thanh toán qua: " + (booking.getPaymentMethod() != null ? booking.getPaymentMethod() : "Ví điện tử") + 
                                 "\nMã GD: " + (booking.getPaymentId() != null ? booking.getPaymentId() : "N/A"));

        // Status Badge
        String statusText = "Trạng thái: " + booking.getStatus();
        if (booking.isCheckedIn()) {
            statusText = "✅ ĐÃ SỬ DỤNG (CHECKED-IN)";
            tvStatus.setBackgroundResource(R.drawable.bg_chip_light);
            tvStatus.setTextColor(android.graphics.Color.parseColor("#4CAF50"));
        } else if ("Confirmed".equals(booking.getStatus())) {
            statusText = "🎟️ CHƯA SỬ DỤNG";
            tvStatus.setBackgroundResource(R.drawable.bg_chip_light);
            tvStatus.setTextColor(android.graphics.Color.parseColor("#FF5C00"));
        } else {
            tvStatus.setTextColor(android.graphics.Color.parseColor("#F44336"));
        }
        tvStatus.setText(statusText);

        // --- QR Logic ---
        tvId.setText("Mã vé: #" + (bId.length() > 8 ? bId.substring(0, 8) : bId).toUpperCase());
        long thresholdMs = ConfigController.getInstance().getQrReleaseThresholdMs();
        boolean isQRActive = true;
        if (event != null && event.getDate() != null) {
            long eventTimeMs = event.getDate().toDate().getTime();
            long currentTimeMs = System.currentTimeMillis();
            isQRActive = (eventTimeMs - currentTimeMs <= thresholdMs);
        }

        // QR active only if Confirmed and not Checked-in
        boolean canShowQR = isQRActive && "Confirmed".equals(booking.getStatus()) && !booking.isCheckedIn();

        final Bitmap[] qrBitmap = {null};
        if (canShowQR) {
            ivQr.setVisibility(View.VISIBLE);
            if (tvLockedMsg != null) tvLockedMsg.setVisibility(View.GONE);
            if (btnSave != null) btnSave.setVisibility(View.VISIBLE);
            if (btnCancel != null) btnCancel.setVisibility(View.GONE);

            try {
                BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                Bitmap bitmap = barcodeEncoder.encodeBitmap(booking.getBookingId(), BarcodeFormat.QR_CODE, 600, 600);
                ivQr.setImageBitmap(bitmap);
                qrBitmap[0] = bitmap;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            ivQr.setVisibility(View.GONE);
            if (btnSave != null) btnSave.setVisibility(View.GONE);
            
            if (booking.isCheckedIn()) {
                if (tvLockedMsg != null) {
                    tvLockedMsg.setVisibility(View.VISIBLE);
                    tvLockedMsg.setText("Vé đã được sử dụng để vào cổng.");
                }
                if (btnCancel != null) btnCancel.setVisibility(View.GONE);
            } else if (!isQRActive && "Confirmed".equals(booking.getStatus())) {
                if (tvLockedMsg != null) {
                    tvLockedMsg.setVisibility(View.VISIBLE);
                    if (event != null && event.getDate() != null) {
                        long releaseTimeMs = event.getDate().toDate().getTime() - thresholdMs;
                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault());
                        tvLockedMsg.setText("Mã QR sẽ tự động kích hoạt vào:\n" + sdf.format(new java.util.Date(releaseTimeMs)));
                    }
                }
                if (btnCancel != null) btnCancel.setVisibility(View.VISIBLE);
            } else {
                if (tvLockedMsg != null) {
                    tvLockedMsg.setVisibility(View.VISIBLE);
                    tvLockedMsg.setText("Vé không khả dụng (Đã hủy hoặc chờ thanh toán)");
                }
                if (btnCancel != null) btnCancel.setVisibility(View.GONE);
            }
        }

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .create();

        dialogView.findViewById(R.id.btn_qr_close).setOnClickListener(v -> dialog.dismiss());
        View btnCloseBottom = dialogView.findViewById(R.id.btn_invoice_close_bottom);
        if (btnCloseBottom != null) {
            btnCloseBottom.setOnClickListener(v -> dialog.dismiss());
        }
        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> handleCancelTicket(booking, dialog));
        }

        if (btnSave != null) {
            btnSave.setOnClickListener(v -> {
                if (qrBitmap[0] != null) {
                    String bIdShort = booking.getBookingId();
                    if (bIdShort.length() > 8) bIdShort = bIdShort.substring(0, 8);
                    saveBitmapToGallery(qrBitmap[0], "Booking_" + bIdShort);
                }
            });
        }

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        dialog.show();
    }

    private void saveBitmapToGallery(Bitmap bitmap, String title) {
        if (getContext() == null) return;
        
        android.content.ContentResolver resolver = getContext().getContentResolver();
        android.content.ContentValues contentValues = new android.content.ContentValues();
        contentValues.put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, title + "_" + System.currentTimeMillis() + ".jpg");
        contentValues.put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            contentValues.put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_PICTURES + "/EventPass");
            contentValues.put(android.provider.MediaStore.MediaColumns.IS_PENDING, 1);
        }
        
        android.net.Uri imageUri = resolver.insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        if (imageUri != null) {
            try {
                java.io.OutputStream out = resolver.openOutputStream(imageUri);
                if (out != null) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    out.close();
                }
                
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    contentValues.clear();
                    contentValues.put(android.provider.MediaStore.MediaColumns.IS_PENDING, 0);
                    resolver.update(imageUri, contentValues, null, null);
                }
                
                Toast.makeText(getContext(), "Đã lưu vé vào Thư viện ảnh thành công! 💾", Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Không thể lưu vé: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "Không thể tạo file trong Thư viện", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleCancelTicket(Booking booking, AlertDialog parentDialog) {
        new AlertDialog.Builder(getContext())
                .setTitle("Xác nhận hủy vé?")
                .setMessage("Bạn có chắc chắn muốn hủy đơn vé này không? Tiền vé sẽ được hoàn trả vào tài khoản của bạn. Xin lưu ý: Quá trình hoàn tiền sẽ được xử lý trong vòng 3 ngày làm việc.")
                .setPositiveButton("Hủy vé & Hoàn tiền", (dialogInterface, i) -> {
                    parentDialog.dismiss();

                    // Tạo Loading Dialog
                    AlertDialog loadingDialog = new AlertDialog.Builder(getContext())
                            .setView(new ProgressBar(getContext()))
                            .setMessage("Đang gửi yêu cầu hoàn tiền...")
                            .setCancelable(false)
                            .create();
                    loadingDialog.show();

                    // Phase B: Đặt trạng thái sang "Refund Pending" — Admin sẽ duyệt và hoàn trả kho vé
                    booking.setStatus("Refund Pending");
                    bookingController.saveBooking(booking, () -> {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                loadingDialog.dismiss();
                                showRefundNoticeDialog();
                                loadMyTickets(); // Reload danh sách
                            });
                        }
                    }, err -> {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                loadingDialog.dismiss();
                                Toast.makeText(getContext(), "Lỗi gửi yêu cầu: " + err, Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
                })
                .setNegativeButton("Quay lại", null)
                .show();
    }

    private void showRefundNoticeDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("Đang chờ hoàn tiền")
                .setMessage("Yêu cầu hủy vé của bạn đã được ghi nhận thành công.\nHệ thống đang thực hiện hoàn tiền. Vui lòng chờ đợi trong vòng 3 ngày làm việc để tiền được cộng về tài khoản.")
                .setPositiveButton("Đồng ý", null)
                .show();
    }
}
