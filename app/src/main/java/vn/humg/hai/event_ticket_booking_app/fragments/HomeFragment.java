package vn.humg.hai.event_ticket_booking_app.fragments;

import android.content.Intent;
import android.os.Bundle;
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
import vn.humg.hai.event_ticket_booking_app.model.Event;
import vn.humg.hai.event_ticket_booking_app.model.Favorite;
import vn.humg.hai.event_ticket_booking_app.view.EventDetailActivity;
import vn.humg.hai.event_ticket_booking_app.view.MainActivity;

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
    private TextView tvSeeAll, chipMusic, chipSeminar, chipWorkshop, tvHotTitle, tvAllTitle;
    private ImageView ivProfileTop;
    private TextView chipUpcoming, chipPast;
    private EditText etSearch;
    private ImageButton btnMenuDrawer;
    private View layoutSearch, layoutHotEvents, layoutCategories, layoutTimeFilters;
    
    private String selectedCategory = null;
    private boolean showUpcomingOnly = true;
    private boolean showFavoritesOnly = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        
        eventViewModel = new ViewModelProvider(this).get(EventViewModel.class);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        setupObservers();

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
                for (Event e : sortedEvents) {
                    boolean expired = e.getDate() != null && e.getDate().getSeconds() < now;
                    if (e.isHot() && !expired) hotEventList.add(e);
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
        ivProfileTop = view.findViewById(R.id.iv_profile_top);
        
        tvHotTitle = view.findViewById(R.id.tv_hot_events_title); 
        tvAllTitle = view.findViewById(R.id.tv_all_events_title);
        btnMenuDrawer = view.findViewById(R.id.btn_menu_drawer);
        
        layoutSearch = view.findViewById(R.id.layout_search);
        layoutHotEvents = view.findViewById(R.id.layout_hot_events);
        layoutCategories = view.findViewById(R.id.layout_categories);
        layoutTimeFilters = view.findViewById(R.id.layout_time_filters);
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

        tvSeeAll.setOnClickListener(v -> resetFilter());
        
        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    applyFilters();
                }
                @Override public void afterTextChanged(Editable s) {}
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
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            loadEvents();
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

            // Search query filter
            if (etSearch != null && etSearch.getText() != null) {
                String query = etSearch.getText().toString().toLowerCase().trim();
                if (!query.isEmpty() && !e.getTitle().toLowerCase().contains(query)) {
                    continue;
                }
            }

            displayList.add(e);
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

    private void openEventDetail(Event event) {
        Intent intent = new Intent(getContext(), EventDetailActivity.class);
        intent.putExtra("EXTRA_EVENT_ID", event.getEventId());
        startActivity(intent);
    }
}
