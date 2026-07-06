package vn.humg.hai.event_ticket_booking_app.view;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Locale;
import java.util.UUID;

import vn.humg.hai.event_ticket_booking_app.R;
import androidx.lifecycle.ViewModelProvider;
import vn.humg.hai.event_ticket_booking_app.viewmodel.BookingViewModel;
import vn.humg.hai.event_ticket_booking_app.viewmodel.EventViewModel;
import vn.humg.hai.event_ticket_booking_app.viewmodel.AuthViewModel;
import vn.humg.hai.event_ticket_booking_app.model.Booking;
import vn.humg.hai.event_ticket_booking_app.model.Event;

public class PaymentActivity extends AppCompatActivity {

    private String eventId;
    private int quantity;
    private double totalPrice;
    private double discount;
    private String voucherCode;
    private String tierId;
    private String tierName;
    private double tierPrice;
    private String userVoucherId;

    // ViewModels
    private BookingViewModel bookingViewModel;
    private EventViewModel eventViewModel;
    private AuthViewModel authViewModel;
    private Event currentEvent;

    // Stepper Views
    private TextView tvStep1Icon, tvStep1Text;
    private TextView tvStep2Icon, tvStep2Text;
    private TextView tvStep3Icon, tvStep3Text;

    // Container
    private ViewFlipper viewFlipper;
    private MaterialButton btnNext;

    // Step 1: Contact Info
    private TextInputEditText edtName, edtPhone, edtEmail;

    // Step 2: Payment Methods Cards & Buttons
    private RadioGroup rgPaymentMethods;
    private RadioButton rbBank, rbMomo, rbVnpay, rbCod;
    private MaterialCardView cardBank, cardMomo, cardVnpay, cardCod;

    // Step 3: Summary
    private TextView tvSummaryEventTitle, tvSummaryTicketCount, tvSummaryContact, tvSummaryPaymentMethod, tvSummaryTotalPrice;

    // Step Indicators & Dividers
    private View viewStep1Divider, viewStep2Divider;

    private AlertDialog loadingDialog;
    private int currentStep = 0; // 0: Info, 1: Payment, 2: Summary

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        eventId = getIntent().getStringExtra("EXTRA_EVENT_ID");
        quantity = getIntent().getIntExtra("EXTRA_QUANTITY", 1);
        totalPrice = getIntent().getDoubleExtra("EXTRA_TOTAL_PRICE", 0);
        discount = getIntent().getDoubleExtra("EXTRA_DISCOUNT", 0);
        voucherCode = getIntent().getStringExtra("EXTRA_VOUCHER_CODE");
        tierId = getIntent().getStringExtra("EXTRA_TIER_ID");
        tierName = getIntent().getStringExtra("EXTRA_TIER_NAME");
        tierPrice = getIntent().getDoubleExtra("EXTRA_TIER_PRICE", 0);
        userVoucherId = getIntent().getStringExtra("EXTRA_USER_VOUCHER_ID");

        if (eventId == null) {
            finish();
            return;
        }

        bookingViewModel = new ViewModelProvider(this).get(BookingViewModel.class);
        eventViewModel = new ViewModelProvider(this).get(EventViewModel.class);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        setupObservers();

