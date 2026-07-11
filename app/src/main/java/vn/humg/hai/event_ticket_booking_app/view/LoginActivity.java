package vn.humg.hai.event_ticket_booking_app.view;

import vn.humg.hai.event_ticket_booking_app.R;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import androidx.lifecycle.ViewModelProvider;
import vn.humg.hai.event_ticket_booking_app.adapter.AuthResultAdapter;
import vn.humg.hai.event_ticket_booking_app.utils.ValidationUtils;
import vn.humg.hai.event_ticket_booking_app.viewmodel.AuthViewModel;

public class LoginActivity extends AppCompatActivity {

    private MaterialButton btnLogin, btnGoogle, btnFacebook;
    private TextView tvRegisterLink;
    private TextInputEditText edtEmail, edtPassword;
    private TextInputLayout tilEmail, tilPassword;
    private LinearLayout layoutContainer;
    private AuthViewModel authViewModel;
    private ActivityResultLauncher<Intent> registerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        setupObservers();

        registerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        String email = AuthResultAdapter.getPrefillEmail(data);
                        String password = AuthResultAdapter.getPrefillPassword(data);
                        boolean registrationSuccess = AuthResultAdapter.isRegistrationSuccess(data);
                        if (email != null) {
                            edtEmail.setText(email);
                        }
                        if (password != null) {
                            edtPassword.setText(password);
                        }
                        if (registrationSuccess) {
                            Toast.makeText(this, "Đăng ký thành công! Vui lòng đăng nhập.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        initViews();
        initEvents();
        prefillLoginFields();
        startEntranceAnimation();
    }

    private void setupObservers() {
        authViewModel.getAuthSuccessState().observe(this, success -> {
            if (success) {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });

        authViewModel.getErrorState().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, "Đăng nhập thất bại: " + error, Toast.LENGTH_LONG).show();
                // Shake both layouts on general authentication failure
                shakeView(tilEmail);
                shakeView(tilPassword);
            }
        });
    }

    private void prefillLoginFields() {
        String email = getIntent().getStringExtra("prefill_email");
        String password = getIntent().getStringExtra("prefill_password");
        if (email != null) {
            edtEmail.setText(email);
        }
        if (password != null) {
            edtPassword.setText(password);
        }
    }

    private void initViews() {
        btnLogin = findViewById(R.id.btn_login);
        btnGoogle = findViewById(R.id.btn_login_google);
        btnFacebook = findViewById(R.id.btn_login_facebook);
        tvRegisterLink = findViewById(R.id.tv_link_register);
        edtEmail = findViewById(R.id.edt_login_email);
        edtPassword = findViewById(R.id.edt_login_password);
        tilEmail = findViewById(R.id.til_login_email);
        tilPassword = findViewById(R.id.til_login_password);
        layoutContainer = findViewById(R.id.layout_login_container);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        prefillLoginFields();
    }

    private void initEvents() {
        // Chuyển sang màn hình đăng ký
        tvRegisterLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            registerLauncher.launch(intent);
        });

        // Xử lý nút đăng nhập
        btnLogin.setOnClickListener(v -> {
            String email = edtEmail.getText() != null ? edtEmail.getText().toString().trim() : "";
            String password = edtPassword.getText() != null ? edtPassword.getText().toString().trim() : "";

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ email và mật khẩu.", Toast.LENGTH_SHORT).show();
                if (email.isEmpty()) shakeView(tilEmail);
                if (password.isEmpty()) shakeView(tilPassword);
                return;
            }

            if (!ValidationUtils.isValidEmail(email)) {
                Toast.makeText(this, "Định dạng Email không hợp lệ.", Toast.LENGTH_SHORT).show();
                shakeView(tilEmail);
                return;
            }

            if (!ValidationUtils.isValidPassword(password)) {
                Toast.makeText(this, "Mật khẩu phải chứa ít nhất 6 ký tự.", Toast.LENGTH_SHORT).show();
                shakeView(tilPassword);
                return;
            }

            authViewModel.login(email, password);
        });

        // Đăng nhập MXH
        btnGoogle.setOnClickListener(v ->
                Toast.makeText(this, "Đăng nhập bằng Google...", Toast.LENGTH_SHORT).show());

        btnFacebook.setOnClickListener(v ->
                Toast.makeText(this, "Đăng nhập bằng Facebook...", Toast.LENGTH_SHORT).show());
    }

    /**
     * Triggers a smooth slide-up and fade-in entrance animation for the login container.
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
