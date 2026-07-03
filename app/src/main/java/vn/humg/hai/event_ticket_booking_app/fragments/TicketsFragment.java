package vn.humg.hai.event_ticket_booking_app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import vn.humg.hai.event_ticket_booking_app.adapter.TicketAdapter;
import vn.humg.hai.event_ticket_booking_app.controller.BookingController;
import vn.humg.hai.event_ticket_booking_app.model.Booking;

public class TicketsFragment extends Fragment {

    private final BookingController bookingController = new BookingController();
    private final List<Booking> fullBookingList = new ArrayList<>();
    private final List<Booking> displayList = new ArrayList<>();
    private TicketAdapter ticketAdapter;
    
    private RecyclerView recyclerTickets;
    private TextView tvTicketCount;
    private TextView filterActive, filterUsed, filterCancelled;
    private String currentStatusFilter = "Confirmed";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tickets, container, false);
        initViews(view);
        setupRecyclerView();
        initEvents();
        loadMyTickets();
        return view;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            loadMyTickets(); // Tải lại dữ liệu khi tab được hiện lên
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadMyTickets(); // Tải lại dữ liệu khi quay lại từ Activity khác
    }

    private void initViews(View view) {
        recyclerTickets = view.findViewById(R.id.recycler_tickets);
        tvTicketCount = view.findViewById(R.id.tv_ticket_count);
        
        filterActive = view.findViewById(R.id.filter_active);
        filterUsed = view.findViewById(R.id.filter_used);
        filterCancelled = view.findViewById(R.id.filter_cancelled);
    }

    private void setupRecyclerView() {
        recyclerTickets.setLayoutManager(new LinearLayoutManager(getContext()));
        ticketAdapter = new TicketAdapter(displayList);
        recyclerTickets.setAdapter(ticketAdapter);
    }

    private void initEvents() {
        filterActive.setOnClickListener(v -> filterBookings("Confirmed"));
        filterUsed.setOnClickListener(v -> filterBookings("Used"));
        filterCancelled.setOnClickListener(v -> filterBookings("Cancelled"));
    }

    private void loadMyTickets() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) return;

        bookingController.getBookingsByUser(userId, bookings -> {
            if (!isAdded() || getActivity() == null) return;
            getActivity().runOnUiThread(() -> {
                fullBookingList.clear();
                fullBookingList.addAll(bookings);
                filterBookings(currentStatusFilter);
            });
        }, error -> {
            if (!isAdded() || getActivity() == null) return;
            getActivity().runOnUiThread(() -> 
                Toast.makeText(getContext(), getString(R.string.msg_load_error), Toast.LENGTH_SHORT).show()
            );
        });
    }

    private void filterBookings(String status) {
        currentStatusFilter = status;
        displayList.clear();
        for (Booking b : fullBookingList) {
            if (status.equalsIgnoreCase(b.getStatus())) {
                displayList.add(b);
            }
        }
        ticketAdapter.notifyDataSetChanged();
        updateCountText(status);
        updateFilterUI(status);
    }

    private void updateCountText(String status) {
        if (displayList.isEmpty()) {
            tvTicketCount.setText(getString(R.string.msg_no_tickets));
        } else {
            tvTicketCount.setText(getString(R.string.msg_ticket_count, displayList.size()));
        }
    }

    private void updateFilterUI(String activeStatus) {
        resetFilterStyle(filterActive);
        resetFilterStyle(filterUsed);
        resetFilterStyle(filterCancelled);

        TextView activeView;
        if (activeStatus.equals("Confirmed")) activeView = filterActive;
        else if (activeStatus.equals("Used")) activeView = filterUsed;
        else activeView = filterCancelled;

        activeView.setBackgroundResource(R.drawable.bg_chip);
        activeView.setTextColor(getResources().getColor(R.color.white));
    }

    private void resetFilterStyle(TextView view) {
        view.setBackgroundResource(R.drawable.bg_chip_light);
        view.setTextColor(getResources().getColor(R.color.ink_dark));
    }
}