        initViews();
        loadEventData();
        loadUserData();
        setupEvents();
        updateStepperUI();
        updatePaymentCardBorders(getSelectedPaymentMethodId());
    }

    private void setupObservers() {
        eventViewModel.getEventDetailState().observe(this, event -> {
            if (event != null) {
                this.currentEvent = event;
            }
        });

        authViewModel.getUserProfileState().observe(this, user -> {
            if (user != null) {
                edtName.setText(user.getFullName());
                edtPhone.setText(user.getPhone() != null ? user.getPhone() : "");
                edtEmail.setText(user.getEmail());
            }
        });

        bookingViewModel.getBookingResultState().observe(this, result -> {
            if (result.status == BookingViewModel.BookingStatus.LOADING) {
                // logic handled by dialog
            } else if (result.status == BookingViewModel.BookingStatus.SUCCESS) {
                if (loadingDialog != null) loadingDialog.dismiss();
                navigateToSuccess(result.bookingId);
            } else if (result.status == BookingViewModel.BookingStatus.LEVEL_UP) {
                if (loadingDialog != null) loadingDialog.dismiss();
                showLevelUpDialog(result.newTier, result.bookingId);
            } else if (result.status == BookingViewModel.BookingStatus.ERROR) {
                if (loadingDialog != null) loadingDialog.dismiss();
                Toast.makeText(this, getString(R.string.msg_booking_save_error_format, result.errorMessage), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void initViews() {
        findViewById(R.id.btn_back_custom).setOnClickListener(v -> handleBack());

        tvStep1Icon = findViewById(R.id.tv_step1_icon);
        tvStep1Text = findViewById(R.id.tv_step1_text);
        tvStep2Icon = findViewById(R.id.tv_step2_icon);
        tvStep2Text = findViewById(R.id.tv_step2_text);
        tvStep3Icon = findViewById(R.id.tv_step3_icon);
        tvStep3Text = findViewById(R.id.tv_step3_text);

        viewFlipper = findViewById(R.id.view_flipper_checkout);
        btnNext = findViewById(R.id.btn_action_next);
        viewStep1Divider = findViewById(R.id.view_step1_divider);
        viewStep2Divider = findViewById(R.id.view_step2_divider);

        // Step 1
        edtName = findViewById(R.id.edt_checkout_name);
        edtPhone = findViewById(R.id.edt_checkout_phone);
        edtEmail = findViewById(R.id.edt_checkout_email);

        // Step 2
        rgPaymentMethods = findViewById(R.id.rg_payment_methods);
        rbBank = findViewById(R.id.rb_bank);
        rbMomo = findViewById(R.id.rb_momo);
        rbVnpay = findViewById(R.id.rb_vnpay);
        rbCod = findViewById(R.id.rb_cod);

        cardBank = findViewById(R.id.card_pm_bank);
        cardMomo = findViewById(R.id.card_pm_momo);
        cardVnpay = findViewById(R.id.card_pm_vnpay);
        cardCod = findViewById(R.id.card_pm_cod);

        // Step 3
        tvSummaryEventTitle = findViewById(R.id.tv_summary_event_title);
        tvSummaryTicketCount = findViewById(R.id.tv_summary_ticket_count);
        tvSummaryContact = findViewById(R.id.tv_summary_contact);
        tvSummaryPaymentMethod = findViewById(R.id.tv_summary_payment_method);
        tvSummaryTotalPrice = findViewById(R.id.tv_summary_total_price);
    }

    private void loadEventData() {
        eventViewModel.loadEventById(eventId);
    }

    private void loadUserData() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            authViewModel.getUserProfile(uid);
        }
    }

    private void setupEvents() {
        // Handle Card Clicks to select the radio buttons
        cardBank.setOnClickListener(v -> selectPaymentMethod(R.id.rb_bank));
        cardMomo.setOnClickListener(v -> selectPaymentMethod(R.id.rb_momo));
        cardVnpay.setOnClickListener(v -> selectPaymentMethod(R.id.rb_vnpay));
        cardCod.setOnClickListener(v -> selectPaymentMethod(R.id.rb_cod));

        rbBank.setOnClickListener(v -> selectPaymentMethod(R.id.rb_bank));
        rbMomo.setOnClickListener(v -> selectPaymentMethod(R.id.rb_momo));
        rbVnpay.setOnClickListener(v -> selectPaymentMethod(R.id.rb_vnpay));
        rbCod.setOnClickListener(v -> selectPaymentMethod(R.id.rb_cod));

        btnNext.setOnClickListener(v -> {
            if (currentStep == 0) {
                if (validateStep1()) {
                    if (totalPrice == 0) {
                        currentStep = 2;
                        viewFlipper.setDisplayedChild(2);
                        updateStepperUI();
                        prepareSummary();
                    } else {
                        currentStep = 1;
                        viewFlipper.setDisplayedChild(1);
                        updateStepperUI();
                    }
                }
            } else if (currentStep == 1) {
                if (validateStep2()) {
                    currentStep = 2;
                    viewFlipper.setDisplayedChild(2);
                    updateStepperUI();
                    prepareSummary();
                }
            } else if (currentStep == 2) {
                startPaymentFlow();
            }
        });
    }

    private void selectPaymentMethod(int checkedId) {
        rbBank.setChecked(checkedId == R.id.rb_bank);
        rbMomo.setChecked(checkedId == R.id.rb_momo);
        rbVnpay.setChecked(checkedId == R.id.rb_vnpay);
        rbCod.setChecked(checkedId == R.id.rb_cod);
        updatePaymentCardBorders(checkedId);
    }

    private int getSelectedPaymentMethodId() {
        if (rbBank.isChecked()) return R.id.rb_bank;
        if (rbMomo.isChecked()) return R.id.rb_momo;
        if (rbVnpay.isChecked()) return R.id.rb_vnpay;
        if (rbCod.isChecked()) return R.id.rb_cod;
        return -1;
    }

    private void updatePaymentCardBorders(int checkedId) {
        int activeColor = ContextCompat.getColor(this, R.color.color_primary_periwinkle);
        int inactiveColor = ContextCompat.getColor(this, R.color.rule_border);
        int activeStrokeWidth = (int) (2 * getResources().getDisplayMetrics().density);
        int inactiveStrokeWidth = (int) (1 * getResources().getDisplayMetrics().density);

        cardBank.setStrokeColor(checkedId == R.id.rb_bank ? activeColor : inactiveColor);
        cardBank.setStrokeWidth(checkedId == R.id.rb_bank ? activeStrokeWidth : inactiveStrokeWidth);

        cardMomo.setStrokeColor(checkedId == R.id.rb_momo ? activeColor : inactiveColor);
        cardMomo.setStrokeWidth(checkedId == R.id.rb_momo ? activeStrokeWidth : inactiveStrokeWidth);

        cardVnpay.setStrokeColor(checkedId == R.id.rb_vnpay ? activeColor : inactiveColor);
        cardVnpay.setStrokeWidth(checkedId == R.id.rb_vnpay ? activeStrokeWidth : inactiveStrokeWidth);

        cardCod.setStrokeColor(checkedId == R.id.rb_cod ? activeColor : inactiveColor);
        cardCod.setStrokeWidth(checkedId == R.id.rb_cod ? activeStrokeWidth : inactiveStrokeWidth);
    }

    private void handleBack() {
        if (currentStep > 0) {
            if (currentStep == 2 && totalPrice == 0) {
                currentStep = 0;
            } else {
                currentStep--;
            }
            viewFlipper.setDisplayedChild(currentStep);
            updateStepperUI();
        } else {
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        handleBack();
    }

    private boolean validateStep1() {
        String name = edtName.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();

        if (name.isEmpty() || phone.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, getString(R.string.msg_input_contact_required), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean validateStep2() {
        int checkedId = getSelectedPaymentMethodId();
        if (checkedId == -1) {
            Toast.makeText(this, getString(R.string.msg_select_payment_required), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void prepareSummary() {
        if (currentEvent != null) {
            tvSummaryEventTitle.setText(currentEvent.getTitle());
        }
        tvSummaryTicketCount.setText(getString(R.string.ticket_count_format, quantity));
        tvSummaryContact.setText(getString(R.string.contact_summary_format, edtName.getText().toString(), edtPhone.getText().toString()));
        
        String paymentMethod;
        if (totalPrice == 0) {
            paymentMethod = getString(R.string.payment_method_free);
        } else {
            paymentMethod = getString(R.string.payment_method_bank);
            int checkedId = getSelectedPaymentMethodId();
            if (checkedId == R.id.rb_momo) paymentMethod = getString(R.string.payment_method_momo);
            else if (checkedId == R.id.rb_vnpay) paymentMethod = getString(R.string.payment_method_vnpay);
            else if (checkedId == R.id.rb_cod) paymentMethod = getString(R.string.payment_method_cod);
        }
        
        tvSummaryPaymentMethod.setText(paymentMethod);
        tvSummaryTotalPrice.setText(formatPrice(totalPrice));
    }

    private void updateStepperUI() {
        // Reset all
        tvStep1Icon.setBackgroundResource(R.drawable.bg_circle_gray);
        tvStep2Icon.setBackgroundResource(R.drawable.bg_circle_gray);
        tvStep3Icon.setBackgroundResource(R.drawable.bg_circle_gray);
        tvStep1Text.setTextColor(ContextCompat.getColor(this, R.color.text_muted));
        tvStep2Text.setTextColor(ContextCompat.getColor(this, R.color.text_muted));
        tvStep3Text.setTextColor(ContextCompat.getColor(this, R.color.text_muted));
        if (viewStep1Divider != null) {
            viewStep1Divider.setBackgroundColor(ContextCompat.getColor(this, R.color.rule_border));
        }
        if (viewStep2Divider != null) {
            viewStep2Divider.setBackgroundColor(ContextCompat.getColor(this, R.color.rule_border));
        }

        // Active state
        int purpleColor = ContextCompat.getColor(this, R.color.color_primary_periwinkle);
        if (currentStep >= 0) {
            tvStep1Icon.setBackgroundResource(R.drawable.bg_circle_purple);
            tvStep1Text.setTextColor(purpleColor);
        }
        
        if (totalPrice == 0) {
            // For free events, Step 2 is bypassed
            if (currentStep >= 2) {
                tvStep3Icon.setBackgroundResource(R.drawable.bg_circle_purple);
                tvStep3Text.setTextColor(purpleColor);
                if (viewStep1Divider != null) {
                    viewStep1Divider.setBackgroundColor(purpleColor);
                }
                if (viewStep2Divider != null) {
                    viewStep2Divider.setBackgroundColor(purpleColor);
                }
            }
        } else {
            if (currentStep >= 1) {
                tvStep2Icon.setBackgroundResource(R.drawable.bg_circle_purple);
                tvStep2Text.setTextColor(purpleColor);
                if (viewStep1Divider != null) {
                    viewStep1Divider.setBackgroundColor(purpleColor);
                }
            }
            if (currentStep >= 2) {
                tvStep3Icon.setBackgroundResource(R.drawable.bg_circle_purple);
                tvStep3Text.setTextColor(purpleColor);
                if (viewStep2Divider != null) {
                    viewStep2Divider.setBackgroundColor(purpleColor);
                }
            }
        }

        if (currentStep == 2) {
            btnNext.setText(getString(R.string.btn_confirm_order));
        } else {
            btnNext.setText(getString(R.string.btn_continue));
        }
    }

    private void startPaymentFlow() {
        if (currentEvent == null) {
            Toast.makeText(this, getString(R.string.msg_load_event_failed), Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentEvent.getRemainingTicket() < quantity) {
            Toast.makeText(this, getString(R.string.msg_ticket_remaining_format, currentEvent.getRemainingTicket()), Toast.LENGTH_LONG).show();
            return;
        }

        View dialogView = LayoutInflater.from(this).inflate(R.layout.activity_splash, null);
        TextView tvStatus = dialogView.findViewById(R.id.tv_footer);
        if (tvStatus != null) tvStatus.setText(getString(R.string.msg_processing_transaction));
        
        loadingDialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();
        loadingDialog.show();

        new Handler().postDelayed(() -> {
            if (loadingDialog != null && loadingDialog.isShowing()) {
                loadingDialog.setMessage(getString(R.string.msg_verifying_transaction));
                handleActualBooking();
            }
        }, 2000);
    }

    private void handleActualBooking() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            loadingDialog.dismiss();
            Toast.makeText(this, R.string.msg_login_required, Toast.LENGTH_SHORT).show();
            return;
        }

        String bookingId = "BK" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        Booking booking = new Booking();
        booking.setBookingId(bookingId);
        booking.setUserId(uid);
        booking.setSellerId(currentEvent != null ? currentEvent.getCreatedByAdminId() : "");
        booking.setEventId(eventId);
        booking.setTierId(tierId);
        booking.setTierName(tierName);
        booking.setPricePerTicket(tierPrice > 0 ? tierPrice : (currentEvent != null ? currentEvent.getPrice() : 0));
        booking.setQuantity(quantity);
        booking.setTotalPrice(totalPrice);
        booking.setDiscount(discount);
        booking.setVoucherCode(voucherCode);
        booking.setBookingDate(Timestamp.now());
        
        if (totalPrice == 0) {
            booking.setStatus("Confirmed");
        } else {
            int checkedId = getSelectedPaymentMethodId();
            if (checkedId == R.id.rb_cod) {
                 booking.setStatus("Pending Payment");
            } else {
                 booking.setStatus("Confirmed");
            }
        }

        bookingViewModel.checkoutBooking(booking, currentEvent, userVoucherId);
    }

    private void showLevelUpDialog(String newTier, String bookingId) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_level_up, null);
        TextView tvTierName = dialogView.findViewById(R.id.tv_level_up_tier);
        MaterialButton btnGotIt = dialogView.findViewById(R.id.btn_level_up_got_it);

        if (tvTierName != null) {
            tvTierName.setText("HẠNG: " + newTier.toUpperCase());
        }

        AlertDialog levelUpDialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        if (btnGotIt != null) {
            btnGotIt.setOnClickListener(v -> {
                levelUpDialog.dismiss();
                navigateToSuccess(bookingId);
            });
        }

        if (levelUpDialog.getWindow() != null) {
            levelUpDialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        levelUpDialog.show();
    }

    private void navigateToSuccess(String bookingId) {
        if (loadingDialog != null) loadingDialog.dismiss();
        Intent intent = new Intent(this, PaymentSuccessActivity.class);
        intent.putExtra("EXTRA_BOOKING_ID", bookingId);
        intent.putExtra("EXTRA_EVENT_ID", eventId);
        intent.putExtra("EXTRA_QUANTITY", quantity);
        startActivity(intent);
        finish();
    }

    private String formatPrice(double price) {
        return String.format(Locale.getDefault(), "%,.0fđ", price);
    }
}
