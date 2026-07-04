package vn.humg.hai.event_ticket_booking_app.view;

import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import vn.humg.hai.event_ticket_booking_app.R;
import vn.humg.hai.event_ticket_booking_app.controller.UserController;
import vn.humg.hai.event_ticket_booking_app.model.User;

public class EditProfileActivity extends AppCompatActivity {

    private TextInputEditText edtFullname, edtEmail, edtPhone, edtBirthday;
    private AutoCompleteTextView spinnerGender;
    private ChipGroup chipGroupInterests;
    private ImageView ivAvatar;
    private FloatingActionButton fabChangeAvatar;
    private MaterialButton btnSave;

    private final UserController userController = new UserController();
    private User currentUser;
    private Calendar calendar = Calendar.getInstance();

    private static final String[] PRESET_AVATARS = {
        "https://i.pravatar.cc/150?img=11",
        "https://i.pravatar.cc/150?img=12",
        "https://i.pravatar.cc/150?img=13",
        "https://i.pravatar.cc/150?img=14"
    };

    private String selectedAvatarUrl = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        initViews();
        setupGenderDropdown();
        loadUserData();
        setupEvents();
    }

    private void initViews() {
        findViewById(R.id.btn_back_custom).setOnClickListener(v -> finish());

        edtFullname = findViewById(R.id.edt_edit_fullname);
        edtEmail = findViewById(R.id.edt_edit_email);
        edtPhone = findViewById(R.id.edt_edit_phone);
        edtBirthday = findViewById(R.id.edt_edit_birthday);
        spinnerGender = findViewById(R.id.spinner_edit_gender);
        chipGroupInterests = findViewById(R.id.chip_group_interests);
        ivAvatar = findViewById(R.id.iv_edit_avatar);
        fabChangeAvatar = findViewById(R.id.fab_change_avatar);
        btnSave = findViewById(R.id.btn_save_profile);
    }

    private void setupGenderDropdown() {
        String[] genders = {"Nam", "Nữ", "Khác"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, genders);
        spinnerGender.setAdapter(adapter);
    }

    private void loadUserData() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        userController.getUserById(uid, user -> {
            if (user != null) {
                currentUser = user;
                runOnUiThread(() -> {
                    edtFullname.setText(user.getFullName());
                    edtEmail.setText(user.getEmail());
                    edtPhone.setText(user.getPhone());
                    edtBirthday.setText(user.getBirthday() != null ? user.getBirthday() : "");
                    
                    if (user.getGender() != null) {
                        spinnerGender.setText(user.getGender(), false);
                    }
                    
                    selectedAvatarUrl = user.getAvatarName() != null ? user.getAvatarName() : "";
                    
                    if (!selectedAvatarUrl.isEmpty()) {
                        Glide.with(this)
                                .load(selectedAvatarUrl)
                                .circleCrop()
                                .placeholder(R.drawable.img_logo_event_ticket_booking)
                                .into(ivAvatar);
                    } else {
                        Glide.with(this)
                                .load(R.drawable.img_logo_event_ticket_booking)
                                .circleCrop()
                                .into(ivAvatar);
                    }

                    // Render Interests Chips
                    renderInterests(user.getInterests());
                });
            }
        }, e -> Toast.makeText(this, "Lỗi tải thông tin", Toast.LENGTH_SHORT).show());
    }

    private void renderInterests(List<String> interests) {
        chipGroupInterests.removeAllViews();
        
        if (interests == null) {
            interests = new ArrayList<>();
        }

        // Nếu trống thì tạo một số sở thích mặc định để giao diện không bị trống trải
        if (interests.isEmpty()) {
            interests.add("Âm nhạc");
            interests.add("Công nghệ");
            currentUser.setInterests(interests);
        }

        for (String interest : interests) {
            addInterestChip(interest);
        }

        // Nút thêm chip mới
        addAddInterestChipButton();
    }

    private void addInterestChip(String interestText) {
        Chip chip = new Chip(this);
        chip.setText(interestText);
        chip.setCloseIconVisible(true);
        chip.setChipBackgroundColorResource(R.color.brand_light_purple);
        chip.setTextColor(getResources().getColor(R.color.brand_primary));
        chip.setCloseIconTintResource(R.color.brand_primary);
        
        chip.setOnCloseIconClickListener(v -> {
            if (currentUser != null && currentUser.getInterests() != null) {
                currentUser.getInterests().remove(interestText);
                chipGroupInterests.removeView(chip);
            }
        });

        // Add trước nút "Thêm thẻ"
        int childCount = chipGroupInterests.getChildCount();
        chipGroupInterests.addView(chip, childCount > 0 ? childCount - 1 : 0);
    }

    private void addAddInterestChipButton() {
        Chip addChip = new Chip(this);
        addChip.setText("+ Thêm thẻ");
        addChip.setChipBackgroundColorResource(R.color.surface_light);
        addChip.setTextColor(getResources().getColor(R.color.ink_dark));
        addChip.setOnClickListener(v -> showAddInterestDialog());
        chipGroupInterests.addView(addChip);
    }

    private void showAddInterestDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Thêm sở thích mới");

        final EditText input = new EditText(this);
        input.setHint("Ví dụ: Hội thảo, Workshop...");
        input.setSingleLine(true);
        
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(40, 20, 40, 20);
        input.setLayoutParams(params);
        container.addView(input);
        
        builder.setView(container);

        builder.setPositiveButton("Thêm", (dialog, which) -> {
            String text = input.getText().toString().trim();
            if (!text.isEmpty()) {
                if (currentUser.getInterests() == null) {
                    currentUser.setInterests(new ArrayList<>());
                }
                if (!currentUser.getInterests().contains(text)) {
                    currentUser.getInterests().add(text);
                    addInterestChip(text);
                } else {
                    Toast.makeText(this, "Sở thích này đã tồn tại", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void setupEvents() {
        fabChangeAvatar.setOnClickListener(v -> showAvatarPickerDialog());
        ivAvatar.setOnClickListener(v -> showAvatarPickerDialog());

        edtBirthday.setOnClickListener(v -> showDatePicker());

        btnSave.setOnClickListener(v -> {
            if (currentUser == null) return;

            String name = edtFullname.getText().toString().trim();
            String phone = edtPhone.getText().toString().trim();
            String birthday = edtBirthday.getText().toString().trim();
            String gender = spinnerGender.getText().toString().trim();

            if (name.isEmpty()) {
                edtFullname.setError("Vui lòng nhập họ tên");
                return;
            }

            currentUser.setFullName(name);
            currentUser.setPhone(phone);
            currentUser.setBirthday(birthday);
            currentUser.setGender(gender);
            currentUser.setAvatarName(selectedAvatarUrl);

            userController.saveUserProfile(currentUser, () -> {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }, e -> Toast.makeText(this, "Lỗi: " + e, Toast.LENGTH_SHORT).show());
        });
    }

    private void showDatePicker() {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectYear, selectMonth, selectDay) -> {
            calendar.set(Calendar.YEAR, selectYear);
            calendar.set(Calendar.MONTH, selectMonth);
            calendar.set(Calendar.DAY_OF_MONTH, selectDay);

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            edtBirthday.setText(sdf.format(calendar.getTime()));
        }, year, month, day);
        datePickerDialog.show();
    }

    private void showAvatarPickerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_avatar_grid, null);
        builder.setView(dialogView);
        
        AlertDialog dialog = builder.create();
        dialog.show();

        ImageView ivAv1 = dialogView.findViewById(R.id.iv_preset_1);
        ImageView ivAv2 = dialogView.findViewById(R.id.iv_preset_2);
        ImageView ivAv3 = dialogView.findViewById(R.id.iv_preset_3);
        ImageView ivAv4 = dialogView.findViewById(R.id.iv_preset_4);

        Glide.with(this).load(PRESET_AVATARS[0]).circleCrop().into(ivAv1);
        Glide.with(this).load(PRESET_AVATARS[1]).circleCrop().into(ivAv2);
        Glide.with(this).load(PRESET_AVATARS[2]).circleCrop().into(ivAv3);
        Glide.with(this).load(PRESET_AVATARS[3]).circleCrop().into(ivAv4);

        ivAv1.setOnClickListener(v -> selectAvatar(PRESET_AVATARS[0], dialog));
        ivAv2.setOnClickListener(v -> selectAvatar(PRESET_AVATARS[1], dialog));
        ivAv3.setOnClickListener(v -> selectAvatar(PRESET_AVATARS[2], dialog));
        ivAv4.setOnClickListener(v -> selectAvatar(PRESET_AVATARS[3], dialog));
    }

    private void selectAvatar(String url, AlertDialog dialog) {
        selectedAvatarUrl = url;
        Glide.with(this)
                .load(selectedAvatarUrl)
                .circleCrop()
                .placeholder(R.drawable.img_logo_event_ticket_booking)
                .into(ivAvatar);
        dialog.dismiss();
    }
}
