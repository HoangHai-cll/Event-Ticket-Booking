package vn.humg.hai.event_ticket_booking_app.view;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import vn.humg.hai.event_ticket_booking_app.R;

public class ChangePasswordActivity extends AppCompatActivity {

    private TextInputEditText edtCurrentPassword, edtNewPassword, edtConfirmPassword;
    private MaterialButton btnUpdate;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        initViews();
        setupEvents();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar_change_password);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        edtCurrentPassword = findViewById(R.id.edt_current_password);
        edtNewPassword = findViewById(R.id.edt_new_password);
        edtConfirmPassword = findViewById(R.id.edt_confirm_password);
        btnUpdate = findViewById(R.id.btn_update_password);
    }

    private void setupEvents() {
        btnUpdate.setOnClickListener(v -> {
            String currentPass = edtCurrentPassword.getText().toString();
            String newPass = edtNewPassword.getText().toString();
            String confirmPass = edtConfirmPassword.getText().toString();

            if (currentPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPass.equals(confirmPass)) {
                Toast.makeText(this, "Mật khẩu mới không khớp", Toast.LENGTH_SHORT).show();
                return;
            }

            if (newPass.length() < 6) {
                Toast.makeText(this, "Mật khẩu phải ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
                return;
            }

            updatePassword(currentPass, newPass);
        });
    }

    private void updatePassword(String currentPass, String newPass) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || user.getEmail() == null) return;

        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPass);

        btnUpdate.setEnabled(false);
        btnUpdate.setText("Đang cập nhật...");

        user.reauthenticate(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                user.updatePassword(newPass).addOnCompleteListener(updateTask -> {
                    btnUpdate.setEnabled(true);
                    btnUpdate.setText("Cập nhật mật khẩu");
                    if (updateTask.isSuccessful()) {
                        Toast.makeText(this, "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Lỗi: " + updateTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                btnUpdate.setEnabled(true);
                btnUpdate.setText("Cập nhật mật khẩu");
                Toast.makeText(this, "Mật khẩu hiện tại không đúng", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
