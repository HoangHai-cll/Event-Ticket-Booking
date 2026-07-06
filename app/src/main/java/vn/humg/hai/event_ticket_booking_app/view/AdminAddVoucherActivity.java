package vn.humg.hai.event_ticket_booking_app.view;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;
import vn.humg.hai.event_ticket_booking_app.R;
import vn.humg.hai.event_ticket_booking_app.controller.VoucherController;
import vn.humg.hai.event_ticket_booking_app.model.Voucher;

public class AdminAddVoucherActivity extends AppCompatActivity {

    private TextInputEditText edtCode, edtTitle, edtValue, edtMinOrder, edtMaxDiscount;
    private RadioGroup rgDiscountType;
    private CheckBox cbSystemDefault;
    private TextView tvExpiry;
    private MaterialButton btnSave, btnPickExpiry;

    private final Calendar calendar = Calendar.getInstance();
    private boolean isExpiryChosen = false;
    private final VoucherController voucherController = new VoucherController();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_voucher);

        initViews();
        setupEvents();
    }

    private void initViews() {
        findViewById(R.id.btn_back_custom).setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        edtCode = findViewById(R.id.edt_voucher_code);
        edtTitle = findViewById(R.id.edt_voucher_title);
        edtValue = findViewById(R.id.edt_voucher_value);
        edtMinOrder = findViewById(R.id.edt_voucher_min_order);
        edtMaxDiscount = findViewById(R.id.edt_voucher_max_discount);
        rgDiscountType = findViewById(R.id.rg_discount_type);
        cbSystemDefault = findViewById(R.id.cb_system_default);
        tvExpiry = findViewById(R.id.tv_voucher_expiry);
        btnSave = findViewById(R.id.btn_save_voucher);
        btnPickExpiry = findViewById(R.id.btn_pick_expiry);
    }

    private void setupEvents() {
        btnPickExpiry.setOnClickListener(v -> showDateTimePicker());
        btnSave.setOnClickListener(v -> saveVoucher());
    }

    private void showDateTimePicker() {
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            new TimePickerDialog(this, (view1, hourOfDay, minute) -> {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                isExpiryChosen = true;
                updateExpiryDisplay();
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateExpiryDisplay() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        tvExpiry.setText(sdf.format(calendar.getTime()));
    }

    private void saveVoucher() {
        String code = edtCode.getText() != null ? edtCode.getText().toString().trim().toUpperCase() : "";
        String title = edtTitle.getText() != null ? edtTitle.getText().toString().trim() : "";
        String valueStr = edtValue.getText() != null ? edtValue.getText().toString().trim() : "";
        String minOrderStr = edtMinOrder.getText() != null ? edtMinOrder.getText().toString().trim() : "";
        String maxDiscountStr = edtMaxDiscount.getText() != null ? edtMaxDiscount.getText().toString().trim() : "";

        if (code.isEmpty()) {
            edtCode.setError("Vui lòng nhập mã Voucher");
            return;
        }
        if (title.isEmpty()) {
            edtTitle.setError("Vui lòng nhập tiêu đề");
            return;
        }
        if (valueStr.isEmpty()) {
            edtValue.setError("Vui lòng nhập giá trị");
            return;
        }
        if (!isExpiryChosen) {
            Toast.makeText(this, "Vui lòng chọn thời gian hết hạn", Toast.LENGTH_SHORT).show();
            return;
        }

        double val = Double.parseDouble(valueStr);
        double minOrder = minOrderStr.isEmpty() ? 0 : Double.parseDouble(minOrderStr);
        double maxDiscount = maxDiscountStr.isEmpty() ? 0 : Double.parseDouble(maxDiscountStr);

        String type = rgDiscountType.getCheckedRadioButtonId() == R.id.rb_type_percent ? "PERCENT" : "FIXED";

        Voucher voucher = new Voucher();
        voucher.setVoucherId(UUID.randomUUID().toString());
        voucher.setCode(code);
        voucher.setTitle(title);
        voucher.setDiscountType(type);
        voucher.setDiscountValue(val);
        voucher.setMinOrderValue(minOrder);
        voucher.setMaxDiscountAmount(maxDiscount);
        voucher.setExpiryDate(new Timestamp(calendar.getTime()));
        voucher.setSystemDefault(cbSystemDefault.isChecked());

        btnSave.setEnabled(false);
        voucherController.addSystemVoucher(voucher, () -> runOnUiThread(() -> {
            Toast.makeText(this, "Lưu Voucher hệ thống thành công!", Toast.LENGTH_SHORT).show();
            finish();
        }), error -> runOnUiThread(() -> {
            btnSave.setEnabled(true);
            Toast.makeText(this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
        }));
    }
}
