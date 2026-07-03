package vn.humg.hai.event_ticket_booking_app.view;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import vn.humg.hai.event_ticket_booking_app.R;
import vn.humg.hai.event_ticket_booking_app.controller.UserController;
import vn.humg.hai.event_ticket_booking_app.model.User;

public class EditProfileActivity extends AppCompatActivity {

    private TextInputEditText edtFullname, edtEmail, edtPhone;
    private ImageView ivAvatar;
    private FloatingActionButton fabChangeAvatar;
    private MaterialButton btnSave;
    private Toolbar toolbar;

    private final UserController userController = new UserController();
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        initViews();
        loadUserData();
        setupEvents();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar_edit_profile);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        edtFullname = findViewById(R.id.edt_edit_fullname);
        edtEmail = findViewById(R.id.edt_edit_email);
        edtPhone = findViewById(R.id.edt_edit_phone);
        ivAvatar = findViewById(R.id.iv_edit_avatar);
        fabChangeAvatar = findViewById(R.id.fab_change_avatar);
        btnSave = findViewById(R.id.btn_save_profile);
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
                    
                    Glide.with(this)
                            .load(R.drawable.img_logo_event_ticket_booking)
                            .circleCrop()
                            .into(ivAvatar);
                });
            }
        }, e -> Toast.makeText(this, "Lỗi tải thông tin", Toast.LENGTH_SHORT).show());
    }

    private void setupEvents() {
        fabChangeAvatar.setOnClickListener(v -> Toast.makeText(this, "Tính năng đang phát triển", Toast.LENGTH_SHORT).show());
        
        btnSave.setOnClickListener(v -> {
            if (currentUser == null) return;
            
            String name = edtFullname.getText().toString().trim();
            String phone = edtPhone.getText().toString().trim();
            
            if (name.isEmpty()) {
                edtFullname.setError("Vui lòng nhập họ tên");
                return;
            }

            currentUser.setFullName(name);
            currentUser.setPhone(phone);

            userController.saveUserProfile(currentUser, () -> {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }, e -> Toast.makeText(this, "Lỗi: " + e, Toast.LENGTH_SHORT).show());
        });
    }
}
