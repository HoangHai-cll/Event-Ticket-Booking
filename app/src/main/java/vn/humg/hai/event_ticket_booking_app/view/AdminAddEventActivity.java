package vn.humg.hai.event_ticket_booking_app.view;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
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
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import android.net.Uri;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import android.widget.RadioGroup;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import android.app.ProgressDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import vn.humg.hai.event_ticket_booking_app.R;
import vn.humg.hai.event_ticket_booking_app.controller.EventController;
import vn.humg.hai.event_ticket_booking_app.model.Event;
import vn.humg.hai.event_ticket_booking_app.model.TicketTier;

public class AdminAddEventActivity extends AppCompatActivity {

    // --- Inner class đại diện cho dữ liệu nhập từng hạng vé ---
    private static class TierInput {
        View rootView;
        TextInputEditText edtName, edtPrice, edtQuantity;
        MaterialCardView cardSeatmapPreview;
        ImageView ivSeatmapPreview;
        TextView tvSeatmapStatus;
        Uri seatmapUri;
        String uploadedSeatmapUrl; // URL sau khi upload lên Storage
    }

    private TextInputEditText edtTitle, edtDesc, edtLocation, edtImage, edtGoogleMapsUrl, edtSpeakers;
    private AutoCompleteTextView spinnerCategory;
    private AutoCompleteTextView spinnerRequiredTier;
    private TextView tvDateTime, tvStep1Num, tvStep2Num, tvStep3Num, tvPreviewTitle, tvPreviewInfo;
    private TextView tvStep1Text, tvStep2Text, tvStep3Text;
    private View viewStep1Divider, viewStep2Divider;
    private CheckBox cbIsHot;
    private SwitchMaterial swIsFree;
    private MaterialButton btnSave, btnNext, btnPrev, btnAddTier;
    private LinearLayout layoutStep1, layoutStep2, layoutStep3;
    private LinearLayout containerTiers;
    private TextInputLayout layoutImageUrl;
    private ImageView ivPreview;
    
    private RadioGroup rgImageSource;
    private MaterialCardView cardImageUpload;
    private View layoutUploadPlaceholder;
    private Uri selectedImageUri = null;
    private ActivityResultLauncher<String> imagePickerLauncher;

    // Tier management
    private final List<TierInput> tierInputList = new ArrayList<>();
    // Launcher hiện tại đang chờ upload ảnh sơ đồ cho tier nào
    private TierInput pendingTierForSeatmap = null;
    private ActivityResultLauncher<String> seatmapPickerLauncher;

