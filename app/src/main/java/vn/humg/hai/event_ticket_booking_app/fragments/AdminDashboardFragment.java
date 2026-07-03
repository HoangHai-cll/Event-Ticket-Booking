package vn.humg.hai.event_ticket_booking_app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.firebase.auth.FirebaseAuth;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
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
import vn.humg.hai.event_ticket_booking_app.view.AdminAddEventActivity;
import vn.humg.hai.event_ticket_booking_app.view.AdminManageBookingsActivity;
import vn.humg.hai.event_ticket_booking_app.view.AdminManageEventsActivity;
import vn.humg.hai.event_ticket_booking_app.view.AdminManageReviewsActivity;
import vn.humg.hai.event_ticket_booking_app.view.AdminManageUsersActivity;

public class AdminDashboardFragment extends Fragment {

    private TextView tvWelcomeName, tvTotalRevenue, tvTotalTickets, tvTotalUsers;
    private TextView tvRevenueToday, tvRevenueMonth, tvAvgRating;
    private ProgressBar pb5, pb4, pb3, pb2, pb1;
    private RecyclerView rvCategoryReport, rvRecentReviews, rvTopEvents, rvTopUsers, rvRecentBookings;
    private SwipeRefreshLayout swipeRefresh;

    private final BookingController bookingController = new BookingController();
    private final EventController eventController = new EventController();
    private final ReviewController reviewController = new ReviewController();
    private final UserController userController = new UserController();
    
