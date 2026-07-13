package vn.humg.hai.event_ticket_booking_app.fragments;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import vn.humg.hai.event_ticket_booking_app.R;
import androidx.lifecycle.ViewModelProvider;
import vn.humg.hai.event_ticket_booking_app.adapter.EventAdapter;
import vn.humg.hai.event_ticket_booking_app.viewmodel.AuthViewModel;
import vn.humg.hai.event_ticket_booking_app.viewmodel.EventViewModel;
import vn.humg.hai.event_ticket_booking_app.viewmodel.BookingViewModel;
import vn.humg.hai.event_ticket_booking_app.model.Event;
import vn.humg.hai.event_ticket_booking_app.model.Favorite;
import vn.humg.hai.event_ticket_booking_app.model.Booking;
import vn.humg.hai.event_ticket_booking_app.utils.LocalNotificationDbHelper;
import vn.humg.hai.event_ticket_booking_app.utils.MyFirebaseMessagingService;
import vn.humg.hai.event_ticket_booking_app.utils.EventReminderReceiver;
import vn.humg.hai.event_ticket_booking_app.view.EventDetailActivity;
import vn.humg.hai.event_ticket_booking_app.view.MainActivity;
import vn.humg.hai.event_ticket_booking_app.view.NotificationActivity;
import vn.humg.hai.event_ticket_booking_app.view.LoginActivity;

public class HomeFragment extends Fragment {
    private EventViewModel eventViewModel;
    private AuthViewModel authViewModel;
    
    private final List<Event> fullEventList = new ArrayList<>();
    private final List<Event> hotEventList = new ArrayList<>();
    private final List<Event> displayList = new ArrayList<>();
    private final Set<String> userFavoriteIds = new HashSet<>();
    
    private EventAdapter eventAdapter;
    private EventAdapter hotEventAdapter;
    
    private RecyclerView recyclerEvents, recyclerHotEvents;
    private TextView tvSeeAll, chipMusic, chipSeminar, chipWorkshop, tvHotTitle, tvAllTitle, tvNoResults;
    private ImageView ivProfileTop;
    private TextView chipUpcoming, chipPast;
    private EditText etSearch;
    private ImageView ivSearchIcon, ivLayoutToggle;
    private ImageButton btnMenuDrawer;
    private View layoutSearch, layoutHotEvents, layoutCategories, layoutTimeFilters;
    private View dividerNoResults, dividerAfterCategories;
    
    private String selectedCategory = null;
    private boolean showUpcomingOnly = true;
    private boolean showFavoritesOnly = false;
    private boolean useCompactLayout = false;

    // Notifications and Countdown Views/Logic
    private View layoutNotificationBell;
    private TextView tvNotificationBadge;
    private View cardCountdownTimer;
    private View btnCloseCountdownCard;
    private TextView tvCountdownEventTitle;
    private TextView tvCountdownDays, tvCountdownHours, tvCountdownMinutes, tvCountdownSeconds;
    private View layoutSimpleDays;
    private TextView tvCountdownDaysSimple;
    private View layoutBentoCountdown;
    private boolean isCountdownCardDismissed = false;
    private long lastDisplayedDays = -1;
    
    // Floating Egg Countdown Views/Logic
    private View layoutFloatingEggContainer;
    private View layoutFloatingEgg;
    private TextView tvFloatingCountdown;
    private View btnCloseFloatingEgg;
    private boolean isFloatingEggDismissed = false;

    private BroadcastReceiver unreadUpdateReceiver;
    private BookingViewModel bookingViewModel;
    private CountDownTimer countDownTimer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        
        eventViewModel = new ViewModelProvider(this).get(EventViewModel.class);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        bookingViewModel = new ViewModelProvider(this).get(BookingViewModel.class);
        
        setupObservers();
        setupBookingObservers();
        setupNotificationReceiver();

