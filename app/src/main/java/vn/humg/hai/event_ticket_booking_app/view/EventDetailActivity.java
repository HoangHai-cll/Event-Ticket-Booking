package vn.humg.hai.event_ticket_booking_app.view;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import vn.humg.hai.event_ticket_booking_app.R;
import vn.humg.hai.event_ticket_booking_app.controller.EventController;
import vn.humg.hai.event_ticket_booking_app.model.Event;

public class EventDetailActivity extends AppCompatActivity {
    public static final String EXTRA_EVENT_ID = "extra_event_id";

    private final EventController eventController = new EventController();
    private TextView tvEventTitle;
    private TextView tvEventDate;
    private TextView tvLocation;
    private TextView tvDescription;
    private TextView tvPrice;
    private TextView tvLike;
    private ImageView ivCover;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        ivCover = findViewById(R.id.iv_event_cover);
        TextView tvBack = findViewById(R.id.tv_back);
        tvEventTitle = findViewById(R.id.tv_event_title);
        tvEventDate = findViewById(R.id.tv_event_date);
        tvLocation = findViewById(R.id.tv_location);
        tvDescription = findViewById(R.id.tv_event_description);
        tvPrice = findViewById(R.id.tv_price);
        tvLike = findViewById(R.id.tv_like);
        MaterialButton btnBookNow = findViewById(R.id.btn_book_now);

        tvBack.setOnClickListener(v -> finish());
        tvLike.setOnClickListener(v -> tvLike.setTextColor(getResources().getColor(R.color.brand_primary)));
        btnBookNow.setOnClickListener(v -> Toast.makeText(this, "Chức năng đặt vé đang cập nhật", Toast.LENGTH_SHORT).show());

        String eventId = getIntent().getStringExtra(EXTRA_EVENT_ID);
        if (eventId != null && !eventId.isEmpty()) {
            eventController.getEventById(eventId, this::bindEvent, error -> {
                Toast.makeText(this, "Không tải được sự kiện: " + error, Toast.LENGTH_LONG).show();
                bindFallback();
            });
        } else {
            bindFallback();
        }
    }

    private void bindEvent(Event event) {
        runOnUiThread(() -> {
            if (event == null) {
                bindFallback();
                return;
            }
            tvEventTitle.setText(event.getTitle());
            tvEventDate.setText(formatTimestamp(event.getDate()));
            tvLocation.setText(event.getLocation());
            tvDescription.setText(event.getDescription());
            tvPrice.setText(formatPrice(event.getPrice()));
            if (event.getImage() != null && !event.getImage().isEmpty()) {
                // Keep default image for now; custom image loading can be added later.
            }
        });
    }

    private void bindFallback() {
        tvEventTitle.setText("Lễ Hội Âm Nhạc Horizon: Neon Nights 2024");
        tvEventDate.setText("20:00, Thứ 6, 15/12/2024");
        tvLocation.setText("Sân vận động Quốc gia Mỹ Đình, Hà Nội");
        tvDescription.setText("Hãy sẵn sàng cho một đêm không thể quên tại Horizon Music Festival! Neon Nights 2024. Sự kiện âm nhạc điện tử lớn nhất trong năm sẽ quy tụ những tài năng hàng đầu cùng hiệu ứng ánh sáng và sân khấu hoành tráng. Với hệ thống âm thanh đỉnh cao và sân khấu sống động, bạn sẽ được trải nghiệm những màn trình diễn không thể nào quên.");
        tvPrice.setText("850.000đ");
        ivCover.setImageResource(R.drawable.img_logo_event_ticket_booking);
    }

    private String formatTimestamp(Timestamp timestamp) {
        if (timestamp == null) {
            return "Thời gian chưa xác định";
        }
        Date date = timestamp.toDate();
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm, EEE, dd/MM/yyyy", Locale.getDefault());
        return formatter.format(date);
    }

    private String formatPrice(double price) {
        return String.format(Locale.getDefault(), "%,.0fđ", price);
    }
}
