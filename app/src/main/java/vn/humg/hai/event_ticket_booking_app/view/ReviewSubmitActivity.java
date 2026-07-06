package vn.humg.hai.event_ticket_booking_app.view;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import android.net.Uri;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import android.view.View;
import android.widget.ImageButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import android.app.ProgressDialog;
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
    private MaterialButton btnSubmit, btnAddImage;
    private ImageButton btnRemoveImage;
    private ImageView ivUploadPreview;
    private MaterialCardView cardReviewImage;
    
    private Uri selectedImageUri = null;
    private ActivityResultLauncher<String> imagePickerLauncher;

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
        findViewById(R.id.btn_back_custom).setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        ivEventImage = findViewById(R.id.iv_review_event_image);
        tvEventTitle = findViewById(R.id.tv_review_event_title);
        rbRating = findViewById(R.id.rb_submit_rating);
        edtComment = findViewById(R.id.edt_review_comment);
        btnSubmit = findViewById(R.id.btn_submit_review);
        btnAddImage = findViewById(R.id.btn_add_image);
        btnRemoveImage = findViewById(R.id.btn_remove_image);
        ivUploadPreview = findViewById(R.id.iv_review_upload_preview);
        cardReviewImage = findViewById(R.id.card_review_image);

        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                selectedImageUri = uri;
                cardReviewImage.setVisibility(View.VISIBLE);
                btnRemoveImage.setVisibility(View.VISIBLE);
                ivUploadPreview.setImageURI(uri);
            }
        });
    }

    private void initEvents() {
        btnSubmit.setOnClickListener(v -> submitReview());
        btnAddImage.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        btnRemoveImage.setOnClickListener(v -> {
            selectedImageUri = null;
            cardReviewImage.setVisibility(View.GONE);
            btnRemoveImage.setVisibility(View.GONE);
        });
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

        if (selectedImageUri != null) {
            uploadImageAndSaveReview(userId, rating, comment);
        } else {
            finalizeSaveReview(userId, rating, comment, null);
        }
    }

    private void uploadImageAndSaveReview(String userId, float rating, String comment) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang tải ảnh lên...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        StorageReference storageRef = FirebaseStorage.getInstance().getReference("review_images/" + UUID.randomUUID().toString());
        storageRef.putFile(selectedImageUri).addOnSuccessListener(taskSnapshot -> {
            storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                progressDialog.dismiss();
                finalizeSaveReview(userId, rating, comment, uri.toString());
            }).addOnFailureListener(e -> {
                progressDialog.dismiss();
                resetSubmitButton();
                Toast.makeText(this, "Lỗi lấy link ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            resetSubmitButton();
            Toast.makeText(this, "Lỗi upload ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void finalizeSaveReview(String userId, float rating, String comment, String imageUrl) {
        userController.getUserById(userId, user -> {
            Review review = new Review();
            review.setReviewId(UUID.randomUUID().toString());
            review.setEventId(eventId);
            review.setUserId(userId);
            review.setUserName(user != null ? user.getFullName() : "Người dùng");
            review.setRating(rating);
            review.setComment(comment);
            review.setImageUrl(imageUrl);
            review.setCreatedAt(Timestamp.now());

            reviewController.saveReview(review, () -> {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Cảm ơn bạn đã đánh giá!", Toast.LENGTH_LONG).show();
                    finish();
                });
            }, error -> {
                runOnUiThread(() -> {
                    resetSubmitButton();
                    Toast.makeText(this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                });
            });
        }, error -> {
            runOnUiThread(this::resetSubmitButton);
        });
    }

    private void resetSubmitButton() {
        btnSubmit.setEnabled(true);
        btnSubmit.setText("Gửi đánh giá");
    }
}
