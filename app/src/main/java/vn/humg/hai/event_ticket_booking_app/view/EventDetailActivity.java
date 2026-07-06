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
import androidx.lifecycle.ViewModelProvider;
import vn.humg.hai.event_ticket_booking_app.viewmodel.AuthViewModel;
import vn.humg.hai.event_ticket_booking_app.viewmodel.EventViewModel;
import vn.humg.hai.event_ticket_booking_app.model.Event;
import vn.humg.hai.event_ticket_booking_app.model.Review;
import android.widget.EditText;
import android.widget.RatingBar;
import androidx.appcompat.app.AlertDialog;
import com.google.firebase.Timestamp;
import java.util.UUID;
import android.view.LayoutInflater;

public class EventDetailActivity extends AppCompatActivity {

    private EventViewModel eventViewModel;
    private AuthViewModel authViewModel;
    
    private String eventId;
    private Event currentEvent;

    private ImageView ivDetailImage, ivOrganizer, ivArtist;
    private TextView tvTitle, tvDate, tvTime, tvLocation, tvDescription, tvPrice, tvOrganizerName, tvArtistName;
    private View layoutOrganizer, layoutArtist;
    private android.widget.ImageButton btnOpenMap;
    private MaterialButton btnBookNow, btnWriteReview;
    
    private RecyclerView rvReviews;
    private ReviewAdapter reviewAdapter;
    private final List<Review> reviewList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        eventId = getIntent().getStringExtra("EXTRA_EVENT_ID");
        eventViewModel = new ViewModelProvider(this).get(EventViewModel.class);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        setupObservers();

