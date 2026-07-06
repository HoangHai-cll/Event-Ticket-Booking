package vn.humg.hai.event_ticket_booking_app.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import vn.humg.hai.event_ticket_booking_app.R;
import vn.humg.hai.event_ticket_booking_app.adapter.AuthResultAdapter;
import vn.humg.hai.event_ticket_booking_app.viewmodel.AuthViewModel;

public class RegisterActivity extends AppCompatActivity {

    private MaterialButton btnRegister;
    private TextView tvLoginLink, tvTerms, tvPolicy;
    private TextInputEditText edtFullname, edtEmail, edtPhone, edtPassword, edtConfirmPassword, edtAdminCode;
    private CheckBox cbAgree;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        setupObservers();

        initViews();
        initEvents();
    }

    private void initViews() {
        btnRegister = findViewById(R.id.btn_register);
        tvLoginLink = findViewById(R.id.tv_link_login);
        tvTerms = findViewById(R.id.tv_terms);
        tvPolicy = findViewById(R.id.tv_policy);
        edtFullname = findViewById(R.id.edt_fullname);
        edtEmail = findViewById(R.id.edt_register_email);
        edtPhone = findViewById(R.id.edt_phone);
        edtPassword = findViewById(R.id.edt_register_password);
        edtConfirmPassword = findViewById(R.id.edt_confirm_password);
        edtAdminCode = findViewById(R.id.edt_admin_code);
        cbAgree = findViewById(R.id.cb_agree);
    }

    private void setupObservers() {
        authViewModel.getAuthSuccessState().observe(this, success -> {
            if (success) {
                String email = edtEmail.getText().toString().trim();
                if (!email.contains("@")) email += "@gmail.com";
                String password = edtPassword.getText().toString();
                
                Intent data = AuthResultAdapter.createResult(email, password, true);
                setResult(RESULT_OK, data);
                finish();
            }
        });

        authViewModel.getErrorState().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, "Đăng ký thất bại: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initEvents() {
        tvLoginLink.setOnClickListener(v -> finish());

        // Mở màn hình Điều khoản
        tvTerms.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, TermsPolicyActivity.class);
            intent.putExtra("type", "terms");
            startActivity(intent);
        });

        // Mở màn hình Chính sách
        tvPolicy.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, TermsPolicyActivity.class);
            intent.putExtra("type", "policy");
            startActivity(intent);
        });
        
        edtEmail.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String email = edtEmail.getText().toString().trim();
                if (!email.isEmpty() && !email.contains("@")) {
                    edtEmail.setText(email + "@gmail.com");
                }
            }
        });

        btnRegister.setOnClickListener(v -> {
            String fullname = edtFullname.getText().toString().trim();
            String rawEmail = edtEmail.getText().toString().trim();
            String phone = edtPhone.getText().toString().trim();
            String rawPassword = edtPassword.getText().toString();
            String confirmPassword = edtConfirmPassword.getText().toString();
            String adminCode = edtAdminCode.getText().toString().trim();

            if (fullname.isEmpty() || rawEmail.isEmpty() || rawPassword.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!rawEmail.contains("@")) rawEmail += "@gmail.com";
            if (!rawPassword.equals(confirmPassword)) {
                Toast.makeText(this, "Mật khẩu không khớp", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!cbAgree.isChecked()) {
                Toast.makeText(this, "Bạn cần đồng ý với điều khoản", Toast.LENGTH_SHORT).show();
                return;
            }

            authViewModel.register(rawEmail, rawPassword, fullname, phone, adminCode);
        });
    }
}
