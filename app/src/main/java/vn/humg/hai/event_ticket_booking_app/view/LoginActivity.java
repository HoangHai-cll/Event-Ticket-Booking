package vn.humg.hai.event_ticket_booking_app.view;

import vn.humg.hai.event_ticket_booking_app.R;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import androidx.lifecycle.ViewModelProvider;
import vn.humg.hai.event_ticket_booking_app.adapter.AuthResultAdapter;
import vn.humg.hai.event_ticket_booking_app.viewmodel.AuthViewModel;

public class LoginActivity extends AppCompatActivity {

    private MaterialButton btnLogin, btnGoogle, btnFacebook;
    private TextView tvRegisterLink;
    private TextInputEditText edtEmail, edtPassword;
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
                Toast.makeText(this, "Vui lòng nhập email và mật khẩu.", Toast.LENGTH_SHORT).show();
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
}
