package vn.humg.hai.event_ticket_booking_app.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import vn.humg.hai.event_ticket_booking_app.R;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Kích hoạt tường minh tính năng lưu ngoại tuyến (Offline Persistence) cho Firestore
        try {
            com.google.firebase.firestore.FirebaseFirestoreSettings settings = 
                new com.google.firebase.firestore.FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(true)
                    .build();
            com.google.firebase.firestore.FirebaseFirestore.getInstance().setFirestoreSettings(settings);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Chờ 2 giây rồi kiểm tra đăng nhập
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                // Đã đăng nhập -> Vào thẳng màn hình chính
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
            } else {
                // Chưa đăng nhập -> Vào màn hình Login
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            }
            finish();
        }, 2000);
    }
}
