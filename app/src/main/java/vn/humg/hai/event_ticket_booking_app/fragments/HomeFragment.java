package vn.humg.hai.event_ticket_booking_app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import java.util.ArrayList;
import java.util.List;
import vn.humg.hai.event_ticket_booking_app.R;
import vn.humg.hai.event_ticket_booking_app.adapter.EventAdapter;
import vn.humg.hai.event_ticket_booking_app.controller.EventController;
import vn.humg.hai.event_ticket_booking_app.controller.UserController;
import vn.humg.hai.event_ticket_booking_app.model.Event;
import vn.humg.hai.event_ticket_booking_app.view.EventDetailActivity;
import vn.humg.hai.event_ticket_booking_app.view.MainActivity;

public class HomeFragment extends Fragment {
    private final EventController eventController = new EventController();
    private final UserController userController = new UserController();
    private final List<Event> fullEventList = new ArrayList<>();
    private final List<Event> hotEventList = new ArrayList<>();
    private final List<Event> displayList = new ArrayList<>();
    
    private EventAdapter eventAdapter;
    private EventAdapter hotEventAdapter;
    
    private RecyclerView recyclerEvents, recyclerHotEvents;
    private TextView tvSeeAll, chipMusic, chipSeminar, chipWorkshop, ivProfileTop, tvHotTitle, tvAllTitle;
    private EditText etSearch;
    private View layoutSearch, layoutHotEvents, layoutCategories;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        initViews(view);
        setupRecyclerView();
        initEvents();
        checkUserRoleAndAdjustUI();
        loadEvents();
        return view;
    }

    private void initViews(View view) {
        recyclerEvents = view.findViewById(R.id.recycler_events);
        recyclerHotEvents = view.findViewById(R.id.recycler_hot_events);
        tvSeeAll = view.findViewById(R.id.tv_see_all);
        chipMusic = view.findViewById(R.id.chip_music);
        chipSeminar = view.findViewById(R.id.chip_seminar);
        chipWorkshop = view.findViewById(R.id.chip_workshop);
        etSearch = view.findViewById(R.id.et_search);
        ivProfileTop = view.findViewById(R.id.iv_profile_top);
        
        tvHotTitle = view.findViewById(R.id.tv_hot_events_title); 
        tvAllTitle = view.findViewById(R.id.tv_all_events_title);
        
        layoutSearch = view.findViewById(R.id.layout_search);
        layoutHotEvents = view.findViewById(R.id.layout_hot_events);
        layoutCategories = view.findViewById(R.id.layout_categories);
    }

    private void checkUserRoleAndAdjustUI() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        userController.getUserById(uid, user -> {
            if (user != null && "admin".equalsIgnoreCase(user.getRole()) && isAdded()) {
                getActivity().runOnUiThread(() -> {
                    // Nếu là Admin: Chỉ hiện bảng sự kiện và profile
                    if (layoutSearch != null) layoutSearch.setVisibility(View.GONE);
                    if (layoutHotEvents != null) layoutHotEvents.setVisibility(View.GONE);
                    if (layoutCategories != null) layoutCategories.setVisibility(View.GONE);
                    
                    if (tvAllTitle != null) {
                        tvAllTitle.setText("Tất cả sự kiện hệ thống");
                        tvAllTitle.setTextSize(22);
                    }
                });
            }
        }, e -> {});
    }

    private void setupRecyclerView() {
        recyclerHotEvents.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        hotEventAdapter = new EventAdapter(hotEventList, true, this::openEventDetail);
        recyclerHotEvents.setAdapter(hotEventAdapter);

        recyclerEvents.setLayoutManager(new LinearLayoutManager(getContext()));
        eventAdapter = new EventAdapter(displayList, false, this::openEventDetail);
        recyclerEvents.setAdapter(eventAdapter);
    }

    private void initEvents() {
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
                    performSearch(s.toString());
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadEvents();
    }

    private void performSearch(String query) {
        displayList.clear();
        if (query.isEmpty()) {
            displayList.addAll(fullEventList);
        } else {
            String q = query.toLowerCase().trim();
            for (Event e : fullEventList) {
                if (e.getTitle().toLowerCase().contains(q)) {
                    displayList.add(e);
                }
            }
        }
        eventAdapter.notifyDataSetChanged();
    }

    private void filterEvents(String category) {
        displayList.clear();
        for (Event e : fullEventList) {
            if (category.equalsIgnoreCase(e.getCategory())) {
                displayList.add(e);
            }
        }
        eventAdapter.notifyDataSetChanged();
    }

    private void resetFilter() {
        if (etSearch != null) etSearch.setText("");
        displayList.clear();
        displayList.addAll(fullEventList);
        eventAdapter.notifyDataSetChanged();
    }

    private void loadEvents() {
        eventController.getAllEvents(events -> {
            if (getActivity() == null || !isAdded()) return;
            getActivity().runOnUiThread(() -> {
                fullEventList.clear();
                fullEventList.addAll(events);
                
                hotEventList.clear();
                for (Event e : events) {
                    if (e.isHot()) hotEventList.add(e);
                }

                displayList.clear();
                displayList.addAll(events);

                hotEventAdapter.notifyDataSetChanged();
                eventAdapter.notifyDataSetChanged();
            });
        }, error -> {});
    }

    private void openEventDetail(Event event) {
        Intent intent = new Intent(getContext(), EventDetailActivity.class);
        intent.putExtra("EXTRA_EVENT_ID", event.getEventId());
        startActivity(intent);
    }
}
