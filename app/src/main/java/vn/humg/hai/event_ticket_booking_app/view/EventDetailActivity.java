package vn.humg.hai.event_ticket_booking_app.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import vn.humg.hai.event_ticket_booking_app.R;
import vn.humg.hai.event_ticket_booking_app.adapter.ReviewAdapter;
import vn.humg.hai.event_ticket_booking_app.controller.EventController;
import vn.humg.hai.event_ticket_booking_app.controller.ReviewController;
import vn.humg.hai.event_ticket_booking_app.controller.UserController;
import vn.humg.hai.event_ticket_booking_app.model.Event;
import vn.humg.hai.event_ticket_booking_app.model.Review;

public class EventDetailActivity extends AppCompatActivity {

    private EventController eventController;
    private ReviewController reviewController;
    
    private String eventId;
    private Event currentEvent;
    private final UserController userController = new UserController();

    private ImageView ivDetailImage;
    private TextView tvTitle, tvDate, tvTime, tvLocation, tvDescription, tvPrice;
    private MaterialButton btnBookNow;
    
    private RecyclerView rvReviews;
    private ReviewAdapter reviewAdapter;
    private final List<Review> reviewList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        eventId = getIntent().getStringExtra("EXTRA_EVENT_ID");
        eventController = new EventController();
        reviewController = new ReviewController();

        initViews();
        setupRecyclerView();
        initEvents();
        checkUserRoleAndAdjustUI();
        loadEventDetails();
        loadReviews();
    }

    private void checkUserRoleAndAdjustUI() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            userController.getUserById(uid, user -> {
                if (user != null && "admin".equalsIgnoreCase(user.getRole())) {
                    runOnUiThread(() -> btnBookNow.setVisibility(View.GONE));
                }
            }, e -> {});
        }
    }

    private void initViews() {
        findViewById(R.id.btn_back_custom).setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        ivDetailImage = findViewById(R.id.iv_detail_image);
        tvTitle = findViewById(R.id.tv_detail_title);
        tvDate = findViewById(R.id.tv_detail_date);
        tvTime = findViewById(R.id.tv_detail_time);
        tvLocation = findViewById(R.id.tv_detail_location);
        tvDescription = findViewById(R.id.tv_detail_description);
        tvPrice = findViewById(R.id.tv_detail_price);
        btnBookNow = findViewById(R.id.btn_book_now);
        rvReviews = findViewById(R.id.recycler_reviews);
    }

    private void setupRecyclerView() {
        rvReviews.setLayoutManager(new LinearLayoutManager(this));
        reviewAdapter = new ReviewAdapter(reviewList);
        rvReviews.setAdapter(reviewAdapter);
    }

    private void initEvents() {
        btnBookNow.setOnClickListener(v -> startBookingFlow());
    }

    private void startBookingFlow() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            Toast.makeText(this, getString(R.string.msg_login_required), Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            return;
        }

        if (currentEvent != null) {
            Intent intent = new Intent(this, SelectTicketActivity.class);
            intent.putExtra("EXTRA_EVENT_ID", eventId);
            startActivity(intent);
        }
    }

    private void loadEventDetails() {
        if (eventId == null) { finish(); return; }
        eventController.getEventById(eventId, event -> runOnUiThread(() -> {
            if (event != null) {
                currentEvent = event;
                displayEvent(event);
            }
        }), error -> runOnUiThread(() -> Toast.makeText(this, getString(R.string.msg_load_error), Toast.LENGTH_SHORT).show()));
    }

    private void loadReviews() {
        reviewController.getReviewsByEvent(eventId, reviews -> runOnUiThread(() -> {
            reviewList.clear();
            reviewList.addAll(reviews);
            reviewAdapter.notifyDataSetChanged();
        }), error -> {});
    }

    private void displayEvent(Event event) {
        tvTitle.setText(event.getTitle());
        tvDescription.setText(event.getDescription());
        tvLocation.setText(event.getLocation());
        tvPrice.setText(String.format(Locale.getDefault(), getString(R.string.price_format), (long) event.getPrice()));

        if (event.getDate() != null) {
            SimpleDateFormat dateHeader = new SimpleDateFormat("dd 'Tháng' MM, yyyy", Locale.getDefault());
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            tvDate.setText(dateHeader.format(event.getDate().toDate()));
            tvTime.setText(timeFormat.format(event.getDate().toDate()));
        }

        Glide.with(this)
                .load(event.getImage())
                .placeholder(R.drawable.img_logo_event_ticket_booking)
                .into(ivDetailImage);
    }
}