        initViews();
        setupRecyclerView();
        initEvents();
        checkUserRoleAndAdjustUI();
    }

    private void setupObservers() {
        eventViewModel.getEventDetailState().observe(this, event -> {
            if (event != null) {
                currentEvent = event;
                displayEvent(event);
            }
        });

        eventViewModel.getReviewsState().observe(this, reviews -> {
            if (reviews != null) {
                reviewList.clear();
                reviewList.addAll(reviews);
                reviewAdapter.notifyDataSetChanged();
            }
        });

        authViewModel.getUserProfileState().observe(this, user -> {
            if (user != null && "admin".equalsIgnoreCase(user.getRole())) {
                btnBookNow.setVisibility(View.GONE);
                btnWriteReview.setVisibility(View.GONE);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadEventDetails();
        loadReviews();
    }

    private void checkUserRoleAndAdjustUI() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            authViewModel.getUserProfile(uid);
        }
    }

    private void initViews() {
        findViewById(R.id.btn_back_custom).setOnClickListener(v -> finish());

        ivDetailImage = findViewById(R.id.iv_detail_image);
        tvTitle = findViewById(R.id.tv_detail_title);
        tvDate = findViewById(R.id.tv_detail_date);
        tvTime = findViewById(R.id.tv_detail_time);
        tvLocation = findViewById(R.id.tv_detail_location);
        tvDescription = findViewById(R.id.tv_detail_description);
        tvPrice = findViewById(R.id.tv_detail_price);
        btnBookNow = findViewById(R.id.btn_book_now);
        btnWriteReview = findViewById(R.id.btn_write_review);
        rvReviews = findViewById(R.id.recycler_reviews);

        btnOpenMap = findViewById(R.id.btn_open_map);
        layoutOrganizer = findViewById(R.id.layout_organizer);
        layoutArtist = findViewById(R.id.layout_artist);
        ivOrganizer = findViewById(R.id.iv_organizer);
        ivArtist = findViewById(R.id.iv_artist);
        tvOrganizerName = findViewById(R.id.tv_organizer_name);
        tvArtistName = findViewById(R.id.tv_artist_name);
    }

    private void setupRecyclerView() {
        rvReviews.setLayoutManager(new LinearLayoutManager(this));
        reviewAdapter = new ReviewAdapter(reviewList);
        rvReviews.setAdapter(reviewAdapter);
    }

    private void initEvents() {
        btnBookNow.setOnClickListener(v -> startBookingFlow());
        
        btnWriteReview.setOnClickListener(v -> {
            String uid = FirebaseAuth.getInstance().getUid();
            if (uid == null) {
                Toast.makeText(this, getString(R.string.msg_login_required), Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, LoginActivity.class));
                return;
            }
            Intent intent = new Intent(this, ReviewSubmitActivity.class);
            intent.putExtra("EXTRA_EVENT_ID", eventId);
            startActivity(intent);
        });
    }


    private void startBookingFlow() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            Toast.makeText(this, getString(R.string.msg_login_required), Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            return;
        }

        if (currentEvent == null) return;
        
        // Kiểm tra sự kiện đã qua chưa
        if (currentEvent.getDate() != null) {
            boolean isExpired = currentEvent.getDate().getSeconds() < (System.currentTimeMillis() / 1000);
            if (isExpired) {
                Toast.makeText(this, "Sự kiện này đã kết thúc, không thể đặt vé được!", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        Intent intent = new Intent(this, SelectTicketActivity.class);
        intent.putExtra("EXTRA_EVENT_ID", eventId);
        startActivity(intent);
    }

    private void loadEventDetails() {
        if (eventId == null) { finish(); return; }
        eventViewModel.loadEventById(eventId);
    }

    private void loadReviews() {
        eventViewModel.loadReviewsForEvent(eventId);
    }

    private void displayEvent(Event event) {
        String title = event.getTitle();
        if (event.getRequiredTier() != null && !event.getRequiredTier().isEmpty() && !event.getRequiredTier().equalsIgnoreCase("Thường")) {
            title = "🔒 [Ưu tiên " + event.getRequiredTier() + "] " + title;
        }
        tvTitle.setText(title);
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

        btnOpenMap.setVisibility(View.VISIBLE);
        btnOpenMap.setOnClickListener(v -> {
            String mapUri;
            if (event.getGoogleMapsUrl() != null && !event.getGoogleMapsUrl().isEmpty()) {
                mapUri = event.getGoogleMapsUrl();
            } else {
                mapUri = "geo:0,0?q=" + android.net.Uri.encode(event.getLocation());
            }
            Intent intent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(mapUri));
            intent.setPackage("com.google.android.apps.maps");
            if (intent.resolveActivity(getPackageManager()) == null) {
                intent.setPackage(null);
            }
            try {
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Không thể mở ứng dụng bản đồ", Toast.LENGTH_SHORT).show();
            }
        });

        if (event.getOrganizerName() != null && !event.getOrganizerName().isEmpty()) {
            layoutOrganizer.setVisibility(View.VISIBLE);
            tvOrganizerName.setText(event.getOrganizerName());
            if (event.getOrganizerImage() != null && !event.getOrganizerImage().isEmpty()) {
                Glide.with(this)
                        .load(event.getOrganizerImage())
                        .placeholder(R.drawable.img_logo_event_ticket_booking)
                        .into(ivOrganizer);
            }
        } else {
            layoutOrganizer.setVisibility(View.GONE);
        }

        if (event.getArtistName() != null && !event.getArtistName().isEmpty()) {
            layoutArtist.setVisibility(View.VISIBLE);
            tvArtistName.setText(event.getArtistName());
            if (event.getArtistImage() != null && !event.getArtistImage().isEmpty()) {
                Glide.with(this)
                        .load(event.getArtistImage())
                        .placeholder(R.drawable.img_logo_event_ticket_booking)
                        .into(ivArtist);
            }
        } else {
            layoutArtist.setVisibility(View.GONE);
        }

        // Vô hiệu hóa nút đặt vé nếu sự kiện đã kết thúc
        if (event.getDate() != null) {
            boolean isExpired = event.getDate().getSeconds() < (System.currentTimeMillis() / 1000);
            if (isExpired) {
                btnBookNow.setText("Đã kết thúc");
                btnBookNow.setEnabled(false);
                btnBookNow.setAlpha(0.5f);
            } else {
                btnBookNow.setText("Đặt vé ngay");
                btnBookNow.setEnabled(true);
                btnBookNow.setAlpha(1f);
            }
        }
    }
}
