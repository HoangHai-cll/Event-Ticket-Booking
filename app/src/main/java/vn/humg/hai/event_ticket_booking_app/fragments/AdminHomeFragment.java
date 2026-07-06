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
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import vn.humg.hai.event_ticket_booking_app.R;
import vn.humg.hai.event_ticket_booking_app.adapter.AdminEventAdapter;
import vn.humg.hai.event_ticket_booking_app.controller.EventController;
import vn.humg.hai.event_ticket_booking_app.controller.UserController;
import vn.humg.hai.event_ticket_booking_app.model.Event;
import vn.humg.hai.event_ticket_booking_app.view.AdminAddEventActivity;

public class AdminHomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private AdminEventAdapter adapter;
    private final List<Event> eventList = new ArrayList<>();
    private final List<Event> allEvents = new ArrayList<>();
    private final EventController eventController = new EventController();
    private final UserController userController = new UserController();
    private String currentAdminId;
    private String currentStatusFilter = "ALL";
    private String currentQuery = "";

    private TextView tvWelcome;
    private TextView chipAll, chipActive, chipDrafts, chipCompleted;
    private EditText etSearch;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_home, container, false);
        currentAdminId = FirebaseAuth.getInstance().getUid();

        initViews(view);
        setupRecyclerView();
        setupFilterChips();
        setupSearchListener();
        loadWelcomeMessage();
        loadEvents();
        return view;
    }

    private void initViews(View view) {
        tvWelcome = view.findViewById(R.id.tv_admin_home_welcome);
        recyclerView = view.findViewById(R.id.recycler_admin_home_events);
        FloatingActionButton fabAdd = view.findViewById(R.id.fab_admin_home_add);
        fabAdd.setOnClickListener(v -> startActivity(new Intent(getContext(), AdminAddEventActivity.class)));

        chipAll = view.findViewById(R.id.chip_filter_all);
        chipActive = view.findViewById(R.id.chip_filter_active);
        chipDrafts = view.findViewById(R.id.chip_filter_drafts);
        chipCompleted = view.findViewById(R.id.chip_filter_completed);
        etSearch = view.findViewById(R.id.et_admin_home_search);
    }

    private void setupFilterChips() {
        chipAll.setOnClickListener(v -> updateFilter("ALL"));
        chipActive.setOnClickListener(v -> updateFilter("ACTIVE"));
        chipDrafts.setOnClickListener(v -> updateFilter("DRAFT"));
        chipCompleted.setOnClickListener(v -> updateFilter("COMPLETED"));
    }

    private void updateFilter(String status) {
        currentStatusFilter = status;
        
        int activeBg = R.drawable.bg_chip;
        int inactiveBg = R.drawable.bg_chip_light;
        int activeText = ContextCompat.getColor(requireContext(), R.color.white);
        int inactiveText = ContextCompat.getColor(requireContext(), R.color.ink_dark);

        chipAll.setBackgroundResource(status.equals("ALL") ? activeBg : inactiveBg);
        chipAll.setTextColor(status.equals("ALL") ? activeText : inactiveText);

        chipActive.setBackgroundResource(status.equals("ACTIVE") ? activeBg : inactiveBg);
        chipActive.setTextColor(status.equals("ACTIVE") ? activeText : inactiveText);

        chipDrafts.setBackgroundResource(status.equals("DRAFT") ? activeBg : inactiveBg);
        chipDrafts.setTextColor(status.equals("DRAFT") ? activeText : inactiveText);

        chipCompleted.setBackgroundResource(status.equals("COMPLETED") ? activeBg : inactiveBg);
        chipCompleted.setTextColor(status.equals("COMPLETED") ? activeText : inactiveText);

        applyFilters();
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AdminEventAdapter(eventList, new AdminEventAdapter.OnEventActionListener() {
            @Override
            public void onDelete(Event event) {
                showDeleteConfirmDialog(event);
            }

            @Override
            public void onEdit(Event event) {
                Intent intent = new Intent(getContext(), AdminAddEventActivity.class);
                intent.putExtra("EDIT_EVENT_ID", event.getEventId());
                startActivity(intent);
            }

            @Override
            public void onViewStats(Event event) {
                int attended = event.getTotalTicket() - event.getRemainingTicket();
                String stats = "Vé đã bán: " + attended + "/" + event.getTotalTicket() + "\n"
                        + "Đánh giá trung bình: " + (event.getReviewCount() > 0 ? String.format(Locale.getDefault(), "%.1f ⭐", event.getAverageRating()) : "Chưa có đánh giá");
                
                new AlertDialog.Builder(requireContext())
                        .setTitle(event.getTitle())
                        .setMessage(stats)
                        .setPositiveButton("Đóng", null)
                        .show();
            }

            @Override
            public void onShare(Event event) {
                Toast.makeText(getContext(), "Chia sẻ sự kiện: " + event.getTitle(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onArchive(Event event) {
                Toast.makeText(getContext(), "Đã lưu trữ sự kiện", Toast.LENGTH_SHORT).show();
            }
        });
        recyclerView.setAdapter(adapter);
    }

    private void setupSearchListener() {
        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    currentQuery = s.toString();
                    applyFilters();
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void loadWelcomeMessage() {
        if (currentAdminId != null) {
            userController.getUserById(currentAdminId, user -> {
                if (user != null && isAdded()) {
                    getActivity().runOnUiThread(() -> tvWelcome.setText("Chào Admin, " + user.getFullName()));
                }
            }, e -> {});
        }
    }

    private void loadEvents() {
        eventController.getAllEvents(events -> {
            if (getActivity() == null || !isAdded()) return;
            getActivity().runOnUiThread(() -> {
                allEvents.clear();
                for (Event e : events) {
                    if (currentAdminId != null && currentAdminId.equals(e.getCreatedByAdminId())) {
                        allEvents.add(e);
                    }
                }
                applyFilters();
            });
        }, error -> {
            if (getActivity() == null || !isAdded()) return;
            getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Lỗi tải sự kiện: " + error, Toast.LENGTH_SHORT).show());
        });
    }

    private void applyFilters() {
        eventList.clear();
        long now = System.currentTimeMillis();

        for (Event event : allEvents) {
            boolean matchesSearch = currentQuery.isEmpty() || 
                    event.getTitle().toLowerCase().contains(currentQuery.toLowerCase());
            
            boolean matchesStatus = false;
            if (currentStatusFilter.equals("ALL")) {
                matchesStatus = true;
            } else if (currentStatusFilter.equals("ACTIVE")) {
                matchesStatus = event.getDate() != null && event.getDate().toDate().getTime() >= now;
            } else if (currentStatusFilter.equals("COMPLETED")) {
                matchesStatus = event.getDate() != null && event.getDate().toDate().getTime() < now;
            } else if (currentStatusFilter.equals("DRAFT")) {
                matchesStatus = event.getDate() == null;
            }

            if (matchesSearch && matchesStatus) {
                eventList.add(event);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void showDeleteConfirmDialog(Event event) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xóa sự kiện")
                .setMessage("Bạn có chắc chắn muốn xóa sự kiện này? Hành động này không thể hoàn tác.")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    eventController.deleteEvent(event.getEventId(), () -> {
                        if (getActivity() == null || !isAdded()) return;
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "Đã xóa sự kiện", Toast.LENGTH_SHORT).show();
                            loadEvents();
                        });
                    }, error -> {
                        if (getActivity() == null || !isAdded()) return;
                        getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show());
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
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
}
