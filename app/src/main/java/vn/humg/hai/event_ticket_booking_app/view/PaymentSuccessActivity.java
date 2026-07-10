package vn.humg.hai.event_ticket_booking_app.view;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import vn.humg.hai.event_ticket_booking_app.controller.ConfigController;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import java.text.SimpleDateFormat;
import java.util.Locale;
import vn.humg.hai.event_ticket_booking_app.R;
import vn.humg.hai.event_ticket_booking_app.controller.EventController;

public class PaymentSuccessActivity extends AppCompatActivity {

    private TextView tvEventTitle, tvDate, tvTime, tvQuantity, tvBookingId, tvCategory, tvQrNotice;
    private ImageView ivQr;
    private MaterialButton btnViewTickets, btnGoHome;
    private final EventController eventController = new EventController();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_success);

        initViews();
        loadTicketData();
        setupEvents();
    }

    private void initViews() {
        tvEventTitle = findViewById(R.id.tv_success_event_title);
        tvDate = findViewById(R.id.tv_success_date);
        tvTime = findViewById(R.id.tv_success_time);
        tvQuantity = findViewById(R.id.tv_success_quantity);
        tvBookingId = findViewById(R.id.tv_success_booking_id);
        tvCategory = findViewById(R.id.tv_success_category);
        tvQrNotice = findViewById(R.id.tv_success_qr_notice);
        ivQr = findViewById(R.id.iv_success_qr);
        btnViewTickets = findViewById(R.id.btn_view_tickets);
        btnGoHome = findViewById(R.id.btn_go_home);
    }

    private void loadTicketData() {
        String bookingId = getIntent().getStringExtra("EXTRA_BOOKING_ID");
        String eventId = getIntent().getStringExtra("EXTRA_EVENT_ID");
        int quantity = getIntent().getIntExtra("EXTRA_QUANTITY", 1);

        tvBookingId.setText(bookingId);
        tvQuantity.setText(getString(R.string.label_event_summary_format, "", quantity).replace(" • ", "").trim());

        eventController.getEventById(eventId, event -> {
            if (event != null) {
                // Get threshold from ConfigController
                long thresholdMs = ConfigController.getInstance().getQrReleaseThresholdMs();

                // Determine if QR is active (X hours before event starts)
                boolean isQRActive = true;
                if (event.getDate() != null) {
                    long eventTimeMs = event.getDate().toDate().getTime();
                    long currentTimeMs = System.currentTimeMillis();
                    long timeDiff = eventTimeMs - currentTimeMs;
                    isQRActive = (timeDiff <= thresholdMs);
                }

                final boolean finalIsQRActive = isQRActive;

                runOnUiThread(() -> {
                    tvEventTitle.setText(event.getTitle());
                    tvCategory.setText(event.getCategory() != null ? event.getCategory() : getString(R.string.app_name));
                    
                    if (event.getDate() != null) {
                        SimpleDateFormat dateFmt = new SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault());
                        SimpleDateFormat timeFmt = new SimpleDateFormat("HH:mm", Locale.getDefault());
                        tvDate.setText(dateFmt.format(event.getDate().toDate()));
                        tvTime.setText(timeFmt.format(event.getDate().toDate()));
                    }

                    if (finalIsQRActive) {
                        if (tvQrNotice != null) tvQrNotice.setVisibility(android.view.View.GONE);
                        // Generate QR
                        if (bookingId != null) {
                            new Thread(() -> {
                                try {
                                    BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                                    final Bitmap bitmap = barcodeEncoder.encodeBitmap(bookingId, BarcodeFormat.QR_CODE, 400, 400);
                                    runOnUiThread(() -> ivQr.setImageBitmap(bitmap));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }).start();
                        }
                    } else {
                        // QR is locked - show placeholder logo & notice message
                        ivQr.setImageResource(R.drawable.img_logo_event_ticket_booking);
                        if (tvQrNotice != null) {
                            tvQrNotice.setVisibility(android.view.View.VISIBLE);
                        }
                    }
                });
            } else {
                // Fallback: show QR if event not found
                if (bookingId != null) {
                    new Thread(() -> {
                        try {
                            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                            final Bitmap bitmap = barcodeEncoder.encodeBitmap(bookingId, BarcodeFormat.QR_CODE, 400, 400);
                            runOnUiThread(() -> ivQr.setImageBitmap(bitmap));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }).start();
                }
            }
        }, error -> {
            // Fallback on error
            if (bookingId != null) {
                new Thread(() -> {
                    try {
                        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                        final Bitmap bitmap = barcodeEncoder.encodeBitmap(bookingId, BarcodeFormat.QR_CODE, 400, 400);
                        runOnUiThread(() -> ivQr.setImageBitmap(bitmap));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        });
    }

    private void setupEvents() {
        btnViewTickets.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("TARGET_TAB", "Tickets");
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        btnGoHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }
}
