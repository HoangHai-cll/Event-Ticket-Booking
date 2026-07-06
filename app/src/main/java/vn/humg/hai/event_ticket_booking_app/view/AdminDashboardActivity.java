package vn.humg.hai.event_ticket_booking_app.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.firebase.auth.FirebaseAuth;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import vn.humg.hai.event_ticket_booking_app.R;
import vn.humg.hai.event_ticket_booking_app.adapter.AdminCategoryReportAdapter;
import vn.humg.hai.event_ticket_booking_app.adapter.AdminReviewAdapter;
import vn.humg.hai.event_ticket_booking_app.adapter.AdminTopEventAdapter;
import vn.humg.hai.event_ticket_booking_app.adapter.AdminTopUserAdapter;
import vn.humg.hai.event_ticket_booking_app.adapter.TransactionAdapter;
import vn.humg.hai.event_ticket_booking_app.controller.BookingController;
import vn.humg.hai.event_ticket_booking_app.controller.EventController;
import vn.humg.hai.event_ticket_booking_app.controller.ReviewController;
import vn.humg.hai.event_ticket_booking_app.controller.UserController;
import vn.humg.hai.event_ticket_booking_app.model.Booking;
import vn.humg.hai.event_ticket_booking_app.model.Event;
import vn.humg.hai.event_ticket_booking_app.model.Review;
import vn.humg.hai.event_ticket_booking_app.model.User;

public class AdminDashboardActivity extends AppCompatActivity {

    private TextView tvWelcomeName, tvTotalRevenue, tvTotalTickets, tvTotalUsers;
    private TextView tvRevenueToday, tvRevenueMonth, tvAvgRating;
    private ProgressBar pb5, pb4, pb3, pb2, pb1;
    private RecyclerView rvCategoryReport, rvRecentReviews, rvTopEvents, rvTopUsers, rvRecentBookings;
    private SwipeRefreshLayout swipeRefresh;

