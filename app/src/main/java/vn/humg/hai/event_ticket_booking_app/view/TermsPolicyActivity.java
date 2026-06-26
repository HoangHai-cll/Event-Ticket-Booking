package vn.humg.hai.event_ticket_booking_app.view;

import vn.humg.hai.event_ticket_booking_app.R;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import vn.humg.hai.event_ticket_booking_app.R;

public class TermsPolicyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms_policy);

        TextView tvTitle = findViewById(R.id.tv_terms_policy_title);
        TextView tvContent = findViewById(R.id.tv_terms_policy_content);

        String type = getIntent().getStringExtra("type");
        if ("policy" .equals(type)) {
            tvTitle.setText("Chính sách bảo mật");
            tvContent.setText("Chính sách bảo mật: Chúng tôi cam kết bảo vệ dữ liệu cá nhân của bạn. Thông tin thu thập bao gồm email, số điện thoại và dữ liệu đăng nhập để cung cấp dịch vụ sự kiện. Dữ liệu này chỉ được sử dụng cho việc xác thực, quản lý tài khoản và gửi thông báo liên quan đến sự kiện. Chúng tôi không chia sẻ thông tin cá nhân với bên thứ ba ngoài những đối tác cần thiết cho hoạt động ứng dụng, trừ khi có yêu cầu pháp lý.");
        } else {
            tvTitle.setText("Điều khoản sử dụng");
            tvContent.setText("Điều khoản sử dụng: Khi sử dụng EventPass, bạn đồng ý cung cấp thông tin chính xác và tuân thủ các quy định hiện hành. Bạn chịu trách nhiệm bảo mật thông tin đăng nhập của mình. Mọi hành vi lạm dụng, giả mạo hoặc đăng tải nội dung sai sự thật sẽ bị xử lý theo quy định. EventPass có quyền cập nhật điều khoản tại bất kỳ thời điểm nào; thay đổi sẽ được thông báo trong ứng dụng.");
        }
    }
}
