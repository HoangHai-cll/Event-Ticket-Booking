package vn.humg.hai.event_ticket_booking_app.view;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import vn.humg.hai.event_ticket_booking_app.R;

public class SettingsActivity extends AppCompatActivity {

    private SwitchMaterial switchAppNotification, switchPromoEmail, switchDarkMode, switch2fa;
    private MaterialButton btnLogout;
    private SharedPreferences sharedPreferences;

    private static final String PREFS_NAME = "app_settings";
    private static final String KEY_APP_NOTIF = "app_notif";
    private static final String KEY_PROMO_EMAIL = "promo_email";
    private static final String KEY_DARK_MODE = "dark_mode";
    private static final String KEY_2FA = "two_factor";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        initViews();
        loadSettings();
        setupEvents();
    }

    private void initViews() {
        findViewById(R.id.btn_back_custom).setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        switchAppNotification = findViewById(R.id.switch_app_notification);
        switchPromoEmail = findViewById(R.id.switch_promo_email);
        switchDarkMode = findViewById(R.id.switch_dark_mode);
        switch2fa = findViewById(R.id.switch_2fa);
        btnLogout = findViewById(R.id.btn_logout);
    }

    private void loadSettings() {
        switchAppNotification.setChecked(sharedPreferences.getBoolean(KEY_APP_NOTIF, true));
        switchPromoEmail.setChecked(sharedPreferences.getBoolean(KEY_PROMO_EMAIL, false));
        switchDarkMode.setChecked(sharedPreferences.getBoolean(KEY_DARK_MODE, false));
        switch2fa.setChecked(sharedPreferences.getBoolean(KEY_2FA, true));
    }

    private void setupEvents() {
        switchAppNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean(KEY_APP_NOTIF, isChecked).apply();
            Toast.makeText(this, isChecked ? "Đã bật thông báo ứng dụng" : "Đã tắt thông báo ứng dụng", Toast.LENGTH_SHORT).show();
        });

        switchPromoEmail.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean(KEY_PROMO_EMAIL, isChecked).apply();
        });

        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean(KEY_DARK_MODE, isChecked).apply();
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        switch2fa.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean(KEY_2FA, isChecked).apply();
            Toast.makeText(this, isChecked ? "Đã kích hoạt 2FA" : "Đã hủy 2FA", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btn_settings_change_password).setOnClickListener(v -> {
            startActivity(new Intent(this, ChangePasswordActivity.class));
        });

        findViewById(R.id.btn_settings_legal).setOnClickListener(v -> {
            Intent intent = new Intent(this, TermsPolicyActivity.class);
            intent.putExtra("type", "policy");
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(this, "Đăng xuất thành công", Toast.LENGTH_SHORT).show();
            
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
