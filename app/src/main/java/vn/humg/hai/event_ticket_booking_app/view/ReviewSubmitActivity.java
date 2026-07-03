package vn.humg.hai.event_ticket_booking_app.view;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import java.util.UUID;
import vn.humg.hai.event_ticket_booking_app.R;
import vn.humg.hai.event_ticket_booking_app.controller.EventController;
import vn.humg.hai.event_ticket_booking_app.controller.ReviewController;
import vn.humg.hai.event_ticket_booking_app.controller.UserController;
import vn.humg.hai.event_ticket_booking_app.model.Review;

public class ReviewSubmitActivity extends AppCompatActivity {

    private String eventId;
    private EventController eventController;
    private ReviewController reviewController;
    private UserController userController;

    private ImageView ivEventImage;
    private TextView tvEventTitle;
    private RatingBar rbRating;
    private TextInputEditText edtComment;
    private MaterialButton btnSubmit;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_submit);

        eventId = getIntent().getStringExtra("EXTRA_EVENT_ID");
        if (eventId == null) {
            finish();
            return;
        }

        eventController = new EventController();
        reviewController = new ReviewController();
        userController = new UserController();

        initViews();
        initEvents();
        loadEventInfo();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar_review);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        ivEventImage = findViewById(R.id.iv_review_event_image);
        tvEventTitle = findViewById(R.id.tv_review_event_title);
        rbRating = findViewById(R.id.rb_submit_rating);
        edtComment = findViewById(R.id.edt_review_comment);
        btnSubmit = findViewById(R.id.btn_submit_review);
    }

    private void initEvents() {
        toolbar.setNavigationOnClickListener(v -> finish());

        btnSubmit.setOnClickListener(v -> submitReview());
    }

    private void loadEventInfo() {
        eventController.getEventById(eventId, event -> {
            if (event != null) {
                runOnUiThread(() -> {
                    tvEventTitle.setText(event.getTitle());
                    Glide.with(this)
                            .load(event.getImage())
                            .placeholder(R.drawable.img_logo_event_ticket_booking)
                            .into(ivEventImage);
                });
            }
        }, error -> {});
    }

    private void submitReview() {
        float rating = rbRating.getRating();
        String comment = edtComment.getText().toString().trim();
        String userId = FirebaseAuth.getInstance().getUid();

        if (rating == 0) {
            Toast.makeText(this, "Vui lòng chọn số sao đánh giá", Toast.LENGTH_SHORT).show();
            return;
        }

        if (userId == null) return;

        btnSubmit.setEnabled(false);
        btnSubmit.setText("Đang gửi...");

        userController.getUserById(userId, user -> {
            Review review = new Review();
            review.setReviewId(UUID.randomUUID().toString());
            review.setEventId(eventId);
            review.setUserId(userId);
            review.setUserName(user != null ? user.getFullName() : "Người dùng");
            review.setRating(rating);
            review.setComment(comment);
            review.setCreatedAt(Timestamp.now());

            reviewController.saveReview(review, () -> {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Cảm ơn bạn đã đánh giá!", Toast.LENGTH_LONG).show();
                    finish();
                });
            }, error -> {
                runOnUiThread(() -> {
                    btnSubmit.setEnabled(true);
                    btnSubmit.setText("Gửi đánh giá");
                    Toast.makeText(this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                });
            });
        }, error -> {
            runOnUiThread(() -> {
                btnSubmit.setEnabled(true);
                btnSubmit.setText("Gửi đánh giá");
            });
        });
    }
}
