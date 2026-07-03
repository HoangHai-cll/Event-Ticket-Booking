package vn.humg.hai.event_ticket_booking_app.view;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import vn.humg.hai.event_ticket_booking_app.R;

public class HelpCenterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_center);

        Toolbar toolbar = findViewById(R.id.toolbar_help_center);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // Gán sự kiện cho các Card
        findViewById(R.id.btn_admin_manage_bookings_link).setOnClickListener(v -> 
            Toast.makeText(this, "Tính năng hỗ trợ đặt vé", Toast.LENGTH_SHORT).show());
    }
}
