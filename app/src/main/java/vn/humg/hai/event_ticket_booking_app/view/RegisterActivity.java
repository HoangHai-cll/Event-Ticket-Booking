package vn.humg.hai.event_ticket_booking_app.view;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import vn.humg.hai.event_ticket_booking_app.R;
import vn.humg.hai.event_ticket_booking_app.adapter.AuthResultAdapter;
import vn.humg.hai.event_ticket_booking_app.utils.ValidationUtils;
import vn.humg.hai.event_ticket_booking_app.viewmodel.AuthViewModel;

public class RegisterActivity extends AppCompatActivity {

    private MaterialButton btnRegister;
    private TextView tvLoginLink, tvTerms, tvPolicy;
    private TextInputEditText edtFullname, edtEmail, edtPhone, edtPassword, edtConfirmPassword, edtAdminCode;
    private TextInputLayout tilFullname, tilEmail, tilPhone, tilPassword, tilConfirmPassword, tilAdminCode;
    private LinearLayout layoutContainer;
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
        startEntranceAnimation();
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

        tilFullname = findViewById(R.id.til_reg_fullname);
        tilEmail = findViewById(R.id.til_reg_email);
        tilPhone = findViewById(R.id.til_reg_phone);
        tilPassword = findViewById(R.id.til_reg_password);
        tilConfirmPassword = findViewById(R.id.til_reg_confirm_password);
        tilAdminCode = findViewById(R.id.til_reg_admin_code);
        layoutContainer = findViewById(R.id.layout_register_container);
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
                if (fullname.isEmpty()) shakeView(tilFullname);
                if (rawEmail.isEmpty()) shakeView(tilEmail);
                if (rawPassword.isEmpty()) shakeView(tilPassword);
                return;
            }

            if (!rawEmail.contains("@")) rawEmail += "@gmail.com";

            if (!ValidationUtils.isValidEmail(rawEmail)) {
                Toast.makeText(this, "Định dạng Email không hợp lệ", Toast.LENGTH_SHORT).show();
                shakeView(tilEmail);
                return;
            }

            if (!ValidationUtils.isValidPassword(rawPassword)) {
                Toast.makeText(this, "Mật khẩu phải chứa ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
                shakeView(tilPassword);
                return;
            }

            if (!rawPassword.equals(confirmPassword)) {
                Toast.makeText(this, "Mật khẩu không khớp", Toast.LENGTH_SHORT).show();
                shakeView(tilConfirmPassword);
                return;
            }
            if (!cbAgree.isChecked()) {
                Toast.makeText(this, "Bạn cần đồng ý với điều khoản", Toast.LENGTH_SHORT).show();
                shakeView(cbAgree);
                return;
            }

            authViewModel.register(rawEmail, rawPassword, fullname, phone, adminCode);
        });
    }

    /**
     * Triggers a smooth slide-up and fade-in entrance animation for the registration container.
     */
    private void startEntranceAnimation() {
        if (layoutContainer != null) {
            layoutContainer.setAlpha(0f);
            layoutContainer.setTranslationY(80f);
            layoutContainer.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(600)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        }
    }

    /**
     * Plays a horizontal shake animation on the specified view to indicate a validation error.
     */
    private void shakeView(View view) {
        if (view == null) return;
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationX", 0, 25, -25, 25, -25, 15, -15, 6, -6, 0);
        animator.setDuration(500);
        animator.start();
    }
}