    private final EventController eventController = new EventController();
    private final vn.humg.hai.event_ticket_booking_app.controller.UserController userController = new vn.humg.hai.event_ticket_booking_app.controller.UserController();
    private final Calendar calendar = Calendar.getInstance();
    private String editEventId = null;
    private Event existingEvent = null;
    private int currentStep = 1;
    private boolean isDateTimeSelected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_event);

        editEventId = getIntent().getStringExtra("EDIT_EVENT_ID");

        initViews();
        setupCategoryDropdown();
        initEvents();
        updateStepUI();

        // Tự động thêm 1 hạng vé mặc định khi mở
        addTierCard();

        if (editEventId != null) {
            loadExistingEventData();
        }
        
        checkHotEventPermission();
    }

    private void initViews() {
        findViewById(R.id.btn_back_custom).setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                selectedImageUri = uri;
                ivPreview.setImageURI(uri);
                layoutUploadPlaceholder.setVisibility(View.GONE);
            }
        });

        seatmapPickerLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null && pendingTierForSeatmap != null) {
                pendingTierForSeatmap.seatmapUri = uri;
                pendingTierForSeatmap.cardSeatmapPreview.setVisibility(View.VISIBLE);
                pendingTierForSeatmap.ivSeatmapPreview.setImageURI(uri);
                pendingTierForSeatmap.tvSeatmapStatus.setText("✓ Đã chọn");
                pendingTierForSeatmap.tvSeatmapStatus.setTextColor(ContextCompat.getColor(this, R.color.brand_primary));
            }
        });

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
        containerTiers = findViewById(R.id.container_tiers);
        btnAddTier = findViewById(R.id.btn_add_tier);

        // Fields
        edtTitle = findViewById(R.id.edt_admin_title);
        edtDesc = findViewById(R.id.edt_admin_desc);
        spinnerCategory = findViewById(R.id.spinner_admin_category);
        spinnerRequiredTier = findViewById(R.id.spinner_admin_required_tier);
        edtLocation = findViewById(R.id.edt_admin_location);
        edtImage = findViewById(R.id.edt_admin_image);
        edtGoogleMapsUrl = findViewById(R.id.edt_admin_google_maps);
        edtSpeakers = findViewById(R.id.edt_admin_speakers);
        
        tvDateTime = findViewById(R.id.tv_admin_datetime);
        tvPreviewTitle = findViewById(R.id.tv_preview_title);
        tvPreviewInfo = findViewById(R.id.tv_preview_info);
        
        cbIsHot = findViewById(R.id.cb_admin_is_hot);
        swIsFree = findViewById(R.id.sw_admin_is_free);
        ivPreview = findViewById(R.id.iv_admin_preview_image);
        
        rgImageSource = findViewById(R.id.rg_image_source);
        layoutImageUrl = findViewById(R.id.layout_admin_image_url);
        cardImageUpload = findViewById(R.id.card_admin_image_upload);
        layoutUploadPlaceholder = findViewById(R.id.layout_upload_placeholder);

        // Buttons
        btnSave = findViewById(R.id.btn_admin_save);
        btnNext = findViewById(R.id.btn_admin_next);
        btnPrev = findViewById(R.id.btn_admin_prev);
    }

    private void setupCategoryDropdown() {
        String[] categories = {"Hội thảo", "Workshop", "Nhạc hội", "Triển lãm", "Talkshow", "Khác"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categories);
        spinnerCategory.setAdapter(adapter);

        String[] tiers = {"Thường", "Đồng", "Bạc", "Vàng", "Thân thiết số một"};
        ArrayAdapter<String> tierAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, tiers);
        spinnerRequiredTier.setAdapter(tierAdapter);
        spinnerRequiredTier.setText("Thường", false); // Mặc định: không giới hạn
    }

    private void initEvents() {
        btnAddTier.setOnClickListener(v -> addTierCard());
        findViewById(R.id.btn_admin_pick_date).setOnClickListener(v -> showDateTimePicker());
        btnSave.setOnClickListener(v -> saveEvent());
        btnNext.setOnClickListener(v -> nextStep());
        btnPrev.setOnClickListener(v -> prevStep());

        swIsFree.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Khi miễn phí: set giá tất cả tier = 0
            for (TierInput ti : tierInputList) {
                ti.edtPrice.setEnabled(!isChecked);
                if (isChecked) ti.edtPrice.setText("0");
            }
        });

        edtImage.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (rgImageSource.getCheckedRadioButtonId() == R.id.rb_image_link) {
                    updateImagePreview(s.toString());
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });
        
        rgImageSource.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_image_upload) {
                cardImageUpload.setVisibility(View.VISIBLE);
                layoutImageUrl.setVisibility(View.GONE);
                if (selectedImageUri == null) {
                    layoutUploadPlaceholder.setVisibility(View.VISIBLE);
                } else {
                    layoutUploadPlaceholder.setVisibility(View.GONE);
                    ivPreview.setImageURI(selectedImageUri);
                }
            } else {
                cardImageUpload.setVisibility(View.GONE);
                layoutImageUrl.setVisibility(View.VISIBLE);
                updateImagePreview(edtImage.getText().toString());
            }
        });

        cardImageUpload.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
    }

    // ================================================================
    // TIER MANAGEMENT
    // ================================================================

    private void addTierCard() {
        View tierView = LayoutInflater.from(this).inflate(R.layout.item_tier_input, containerTiers, false);

        TierInput ti = new TierInput();
        ti.rootView = tierView;
        ti.edtName = tierView.findViewById(R.id.edt_tier_name);
        ti.edtPrice = tierView.findViewById(R.id.edt_tier_price);
        ti.edtQuantity = tierView.findViewById(R.id.edt_tier_quantity);
        ti.cardSeatmapPreview = tierView.findViewById(R.id.card_tier_seatmap_preview);
        ti.ivSeatmapPreview = tierView.findViewById(R.id.iv_tier_seatmap_preview);
        ti.tvSeatmapStatus = tierView.findViewById(R.id.tv_tier_seatmap_status);

        // Label số thứ tự
        TextView tvLabel = tierView.findViewById(R.id.tv_tier_label);
        tvLabel.setText("Hạng " + (tierInputList.size() + 1));

        // Nút xóa hạng
        MaterialButton btnRemove = tierView.findViewById(R.id.btn_remove_tier);
        btnRemove.setOnClickListener(v -> {
            containerTiers.removeView(tierView);
            tierInputList.remove(ti);
            refreshTierLabels();
        });

        // Nút upload ảnh sơ đồ
        MaterialButton btnUploadSeatmap = tierView.findViewById(R.id.btn_tier_upload_seatmap);
        btnUploadSeatmap.setOnClickListener(v -> {
            pendingTierForSeatmap = ti;
            seatmapPickerLauncher.launch("image/*");
        });

        tierInputList.add(ti);
        containerTiers.addView(tierView);
    }

    private void refreshTierLabels() {
        for (int i = 0; i < tierInputList.size(); i++) {
            TextView tvLabel = tierInputList.get(i).rootView.findViewById(R.id.tv_tier_label);
            tvLabel.setText("Hạng " + (i + 1));
        }
    }

    private boolean validateTiers() {
        if (tierInputList.isEmpty()) {
            Toast.makeText(this, "Vui lòng thêm ít nhất 1 hạng vé", Toast.LENGTH_SHORT).show();
            return false;
        }
        for (int i = 0; i < tierInputList.size(); i++) {
            TierInput ti = tierInputList.get(i);
            String name = ti.edtName.getText() != null ? ti.edtName.getText().toString().trim() : "";
            String priceStr = ti.edtPrice.getText() != null ? ti.edtPrice.getText().toString().trim() : "";
            String qtyStr = ti.edtQuantity.getText() != null ? ti.edtQuantity.getText().toString().trim() : "";

            if (name.isEmpty()) {
                ti.edtName.setError("Nhập tên hạng vé");
                return false;
            }
            if (priceStr.isEmpty() && !swIsFree.isChecked()) {
                ti.edtPrice.setError("Nhập giá vé");
                return false;
            }
            if (qtyStr.isEmpty()) {
                ti.edtQuantity.setError("Nhập số lượng");
                return false;
            }
        }
        return true;
    }

    // ================================================================
    // NAVIGATION
    // ================================================================

    private void nextStep() {
        if (currentStep == 1) {
            if (validateStep1()) {
                currentStep = 2;
                updateStepUI();
            }
        } else if (currentStep == 2) {
            if (validateTiers() && validateDateTime()) {
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

    private boolean validateDateTime() {
        if (!isDateTimeSelected) {
            Toast.makeText(this, "Vui lòng chọn thời gian tổ chức sự kiện", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void updateStepUI() {
        layoutStep1.setVisibility(currentStep == 1 ? View.VISIBLE : View.GONE);
        layoutStep2.setVisibility(currentStep == 2 ? View.VISIBLE : View.GONE);
        layoutStep3.setVisibility(currentStep == 3 ? View.VISIBLE : View.GONE);

        btnPrev.setVisibility(currentStep == 1 ? View.INVISIBLE : View.VISIBLE);
        btnNext.setVisibility(currentStep == 3 ? View.GONE : View.VISIBLE);
        btnSave.setVisibility(currentStep == 3 ? View.VISIBLE : View.GONE);

        tvStep1Num.setBackgroundResource(currentStep >= 1 ? R.drawable.bg_chip : R.drawable.bg_chip_light);
        tvStep1Num.setTextColor(currentStep >= 1 ? ContextCompat.getColor(this, R.color.white) : ContextCompat.getColor(this, R.color.text_muted));
        
        tvStep2Num.setBackgroundResource(currentStep >= 2 ? R.drawable.bg_chip : R.drawable.bg_chip_light);
        tvStep2Num.setTextColor(currentStep >= 2 ? ContextCompat.getColor(this, R.color.white) : ContextCompat.getColor(this, R.color.text_muted));
        
        tvStep3Num.setBackgroundResource(currentStep >= 3 ? R.drawable.bg_chip : R.drawable.bg_chip_light);
        tvStep3Num.setTextColor(currentStep >= 3 ? ContextCompat.getColor(this, R.color.white) : ContextCompat.getColor(this, R.color.text_muted));

        if (tvStep1Text != null) tvStep1Text.setTextColor(currentStep >= 1 ? ContextCompat.getColor(this, R.color.white) : 0xAAFFFFFF);
        if (tvStep2Text != null) tvStep2Text.setTextColor(currentStep >= 2 ? ContextCompat.getColor(this, R.color.white) : 0xAAFFFFFF);
        if (tvStep3Text != null) tvStep3Text.setTextColor(currentStep >= 3 ? ContextCompat.getColor(this, R.color.white) : 0xAAFFFFFF);

        if (viewStep1Divider != null) viewStep1Divider.setBackgroundColor(currentStep >= 2 ? 0xFFFFFFFF : 0x66FFFFFF);
        if (viewStep2Divider != null) viewStep2Divider.setBackgroundColor(currentStep >= 3 ? 0xFFFFFFFF : 0x66FFFFFF);
    }

    private void updatePreviewData() {
        tvPreviewTitle.setText(edtTitle.getText().toString());
        String info = spinnerCategory.getText().toString() + " • " + edtLocation.getText().toString();
        tvPreviewInfo.setText(info);
    }

    // ================================================================
    // VALIDATION
    // ================================================================

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

    // ================================================================
    // SAVE EVENT
    // ================================================================

    private void saveEvent() {
        btnSave.setEnabled(false);
        if (rgImageSource.getCheckedRadioButtonId() == R.id.rb_image_upload && selectedImageUri != null) {
            uploadImageAndSaveEvent();
        } else {
            finalizeSaveEvent(edtImage.getText().toString().trim());
        }
    }

    private void uploadImageAndSaveEvent() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang tải ảnh bìa lên...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        StorageReference storageRef = FirebaseStorage.getInstance().getReference("event_images/" + UUID.randomUUID());
        storageRef.putFile(selectedImageUri).addOnSuccessListener(taskSnapshot ->
            storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                progressDialog.dismiss();
                finalizeSaveEvent(uri.toString());
            }).addOnFailureListener(e -> {
                progressDialog.dismiss();
                btnSave.setEnabled(true);
                Toast.makeText(this, "Lỗi lấy link ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            })
        ).addOnFailureListener(e -> {
            progressDialog.dismiss();
            btnSave.setEnabled(true);
            Toast.makeText(this, "Lỗi upload ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void finalizeSaveEvent(String imageUrl) {
        // Upload ảnh sơ đồ cho từng tier (nếu có) rồi mới lưu
        List<TierInput> tiersWithPendingUpload = new ArrayList<>();
        for (TierInput ti : tierInputList) {
            if (ti.seatmapUri != null && ti.uploadedSeatmapUrl == null) {
                tiersWithPendingUpload.add(ti);
            }
        }

        if (tiersWithPendingUpload.isEmpty()) {
            buildAndSaveEvent(imageUrl);
        } else {
            ProgressDialog pd = new ProgressDialog(this);
            pd.setMessage("Đang tải ảnh sơ đồ...");
            pd.setCancelable(false);
            pd.show();

            AtomicInteger remaining = new AtomicInteger(tiersWithPendingUpload.size());
            for (TierInput ti : tiersWithPendingUpload) {
                StorageReference ref = FirebaseStorage.getInstance()
                        .getReference("seat_maps/" + UUID.randomUUID());
                ref.putFile(ti.seatmapUri)
                    .addOnSuccessListener(snap -> ref.getDownloadUrl().addOnSuccessListener(uri -> {
                        ti.uploadedSeatmapUrl = uri.toString();
                        if (remaining.decrementAndGet() == 0) {
                            pd.dismiss();
                            buildAndSaveEvent(imageUrl);
                        }
                    }))
                    .addOnFailureListener(e -> {
                        pd.dismiss();
                        btnSave.setEnabled(true);
                        Toast.makeText(this, "Lỗi upload sơ đồ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
            }
        }
    }

    private void buildAndSaveEvent(String imageUrl) {
        Event event = (existingEvent != null) ? existingEvent : new Event();
        String currentAdminId = FirebaseAuth.getInstance().getUid();

        event.setCreatedByAdminId(currentAdminId);
        event.setTitle(edtTitle.getText().toString().trim());
        event.setCategory(spinnerCategory.getText().toString().trim());
        event.setDescription(edtDesc.getText().toString().trim());
        event.setLocation(edtLocation.getText().toString().trim());
        event.setGoogleMapsUrl(edtGoogleMapsUrl.getText().toString().trim());
        event.setFree(swIsFree.isChecked());
        event.setImage(imageUrl);
        
        boolean wasHot = event.isHot();
        boolean isNowHot = cbIsHot.getVisibility() == View.VISIBLE && cbIsHot.isChecked();
        event.setHot(isNowHot);
        
        // Nếu admin can thiệp thủ công:
        // 1. Chuyển từ KHÔNG HOT sang HOT -> Đặt thời điểm HOT và tắt flag Tự động (để được 3 ngày)
        // 2. Chuyển từ HOT sang KHÔNG HOT -> Xóa thời điểm HOT và flag Tự động
        if (isNowHot && !wasHot) {
            event.setHotSetAt(Timestamp.now());
            event.setAutoHot(false); 
        } else if (!isNowHot) {
            event.setHotSetAt(null);
            event.setAutoHot(false);
        }

        event.setDate(new Timestamp(calendar.getTime()));
        // Phase A: Ghi nhận hạng thành viên tối thiểu để mua vé
        String selectedTier = spinnerRequiredTier.getText().toString().trim();
        event.setRequiredTier(selectedTier.isEmpty() ? "Thường" : selectedTier);

        // Tạo danh sách tiers
        List<TicketTier> tiers = new ArrayList<>();
        int totalTickets = 0;
        double minPrice = Double.MAX_VALUE;

        for (TierInput ti : tierInputList) {
            String name = ti.edtName.getText().toString().trim();
            String priceStr = ti.edtPrice.getText().toString().trim();
            String qtyStr = ti.edtQuantity.getText().toString().trim();

            double tierPrice = swIsFree.isChecked() ? 0 : (priceStr.isEmpty() ? 0 : Double.parseDouble(priceStr));
            int tierQty = qtyStr.isEmpty() ? 0 : Integer.parseInt(qtyStr);
            String seatmapUrl = ti.uploadedSeatmapUrl != null ? ti.uploadedSeatmapUrl : "";

            TicketTier tier = new TicketTier(UUID.randomUUID().toString(), name, tierPrice, tierQty, seatmapUrl);
            tiers.add(tier);
            totalTickets += tierQty;
            if (tierPrice < minPrice) minPrice = tierPrice;
        }

        event.setTiers(tiers);
        event.setTotalTicket(totalTickets);
        event.setRemainingTicket(totalTickets);
        event.setPrice(minPrice == Double.MAX_VALUE ? 0 : minPrice); // price = giá thấp nhất (backward compat)

        if (editEventId == null) {
            event.setEventId(UUID.randomUUID().toString());
        }

        // Lấy thông tin cấp độ của admin đang tạo/sửa sự kiện
        userController.getAdminById(currentAdminId, admin -> {
            int level = (admin != null) ? admin.getAccessLevel() : 1; // Mặc định 1: Staff
            event.setCreatorAccessLevel(level);
            
            eventController.saveEvent(event, () -> runOnUiThread(() -> {
                Toast.makeText(this, "Đăng sự kiện thành công!", Toast.LENGTH_SHORT).show();
                finish();
            }), error -> runOnUiThread(() -> {
                btnSave.setEnabled(true);
                Toast.makeText(this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }));
        }, err -> {
            event.setCreatorAccessLevel(1); // Mặc định là Staff nếu lỗi
            eventController.saveEvent(event, () -> runOnUiThread(() -> {
                Toast.makeText(this, "Đăng sự kiện thành công!", Toast.LENGTH_SHORT).show();
                finish();
            }), error -> runOnUiThread(() -> {
                btnSave.setEnabled(true);
                Toast.makeText(this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }));
        });
    }

    // ================================================================
    // HELPERS
    // ================================================================

    private void updateImagePreview(String url) {
        if (url != null && !url.isEmpty()) {
            Glide.with(this).load(url).placeholder(R.drawable.img_logo_event_ticket_booking).into(ivPreview);
        }
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
                    edtImage.setText(event.getImage());
                    edtGoogleMapsUrl.setText(event.getGoogleMapsUrl());
                    if (event.getSpeakers() != null) {
                        edtSpeakers.setText(String.join(", ", event.getSpeakers()));
                    }
                    cbIsHot.setChecked(event.isHot());
                    swIsFree.setChecked(event.isFree());
                    updateImagePreview(event.getImage());
                    // Phase A: Khôi phục hạng ưu tiên khi chỉnh sửa sự kiện
                    if (event.getRequiredTier() != null && !event.getRequiredTier().isEmpty()) {
                        spinnerRequiredTier.setText(event.getRequiredTier(), false);
                    }

                    // Load existing tiers
                    if (event.hasTiers()) {
                        containerTiers.removeAllViews();
                        tierInputList.clear();
                        for (TicketTier t : event.getTiers()) {
                            addTierCard();
                            TierInput ti = tierInputList.get(tierInputList.size() - 1);
                            ti.edtName.setText(t.getTierName());
                            ti.edtPrice.setText(String.valueOf((long) t.getPrice()));
                            ti.edtQuantity.setText(String.valueOf(t.getTotalTicket()));
                            if (t.getSeatMapImageUrl() != null && !t.getSeatMapImageUrl().isEmpty()) {
                                ti.uploadedSeatmapUrl = t.getSeatMapImageUrl();
                                ti.tvSeatmapStatus.setText("✓ Đã có");
                                ti.cardSeatmapPreview.setVisibility(View.VISIBLE);
                                Glide.with(this).load(t.getSeatMapImageUrl()).into(ti.ivSeatmapPreview);
                            }
                        }
                    }

                    if (event.getDate() != null) {
                        calendar.setTime(event.getDate().toDate());
                        isDateTimeSelected = true;
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
                isDateTimeSelected = true;
                updateDateTimeDisplay();
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    /** Kiểm tra quyền tạo Sự kiện Hot: Admin Cấp 2 (Manager) trở lên mới được phép gắn thẻ Hot cho sự kiện */
    private void checkHotEventPermission() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            cbIsHot.setVisibility(View.GONE);
            return;
        }
        userController.getAdminById(uid, admin -> {
            if (admin != null && admin.getAccessLevel() >= 2) {
                // Cấp 2 (Manager) và Cấp 3 (Developer): hiển thị checkbox Hot
                runOnUiThread(() -> cbIsHot.setVisibility(View.VISIBLE));
            } else {
                // Cấp 1: ẩn checkbox Hot, bỏ check mặc định
                runOnUiThread(() -> {
                    cbIsHot.setVisibility(View.GONE);
                    cbIsHot.setChecked(false);
                });
            }
        }, err -> runOnUiThread(() -> cbIsHot.setVisibility(View.GONE)));
    }
}
