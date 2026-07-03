package vn.humg.hai.event_ticket_booking_app.view;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import vn.humg.hai.event_ticket_booking_app.R;
import vn.humg.hai.event_ticket_booking_app.adapter.UserAdapter;
import vn.humg.hai.event_ticket_booking_app.controller.UserController;
import vn.humg.hai.event_ticket_booking_app.model.User;

public class AdminManageUsersActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private final List<User> userList = new ArrayList<>();
    private final UserController userController = new UserController();
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage_users);

        initViews();
        setupRecyclerView();
        loadAllUsers();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar_admin_users);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recycler_admin_users);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserAdapter(userList);
        recyclerView.setAdapter(adapter);
    }

    private void loadAllUsers() {
        userController.getAllUsers(users -> {
            runOnUiThread(() -> {
                userList.clear();
                userList.addAll(users);
                adapter.notifyDataSetChanged();
            });
        }, error -> {
            runOnUiThread(() -> Toast.makeText(this, "Lỗi: " + error, Toast.LENGTH_SHORT).show());
        });
    }
}
