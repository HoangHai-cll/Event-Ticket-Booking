package vn.humg.hai.event_ticket_booking_app.view;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import vn.humg.hai.event_ticket_booking_app.R;
import vn.humg.hai.event_ticket_booking_app.adapter.AdminReviewAdapter;
import vn.humg.hai.event_ticket_booking_app.controller.EventController;
import vn.humg.hai.event_ticket_booking_app.controller.ReviewController;
import vn.humg.hai.event_ticket_booking_app.model.Review;

public class AdminManageReviewsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AdminReviewAdapter adapter;
    private final List<Review> reviewList = new ArrayList<>();
    private final ReviewController reviewController = new ReviewController();
    private final EventController eventController = new EventController();
    private Toolbar toolbar;
    private String currentAdminId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage_reviews);

        currentAdminId = FirebaseAuth.getInstance().getUid();

        initViews();
        setupRecyclerView();
        loadFilteredReviews();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar_admin_reviews);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recycler_admin_all_reviews);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminReviewAdapter(reviewList, new AdminReviewAdapter.OnReviewActionListener() {
            @Override
            public void onDelete(Review review) {
                showDeleteConfirmDialog(review);
            }
        });
        recyclerView.setAdapter(adapter);
    }

    private void loadFilteredReviews() {
        // 1. Lấy danh sách sự kiện của Admin này trước
        eventController.getAllEvents(events -> {
            Set<String> myEventIds = new HashSet<>();
            for (vn.humg.hai.event_ticket_booking_app.model.Event e : events) {
                if (currentAdminId != null && currentAdminId.equals(e.getCreatedByAdminId())) {
                    myEventIds.add(e.getEventId());
                }
            }

            // 2. Tải tất cả đánh giá và lọc theo sự kiện của mình
            reviewController.getAllReviews(reviews -> {
                runOnUiThread(() -> {
                    reviewList.clear();
                    for (Review r : reviews) {
                        if (myEventIds.contains(r.getEventId())) {
                            reviewList.add(r);
                        }
                    }
                    adapter.notifyDataSetChanged();
                });
            }, error -> runOnUiThread(() -> Toast.makeText(this, "Lỗi tải đánh giá", Toast.LENGTH_SHORT).show()));

        }, error -> runOnUiThread(() -> Toast.makeText(this, "Lỗi tải dữ liệu sự kiện", Toast.LENGTH_SHORT).show()));
    }

    private void showDeleteConfirmDialog(Review review) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa đánh giá")
                .setMessage("Bạn có chắc chắn muốn xóa đánh giá này không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    reviewController.deleteReview(review.getReviewId(), () -> {
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Đã xóa đánh giá", Toast.LENGTH_SHORT).show();
                            loadFilteredReviews();
                        });
                    }, error -> {
                        runOnUiThread(() -> Toast.makeText(this, "Lỗi khi xóa: " + error, Toast.LENGTH_SHORT).show());
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
