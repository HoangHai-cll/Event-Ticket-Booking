package vn.humg.hai.event_ticket_booking_app.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
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
import vn.humg.hai.event_ticket_booking_app.model.Event;

public class AdminManageEventsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AdminEventAdapter adapter;
    private final List<Event> eventList = new ArrayList<>();
    private final List<Event> allEvents = new ArrayList<>();
    private final EventController eventController = new EventController();
    private String currentAdminId;
    private String currentStatusFilter = "ALL";
    private String currentQuery = "";

    private TextView chipAll, chipActive, chipDrafts, chipCompleted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage_events);

        currentAdminId = FirebaseAuth.getInstance().getUid();

        initViews();
        setupRecyclerView();
        setupFilterChips();
        loadEvents();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar_admin_manage);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recycler_admin_manage_events);
        FloatingActionButton fabAdd = findViewById(R.id.fab_admin_add_event);
        fabAdd.setOnClickListener(v -> startActivity(new Intent(this, AdminAddEventActivity.class)));

        chipAll = findViewById(R.id.chip_filter_all);
        chipActive = findViewById(R.id.chip_filter_active);
        chipDrafts = findViewById(R.id.chip_filter_drafts);
        chipCompleted = findViewById(R.id.chip_filter_completed);
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
        int activeText = ContextCompat.getColor(this, R.color.white);
        int inactiveText = ContextCompat.getColor(this, R.color.ink_dark);

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
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminEventAdapter(eventList, new AdminEventAdapter.OnEventActionListener() {
            @Override
            public void onDelete(Event event) {
                showDeleteConfirmDialog(event);
            }

            @Override
            public void onEdit(Event event) {
                Intent intent = new Intent(AdminManageEventsActivity.this, AdminAddEventActivity.class);
                intent.putExtra("EDIT_EVENT_ID", event.getEventId());
                startActivity(intent);
            }

            @Override
            public void onViewStats(Event event) {
                String stats = "Tickets Sold: " + (event.getTotalTicket() - event.getRemainingTicket()) + "/" + event.getTotalTicket() + "\n"
                        + "Avg Rating: " + (event.getReviewCount() > 0 ? String.format(Locale.getDefault(), "%.1f⭐", event.getAverageRating()) : "N/A");
                
                new AlertDialog.Builder(AdminManageEventsActivity.this)
                        .setTitle(event.getTitle())
                        .setMessage(stats)
                        .setPositiveButton("Close", null)
                        .show();
            }

            @Override
            public void onShare(Event event) {
                Toast.makeText(AdminManageEventsActivity.this, "Chia sẻ sự kiện: " + event.getTitle(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onArchive(Event event) {
                Toast.makeText(AdminManageEventsActivity.this, "Đã lưu trữ sự kiện", Toast.LENGTH_SHORT).show();
            }
        });
        recyclerView.setAdapter(adapter);
    }

    private void loadEvents() {
        eventController.getAllEvents(events -> {
            runOnUiThread(() -> {
                allEvents.clear();
                for (Event e : events) {
                    if (currentAdminId != null && currentAdminId.equals(e.getCreatedByAdminId())) {
                        allEvents.add(e);
                    }
                }
                applyFilters();
            });
        }, error -> runOnUiThread(() -> Toast.makeText(this, "Error: " + error, Toast.LENGTH_SHORT).show()));
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_admin_search, menu);
        MenuItem searchItem = menu.findItem(R.id.action_admin_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint("Search events...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                currentQuery = newText;
                applyFilters();
                return true;
            }
        });
        return true;
    }

    private void showDeleteConfirmDialog(Event event) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Event")
                .setMessage("Are you sure you want to delete this event? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    eventController.deleteEvent(event.getEventId(), () -> {
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Event deleted", Toast.LENGTH_SHORT).show();
                            loadEvents();
                        });
                    }, error -> {
                        runOnUiThread(() -> Toast.makeText(this, "Error: " + error, Toast.LENGTH_SHORT).show());
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadEvents();
    }
}
