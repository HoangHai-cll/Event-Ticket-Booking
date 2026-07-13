package vn.humg.hai.event_ticket_booking_app.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import vn.humg.hai.event_ticket_booking_app.R;

public class SplashActivity extends AppCompatActivity {

    // Animation constants
    private static final int DELAY_NAVIGATE_MS = 2400;
    private static final int DURATION_LOGO_MS = 1000;
    private static final int DURATION_TEXT_MS = 800;
    private static final int DURATION_LOADING_MS = 600;

    private static final int DELAY_TITLE_MS = 200;
    private static final int DELAY_SUBTITLE_MS = 400;
    private static final int DELAY_LOADING_INDICATOR_MS = 750;
    private static final int DELAY_FOOTER_MS = 600;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Initialize Firestore offline database settings
        initFirestoreSettings();

        // Perform coordinated layout animations
        startSplashAnimations();

        // Navigate forward after animations complete
        navigateToNextScreenDelayed();
    }

    /**
     * Configures Firebase Firestore settings to enable offline persistence.
     */
    private void initFirestoreSettings() {
        try {
            com.google.firebase.firestore.FirebaseFirestoreSettings settings = 
                new com.google.firebase.firestore.FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(true)
                    .build();
            com.google.firebase.firestore.FirebaseFirestore.getInstance().setFirestoreSettings(settings);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Prepares and animates all view components on the Splash screen.
     */
    private void startSplashAnimations() {
        MaterialCardView cardLogo = findViewById(R.id.card_logo);
        TextView tvTitle = findViewById(R.id.tv_splash_title);
        TextView tvSubtitle = findViewById(R.id.tv_splash_subtitle);
        ProgressBar pbLoading = findViewById(R.id.pb_splash_loading);
        TextView tvFooter = findViewById(R.id.tv_footer);

        // 1. Prepare initial states (alpha, scale, translations)
        if (cardLogo != null) {
            cardLogo.setAlpha(0f);
            cardLogo.setScaleX(0.5f);
            cardLogo.setScaleY(0.5f);
        }
        if (tvTitle != null) {
            tvTitle.setAlpha(0f);
            tvTitle.setTranslationY(60f);
        }
        if (tvSubtitle != null) {
            tvSubtitle.setAlpha(0f);
            tvSubtitle.setTranslationY(40f);
        }
        if (pbLoading != null) {
            pbLoading.setAlpha(0f);
        }
        if (tvFooter != null) {
            tvFooter.setAlpha(0f);
            tvFooter.setTranslationY(30f);
        }

        // 2. Play animations in an offset cascade
        if (cardLogo != null) {
            cardLogo.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(DURATION_LOGO_MS)
                .setInterpolator(new OvershootInterpolator(1.3f))
                .start();
        }

        if (tvTitle != null) {
            tvTitle.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(DELAY_TITLE_MS)
                .setDuration(DURATION_TEXT_MS)
                .setInterpolator(new DecelerateInterpolator())
                .start();
        }

        if (tvSubtitle != null) {
            tvSubtitle.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(DELAY_SUBTITLE_MS)
                .setDuration(DURATION_TEXT_MS)
                .setInterpolator(new DecelerateInterpolator())
                .start();
        }

        if (pbLoading != null) {
            pbLoading.animate()
                .alpha(1f)
                .setStartDelay(DELAY_LOADING_INDICATOR_MS)
                .setDuration(DURATION_LOADING_MS)
                .start();
        }

        if (tvFooter != null) {
            tvFooter.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(DELAY_FOOTER_MS)
                .setDuration(DURATION_TEXT_MS)
                .setInterpolator(new DecelerateInterpolator())
                .start();
        }
    }

    /**
     * Triggers screen transition after animation delay.
     */
    private void navigateToNextScreenDelayed() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }, DELAY_NAVIGATE_MS);
    }
}
