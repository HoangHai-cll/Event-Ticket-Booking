package vn.humg.hai.event_ticket_booking_app.view;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import vn.humg.hai.event_ticket_booking_app.R;
import vn.humg.hai.event_ticket_booking_app.viewmodel.AuthViewModel;

public class ChangePasswordActivity extends AppCompatActivity {

    private TextInputEditText edtCurrentPassword, edtNewPassword, edtConfirmPassword;
    private MaterialButton btnUpdate;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        setupObservers();

        initViews();
        setupEvents();
    }

    private void initViews() {
        findViewById(R.id.btn_back_custom).setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        edtCurrentPassword = findViewById(R.id.edt_current_password);
        edtNewPassword = findViewById(R.id.edt_new_password);
        edtConfirmPassword = findViewById(R.id.edt_confirm_new_password);
        btnUpdate = findViewById(R.id.btn_update_password);
    }

    private void setupObservers() {
        authViewModel.getLoadingState().observe(this, loading -> {
            if (loading) {
                btnUpdate.setEnabled(false);
                btnUpdate.setText("Đang cập nhật...");
            } else {
                btnUpdate.setEnabled(true);
                btnUpdate.setText("Cập nhật mật khẩu");
            }
        });

        authViewModel.getErrorState().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });

        authViewModel.getPasswordChangeSuccessState().observe(this, success -> {
            if (success) {
                Toast.makeText(this, "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
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

            authViewModel.changePassword(currentPass, newPass);
        });
    }
}
