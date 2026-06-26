package vn.humg.hai.event_ticket_booking_app.view;

import vn.humg.hai.event_ticket_booking_app.R;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.InputFilter;
import android.text.method.DigitsKeyListener;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.ViewGroup;
import android.view.View;
import android.graphics.Color;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import vn.humg.hai.event_ticket_booking_app.adapter.AuthResultAdapter;
import vn.humg.hai.event_ticket_booking_app.controller.AuthController;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private MaterialButton btnRegister, btnGoogle, btnFacebook;
    private TextView tvLoginLink, tvTermsPolicy;
    private TextInputEditText edtFullname, edtEmail, edtPhone, edtPassword, edtConfirmPassword;
    private CheckBox cbAgree;
    private FirebaseAuth auth;
    private AuthController authController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();
        authController = new AuthController(this);

        initViews();
        initEvents();
    }

    private void initViews() {
        btnRegister = findViewById(R.id.btn_register);
        btnGoogle = findViewById(R.id.btn_reg_google);
        btnFacebook = findViewById(R.id.btn_reg_facebook);
        tvLoginLink = findViewById(R.id.tv_link_login);
        tvTermsPolicy = findViewById(R.id.tv_terms_policy);
        edtFullname = findViewById(R.id.edt_fullname);
        // Inline hint TextView for fullname (inserted above the input)
        ViewGroup fullnameParent = (ViewGroup) edtFullname.getParent();
        TextView tvFullnameHint = new TextView(this);
        tvFullnameHint.setText("Chỉ chứa chữ cái và khoảng trắng; không có số hoặc ký tự đặc biệt; không có khoảng trắng thừa.");
        tvFullnameHint.setTextColor(Color.RED);
        tvFullnameHint.setTextSize(12);
        tvFullnameHint.setVisibility(View.GONE);
        int idx = fullnameParent.indexOfChild(edtFullname);
        fullnameParent.addView(tvFullnameHint, idx);
        edtEmail = findViewById(R.id.edt_register_email);
        edtPhone = findViewById(R.id.edt_phone);
        edtPassword = findViewById(R.id.edt_register_password);
        edtConfirmPassword = findViewById(R.id.edt_confirm_password);
        cbAgree = findViewById(R.id.cb_agree);

        // Email: max length 100, auto-append @gmail.com on focus lost if user didn't type domain
        edtEmail.setFilters(new InputFilter[]{new InputFilter.LengthFilter(100)});
        edtEmail.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String text = edtEmail.getText() != null ? edtEmail.getText().toString().trim() : "";
                if (!text.isEmpty() && !text.contains("@")) {
                    String domain = "@gmail.com";
                    int maxNameLen = Math.max(0, 100 - domain.length());
                    String namePart = text.length() > maxNameLen ? text.substring(0, maxNameLen) : text;
                    edtEmail.setText(namePart + domain);
                }
            }
        });

        // Phone: only digits, max length 10
        edtPhone.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
        edtPhone.setKeyListener(DigitsKeyListener.getInstance("0123456789"));
        // Ensure pasted content is digits-only
        edtPhone.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
            @Override public void afterTextChanged(Editable s) {
                String filtered = s.toString().replaceAll("[^0-9]", "");
                if (!filtered.equals(s.toString())) {
                    edtPhone.setText(filtered);
                    edtPhone.setSelection(filtered.length());
                }
            }
        });

        // Real-time basic validation feedback
        edtFullname.addTextChangedListener(new SimpleWatcher(() -> validateFullName(false)));
        edtEmail.addTextChangedListener(new SimpleWatcher(() -> validateEmail(false)));
        
        edtPhone.addTextChangedListener(new SimpleWatcher(() -> validatePhone(false)));
        edtPassword.addTextChangedListener(new SimpleWatcher(() -> validatePassword(false)));
        edtConfirmPassword.addTextChangedListener(new SimpleWatcher(() -> validateConfirmPassword(false)));

        // Show hint when fullname focused
        edtFullname.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                tvFullnameHint.setVisibility(View.VISIBLE);
            } else {
                tvFullnameHint.setVisibility(View.GONE);
            }
        });
    }

    private void initEvents() {
        // Quay lại màn hình đăng nhập
        tvLoginLink.setOnClickListener(v -> finish());

        setupTermsPolicyText();

        // Xử lý nút đăng ký
        btnRegister.setOnClickListener(v -> {
            String fullname = edtFullname.getText() != null ? edtFullname.getText().toString().trim() : "";
            String rawEmail = edtEmail.getText() != null ? edtEmail.getText().toString().trim() : "";
            String phone = edtPhone.getText() != null ? edtPhone.getText().toString().trim() : "";
            String password = edtPassword.getText() != null ? edtPassword.getText().toString().trim() : "";
            String confirmPassword = edtConfirmPassword.getText() != null ? edtConfirmPassword.getText().toString().trim() : "";

            // Normalize email: if user didn't include domain, append @gmail.com and respect 100-char limit
            String email;
            if (rawEmail.contains("@")) {
                email = rawEmail.length() > 100 ? rawEmail.substring(0, 100) : rawEmail;
            } else {
                String domain = "@gmail.com";
                int maxNameLen = Math.max(0, 100 - domain.length());
                String namePart = rawEmail.length() > maxNameLen ? rawEmail.substring(0, maxNameLen) : rawEmail;
                email = namePart + domain;
            }

            // Validate fields with detailed checks
            if (!validateFullName(true) | !validateEmail(true) | !validatePhone(true) | !validatePassword(true) | !validateConfirmPassword(true)) {
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Mật khẩu xác nhận không khớp.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(this, "Mật khẩu phải có ít nhất 6 ký tự.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (password.length() > 200) {
                Toast.makeText(this, "Mật khẩu không được vượt quá 200 ký tự.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!cbAgree.isChecked()) {
                cbAgree.requestFocus();
                Toast.makeText(this, "Vui lòng đồng ý điều khoản sử dụng.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check email uniqueness via Firebase
            auth.fetchSignInMethodsForEmail(email).addOnCompleteListener(fetchTask -> {
                if (!fetchTask.isSuccessful()) {
                    Toast.makeText(this, "Không thể kiểm tra email: " + (fetchTask.getException() != null ? fetchTask.getException().getMessage() : "Lỗi"), Toast.LENGTH_LONG).show();
                    return;
                }
                boolean exists = fetchTask.getResult() != null && !fetchTask.getResult().getSignInMethods().isEmpty();
                if (exists) {
                    edtEmail.setError("Email đã tồn tại");
                    edtEmail.requestFocus();
                    return;
                }

                auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, task -> {
                        if (!task.isSuccessful()) {
                            String errorMessage = task.getException() != null ? task.getException().getMessage() : "Đã xảy ra lỗi khi tạo tài khoản.";
                            Toast.makeText(this, "Đăng ký thất bại: " + errorMessage, Toast.LENGTH_LONG).show();
                            return;
                        }

                        FirebaseUser user = auth.getCurrentUser();
                        if (user == null) {
                            Toast.makeText(this, "Đăng ký thất bại: không thể lấy thông tin người dùng sau khi tạo tài khoản.", Toast.LENGTH_LONG).show();
                            return;
                        }

                        // Trả về Login ngay lập tức (prefill) — lưu profile Firestore chạy bất đồng b
                        Intent data = AuthResultAdapter.createResult(email, password, true);
                        setResult(RESULT_OK, data);
                        // Lưu profile nhưng không chặn UI
                        authController.getFirestore().collection("users").document(user.getUid())
                                .set(new java.util.HashMap<String, Object>() {{
                                    put("fullName", fullname);
                                    put("email", email);
                                    put("phone", phone);
                                    put("createdAt", com.google.firebase.firestore.FieldValue.serverTimestamp());
                                    put("lastLogin", com.google.firebase.firestore.FieldValue.serverTimestamp());
                                }})
                                .addOnFailureListener(e -> {
                                    // Không chặn trả về Login, chỉ log/toast nếu cần
                                });
                        finish();
                    });
            });
        });

        // Đăng ký qua MXH
        btnGoogle.setOnClickListener(v ->
                Toast.makeText(this, "Đăng ký bằng Google...", Toast.LENGTH_SHORT).show());

        btnFacebook.setOnClickListener(v ->
                Toast.makeText(this, "Đăng ký bằng Facebook...", Toast.LENGTH_SHORT).show());
    }

    private void setupTermsPolicyText() {
        String content = "Tôi đồng ý với ";
        String terms = "Điều khoản";
        String policy = "Chính sách";
        String fullText = content + terms + " và " + policy + ".";

        SpannableString spannable = new SpannableString(fullText);
        int termsStart = content.length();
        int termsEnd = termsStart + terms.length();
        int policyStart = termsEnd + 5; // " và " length
        int policyEnd = policyStart + policy.length();

        spannable.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.brand_primary)), termsStart, termsEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.brand_primary)), policyStart, policyEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        spannable.setSpan(new ClickableSpan() {
            @Override
            public void onClick(android.view.View widget) {
                openTermsPolicy("terms");
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(getResources().getColor(R.color.brand_primary));
                ds.setUnderlineText(false);
            }
        }, termsStart, termsEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        spannable.setSpan(new ClickableSpan() {
            @Override
            public void onClick(android.view.View widget) {
                openTermsPolicy("policy");
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(getResources().getColor(R.color.brand_primary));
                ds.setUnderlineText(false);
            }
        }, policyStart, policyEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        tvTermsPolicy.setText(spannable);
        tvTermsPolicy.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void openTermsPolicy(String type) {
        String title;
        String message;
        if ("policy".equals(type)) {
            title = "Chính sách bảo mật";
            message = "Chính sách bảo mật: Chúng tôi cam kết bảo vệ dữ liệu cá nhân của bạn. Thông tin thu thập bao gồm email, số điện thoại và dữ liệu đăng nhập để cung cấp dịch vụ sự kiện. Dữ liệu này chỉ được sử dụng cho việc xác thực, quản lý tài khoản và gửi thông báo liên quan đến sự kiện. Chúng tôi không chia sẻ thông tin cá nhân với bên thứ ba ngoài những đối tác cần thiết cho hoạt động ứng dụng, trừ khi có yêu cầu pháp lý.";
        } else {
            title = "Điều khoản sử dụng";
            message = "Điều khoản sử dụng: Khi sử dụng EventPass, bạn đồng ý cung cấp thông tin chính xác và tuân thủ các quy định hiện hành. Bạn chịu trách nhiệm bảo mật thông tin đăng nhập của mình. Mọi hành vi lạm dụng, giả mạo hoặc đăng tải nội dung sai sự thật sẽ bị xử lý theo quy định. EventPass có quyền cập nhật điều khoản tại bất kỳ thời điểm nào; thay đổi sẽ được thông báo trong ứng dụng.";
        }

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Quay lại", (dialog, which) -> dialog.dismiss())
                .show();
    }

    // Validation helpers
    private boolean validateFullName(boolean showError) {
        String raw = edtFullname.getText() != null ? edtFullname.getText().toString() : "";
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            if (showError) edtFullname.setError("Họ tên không được để trống");
            return false;
        }
        if (trimmed.length() < 2 || trimmed.length() > 50) {
            if (showError) edtFullname.setError("Họ tên phải từ 2 đến 50 ký tự");
            return false;
        }
        if (!trimmed.equals(raw)) {
            if (showError) edtFullname.setError("Không có khoảng trắng thừa ở đầu/cuối");
            return false;
        }
        // Only letters (unicode) and single spaces between words
        if (!Pattern.matches("^[\\p{L}]+( [\\p{L}]+)*$", trimmed)) {
            if (showError) edtFullname.setError("Họ tên chỉ chứa chữ cái và khoảng trắng");
            return false;
        }
        if (showError) edtFullname.setError(null);
        return true;
    }

    private boolean validateEmail(boolean showError) {
        String raw = edtEmail.getText() != null ? edtEmail.getText().toString().trim() : "";
        if (raw.isEmpty()) {
            if (showError) edtEmail.setError("Email không được để trống");
            return false;
        }
        // Use Android's Patterns for basic format
        if (!Patterns.EMAIL_ADDRESS.matcher(raw).matches()) {
            if (showError) edtEmail.setError("Email không hợp lệ");
            return false;
        }
        if (raw.length() > 100) {
            if (showError) edtEmail.setError("Email quá dài (tối đa 100 ký tự)");
            return false;
        }
        if (showError) edtEmail.setError(null);
        return true;
    }

    private boolean validatePhone(boolean showError) {
        String phone = edtPhone.getText() != null ? edtPhone.getText().toString().trim() : "";
        if (phone.isEmpty()) {
            if (showError) edtPhone.setError("Số điện thoại không được để trống");
            return false;
        }
        // VN prefixes: 03,05,07,08,09 and 10 digits total
        if (!Pattern.matches("^0(3|5|7|8|9)[0-9]{8}$", phone)) {
            if (showError) edtPhone.setError("Số điện thoại không hợp lệ (10 chữ số, bắt đầu 03/05/07/08/09)");
            return false;
        }
        if (showError) edtPhone.setError(null);
        return true;
    }

    private boolean validatePassword(boolean showError) {
        String pwd = edtPassword.getText() != null ? edtPassword.getText().toString() : "";
        if (pwd.isEmpty()) {
            if (showError) edtPassword.setError("Mật khẩu không được để trống");
            return false;
        }
        if (pwd.length() < 6) {
            if (showError) edtPassword.setError("Mật khẩu phải ít nhất 6 ký tự");
            return false;
        }
        if (pwd.length() > 200) {
            if (showError) edtPassword.setError("Mật khẩu không quá 200 ký tự");
            return false;
        }
        // Enforce complexity: upper, lower, digit, special
        Pattern complexity = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z0-9]).+$");
        if (!complexity.matcher(pwd).matches()) {
            if (showError) edtPassword.setError("Mật khẩu cần chữ hoa, chữ thường, chữ số và ký tự đặc biệt");
            return false;
        }
        if (showError) edtPassword.setError(null);
        return true;
    }

    private boolean validateConfirmPassword(boolean showError) {
        String pwd = edtPassword.getText() != null ? edtPassword.getText().toString() : "";
        String conf = edtConfirmPassword.getText() != null ? edtConfirmPassword.getText().toString() : "";
        if (conf.isEmpty()) {
            if (showError) edtConfirmPassword.setError("Xác nhận mật khẩu không được để trống");
            return false;
        }
        if (!pwd.equals(conf)) {
            if (showError) edtConfirmPassword.setError("Mật khẩu xác nhận không khớp");
            return false;
        }
        if (showError) edtConfirmPassword.setError(null);
        return true;
    }

    // Simple TextWatcher wrapper for lambda
    private static class SimpleWatcher implements TextWatcher {
        private final Runnable r;
        SimpleWatcher(Runnable r) { this.r = r; }
        @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
        @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
        @Override public void afterTextChanged(Editable s) { r.run(); }
    }
}