        initViews(view);
        setupRecyclerView();
        initEvents();
        checkUserRoleAndAdjustUI();
        loadEvents();
        return view;
    }

    private void setupObservers() {
        authViewModel.getUserProfileState().observe(getViewLifecycleOwner(), user -> {
            if (user != null && "admin".equalsIgnoreCase(user.getRole()) && isAdded()) {
                if (layoutSearch != null) layoutSearch.setVisibility(View.GONE);
                if (layoutHotEvents != null) layoutHotEvents.setVisibility(View.GONE);
                if (layoutCategories != null) layoutCategories.setVisibility(View.GONE);
                if (layoutTimeFilters != null) layoutTimeFilters.setVisibility(View.GONE);
                
                if (tvAllTitle != null) {
                    tvAllTitle.setText("Tất cả sự kiện hệ thống");
                    tvAllTitle.setTextSize(22);
                }
            }
        });

        eventViewModel.getUserFavoritesState().observe(getViewLifecycleOwner(), favorites -> {
            userFavoriteIds.clear();
            List<String> favIds = new ArrayList<>();
            if (favorites != null) {
                for (Favorite fav : favorites) {
                    userFavoriteIds.add(fav.getEventId());
                    favIds.add(fav.getEventId());
                }
            }
            if (eventAdapter != null) {
                eventAdapter.setFavoriteEventIds(favIds);
            }
            if (hotEventAdapter != null) {
                hotEventAdapter.setFavoriteEventIds(favIds);
            }
            eventViewModel.loadAllEvents();
        });

        eventViewModel.getEventsState().observe(getViewLifecycleOwner(), events -> {
            if (events != null && isAdded()) {
                List<Event> sortedEvents = new ArrayList<>(events);
                Collections.sort(sortedEvents, (e1, e2) -> {
                    boolean isExpired1 = false;
                    boolean isExpired2 = false;
                    long currentTime = System.currentTimeMillis() / 1000;
                    if (e1.getDate() != null) {
                        isExpired1 = e1.getDate().getSeconds() < currentTime;
                    }
                    if (e2.getDate() != null) {
                        isExpired2 = e2.getDate().getSeconds() < currentTime;
                    }
                    if (isExpired1 && !isExpired2) return 1;
                    if (!isExpired1 && isExpired2) return -1;
                    
                    int lvl1 = e1.getCreatorAccessLevel();
                    int lvl2 = e2.getCreatorAccessLevel();
                    boolean isLvl2_1 = (lvl1 == 2);
                    boolean isLvl2_2 = (lvl2 == 2);
                    if (isLvl2_1 && !isLvl2_2) return -1;
                    if (!isLvl2_1 && isLvl2_2) return 1;
                    
                    if (lvl1 != lvl2) {
                        return Integer.compare(lvl2, lvl1);
                    }

                    boolean isFav1 = userFavoriteIds.contains(e1.getEventId());
                    boolean isFav2 = userFavoriteIds.contains(e2.getEventId());
                    if (isFav1 && !isFav2) return -1;
                    if (!isFav1 && isFav2) return 1;
                    
                    return 0;
                });

                fullEventList.clear();
                fullEventList.addAll(sortedEvents);
                
                hotEventList.clear();
                long now = System.currentTimeMillis() / 1000L;
                long threeDaysInSeconds = 3 * 24 * 60 * 60;
                long oneDayInSeconds = 24 * 60 * 60;

                for (Event e : sortedEvents) {
                    boolean expired = e.getDate() != null && e.getDate().getSeconds() < now;
                    
                    // Logic Hot Event mới:
                    // 1. Phải được đánh dấu isHot = true
                    // 2. Sự kiện chưa diễn ra (expired = false)
                    // 3. Thời gian tồn tại:
                    //    - Nếu là Auto Hot (do bán 50 vé): tồn tại 1 ngày.
                    //    - Nếu là Admin đặt: tồn tại 3 ngày.
                    boolean withinHotDuration = true;
                    if (e.isHot() && e.getHotSetAt() != null) {
                        long hotAgeSeconds = now - e.getHotSetAt().getSeconds();
                        long duration = e.isAutoHot() ? oneDayInSeconds : threeDaysInSeconds;
                        withinHotDuration = hotAgeSeconds <= duration;
                    }

                    if (e.isHot() && !expired && withinHotDuration) {
                        hotEventList.add(e);
                    }
                }

                hotEventAdapter.notifyDataSetChanged();
                applyFilters();
            }
        });
    }

    private void initViews(View view) {
        recyclerEvents = view.findViewById(R.id.recycler_events);
        recyclerHotEvents = view.findViewById(R.id.recycler_hot_events);
        tvSeeAll = view.findViewById(R.id.tv_see_all);
        chipMusic = view.findViewById(R.id.chip_music);
        chipSeminar = view.findViewById(R.id.chip_seminar);
        chipWorkshop = view.findViewById(R.id.chip_workshop);
        chipUpcoming = view.findViewById(R.id.chip_upcoming);
        chipPast = view.findViewById(R.id.chip_past);
        etSearch = view.findViewById(R.id.et_search);
        ivSearchIcon = view.findViewById(R.id.iv_search_icon);
        ivLayoutToggle = view.findViewById(R.id.iv_layout_toggle);
        ivProfileTop = view.findViewById(R.id.iv_profile_top);

        // Khởi tạo các view thông báo và đếm ngược mới
        layoutNotificationBell = view.findViewById(R.id.layout_notification_bell);
        tvNotificationBadge = view.findViewById(R.id.tv_notification_badge);
        cardCountdownTimer = view.findViewById(R.id.card_countdown_timer);
        btnCloseCountdownCard = view.findViewById(R.id.btn_close_countdown_card);
        tvCountdownEventTitle = view.findViewById(R.id.tv_countdown_event_title);
        tvCountdownDays = view.findViewById(R.id.tv_countdown_days);
        tvCountdownHours = view.findViewById(R.id.tv_countdown_hours);
        tvCountdownMinutes = view.findViewById(R.id.tv_countdown_minutes);
        tvCountdownSeconds = view.findViewById(R.id.tv_countdown_seconds);
        layoutSimpleDays = view.findViewById(R.id.layout_simple_days);
        tvCountdownDaysSimple = view.findViewById(R.id.tv_countdown_days_simple);
        layoutBentoCountdown = view.findViewById(R.id.layout_bento_countdown);
        
        // Khởi tạo các view quả trứng thông báo nổi
        layoutFloatingEggContainer = view.findViewById(R.id.layout_floating_egg_container);
        layoutFloatingEgg = view.findViewById(R.id.layout_floating_egg);
        tvFloatingCountdown = view.findViewById(R.id.tv_floating_countdown);
        btnCloseFloatingEgg = view.findViewById(R.id.btn_close_floating_egg);
        
        tvHotTitle = view.findViewById(R.id.tv_hot_events_title); 
        tvAllTitle = view.findViewById(R.id.tv_all_events_title);
        tvNoResults = view.findViewById(R.id.tv_no_results);
        btnMenuDrawer = view.findViewById(R.id.btn_menu_drawer);
        
        layoutSearch = view.findViewById(R.id.layout_search);
        layoutHotEvents = view.findViewById(R.id.layout_hot_events);
        layoutCategories = view.findViewById(R.id.layout_categories);
        layoutTimeFilters = view.findViewById(R.id.layout_time_filters);
        dividerNoResults = view.findViewById(R.id.divider_no_results);
        dividerAfterCategories = view.findViewById(R.id.divider_after_categories);
    }

    private void checkUserRoleAndAdjustUI() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;
        authViewModel.getUserProfile(uid);
    }

    private void setupRecyclerView() {
        recyclerHotEvents.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        hotEventAdapter = new EventAdapter(hotEventList, true, this::openEventDetail, this::handleFavoriteClick);
        recyclerHotEvents.setAdapter(hotEventAdapter);

        recyclerEvents.setLayoutManager(new LinearLayoutManager(getContext()));
        eventAdapter = new EventAdapter(displayList, false, this::openEventDetail, this::handleFavoriteClick);
        recyclerEvents.setAdapter(eventAdapter);

        // Synchronize favorites between adapters and re-sort lists dynamically
        EventAdapter.OnFavoriteChangeListener favListener = (eventId, isAdded) -> {
            if (isAdded) {
                userFavoriteIds.add(eventId);
            } else {
                userFavoriteIds.remove(eventId);
            }
            List<String> updatedFavIds = new ArrayList<>(userFavoriteIds);
            if (eventAdapter != null) {
                eventAdapter.setFavoriteEventIds(updatedFavIds);
            }
            if (hotEventAdapter != null) {
                hotEventAdapter.setFavoriteEventIds(updatedFavIds);
            }
            applyFilters();
        };

        eventAdapter.setOnFavoriteChangeListener(favListener);
        hotEventAdapter.setOnFavoriteChangeListener(favListener);
    }

    private void handleFavoriteClick(Event event) {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập để thích sự kiện", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getContext(), LoginActivity.class));
            return;
        }
        
        boolean isFavNow = userFavoriteIds.contains(event.getEventId());
        if (isFavNow) {
            Toast.makeText(getContext(), "Đã bỏ thích sự kiện", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Đã thích sự kiện", Toast.LENGTH_SHORT).show();
        }
        eventViewModel.toggleFavorite(userId, event);
    }

    private void initEvents() {
        if (btnMenuDrawer != null) {
            btnMenuDrawer.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).openDrawer();
                }
            });
        }

        ivProfileTop.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToTab("Profile");
            }
        });

        if (layoutNotificationBell != null) {
            layoutNotificationBell.setOnClickListener(v -> {
                if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                    Toast.makeText(getContext(), "Vui lòng đăng nhập để xem thông báo", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getContext(), LoginActivity.class));
                    return;
                }
                Intent intent = new Intent(getContext(), NotificationActivity.class);
                startActivity(intent);
            });
        }

        if (layoutFloatingEgg != null) {
            layoutFloatingEgg.setOnClickListener(v -> {
                if (getView() != null) {
                    androidx.core.widget.NestedScrollView scrollView = getView().findViewById(R.id.nested_scroll_view);
                    if (scrollView != null) {
                        scrollView.smoothScrollTo(0, 0);
                    }
                }
                Toast.makeText(getContext(), "Cuộn lên đầu trang để xem chi tiết sự kiện sắp diễn ra!", Toast.LENGTH_SHORT).show();
            });
        }

        if (btnCloseFloatingEgg != null) {
            btnCloseFloatingEgg.setOnClickListener(v -> {
                isFloatingEggDismissed = true;
                if (layoutFloatingEggContainer != null) {
                    layoutFloatingEggContainer.setVisibility(View.GONE);
                }
            });
        }

        if (btnCloseCountdownCard != null) {
            btnCloseCountdownCard.setOnClickListener(v -> {
                isCountdownCardDismissed = true;
                if (cardCountdownTimer != null) {
                    cardCountdownTimer.setVisibility(View.GONE);
                }
            });
        }

        tvSeeAll.setOnClickListener(v -> resetFilter());
        
        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    applyFilters();
                }
                @Override public void afterTextChanged(Editable s) {}
            });

            etSearch.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                    applyFilters();
                    return true;
                }
                return false;
            });
        }

        if (ivSearchIcon != null) {
            ivSearchIcon.setOnClickListener(v -> applyFilters());
        }

        if (ivLayoutToggle != null) {
            ivLayoutToggle.setOnClickListener(v -> {
                useCompactLayout = !useCompactLayout;
                if (eventAdapter != null) {
                    eventAdapter.setUseCompactLayout(useCompactLayout);
                }
                // Update icon: Show 'Tickets' icon when in List mode, 'Menu' icon when in Card mode
                ivLayoutToggle.setImageResource(useCompactLayout ? R.drawable.ic_nav_tickets : R.drawable.ic_menu);
            });
        }

        // Chip click listeners
        if (chipUpcoming != null) {
            chipUpcoming.setOnClickListener(v -> {
                showUpcomingOnly = true;
                showFavoritesOnly = false;
                applyFilters();
            });
        }

        if (chipPast != null) {
            chipPast.setOnClickListener(v -> {
                showUpcomingOnly = false;
                showFavoritesOnly = false;
                applyFilters();
            });
        }

        if (chipMusic != null) {
            chipMusic.setOnClickListener(v -> {
                showFavoritesOnly = false;
                if ("Nhạc hội".equalsIgnoreCase(selectedCategory)) {
                    selectedCategory = null;
                } else {
                    selectedCategory = "Nhạc hội";
                }
                applyFilters();
            });
        }

        if (chipSeminar != null) {
            chipSeminar.setOnClickListener(v -> {
                showFavoritesOnly = false;
                if ("Hội thảo".equalsIgnoreCase(selectedCategory)) {
                    selectedCategory = null;
                } else {
                    selectedCategory = "Hội thảo";
                }
                applyFilters();
            });
        }

        if (chipWorkshop != null) {
            chipWorkshop.setOnClickListener(v -> {
                showFavoritesOnly = false;
                if ("Workshop".equalsIgnoreCase(selectedCategory)) {
                    selectedCategory = null;
                } else {
                    selectedCategory = "Workshop";
                }
                applyFilters();
            });
        }
    }

    public void filterByFavoritesOnly() {
        selectedCategory = null;
        showUpcomingOnly = true;
        showFavoritesOnly = true;
        applyFilters();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadEvents();
        updateNotificationBadge();
        loadUserBookings();
        
        // Xin quyền thông báo trên Android 13+ (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (getContext() != null && androidx.core.content.ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    private void loadUserBookings() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            bookingViewModel.loadUserBookings(uid);
        } else {
            if (cardCountdownTimer != null) {
                cardCountdownTimer.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            loadEvents();
            updateNotificationBadge();
            loadUserBookings();
        }
    }

    private void resetFilter() {
        if (etSearch != null) etSearch.setText("");
        selectedCategory = null;
        showUpcomingOnly = true;
        showFavoritesOnly = false;
        applyFilters();
    }

    private void loadEvents() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            eventViewModel.loadUserFavorites(uid);
        } else {
            eventViewModel.loadAllEvents();
        }
    }

    private void applyFilters() {
        displayList.clear();
        long now = System.currentTimeMillis() / 1000L;

        // Step 1: Filter and count categories based on the current time filter (Upcoming vs Past) and favorites
        int musicCount = 0;
        int seminarCount = 0;
        int workshopCount = 0;

        for (Event e : fullEventList) {
            boolean isExpired = e.getDate() != null && e.getDate().getSeconds() < now;
            boolean matchesTime = (showUpcomingOnly && !isExpired) || (!showUpcomingOnly && isExpired);
            if (showFavoritesOnly && !userFavoriteIds.contains(e.getEventId())) {
                matchesTime = false;
            }

            if (matchesTime) {
                String cat = e.getCategory();
                if ("Nhạc hội".equalsIgnoreCase(cat)) {
                    musicCount++;
                } else if ("Hội thảo".equalsIgnoreCase(cat)) {
                    seminarCount++;
                } else if ("Workshop".equalsIgnoreCase(cat)) {
                    workshopCount++;
                }
            }
        }

        // Update category chip texts
        if (chipMusic != null) chipMusic.setText("Nhạc hội (" + musicCount + ")");
        if (chipSeminar != null) chipSeminar.setText("Hội thảo (" + seminarCount + ")");
        if (chipWorkshop != null) chipWorkshop.setText("Workshop (" + workshopCount + ")");

        String queryStr = (etSearch != null && etSearch.getText() != null) ? etSearch.getText().toString().toLowerCase().trim() : "";

        // Hide Hot Events section if searching
        if (layoutHotEvents != null) {
            layoutHotEvents.setVisibility(!queryStr.isEmpty() ? View.GONE : View.VISIBLE);
        }

        // Step 2: Apply active filters
        for (Event e : fullEventList) {
            boolean isExpired = e.getDate() != null && e.getDate().getSeconds() < now;
            boolean matchesTime = (showUpcomingOnly && !isExpired) || (!showUpcomingOnly && isExpired);

            if (!matchesTime) continue;

            // Favorites filter
            if (showFavoritesOnly && !userFavoriteIds.contains(e.getEventId())) {
                continue;
            }

            // Category filter
            if (selectedCategory != null && !selectedCategory.equalsIgnoreCase(e.getCategory())) {
                continue;
            }

            // Search query filter (Partial match in title, description, location, etc.)
            if (!queryStr.isEmpty()) {
                boolean matchesTitle = e.getTitle() != null && e.getTitle().toLowerCase().contains(queryStr);
                boolean matchesDesc = e.getDescription() != null && e.getDescription().toLowerCase().contains(queryStr);
                boolean matchesLoc = e.getLocation() != null && e.getLocation().toLowerCase().contains(queryStr);
                boolean matchesOrg = e.getOrganizerName() != null && e.getOrganizerName().toLowerCase().contains(queryStr);
                boolean matchesArtist = e.getArtistName() != null && e.getArtistName().toLowerCase().contains(queryStr);
                
                boolean matchesTags = false;
                if (e.getTags() != null) {
                    for (String tag : e.getTags()) {
                        if (tag.toLowerCase().contains(queryStr)) {
                            matchesTags = true;
                            break;
                        }
                    }
                }

                if (!matchesTitle && !matchesDesc && !matchesLoc && !matchesOrg && !matchesArtist && !matchesTags) {
                    continue;
                }
            }

            displayList.add(e);
        }

        // Step 2.5: If displayList is empty and query is not empty, show red warning and suggest related events
        if (!queryStr.isEmpty() && displayList.isEmpty()) {
            if (tvNoResults != null) {
                tvNoResults.setVisibility(View.VISIBLE);
            }
            if (dividerNoResults != null) {
                dividerNoResults.setVisibility(View.VISIBLE);
            }
            
            // Suggest upcoming related events
            int suggestionLimit = 4;
            int count = 0;
            for (Event e : fullEventList) {
                boolean isExpired = e.getDate() != null && e.getDate().getSeconds() < now;
                if (!isExpired) {
                    // Match category if selected, else take any upcoming
                    if (selectedCategory == null || selectedCategory.equalsIgnoreCase(e.getCategory())) {
                        displayList.add(e);
                        count++;
                        if (count >= suggestionLimit) break;
                    }
                }
            }

            // Fallback: If no upcoming events, suggest any events
            if (displayList.isEmpty()) {
                for (Event e : fullEventList) {
                    displayList.add(e);
                    count++;
                    if (count >= suggestionLimit) break;
                }
            }
        } else {
            if (tvNoResults != null) {
                tvNoResults.setVisibility(View.GONE);
            }
            if (dividerNoResults != null) {
                dividerNoResults.setVisibility(View.GONE);
            }
        }

        // Step 3: Update styles
        updateChipStyles();

        // Step 4: Notify adapter
        if (eventAdapter != null) {
            eventAdapter.notifyDataSetChanged();
        }
    }

    private void updateChipStyles() {
        if (getContext() == null) return;

        // Time Filters
        if (showUpcomingOnly) {
            chipUpcoming.setBackgroundResource(R.drawable.bg_chip);
            chipUpcoming.setTextColor(getResources().getColor(R.color.white));
            chipUpcoming.setTypeface(null, android.graphics.Typeface.BOLD);

            chipPast.setBackgroundResource(R.drawable.bg_chip_light);
            chipPast.setTextColor(getResources().getColor(R.color.ink_dark));
            chipPast.setTypeface(null, android.graphics.Typeface.NORMAL);
        } else {
            chipUpcoming.setBackgroundResource(R.drawable.bg_chip_light);
            chipUpcoming.setTextColor(getResources().getColor(R.color.ink_dark));
            chipUpcoming.setTypeface(null, android.graphics.Typeface.NORMAL);

            chipPast.setBackgroundResource(R.drawable.bg_chip);
            chipPast.setTextColor(getResources().getColor(R.color.white));
            chipPast.setTypeface(null, android.graphics.Typeface.BOLD);
        }

        // Category Filters
        if (chipMusic != null) {
            chipMusic.setBackgroundResource(R.drawable.bg_chip_light);
            chipMusic.setTextColor(getResources().getColor(R.color.ink_dark));
            chipMusic.setTypeface(null, android.graphics.Typeface.NORMAL);
        }

        if (chipSeminar != null) {
            chipSeminar.setBackgroundResource(R.drawable.bg_chip_light);
            chipSeminar.setTextColor(getResources().getColor(R.color.ink_dark));
            chipSeminar.setTypeface(null, android.graphics.Typeface.NORMAL);
        }

        if (chipWorkshop != null) {
            chipWorkshop.setBackgroundResource(R.drawable.bg_chip_light);
            chipWorkshop.setTextColor(getResources().getColor(R.color.ink_dark));
            chipWorkshop.setTypeface(null, android.graphics.Typeface.NORMAL);
        }

        if (selectedCategory != null) {
            if ("Nhạc hội".equalsIgnoreCase(selectedCategory) && chipMusic != null) {
                chipMusic.setBackgroundResource(R.drawable.bg_chip);
                chipMusic.setTextColor(getResources().getColor(R.color.white));
                chipMusic.setTypeface(null, android.graphics.Typeface.BOLD);
            } else if ("Hội thảo".equalsIgnoreCase(selectedCategory) && chipSeminar != null) {
                chipSeminar.setBackgroundResource(R.drawable.bg_chip);
                chipSeminar.setTextColor(getResources().getColor(R.color.white));
                chipSeminar.setTypeface(null, android.graphics.Typeface.BOLD);
            } else if ("Workshop".equalsIgnoreCase(selectedCategory) && chipWorkshop != null) {
                chipWorkshop.setBackgroundResource(R.drawable.bg_chip);
                chipWorkshop.setTextColor(getResources().getColor(R.color.white));
                chipWorkshop.setTypeface(null, android.graphics.Typeface.BOLD);
            }
        }
    }

    private void openEventDetail(Event event, ImageView imageView) {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập để xem chi tiết sự kiện", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getContext(), LoginActivity.class));
            return;
        }
        Intent intent = new Intent(getContext(), EventDetailActivity.class);
        intent.putExtra("EXTRA_EVENT_ID", event.getEventId());
        
        if (getActivity() != null && imageView != null) {
            String transitionName = androidx.core.view.ViewCompat.getTransitionName(imageView);
            intent.putExtra("EXTRA_TRANSITION_NAME", transitionName);
            
            androidx.core.app.ActivityOptionsCompat options = 
                androidx.core.app.ActivityOptionsCompat.makeSceneTransitionAnimation(
                    getActivity(), imageView, transitionName
                );
            startActivity(intent, options.toBundle());
        } else {
            startActivity(intent);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (getContext() != null && unreadUpdateReceiver != null) {
            getContext().unregisterReceiver(unreadUpdateReceiver);
        }
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    private void setupNotificationReceiver() {
        unreadUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateNotificationBadge();
            }
        };
        if (getContext() != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                getContext().registerReceiver(unreadUpdateReceiver, new IntentFilter(MyFirebaseMessagingService.ACTION_UPDATE_UNREAD), Context.RECEIVER_NOT_EXPORTED);
            } else {
                getContext().registerReceiver(unreadUpdateReceiver, new IntentFilter(MyFirebaseMessagingService.ACTION_UPDATE_UNREAD));
            }
        }
    }

    private void updateNotificationBadge() {
        if (getContext() == null || tvNotificationBadge == null) return;
        int count = LocalNotificationDbHelper.getInstance(getContext()).getUnreadCount();
        if (count > 0) {
            tvNotificationBadge.setVisibility(View.VISIBLE);
            tvNotificationBadge.setText(String.valueOf(count));
        } else {
            tvNotificationBadge.setVisibility(View.GONE);
        }
    }

    private void setupBookingObservers() {
        bookingViewModel.getBookingsState().observe(getViewLifecycleOwner(), bookings -> {
            if (bookings == null || bookings.isEmpty() || !isAdded()) {
                if (cardCountdownTimer != null) {
                    cardCountdownTimer.setVisibility(View.GONE);
                }
                return;
            }

            long now = System.currentTimeMillis() / 1000L;
            Event closestEvent = null;
            long minDiff = Long.MAX_VALUE;

            for (Booking booking : bookings) {
                if ("Cancelled".equalsIgnoreCase(booking.getStatus())) continue;
                
                for (Event event : fullEventList) {
                    if (event.getEventId().equals(booking.getEventId())) {
                        if (event.getDate() != null) {
                            long eventTime = event.getDate().getSeconds();
                            if (eventTime > now) {
                                long diff = eventTime - now;
                                if (diff < minDiff) {
                                    minDiff = diff;
                                    closestEvent = event;
                                }
                            }
                        }
                        break;
                    }
                }
            }

            if (closestEvent != null) {
                startCountdown(closestEvent);
                scheduleEventReminders();
            } else {
                if (cardCountdownTimer != null) {
                    cardCountdownTimer.setVisibility(View.GONE);
                }
                if (layoutFloatingEggContainer != null) {
                    layoutFloatingEggContainer.setVisibility(View.GONE);
                }
            }
        });
    }

    private void startCountdown(Event event) {
        if (cardCountdownTimer == null || tvCountdownEventTitle == null 
                || tvCountdownDays == null || tvCountdownHours == null 
                || tvCountdownMinutes == null || tvCountdownSeconds == null) return;

        tvCountdownEventTitle.setText(event.getTitle());
        if (!isCountdownCardDismissed) {
            cardCountdownTimer.setVisibility(View.VISIBLE);
        } else {
            cardCountdownTimer.setVisibility(View.GONE);
        }

        long targetMs = event.getDate().getSeconds() * 1000L;
        long currentMs = System.currentTimeMillis();
        long diffMs = targetMs - currentMs;

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        final long fortyEightHoursMs = 48 * 3600 * 1000L;

        if (diffMs <= 0) {
            cardCountdownTimer.setVisibility(View.GONE);
            if (layoutFloatingEggContainer != null) {
                layoutFloatingEggContainer.setVisibility(View.GONE);
            }
            return;
        }

        // Cập nhật trạng thái hiển thị của các layout và đồng hồ nổi dựa trên 48h ban đầu
        if (diffMs > fortyEightHoursMs) {
            // Hơn 48h: Hiện đếm ngày đơn giản, ẩn Bento Grid, ẩn đồng hồ nổi
            if (layoutSimpleDays != null) layoutSimpleDays.setVisibility(View.VISIBLE);
            if (layoutBentoCountdown != null) layoutBentoCountdown.setVisibility(View.GONE);
            if (layoutFloatingEggContainer != null) layoutFloatingEggContainer.setVisibility(View.GONE);

            long initialDays = diffMs / (24 * 3600 * 1000L);
            lastDisplayedDays = initialDays;
            if (tvCountdownDaysSimple != null) {
                tvCountdownDaysSimple.setText("Còn " + initialDays + " ngày nữa diễn ra");
            }
        } else {
            // Dưới 48h: Hiện Bento Grid, ẩn đếm ngày đơn giản, hiện đồng hồ nổi
            if (layoutSimpleDays != null) layoutSimpleDays.setVisibility(View.GONE);
            if (layoutBentoCountdown != null) layoutBentoCountdown.setVisibility(View.VISIBLE);
            if (layoutFloatingEggContainer != null && !isFloatingEggDismissed) {
                layoutFloatingEggContainer.setVisibility(View.VISIBLE);
            }
            lastDisplayedDays = -1;
        }

        countDownTimer = new CountDownTimer(diffMs, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (!isAdded()) return;
                long seconds = millisUntilFinished / 1000;
                long days = seconds / (24 * 3600);
                seconds %= (24 * 3600);
                long hours = seconds / 3600;
                seconds %= 3600;
                long minutes = seconds / 60;
                long secs = seconds % 60;

                if (millisUntilFinished > fortyEightHoursMs) {
                    // Trên 48 tiếng: Chỉ cập nhật UI khi số ngày thực sự thay đổi
                    if (lastDisplayedDays != days) {
                        lastDisplayedDays = days;
                        if (layoutSimpleDays != null && layoutSimpleDays.getVisibility() != View.VISIBLE) {
                            layoutSimpleDays.setVisibility(View.VISIBLE);
                        }
                        if (layoutBentoCountdown != null && layoutBentoCountdown.getVisibility() != View.GONE) {
                            layoutBentoCountdown.setVisibility(View.GONE);
                        }
                        if (layoutFloatingEggContainer != null && layoutFloatingEggContainer.getVisibility() != View.GONE) {
                            layoutFloatingEggContainer.setVisibility(View.GONE);
                        }
                        if (tvCountdownDaysSimple != null) {
                            tvCountdownDaysSimple.setText("Còn " + days + " ngày nữa diễn ra");
                        }
                    }
                } else {
                    // Dưới 48 tiếng: Khôi phục cờ, đổi layout hiển thị Bento Grid và đếm ngược từng giây
                    lastDisplayedDays = -1;
                    if (layoutSimpleDays != null && layoutSimpleDays.getVisibility() != View.GONE) {
                        layoutSimpleDays.setVisibility(View.GONE);
                    }
                    if (layoutBentoCountdown != null && layoutBentoCountdown.getVisibility() != View.VISIBLE) {
                        layoutBentoCountdown.setVisibility(View.VISIBLE);
                    }
                    if (layoutFloatingEggContainer != null && !isFloatingEggDismissed && layoutFloatingEggContainer.getVisibility() != View.VISIBLE) {
                        layoutFloatingEggContainer.setVisibility(View.VISIBLE);
                    }

                    // Cập nhật Bento Grid ở đầu trang
                    tvCountdownDays.setText(String.format(java.util.Locale.getDefault(), "%02d", days));
                    tvCountdownHours.setText(String.format(java.util.Locale.getDefault(), "%02d", hours));
                    tvCountdownMinutes.setText(String.format(java.util.Locale.getDefault(), "%02d", minutes));
                    tvCountdownSeconds.setText(String.format(java.util.Locale.getDefault(), "%02d", secs));

                    // Cập nhật đồng hồ đếm ngược nổi ở góc dưới bên phải dạng HH:MM:SS
                    if (tvFloatingCountdown != null) {
                        long totalHours = (days * 24) + hours;
                        tvFloatingCountdown.setText(String.format(java.util.Locale.getDefault(), "%02d:%02d:%02d", totalHours, minutes, secs));
                    }
                }
            }

            @Override
            public void onFinish() {
                if (isAdded()) {
                    cardCountdownTimer.setVisibility(View.GONE);
                    if (layoutFloatingEggContainer != null) {
                        layoutFloatingEggContainer.setVisibility(View.GONE);
                    }
                }
            }
        }.start();
    }

    private void scheduleEventReminders() {
        if (getContext() == null) return;
        AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getContext(), EventReminderReceiver.class);
        
        PendingIntent pendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getBroadcast(getContext(), 2002, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        } else {
            pendingIntent = PendingIntent.getBroadcast(getContext(), 2002, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        }

        if (alarmManager != null) {
            long interval = 2 * 60 * 60 * 1000L;
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + interval,
                    interval,
                    pendingIntent);
        }
    }
}
