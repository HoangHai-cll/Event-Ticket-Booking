package vn.humg.hai.event_ticket_booking_app.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import java.text.SimpleDateFormat;
import java.util.Locale;
import vn.humg.hai.event_ticket_booking_app.R;
import vn.humg.hai.event_ticket_booking_app.controller.EventController;
import vn.humg.hai.event_ticket_booking_app.model.Event;

public class SelectTicketActivity extends AppCompatActivity {

    private String eventId;
    private Event currentEvent;
    private int quantity = 1;
    private double basePrice = 0;
    private final double feePercent = 0.05;
    private double discountAmount = 0;
    private String appliedVoucherCode = "";
    private boolean isVoucherApplied = false;

    private ImageView ivEventImage;
    private TextView tvEventTitle, tvEventDate, tvEventLocation;
    private TextView tvUnitPrice, tvQuantity, tvSummaryQty, tvSummaryBase, tvSummaryFee, tvSummaryTotal, tvSummaryDiscount;
    private ImageButton btnMinus, btnPlus;
    private MaterialButton btnConfirm;
    private EditText etVoucher;
    private TextView btnApplyVoucher;
    private LinearLayout layoutDiscount;
    private Toolbar toolbar;

    private final EventController eventController = new EventController();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_ticket);

        eventId = getIntent().getStringExtra("EXTRA_EVENT_ID");
        if (eventId == null) {
            finish();
            return;
        }

        initViews();
        loadEventData();
        setupEvents();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar_select_ticket);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.label_select_ticket);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        ivEventImage = findViewById(R.id.iv_select_event_image);
        tvEventTitle = findViewById(R.id.tv_select_event_title);
        tvEventDate = findViewById(R.id.tv_select_event_date);
        tvEventLocation = findViewById(R.id.tv_select_event_location);

        tvUnitPrice = findViewById(R.id.tv_ticket_unit_price);
        tvQuantity = findViewById(R.id.tv_quantity);
        btnMinus = findViewById(R.id.btn_minus_qty);
        btnPlus = findViewById(R.id.btn_plus_qty);

        tvSummaryQty = findViewById(R.id.tv_summary_qty_price);
        tvSummaryBase = findViewById(R.id.tv_summary_base_price);
        tvSummaryFee = findViewById(R.id.tv_summary_fee);
        tvSummaryDiscount = findViewById(R.id.tv_summary_discount);
        tvSummaryTotal = findViewById(R.id.tv_summary_total);
        layoutDiscount = findViewById(R.id.layout_discount);

        etVoucher = findViewById(R.id.et_voucher_code);
        btnApplyVoucher = findViewById(R.id.btn_apply_voucher);

        btnConfirm = findViewById(R.id.btn_confirm_select);
    }

    private void loadEventData() {
        eventController.getEventById(eventId, event -> {
            if (event != null) {
                runOnUiThread(() -> {
                    currentEvent = event;
                    basePrice = event.getPrice();
                    displayEventInfo();
                    updateCalculation();
                });
            }
        }, error -> runOnUiThread(() -> 
            Toast.makeText(this, R.string.msg_load_error, Toast.LENGTH_SHORT).show()
        ));
    }

    private void displayEventInfo() {
        tvEventTitle.setText(currentEvent.getTitle());
        tvEventLocation.setText(currentEvent.getLocation());
        tvUnitPrice.setText(formatPrice(basePrice));
        
        if (currentEvent.getDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault());
            tvEventDate.setText(String.format("📅 %s", sdf.format(currentEvent.getDate().toDate())));
        }
        
        Glide.with(this)
                .load(currentEvent.getImage())
                .placeholder(R.drawable.img_logo_event_ticket_booking)
                .centerCrop()
                .into(ivEventImage);
    }

    private void setupEvents() {
        btnPlus.setOnClickListener(v -> {
            quantity++;
            updateCalculation();
        });

        btnMinus.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                updateCalculation();
            }
        });

        btnApplyVoucher.setOnClickListener(v -> {
            String code = etVoucher.getText().toString().trim().toUpperCase();
            if (code.equals("EVENT2026") || code.equals("HELLOSUMMER")) {
                appliedVoucherCode = code;
                isVoucherApplied = true;
                layoutDiscount.setVisibility(View.VISIBLE);
                Toast.makeText(this, "Đã áp dụng mã giảm giá 10%!", Toast.LENGTH_SHORT).show();
            } else {
                appliedVoucherCode = "";
                isVoucherApplied = false;
                layoutDiscount.setVisibility(View.GONE);
                Toast.makeText(this, "Mã giảm giá không hợp lệ", Toast.LENGTH_SHORT).show();
            }
            updateCalculation();
        });

        btnConfirm.setOnClickListener(v -> {
            Intent intent = new Intent(this, PaymentActivity.class);
            intent.putExtra("EXTRA_EVENT_ID", eventId);
            intent.putExtra("EXTRA_QUANTITY", quantity);
            intent.putExtra("EXTRA_TOTAL_PRICE", calculateFinalTotal());
            intent.putExtra("EXTRA_DISCOUNT", discountAmount);
            intent.putExtra("EXTRA_VOUCHER_CODE", appliedVoucherCode);
            startActivity(intent);
        });
    }

    private void updateCalculation() {
        tvQuantity.setText(String.valueOf(quantity));
        tvSummaryQty.setText(getString(R.string.label_ticket_summary_format, quantity));
        
        double subTotal = basePrice * quantity;
        double fee = subTotal * feePercent;
        
        discountAmount = isVoucherApplied ? subTotal * 0.1 : 0;

        double total = subTotal + fee - discountAmount;

        tvSummaryBase.setText(formatPrice(subTotal));
        tvSummaryFee.setText(formatPrice(fee));
        tvSummaryDiscount.setText(String.format("-%s", formatPrice(discountAmount)));
        tvSummaryTotal.setText(formatPrice(total));
        
        btnConfirm.setText(getString(R.string.btn_confirm_ticket_format, formatPrice(total)));
    }

    private double calculateFinalTotal() {
        double subTotal = basePrice * quantity;
        double currentDiscount = isVoucherApplied ? subTotal * 0.1 : 0;
        return subTotal + (subTotal * feePercent) - currentDiscount;
    }

    private String formatPrice(double price) {
        return String.format(Locale.getDefault(), "%,.0fđ", price);
    }
}
