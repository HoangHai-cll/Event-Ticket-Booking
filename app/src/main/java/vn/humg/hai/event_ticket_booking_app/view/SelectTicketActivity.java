package vn.humg.hai.event_ticket_booking_app.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import vn.humg.hai.event_ticket_booking_app.R;
import vn.humg.hai.event_ticket_booking_app.controller.EventController;
import vn.humg.hai.event_ticket_booking_app.controller.UserController;
import vn.humg.hai.event_ticket_booking_app.controller.VoucherController;
import vn.humg.hai.event_ticket_booking_app.model.Event;
import vn.humg.hai.event_ticket_booking_app.model.TicketTier;
import vn.humg.hai.event_ticket_booking_app.model.User;
import vn.humg.hai.event_ticket_booking_app.model.Voucher;

public class SelectTicketActivity extends AppCompatActivity {

    private String eventId;
    private Event currentEvent;
    private int quantity = 1;
    private double basePrice = 0;
    private TicketTier selectedTier = null;
    private final double feePercent = 0.05;
    private double discountAmount = 0;
    private String appliedVoucherCode = "";
    private String appliedUserVoucherId = ""; // Lưu ID của voucher trong ví của user để xóa sau khi mua
    private boolean isVoucherApplied = false;
    private Voucher appliedVoucher = null;

    private ImageView ivEventImage;
    private TextView tvEventTitle, tvEventDate, tvEventLocation;
    private TextView tvUnitPrice, tvQuantity, tvSummaryQty, tvSummaryBase, tvSummaryFee, tvSummaryTotal, tvSummaryDiscount;
    private ImageButton btnMinus, btnPlus;
    private MaterialButton btnConfirm;
    private EditText etVoucher;
    private TextView btnApplyVoucher, btnSelectFromWallet;
    private LinearLayout layoutDiscount;

    // Tier UI
    private LinearLayout layoutTierSelector;
    private LinearLayout containerTierCards;
    private MaterialCardView lastSelectedCard = null;
    private RadioButton lastSelectedRadio = null;

