package vn.humg.hai.event_ticket_booking_app.view;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import vn.humg.hai.event_ticket_booking_app.R;
import vn.humg.hai.event_ticket_booking_app.controller.UserController;
import vn.humg.hai.event_ticket_booking_app.model.User;

public class MembershipDetailsActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView tvCurrentTierName, tvCurrentExpRatio, tvUpgradeTargetMessage;
    private ProgressBar pbUpgradeProgress;

    private MaterialCardView cardRegular, cardBronze, cardSilver, cardGold, cardVip;
    private final UserController userController = new UserController();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_membership_details);

        initViews();
        setupEvents();
        loadUserData();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back_membership);
        tvCurrentTierName = findViewById(R.id.tv_current_tier_name);
        tvCurrentExpRatio = findViewById(R.id.tv_current_exp_ratio);
        tvUpgradeTargetMessage = findViewById(R.id.tv_upgrade_target_message);
        pbUpgradeProgress = findViewById(R.id.pb_upgrade_progress);

        cardRegular = findViewById(R.id.card_tier_regular);
        cardBronze = findViewById(R.id.card_tier_bronze);
        cardSilver = findViewById(R.id.card_tier_silver);
        cardGold = findViewById(R.id.card_tier_gold);
        cardVip = findViewById(R.id.card_tier_vip);
    }

    private void setupEvents() {
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadUserData() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để xem thông tin", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        userController.getUserById(uid, user -> {
            if (user != null) {
                runOnUiThread(() -> bindUserTierInfo(user));
            }
        }, error -> runOnUiThread(() -> {
            Toast.makeText(this, "Lỗi nạp thông tin thành viên: " + error, Toast.LENGTH_SHORT).show();
        }));
    }

    private void bindUserTierInfo(User user) {
        long exp = user.getExp();
        String tier = User.computeTier(exp);

        tvCurrentTierName.setText("Hạng " + tier);
        
        long progress = 0;
        String targetMsg = "";
        
        if (exp < 500) {
            progress = (exp * 100) / 500;
            targetMsg = "Cần thêm " + (500 - exp) + " EXP nữa để thăng hạng Đồng";
            tvCurrentExpRatio.setText(exp + " / 500 EXP");
            highlightCurrentCard(cardRegular);
        } else if (exp < 1500) {
            progress = ((exp - 500) * 100) / 1000;
            targetMsg = "Cần thêm " + (1500 - exp) + " EXP nữa để thăng hạng Bạc";
            tvCurrentExpRatio.setText(exp + " / 1500 EXP");
            highlightCurrentCard(cardBronze);
        } else if (exp < 3500) {
            progress = ((exp - 1500) * 100) / 2000;
            targetMsg = "Cần thêm " + (3500 - exp) + " EXP nữa để thăng hạng Vàng";
            tvCurrentExpRatio.setText(exp + " / 3500 EXP");
            highlightCurrentCard(cardSilver);
        } else if (exp < 7500) {
            progress = ((exp - 3500) * 100) / 4000;
            targetMsg = "Cần thêm " + (7500 - exp) + " EXP nữa để thăng hạng Thân thiết số một";
            tvCurrentExpRatio.setText(exp + " / 7500 EXP");
            highlightCurrentCard(cardGold);
        } else {
            progress = 100;
            targetMsg = "Chúc mừng! Bạn đã đạt thứ hạng cao nhất 🎉";
            tvCurrentExpRatio.setText(exp + " EXP");
            highlightCurrentCard(cardVip);
        }

        pbUpgradeProgress.setProgress((int) progress);
        tvUpgradeTargetMessage.setText(targetMsg);
    }

    private void highlightCurrentCard(MaterialCardView activeCard) {
        // Clear strokes of all cards
        int defaultStroke = (int) (1 * getResources().getDisplayMetrics().density);
        int activeStroke = (int) (3 * getResources().getDisplayMetrics().density);
        int goldColor = getResources().getColor(R.color.star_gold);
        int defaultBorderColor = getResources().getColor(R.color.rule_border);

        cardRegular.setStrokeWidth(defaultStroke);
        cardRegular.setStrokeColor(defaultBorderColor);
        cardBronze.setStrokeWidth(defaultStroke);
        cardBronze.setStrokeColor(getResources().getColor(R.color.text_muted));
        cardSilver.setStrokeWidth(defaultStroke);
        cardSilver.setStrokeColor(getResources().getColor(R.color.text_muted));
        cardGold.setStrokeWidth(defaultStroke);
        cardGold.setStrokeColor(getResources().getColor(R.color.brand_primary));
        cardVip.setStrokeWidth(defaultStroke);
        cardVip.setStrokeColor(goldColor);

        // Highlight current card
        if (activeCard != null) {
            activeCard.setStrokeWidth(activeStroke);
            activeCard.setStrokeColor(goldColor);
        }
    }
}