    private String currentAdminId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_dashboard, container, false);
        currentAdminId = FirebaseAuth.getInstance().getUid();
        initViews(view);
        setupDashboard();
        return view;
    }

    private void initViews(View view) {
        tvWelcomeName = view.findViewById(R.id.tv_admin_welcome_name);
        tvTotalRevenue = view.findViewById(R.id.tv_admin_total_revenue);
        tvTotalTickets = view.findViewById(R.id.tv_admin_total_tickets);
        tvTotalUsers = view.findViewById(R.id.tv_admin_total_users);
        tvRevenueToday = view.findViewById(R.id.tv_admin_revenue_today);
        tvRevenueMonth = view.findViewById(R.id.tv_admin_revenue_month);
        tvAvgRating = view.findViewById(R.id.tv_admin_avg_rating);
        
        pb5 = view.findViewById(R.id.pb_rating_5);
        pb4 = view.findViewById(R.id.pb_rating_4);
        pb3 = view.findViewById(R.id.pb_rating_3);
        pb2 = view.findViewById(R.id.pb_rating_2);
        pb1 = view.findViewById(R.id.pb_rating_1);
        
        rvCategoryReport = view.findViewById(R.id.recycler_admin_category_revenue);
        rvRecentReviews = view.findViewById(R.id.recycler_admin_reviews);
        rvTopEvents = view.findViewById(R.id.recycler_admin_top_events);
        rvTopUsers = view.findViewById(R.id.recycler_admin_top_users);
        rvRecentBookings = view.findViewById(R.id.recycler_admin_recent_bookings);
        
        swipeRefresh = view.findViewById(R.id.swipe_refresh_admin);
        swipeRefresh.setOnRefreshListener(this::setupDashboard);

        // Navigation
        setupClick(view, R.id.btn_admin_manage_bookings_link, AdminManageBookingsActivity.class);
        setupClick(view, R.id.btn_admin_see_all_reviews, AdminManageReviewsActivity.class);
        setupClick(view, R.id.btn_admin_go_to_add_event, AdminAddEventActivity.class);
        setupClick(view, R.id.btn_admin_manage_events, AdminManageEventsActivity.class);
        setupClick(view, R.id.btn_admin_manage_bookings, AdminManageBookingsActivity.class);
        setupClick(view, R.id.btn_admin_manage_users, AdminManageUsersActivity.class);
        setupClick(view, R.id.btn_admin_manage_reviews_link, AdminManageReviewsActivity.class);
    }

    private void setupClick(View parent, int viewId, Class<?> activityClass) {
        View v = parent.findViewById(viewId);
        if (v != null) {
            v.setOnClickListener(view -> startActivity(new Intent(getContext(), activityClass)));
        }
    }

    private void setupDashboard() {
        if (swipeRefresh != null) swipeRefresh.setRefreshing(true);
        
        if (currentAdminId != null) {
            userController.getUserById(currentAdminId, user -> {
                if (user != null && isAdded()) {
                    getActivity().runOnUiThread(() -> tvWelcomeName.setText(getString(R.string.admin_welcome_format, user.getFullName())));
                }
            }, e -> {});
        }

        userController.getAllUsers(users -> 
            eventController.getAllEvents(events -> 
                bookingController.getAllBookings(bookings -> 
                    reviewController.getAllReviews(reviews -> {
                        if (isAdded()) processDashboardData(users, events, bookings, reviews);
                    }, e -> hideLoading()), 
                e -> hideLoading()), 
            e -> hideLoading()), 
        e -> hideLoading());
    }

    private void processDashboardData(List<User> users, List<Event> events, List<Booking> bookings, List<Review> reviews) {
        Set<String> myEventIds = new HashSet<>();
        List<Event> myEvents = new ArrayList<>();
        for (Event e : events) {
            if (currentAdminId != null && currentAdminId.equals(e.getCreatedByAdminId())) {
                myEventIds.add(e.getEventId());
                myEvents.add(e);
            }
        }

        List<Booking> myBookings = new ArrayList<>();
        for (Booking b : bookings) {
            if (myEventIds.contains(b.getEventId())) myBookings.add(b);
        }

        List<Review> myReviews = new ArrayList<>();
        for (Review r : reviews) {
            if (myEventIds.contains(r.getEventId())) myReviews.add(r);
        }

        double totalRev = 0, todayRev = 0, monthRev = 0;
        int tickets = 0;
        Calendar now = Calendar.getInstance();
        Map<String, Double> eventRevenueMap = new HashMap<>();
        Map<String, Double> userSpentMap = new HashMap<>();
        
        for (Booking b : myBookings) {
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
        final int ftu = userSpentMap.size();
        
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                tvTotalRevenue.setText(formatVND(ft));
                tvRevenueToday.setText(formatVND(ftd));
                tvRevenueMonth.setText(formatVND(fm));
                tvTotalTickets.setText(String.valueOf(ftk));
                tvTotalUsers.setText(String.valueOf(ftu));
            });
        }

        if (!myReviews.isEmpty()) {
            float sum = 0;
            int[] stars = new int[6];
            for (Review r : myReviews) {
                sum += r.getRating();
                int s = Math.round(r.getRating());
                if (s >= 1 && s <= 5) stars[s]++;
            }
            final float avg = sum / myReviews.size();
            final int totalR = myReviews.size();
            final int[] fs = stars;
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    tvAvgRating.setText(String.format(Locale.getDefault(), "⭐ %.1f", avg));
                    pb5.setProgress((int) (fs[5] * 100.0 / totalR));
                    pb4.setProgress((int) (fs[4] * 100.0 / totalR));
                    pb3.setProgress((int) (fs[3] * 100.0 / totalR));
                    pb2.setProgress((int) (fs[2] * 100.0 / totalR));
                    pb1.setProgress((int) (fs[1] * 100.0 / totalR));
                });
            }
        }

        Map<String, AdminCategoryReportAdapter.CategoryStats> catStatsMap = new HashMap<>();
        for (Event e : myEvents) {
            String cat = (e.getCategory() == null || e.getCategory().isEmpty()) ? "Khác" : e.getCategory();
            if (!catStatsMap.containsKey(cat)) catStatsMap.put(cat, new AdminCategoryReportAdapter.CategoryStats(cat));
            AdminCategoryReportAdapter.CategoryStats s = catStatsMap.get(cat);
            s.avgRating += e.getAverageRating() * e.getReviewCount();
            s.reviewCount += e.getReviewCount();
        }
        
        for (Booking b : myBookings) {
            if (isPaid(b.getStatus())) {
                for (Event e : myEvents) {
                    if (e.getEventId().equals(b.getEventId())) {
                        String cat = (e.getCategory() == null || e.getCategory().isEmpty()) ? "Khác" : e.getCategory();
                        catStatsMap.get(cat).revenue += b.getTotalPrice();
                        break;
                    }
                }
            }
        }
        
        for (AdminCategoryReportAdapter.CategoryStats s : catStatsMap.values()) {
            if (s.reviewCount > 0) s.avgRating /= s.reviewCount;
        }

        Map<String, String> eventNames = new HashMap<>();
        for (Event e : myEvents) eventNames.put(e.getEventId(), e.getTitle());
        Map<String, String> userNames = new HashMap<>();
        for (User u : users) userNames.put(u.getUid(), u.getFullName());

        List<Map.Entry<String, Double>> topEv = new ArrayList<>(eventRevenueMap.entrySet());
        topEv.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));
        List<Map.Entry<String, Double>> topUs = new ArrayList<>(userSpentMap.entrySet());
        topUs.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));
        
        myBookings.sort((b1, b2) -> {
            if (b1.getBookingDate() == null || b2.getBookingDate() == null) return 0;
            return b2.getBookingDate().compareTo(b1.getBookingDate());
        });

        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                rvCategoryReport.setLayoutManager(new LinearLayoutManager(getContext()));
                rvCategoryReport.setAdapter(new AdminCategoryReportAdapter(catStatsMap));

                rvTopEvents.setLayoutManager(new LinearLayoutManager(getContext()));
                rvTopEvents.setAdapter(new AdminTopEventAdapter(topEv.subList(0, Math.min(topEv.size(), 5)), eventNames));

                rvTopUsers.setLayoutManager(new LinearLayoutManager(getContext()));
                rvTopUsers.setAdapter(new AdminTopUserAdapter(topUs.subList(0, Math.min(topUs.size(), 5)), userNames));

                rvRecentBookings.setLayoutManager(new LinearLayoutManager(getContext()));
                rvRecentBookings.setAdapter(new TransactionAdapter(myBookings.subList(0, Math.min(myBookings.size(), 5))));

                rvRecentReviews.setLayoutManager(new LinearLayoutManager(getContext()));
                rvRecentReviews.setAdapter(new AdminReviewAdapter(myReviews.subList(0, Math.min(myReviews.size(), 3)), null));
                
                hideLoading();
            });
        }
    }

    private void hideLoading() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
            });
        }
    }

    private String formatVND(double amount) {
        return String.format(Locale.getDefault(), "%,.0fđ", amount);
    }

    private boolean isPaid(String status) {
        return "Completed".equalsIgnoreCase(status) || "Hoàn thành".equalsIgnoreCase(status) || "Confirmed".equalsIgnoreCase(status);
    }
}