    private final EventController eventController = new EventController();
    private final VoucherController voucherController = new VoucherController();
    private final UserController userController = new UserController();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_ticket);

        eventId = getIntent().getStringExtra("EXTRA_EVENT_ID");
        if (eventId == null) { finish(); return; }

        initViews();
        loadEventData();
        setupEvents();

        // Tự động kiểm tra và tặng voucher mặc định 100% (tối đa 88k) cho người dùng mới
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            voucherController.checkAndGiveFirstPurchaseVoucher(uid, () -> {});
        }
    }

    private void initViews() {
        findViewById(R.id.btn_back_custom).setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

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
        btnSelectFromWallet = findViewById(R.id.btn_select_from_wallet);
        btnApplyVoucher = findViewById(R.id.btn_apply_voucher);
        btnConfirm = findViewById(R.id.btn_confirm_select);

        layoutTierSelector = findViewById(R.id.layout_tier_selector);
        containerTierCards = findViewById(R.id.container_tier_cards);
    }

    private void loadEventData() {
        eventController.getEventById(eventId, event -> {
            if (event != null) {
                runOnUiThread(() -> {
                    currentEvent = event;
                    displayEventInfo();
                    
                    if (event.hasTiers()) {
                        buildTierCards(event.getTiers());
                        if (layoutTierSelector != null) {
                            layoutTierSelector.setVisibility(View.VISIBLE);
                        }
                    } else {
                        // Sự kiện cũ không có tier: dùng price gốc
                        basePrice = event.getPrice();
                        if (layoutTierSelector != null) layoutTierSelector.setVisibility(View.GONE);
                        updateCalculation();
                    }
                });
            }
        }, error -> runOnUiThread(() ->
            Toast.makeText(this, R.string.msg_load_error, Toast.LENGTH_SHORT).show()
        ));
    }

    private void buildTierCards(List<TicketTier> tiers) {
        containerTierCards.removeAllViews();
        for (TicketTier tier : tiers) {
            if (tier.getRemainingTicket() <= 0) continue; // Skip sold-out tiers

            View cardView = LayoutInflater.from(this).inflate(R.layout.item_tier_select, containerTierCards, false);
            MaterialCardView card = (MaterialCardView) cardView;
            RadioButton rb = cardView.findViewById(R.id.rb_tier_select);
            TextView tvName = cardView.findViewById(R.id.tv_tier_name);
            TextView tvRemaining = cardView.findViewById(R.id.tv_tier_remaining);
            TextView tvPrice = cardView.findViewById(R.id.tv_tier_price);
            ImageView ivSeatmap = cardView.findViewById(R.id.iv_tier_seatmap);

            tvName.setText(tier.getTierName());
            tvRemaining.setText("Còn " + tier.getRemainingTicket() + " vé");
            tvPrice.setText(tier.getPrice() == 0 ? "Miễn phí" : formatPrice(tier.getPrice()));

            if (tier.getSeatMapImageUrl() != null && !tier.getSeatMapImageUrl().isEmpty()) {
                ivSeatmap.setVisibility(View.VISIBLE);
                Glide.with(this).load(tier.getSeatMapImageUrl())
                        .placeholder(R.drawable.img_logo_event_ticket_booking)
                        .into(ivSeatmap);
            }

            card.setOnClickListener(v -> selectTier(card, rb, tier));
            containerTierCards.addView(cardView);

            // Auto select first tier
            if (selectedTier == null) {
                selectTier(card, rb, tier);
            }
        }
    }

    private void selectTier(MaterialCardView card, RadioButton rb, TicketTier tier) {
        // Deselect previous
        if (lastSelectedCard != null) {
            lastSelectedCard.setStrokeColor(getResources().getColor(R.color.rule_border));
            lastSelectedCard.setStrokeWidth((int) (1 * getResources().getDisplayMetrics().density));
        }
        if (lastSelectedRadio != null) {
            lastSelectedRadio.setChecked(false);
        }

        // Select new
        card.setStrokeColor(getResources().getColor(R.color.brand_primary));
        card.setStrokeWidth((int) (2 * getResources().getDisplayMetrics().density));
        rb.setChecked(true);

        lastSelectedCard = card;
        lastSelectedRadio = rb;
        selectedTier = tier;
        basePrice = tier.getPrice();
        tvUnitPrice.setText(tier.getPrice() == 0 ? "Miễn phí" : formatPrice(tier.getPrice()));
        updateCalculation();
    }

    private void displayEventInfo() {
        String title = currentEvent.getTitle();
        if (currentEvent.getRequiredTier() != null && !currentEvent.getRequiredTier().isEmpty() && !currentEvent.getRequiredTier().equalsIgnoreCase("Thường")) {
            title = "🔒 [Ưu tiên " + currentEvent.getRequiredTier() + "] " + title;
        }
        tvEventTitle.setText(title);
        tvEventLocation.setText(currentEvent.getLocation());

        if (currentEvent.getDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault());
            tvEventDate.setText(sdf.format(currentEvent.getDate().toDate()));
        }

        Glide.with(this)
                .load(currentEvent.getImage())
                .placeholder(R.drawable.img_logo_event_ticket_booking)
                .centerCrop()
                .into(ivEventImage);
    }

    private void setupEvents() {
        btnPlus.setOnClickListener(v -> {
            int maxQty = selectedTier != null ? selectedTier.getRemainingTicket() : (currentEvent != null ? currentEvent.getRemainingTicket() : 10);
            if (quantity < maxQty) {
                quantity++;
                updateCalculation();
            } else {
                Toast.makeText(this, "Đã đạt số lượng vé tối đa của hạng này", Toast.LENGTH_SHORT).show();
            }
        });

        btnMinus.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                updateCalculation();
            }
        });

        // Chọn mã voucher từ Ví của User (Shopee-like list)
        btnSelectFromWallet.setOnClickListener(v -> {
            String uid = FirebaseAuth.getInstance().getUid();
            if (uid == null) {
                Toast.makeText(this, "Vui lòng đăng nhập để sử dụng ví Voucher", Toast.LENGTH_SHORT).show();
                return;
            }
            double subTotal = basePrice * quantity;
            VoucherSelectDialog dialog = new VoucherSelectDialog(this, uid, subTotal, appliedVoucher, voucher -> {
                if (voucher != null) {
                    appliedVoucher = voucher;
                    appliedVoucherCode = voucher.getCode();
                    appliedUserVoucherId = voucher.getVoucherId();
                    isVoucherApplied = true;
                    etVoucher.setText(voucher.getCode());
                    layoutDiscount.setVisibility(View.VISIBLE);
                    Toast.makeText(this, "Áp dụng thành công mã: " + voucher.getCode(), Toast.LENGTH_SHORT).show();
                } else {
                    // Bỏ áp dụng
                    appliedVoucher = null;
                    appliedVoucherCode = "";
                    appliedUserVoucherId = "";
                    isVoucherApplied = false;
                    etVoucher.setText("");
                    layoutDiscount.setVisibility(View.GONE);
                }
                updateCalculation();
            });
            dialog.show();
        });

        // Nhập voucher thủ công (Có hỗ trợ fallback 10% như cũ)
        btnApplyVoucher.setOnClickListener(v -> {
            String code = etVoucher.getText().toString().trim().toUpperCase();
            if (code.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập mã giảm giá", Toast.LENGTH_SHORT).show();
                return;
            }

            // Kiểm tra mã fix cứng cũ
            if (code.equals("EVENT2026") || code.equals("HELLOSUMMER")) {
                appliedVoucher = null;
                appliedVoucherCode = code;
                appliedUserVoucherId = "";
                isVoucherApplied = true;
                layoutDiscount.setVisibility(View.VISIBLE);
                Toast.makeText(this, "Đã áp dụng mã giảm giá 10%!", Toast.LENGTH_SHORT).show();
                updateCalculation();
                return;
            }

            // Kiểm tra trong ví voucher của User xem có mã khớp với code vừa gõ không
            String uid = FirebaseAuth.getInstance().getUid();
            if (uid != null) {
                double subTotal = basePrice * quantity;
                voucherController.getUserVouchers(uid, userVouchers -> {
                    Voucher found = null;
                    for (Voucher voucher : userVouchers) {
                        if (voucher.getCode().equalsIgnoreCase(code)) {
                            found = voucher;
                            break;
                        }
                    }

                    if (found != null) {
                        // Kiểm tra HSD
                        long nowSec = System.currentTimeMillis() / 1000;
                        if (found.getExpiryDate() != null && found.getExpiryDate().getSeconds() < nowSec) {
                            runOnUiThread(() -> Toast.makeText(this, "Mã giảm giá đã hết hạn sử dụng", Toast.LENGTH_SHORT).show());
                            return;
                        }

                        // Kiểm tra đơn tối thiểu
                        if (subTotal < found.getMinOrderValue()) {
                            runOnUiThread(() -> Toast.makeText(this, "Đơn hàng tối thiểu chưa đạt điều kiện của mã", Toast.LENGTH_SHORT).show());
                            return;
                        }

                        Voucher finalFound = found;
                        runOnUiThread(() -> {
                            appliedVoucher = finalFound;
                            appliedVoucherCode = finalFound.getCode();
                            appliedUserVoucherId = finalFound.getVoucherId();
                            isVoucherApplied = true;
                            layoutDiscount.setVisibility(View.VISIBLE);
                            Toast.makeText(this, "Đã áp dụng mã: " + finalFound.getCode(), Toast.LENGTH_SHORT).show();
                            updateCalculation();
                        });
                    } else {
                        runOnUiThread(() -> {
                            appliedVoucher = null;
                            appliedVoucherCode = "";
                            appliedUserVoucherId = "";
                            isVoucherApplied = false;
                            layoutDiscount.setVisibility(View.GONE);
                            Toast.makeText(this, "Mã giảm giá không tồn tại trong ví của bạn", Toast.LENGTH_SHORT).show();
                            updateCalculation();
                        });
                    }
                }, error -> runOnUiThread(() -> {
                    Toast.makeText(this, "Lỗi kiểm tra ví voucher: " + error, Toast.LENGTH_SHORT).show();
                }));
            }
        });

        btnConfirm.setOnClickListener(v -> {
            if (currentEvent != null && currentEvent.hasTiers() && selectedTier == null) {
                Toast.makeText(this, "Vui lòng chọn hạng vé", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check priority buying access
            String uid = FirebaseAuth.getInstance().getUid();
            if (uid == null) {
                Toast.makeText(this, "Vui lòng đăng nhập để tiếp tục", Toast.LENGTH_SHORT).show();
                return;
            }

            if (currentEvent != null && currentEvent.getRequiredTier() != null && !currentEvent.getRequiredTier().isEmpty() && !currentEvent.getRequiredTier().equalsIgnoreCase("Thường")) {
                userController.getUserById(uid, user -> {
                    if (user != null) {
                        int userLevel = User.getTierLevel(user.getMemberTier());
                        int requiredLevel = User.getTierLevel(currentEvent.getRequiredTier());
                        if (userLevel < requiredLevel) {
                            runOnUiThread(() -> {
                                Toast.makeText(this, "🔒 Sự kiện này chỉ ưu tiên bán vé cho thành viên hạng từ " + currentEvent.getRequiredTier() + " trở lên!", Toast.LENGTH_LONG).show();
                            });
                        } else {
                            runOnUiThread(() -> proceedToPayment());
                        }
                    } else {
                        runOnUiThread(() -> proceedToPayment());
                    }
                }, error -> {
                    runOnUiThread(() -> proceedToPayment());
                });
            } else {
                proceedToPayment();
            }
        });
    }

    private void proceedToPayment() {
        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra("EXTRA_EVENT_ID", eventId);
        intent.putExtra("EXTRA_QUANTITY", quantity);
        intent.putExtra("EXTRA_TOTAL_PRICE", calculateFinalTotal());
        intent.putExtra("EXTRA_DISCOUNT", discountAmount);
        intent.putExtra("EXTRA_VOUCHER_CODE", appliedVoucherCode);
        intent.putExtra("EXTRA_USER_VOUCHER_ID", appliedUserVoucherId); // Truyền ID sang để xóa khi mua thành công
        if (selectedTier != null) {
            intent.putExtra("EXTRA_TIER_ID", selectedTier.getTierId());
            intent.putExtra("EXTRA_TIER_NAME", selectedTier.getTierName());
            intent.putExtra("EXTRA_TIER_PRICE", selectedTier.getPrice());
        }
        startActivity(intent);
    }

    private void updateCalculation() {
        tvQuantity.setText(String.valueOf(quantity));
        tvSummaryQty.setText(getString(R.string.label_ticket_summary_format, quantity));

        double subTotal = basePrice * quantity;
        double fee = subTotal * feePercent;

        // Tính toán số tiền được giảm
        if (isVoucherApplied) {
            if (appliedVoucher != null) {
                if ("PERCENT".equalsIgnoreCase(appliedVoucher.getDiscountType())) {
                    double percentDiscount = subTotal * (appliedVoucher.getDiscountValue() / 100.0);
                    // Giới hạn giảm tối đa
                    if (appliedVoucher.getMaxDiscountAmount() > 0 && percentDiscount > appliedVoucher.getMaxDiscountAmount()) {
                        discountAmount = appliedVoucher.getMaxDiscountAmount();
                    } else {
                        discountAmount = percentDiscount;
                    }
                } else {
                    discountAmount = appliedVoucher.getDiscountValue();
                }
            } else {
                // Fallback mã fix cứng 10% như cũ
                discountAmount = subTotal * 0.1;
            }
        } else {
            discountAmount = 0;
        }

        // Đảm bảo discount không vượt quá tổng giá gốc + phí
        double total = Math.max(0, subTotal + fee - discountAmount);

        tvSummaryBase.setText(formatPrice(subTotal));
        tvSummaryFee.setText(formatPrice(fee));
        tvSummaryDiscount.setText(String.format("-%s", formatPrice(discountAmount)));
        tvSummaryTotal.setText(formatPrice(total));
        btnConfirm.setText(getString(R.string.btn_confirm_ticket_format, formatPrice(total)));
    }

    private double calculateFinalTotal() {
        double subTotal = basePrice * quantity;
        double fee = subTotal * feePercent;
        
        double calcDiscount = 0;
        if (isVoucherApplied) {
            if (appliedVoucher != null) {
                if ("PERCENT".equalsIgnoreCase(appliedVoucher.getDiscountType())) {
                    double percentDiscount = subTotal * (appliedVoucher.getDiscountValue() / 100.0);
                    if (appliedVoucher.getMaxDiscountAmount() > 0 && percentDiscount > appliedVoucher.getMaxDiscountAmount()) {
                        calcDiscount = appliedVoucher.getMaxDiscountAmount();
                    } else {
                        calcDiscount = percentDiscount;
                    }
                } else {
                    calcDiscount = appliedVoucher.getDiscountValue();
                }
            } else {
                calcDiscount = subTotal * 0.1;
            }
        }
        return Math.max(0, subTotal + fee - calcDiscount);
    }

    private String formatPrice(double price) {
        return String.format(Locale.getDefault(), "%,.0fđ", price);
    }
}