    private final BookingController bookingController = new BookingController();
    private final EventController eventController = new EventController();
    private final ReviewController reviewController = new ReviewController();
    private final UserController userController = new UserController();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        initViews();
        setupDashboard();
    }

    private void initViews() {
        findViewById(R.id.btn_back_custom).setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        tvWelcomeName = findViewById(R.id.tv_admin_welcome_name);
        tvTotalRevenue = findViewById(R.id.tv_admin_total_revenue);
        tvTotalTickets = findViewById(R.id.tv_admin_total_tickets);
        tvTotalUsers = findViewById(R.id.tv_admin_total_users);
        tvRevenueToday = findViewById(R.id.tv_admin_revenue_today);
        tvRevenueMonth = findViewById(R.id.tv_admin_revenue_month);
        tvAvgRating = findViewById(R.id.tv_admin_avg_rating);
        pb5 = findViewById(R.id.pb_rating_5);
        pb4 = findViewById(R.id.pb_rating_4);
        pb3 = findViewById(R.id.pb_rating_3);
        pb2 = findViewById(R.id.pb_rating_2);
        pb1 = findViewById(R.id.pb_rating_1);
        
        rvCategoryReport = findViewById(R.id.recycler_admin_category_revenue);
        rvRecentReviews = findViewById(R.id.recycler_admin_reviews);
        rvTopEvents = findViewById(R.id.recycler_admin_top_events);
        rvTopUsers = findViewById(R.id.recycler_admin_top_users);
        rvRecentBookings = findViewById(R.id.recycler_admin_recent_bookings);
        
        swipeRefresh = findViewById(R.id.swipe_refresh_admin);
        swipeRefresh.setOnRefreshListener(this::setupDashboard);

        // --- CÀI ĐẶT ĐIỀU HƯỚNG ---
        // Link "Xem tất cả"
        if (findViewById(R.id.btn_admin_manage_bookings_link) != null) {
            findViewById(R.id.btn_admin_manage_bookings_link).setOnClickListener(v -> 
                startActivity(new Intent(this, AdminManageBookingsActivity.class)));
        }
        if (findViewById(R.id.btn_admin_see_all_reviews) != null) {
            findViewById(R.id.btn_admin_see_all_reviews).setOnClickListener(v -> 
                startActivity(new Intent(this, AdminManageReviewsActivity.class)));
        }

        // Các nút trong Grid Quản lý hệ thống
        findViewById(R.id.btn_admin_go_to_add_event).setOnClickListener(v -> 
            startActivity(new Intent(this, AdminAddEventActivity.class)));
        findViewById(R.id.btn_admin_manage_events).setOnClickListener(v -> 
            startActivity(new Intent(this, AdminManageEventsActivity.class)));
        findViewById(R.id.btn_admin_manage_bookings).setOnClickListener(v -> 
            startActivity(new Intent(this, AdminManageBookingsActivity.class)));
        findViewById(R.id.btn_admin_manage_users).setOnClickListener(v -> 
            startActivity(new Intent(this, AdminManageUsersActivity.class)));
        
        // Link Quản lý đánh giá
        if (findViewById(R.id.btn_admin_manage_reviews_link) != null) {
            findViewById(R.id.btn_admin_manage_reviews_link).setOnClickListener(v -> 
                startActivity(new Intent(this, AdminManageReviewsActivity.class)));
        }
        if (findViewById(R.id.btn_admin_manage_vouchers) != null) {
            findViewById(R.id.btn_admin_manage_vouchers).setOnClickListener(v -> 
                startActivity(new Intent(this, AdminManageVouchersActivity.class)));
        }
    }


    private void setupDashboard() {
        swipeRefresh.setRefreshing(true);
        
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            hideLoading();
            return;
        }

        userController.getAdminById(uid, admin -> {
            if (admin != null) {
                runOnUiThread(() -> tvWelcomeName.setText("Chào mừng, " + admin.getFullName()));
                
                final int accessLevel = admin.getAccessLevel();
                
                userController.getAllUsers(users -> {
                    eventController.getAllEvents(events -> {
                        bookingController.getAllBookings(bookings -> {
                            reviewController.getAllReviews(reviews -> {
                                processDashboardData(uid, accessLevel, users, events, bookings, reviews);
                            }, e -> hideLoading());
                        }, e -> hideLoading());
                    }, e -> hideLoading());
                }, e -> hideLoading());
            } else {
                hideLoading();
            }
        }, e -> hideLoading());
    }

    private void processDashboardData(String currentAdminId, int accessLevel, List<User> users, List<Event> events, List<Booking> bookings, List<Review> reviews) {
        // Lọc dữ liệu theo admin nếu không phải là Developer (Cấp 3)
        List<Event> filteredEvents = new ArrayList<>();
        List<Booking> filteredBookings = new ArrayList<>();
        List<Review> filteredReviews = new ArrayList<>();
        
        if (accessLevel == 3) {
            filteredEvents.addAll(events);
            filteredBookings.addAll(bookings);
            filteredReviews.addAll(reviews);
        } else {
            // Lọc sự kiện do chính admin này tạo
            java.util.Set<String> myEventIds = new java.util.HashSet<>();
            for (Event e : events) {
                if (currentAdminId.equals(e.getCreatedByAdminId())) {
                    filteredEvents.add(e);
                    myEventIds.add(e.getEventId());
                }
            }
            
            // Lọc giao dịch thuộc sự kiện của admin này (sellerId == currentAdminId)
            for (Booking b : bookings) {
                if (currentAdminId.equals(b.getSellerId())) {
                    filteredBookings.add(b);
                }
            }
            
            // Lọc đánh giá thuộc sự kiện của admin này
            for (Review r : reviews) {
                if (myEventIds.contains(r.getEventId())) {
                    filteredReviews.add(r);
                }
            }
        }

        // Đếm số lượng khách hàng mua vé của admin này
        java.util.Set<String> uniqueCustomerIds = new java.util.HashSet<>();
        for (Booking b : filteredBookings) {
            uniqueCustomerIds.add(b.getUserId());
        }
        
        final int totalCustomersCount = (accessLevel == 3) ? users.size() : uniqueCustomerIds.size();
        runOnUiThread(() -> tvTotalUsers.setText(String.valueOf(totalCustomersCount)));
        
        double totalRev = 0, todayRev = 0, monthRev = 0;
        int tickets = 0;
        Calendar now = Calendar.getInstance();
        Map<String, Double> eventRevenueMap = new HashMap<>();
        Map<String, Double> userSpentMap = new HashMap<>();
        
        for (Booking b : filteredBookings) {
            if (isPaid(b.getStatus())) {
                double p = b.getTotalPrice();
                totalRev += p;
                tickets += b.getQuantity();
                eventRevenueMap.put(b.getEventId(), eventRevenueMap.getOrDefault(b.getEventId(), 0.0) + p);
                userSpentMap.put(b.getUserId(), userSpentMap.getOrDefault(b.getUserId(), 0.0) + p);

                if (b.getBookingDate() != null) {
                    Calendar bCal = Calendar.getInstance();
                    bCal.setTime(b.getBookingDate().toDate());
                    if (bCal.get(Calendar.YEAR) == now.get(Calendar.YEAR)) {
                        if (bCal.get(Calendar.MONTH) == now.get(Calendar.MONTH)) {
                            monthRev += p;
                            if (bCal.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)) todayRev += p;
                        }
                    }
                }
            }
        }

        final double ft = totalRev, ftd = todayRev, fm = monthRev;
        final int ftk = tickets;
        runOnUiThread(() -> {
            tvTotalRevenue.setText(formatVND(ft));
            tvRevenueToday.setText(formatVND(ftd));
            tvRevenueMonth.setText(formatVND(fm));
            tvTotalTickets.setText(String.valueOf(ftk));
        });

        if (!filteredReviews.isEmpty()) {
            float sum = 0;
            int[] stars = new int[6];
            for (Review r : filteredReviews) {
                sum += r.getRating();
                int s = Math.round(r.getRating());
                if (s >= 1 && s <= 5) stars[s]++;
            }
            final float avg = sum / filteredReviews.size();
            final int totalR = filteredReviews.size();
            final int[] fs = stars;
            runOnUiThread(() -> {
                tvAvgRating.setText(String.format(Locale.getDefault(), "⭐ %.1f", avg));
                pb5.setProgress(totalR > 0 ? (int) (fs[5] * 100.0 / totalR) : 0);
                pb4.setProgress(totalR > 0 ? (int) (fs[4] * 100.0 / totalR) : 0);
                pb3.setProgress(totalR > 0 ? (int) (fs[3] * 100.0 / totalR) : 0);
                pb2.setProgress(totalR > 0 ? (int) (fs[2] * 100.0 / totalR) : 0);
                pb1.setProgress(totalR > 0 ? (int) (fs[1] * 100.0 / totalR) : 0);
            });
        } else {
            runOnUiThread(() -> {
                tvAvgRating.setText("⭐ 0.0");
                pb5.setProgress(0);
                pb4.setProgress(0);
                pb3.setProgress(0);
                pb2.setProgress(0);
                pb1.setProgress(0);
            });
        }

        // Xử lý báo cáo danh mục
        Map<String, AdminCategoryReportAdapter.CategoryStats> catStatsMap = new HashMap<>();
        Map<String, String> eventToCat = new HashMap<>();
        for (Event e : filteredEvents) {
            eventToCat.put(e.getEventId(), e.getCategory());
            String cat = (e.getCategory() == null || e.getCategory().isEmpty()) ? "Khác" : e.getCategory();
            if (!catStatsMap.containsKey(cat)) catStatsMap.put(cat, new AdminCategoryReportAdapter.CategoryStats(cat));
            AdminCategoryReportAdapter.CategoryStats s = catStatsMap.get(cat);
            s.avgRating += e.getAverageRating() * e.getReviewCount();
            s.reviewCount += e.getReviewCount();
        }
        
        for (Booking b : filteredBookings) {
            if (isPaid(b.getStatus())) {
                String cat = eventToCat.get(b.getEventId());
                if (cat == null) cat = "Khác";
                if (catStatsMap.containsKey(cat)) {
                    catStatsMap.get(cat).revenue += b.getTotalPrice();
                }
            }
        }
        
        for (AdminCategoryReportAdapter.CategoryStats s : catStatsMap.values()) {
            if (s.reviewCount > 0) s.avgRating /= s.reviewCount;
        }

        Map<String, String> eventNames = new HashMap<>();
        for (Event e : filteredEvents) eventNames.put(e.getEventId(), e.getTitle());
        Map<String, String> userNames = new HashMap<>();
        for (User u : users) userNames.put(u.getUid(), u.getFullName());

        List<Map.Entry<String, Double>> topEv = new ArrayList<>(eventRevenueMap.entrySet());
        topEv.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));
        List<Map.Entry<String, Double>> topUs = new ArrayList<>(userSpentMap.entrySet());
        topUs.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));
        
        List<Booking> recentBookings = new ArrayList<>(filteredBookings);
        recentBookings.sort((b1, b2) -> {
            if (b1.getBookingDate() == null || b2.getBookingDate() == null) return 0;
            return b2.getBookingDate().compareTo(b1.getBookingDate());
        });

        runOnUiThread(() -> {
            rvCategoryReport.setLayoutManager(new LinearLayoutManager(this));
            rvCategoryReport.setAdapter(new AdminCategoryReportAdapter(catStatsMap));

            rvTopEvents.setLayoutManager(new LinearLayoutManager(this));
            rvTopEvents.setAdapter(new AdminTopEventAdapter(topEv.subList(0, Math.min(topEv.size(), 5)), eventNames));

            rvTopUsers.setLayoutManager(new LinearLayoutManager(this));
            rvTopUsers.setAdapter(new AdminTopUserAdapter(topUs.subList(0, Math.min(topUs.size(), 5)), userNames));

            rvRecentBookings.setLayoutManager(new LinearLayoutManager(this));
            rvRecentBookings.setAdapter(new TransactionAdapter(recentBookings.subList(0, Math.min(recentBookings.size(), 5))));

            rvRecentReviews.setLayoutManager(new LinearLayoutManager(this));
            rvRecentReviews.setAdapter(new AdminReviewAdapter(filteredReviews.subList(0, Math.min(filteredReviews.size(), 3)), null));
            
            hideLoading();
        });
    }

    private void hideLoading() {
        runOnUiThread(() -> swipeRefresh.setRefreshing(false));
    }

    private String formatVND(double amount) {
        return String.format(Locale.getDefault(), "%,.0fđ", amount);
    }

    private boolean isPaid(String status) {
        return "Completed".equalsIgnoreCase(status) || "Hoàn thành".equalsIgnoreCase(status) || "Confirmed".equalsIgnoreCase(status);
    }
}
