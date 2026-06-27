package vn.humg.hai.event_ticket_booking_app.view;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;
import vn.humg.hai.event_ticket_booking_app.R;
import vn.humg.hai.event_ticket_booking_app.adapter.EventAdapter;
import vn.humg.hai.event_ticket_booking_app.controller.EventController;
import vn.humg.hai.event_ticket_booking_app.model.Event;

public class HomeActivity extends AppCompatActivity {
    private final EventController eventController = new EventController();
    private final List<Event> eventList = new ArrayList<>();
    private EventAdapter eventAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        RecyclerView recyclerEvents = findViewById(R.id.recycler_events);
        MaterialButton btnLogout = findViewById(R.id.btn_logout);

        recyclerEvents.setLayoutManager(new LinearLayoutManager(this));
        eventAdapter = new EventAdapter(eventList, event -> {
            Intent intent = new Intent(HomeActivity.this, EventDetailActivity.class);
            intent.putExtra(EventDetailActivity.EXTRA_EVENT_ID, event.getEventId());
            startActivity(intent);
        });
        recyclerEvents.setAdapter(eventAdapter);

        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        loadEvents();
    }

    private void loadEvents() {
        eventController.getAllEvents(events -> runOnUiThread(() -> {
            eventList.clear();
            eventList.addAll(events);
            eventAdapter.notifyDataSetChanged();
        }), error -> runOnUiThread(() -> {
            // TODO: show error state if needed.
        }));
    }
}
