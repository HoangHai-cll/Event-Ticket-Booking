package vn.humg.hai.event_ticket_booking_app.view;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;
import vn.humg.hai.event_ticket_booking_app.R;
import vn.humg.hai.event_ticket_booking_app.controller.EventController;
import vn.humg.hai.event_ticket_booking_app.model.Event;

public class AdminAddEventActivity extends AppCompatActivity {

    private TextInputEditText edtTitle, edtDesc, edtLocation, edtPrice, edtTotalTickets, edtImage, edtGoogleMapsUrl, edtSpeakers;
    private AutoCompleteTextView spinnerCategory;
    private TextView tvDateTime, tvStep1Num, tvStep2Num, tvStep3Num, tvPreviewTitle, tvPreviewInfo;
    private TextView tvStep1Text, tvStep2Text, tvStep3Text;
    private View viewStep1Divider, viewStep2Divider;
    private CheckBox cbIsHot;
    private SwitchMaterial swIsFree;
    private MaterialButton btnPickDate, btnSave, btnNext, btnPrev;
    private LinearLayout layoutStep1, layoutStep2, layoutStep3;
    private TextInputLayout inputPrice;
    private ImageView ivPreview;

    private final EventController eventController = new EventController();
    private Calendar calendar = Calendar.getInstance();
    private String editEventId = null;
    private Event existingEvent = null;
    private int currentStep = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_event);

        editEventId = getIntent().getStringExtra("EDIT_EVENT_ID");

        initViews();
        setupCategoryDropdown();
        initEvents();
        updateStepUI();

        if (editEventId != null) {
            loadExistingEventData();
        }
    }

    private void initViews() {
        findViewById(R.id.btn_back_custom).setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        // Stepper
        tvStep1Num = findViewById(R.id.tv_step_1_num);
        tvStep2Num = findViewById(R.id.tv_step_2_num);
        tvStep3Num = findViewById(R.id.tv_step_3_num);
        tvStep1Text = findViewById(R.id.tv_step_1_text);
        tvStep2Text = findViewById(R.id.tv_step_2_text);
        tvStep3Text = findViewById(R.id.tv_step_3_text);
        viewStep1Divider = findViewById(R.id.view_step1_divider);
        viewStep2Divider = findViewById(R.id.view_step2_divider);

        // Layouts
        layoutStep1 = findViewById(R.id.layout_step_1);
        layoutStep2 = findViewById(R.id.layout_step_2);
        layoutStep3 = findViewById(R.id.layout_step_3);
        inputPrice = findViewById(R.id.input_admin_price);

        // Fields
        edtTitle = findViewById(R.id.edt_admin_title);
        edtDesc = findViewById(R.id.edt_admin_desc);
        spinnerCategory = findViewById(R.id.spinner_admin_category);
        edtLocation = findViewById(R.id.edt_admin_location);
        edtPrice = findViewById(R.id.edt_admin_price);
        edtTotalTickets = findViewById(R.id.edt_admin_total_tickets);
        edtImage = findViewById(R.id.edt_admin_image);
        edtGoogleMapsUrl = findViewById(R.id.edt_admin_google_maps);
        edtSpeakers = findViewById(R.id.edt_admin_speakers);
        
        tvDateTime = findViewById(R.id.tv_admin_datetime);
        tvPreviewTitle = findViewById(R.id.tv_preview_title);
        tvPreviewInfo = findViewById(R.id.tv_preview_info);
        
        cbIsHot = findViewById(R.id.cb_admin_is_hot);
        swIsFree = findViewById(R.id.sw_admin_is_free);
        ivPreview = findViewById(R.id.iv_admin_preview_image);

        // Buttons
        btnPickDate = findViewById(R.id.btn_admin_pick_date);
        btnSave = findViewById(R.id.btn_admin_save);
        btnNext = findViewById(R.id.btn_admin_next);
        btnPrev = findViewById(R.id.btn_admin_prev);
    }

    private void setupCategoryDropdown() {
        String[] categories = {"Hội thảo", "Workshop", "Nhạc hội", "Triển lãm", "Talkshow", "Khác"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categories);
        spinnerCategory.setAdapter(adapter);
    }

    private void initEvents() {
        btnPickDate.setOnClickListener(v -> showDateTimePicker());
        btnSave.setOnClickListener(v -> saveEvent());
        btnNext.setOnClickListener(v -> nextStep());
        btnPrev.setOnClickListener(v -> prevStep());

        swIsFree.setOnCheckedChangeListener((buttonView, isChecked) -> {
            inputPrice.setEnabled(!isChecked);
            if (isChecked) edtPrice.setText("0");
        });

        edtImage.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateImagePreview(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void updateImagePreview(String url) {
        if (url != null && !url.isEmpty()) {
            Glide.with(this).load(url).placeholder(R.drawable.img_logo_event_ticket_booking).into(ivPreview);
        }
    }

    private void nextStep() {
        if (currentStep == 1) {
            if (validateStep1()) {
                currentStep = 2;
                updateStepUI();
            }
        } else if (currentStep == 2) {
            if (validateStep2()) {
                currentStep = 3;
                updatePreviewData();
                updateStepUI();
            }
        }
    }

    private void prevStep() {
        if (currentStep > 1) {
            currentStep--;
            updateStepUI();
        }
    }

    private void updateStepUI() {
        layoutStep1.setVisibility(currentStep == 1 ? View.VISIBLE : View.GONE);
        layoutStep2.setVisibility(currentStep == 2 ? View.VISIBLE : View.GONE);
        layoutStep3.setVisibility(currentStep == 3 ? View.VISIBLE : View.GONE);

        btnPrev.setVisibility(currentStep == 1 ? View.INVISIBLE : View.VISIBLE);
        btnNext.setVisibility(currentStep == 3 ? View.GONE : View.VISIBLE);
        btnSave.setVisibility(currentStep == 3 ? View.VISIBLE : View.GONE);

        // Update colors for step numbers
        tvStep1Num.setBackgroundResource(currentStep >= 1 ? R.drawable.bg_chip : R.drawable.bg_chip_light);
        tvStep1Num.setTextColor(currentStep >= 1 ? ContextCompat.getColor(this, R.color.white) : ContextCompat.getColor(this, R.color.text_muted));
        
        tvStep2Num.setBackgroundResource(currentStep >= 2 ? R.drawable.bg_chip : R.drawable.bg_chip_light);
        tvStep2Num.setTextColor(currentStep >= 2 ? ContextCompat.getColor(this, R.color.white) : ContextCompat.getColor(this, R.color.text_muted));
        
        tvStep3Num.setBackgroundResource(currentStep >= 3 ? R.drawable.bg_chip : R.drawable.bg_chip_light);
        tvStep3Num.setTextColor(currentStep >= 3 ? ContextCompat.getColor(this, R.color.white) : ContextCompat.getColor(this, R.color.text_muted));

        // Update colors for step titles
        if (tvStep1Text != null) tvStep1Text.setTextColor(currentStep >= 1 ? ContextCompat.getColor(this, R.color.white) : 0xAAFFFFFF);
        if (tvStep2Text != null) tvStep2Text.setTextColor(currentStep >= 2 ? ContextCompat.getColor(this, R.color.white) : 0xAAFFFFFF);
        if (tvStep3Text != null) tvStep3Text.setTextColor(currentStep >= 3 ? ContextCompat.getColor(this, R.color.white) : 0xAAFFFFFF);

        // Update divider line colors
        if (viewStep1Divider != null) viewStep1Divider.setBackgroundColor(currentStep >= 2 ? 0xFFFFFFFF : 0x66FFFFFF);
        if (viewStep2Divider != null) viewStep2Divider.setBackgroundColor(currentStep >= 3 ? 0xFFFFFFFF : 0x66FFFFFF);
    }

    private void updatePreviewData() {
        tvPreviewTitle.setText(edtTitle.getText().toString());
        String info = spinnerCategory.getText().toString() + " • " + edtLocation.getText().toString();
        tvPreviewInfo.setText(info);
    }

    private boolean validateStep1() {
        if (edtTitle.getText().toString().trim().isEmpty()) {
            edtTitle.setError("Cần nhập tên sự kiện");
            return false;
        }
        if (spinnerCategory.getText().toString().isEmpty()) {
            Toast.makeText(this, "Chọn danh mục", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (edtLocation.getText().toString().trim().isEmpty()) {
            edtLocation.setError("Cần nhập địa điểm");
            return false;
        }
        return true;
    }

    private boolean validateStep2() {
        if (!swIsFree.isChecked() && edtPrice.getText().toString().isEmpty()) {
            edtPrice.setError("Nhập giá vé");
            return false;
        }
        if (edtTotalTickets.getText().toString().isEmpty()) {
            edtTotalTickets.setError("Nhập số lượng");
            return false;
        }
        return true;
    }

    private void loadExistingEventData() {
        eventController.getEventById(editEventId, event -> {
            if (event != null) {
                existingEvent = event;
                runOnUiThread(() -> {
                    edtTitle.setText(event.getTitle());
                    edtDesc.setText(event.getDescription());
                    spinnerCategory.setText(event.getCategory(), false);
                    edtLocation.setText(event.getLocation());
                    edtPrice.setText(String.valueOf((long)event.getPrice()));
                    edtTotalTickets.setText(String.valueOf(event.getTotalTicket()));
                    edtImage.setText(event.getImage());
                    edtGoogleMapsUrl.setText(event.getGoogleMapsUrl());
                    edtSpeakers.setText(String.join(", ", event.getSpeakers()));
                    cbIsHot.setChecked(event.isHot());
                    swIsFree.setChecked(event.isFree());
                    updateImagePreview(event.getImage());
                    
                    if (event.getDate() != null) {
                        calendar.setTime(event.getDate().toDate());
                        updateDateTimeDisplay();
                    }
                });
            }
        }, e -> {});
    }

    private void updateDateTimeDisplay() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        tvDateTime.setText(sdf.format(calendar.getTime()));
    }

    private void showDateTimePicker() {
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            new TimePickerDialog(this, (view1, hourOfDay, minute) -> {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                updateDateTimeDisplay();
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void saveEvent() {
        btnSave.setEnabled(false);
        
        Event event = (existingEvent != null) ? existingEvent : new Event();
        String currentAdminId = FirebaseAuth.getInstance().getUid();
        
        event.setCreatedByAdminId(currentAdminId);
        event.setTitle(edtTitle.getText().toString().trim());
        event.setCategory(spinnerCategory.getText().toString().trim());
        event.setDescription(edtDesc.getText().toString().trim());
        event.setLocation(edtLocation.getText().toString().trim());
        event.setGoogleMapsUrl(edtGoogleMapsUrl.getText().toString().trim());
        event.setPrice(swIsFree.isChecked() ? 0 : Double.parseDouble(edtPrice.getText().toString().trim()));
        event.setFree(swIsFree.isChecked());
        
        int total = Integer.parseInt(edtTotalTickets.getText().toString().trim());
        if (editEventId == null) {
            event.setEventId(UUID.randomUUID().toString());
            event.setTotalTicket(total);
            event.setRemainingTicket(total);
        } else {
            int diff = total - event.getTotalTicket();
            event.setTotalTicket(total);
            event.setRemainingTicket(event.getRemainingTicket() + diff);
        }

        event.setImage(edtImage.getText().toString().trim());
        event.setHot(cbIsHot.isChecked());
        event.setDate(new Timestamp(calendar.getTime()));

        eventController.saveEvent(event, () -> {
            runOnUiThread(() -> {
                Toast.makeText(this, "Đăng sự kiện thành công!", Toast.LENGTH_SHORT).show();
                finish();
            });
        }, error -> {
            runOnUiThread(() -> {
                btnSave.setEnabled(true);
                Toast.makeText(this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            });
        });
    }
}
