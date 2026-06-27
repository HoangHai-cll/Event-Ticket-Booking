package vn.humg.hai.event_ticket_booking_app.view;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import vn.humg.hai.event_ticket_booking_app.R;

public class EventDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        ImageView ivCover = findViewById(R.id.iv_event_cover);
        TextView tvBack = findViewById(R.id.tv_back);
        TextView tvEventTitle = findViewById(R.id.tv_event_title);
        TextView tvEventDate = findViewById(R.id.tv_event_date);
        TextView tvLocation = findViewById(R.id.tv_location);
        TextView tvDescription = findViewById(R.id.tv_event_description);
        TextView tvPrice = findViewById(R.id.tv_price);
        TextView tvLike = findViewById(R.id.tv_like);
        MaterialButton btnBookNow = findViewById(R.id.btn_book_now);

        tvBack.setOnClickListener(v -> finish());
        tvLike.setOnClickListener(v -> tvLike.setTextColor(getResources().getColor(R.color.brand_primary)));
        btnBookNow.setOnClickListener(v -> Toast.makeText(this, "Chức năng đặt vé đang cập nhật", Toast.LENGTH_SHORT).show());

        tvEventTitle.setText("Lễ Hội Âm Nhạc Horizon: Neon Nights 2024");
        tvEventDate.setText("20:00, Thứ 6, 15/12/2024");
        tvLocation.setText("Sân vận động Quốc gia Mỹ Đình, Hà Nội");
        tvDescription.setText("Hãy sẵn sàng cho một đêm không thể quên tại Horizon Music Festival! Neon Nights 2024. Sự kiện âm nhạc điện tử lớn nhất trong năm sẽ quy tụ những tài năng hàng đầu cùng hiệu ứng ánh sáng và sân khấu hoành tráng. Với hệ thống âm thanh đỉnh cao và sân khấu sống động, bạn sẽ được trải nghiệm những màn trình diễn không thể nào quên.");
        tvPrice.setText("850.000đ");
        ivCover.setImageResource(R.drawable.img_logo_event_ticket_booking);
    }
}
