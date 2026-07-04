package vn.humg.hai.event_ticket_booking_app.view;

import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import vn.humg.hai.event_ticket_booking_app.R;

public class TermsPolicyActivity extends AppCompatActivity {

    private TextView tvTitle, tvContent;
    private TextView tabGeneralTerms, tabBookingPolicy, tabRefundPolicy;
    private CheckBox cbAcceptTerms;
    private MaterialButton btnConfirmTerms;

    private static final String TEXT_GENERAL_TERMS_TITLE = "⚖️ 1. Điều khoản chung";
    private static final String TEXT_GENERAL_TERMS_CONTENT = "Bằng cách truy cập hoặc sử dụng ứng dụng EventPass, bạn đồng ý chịu sự ràng buộc bởi các Điều khoản và Điều kiện này. Nền tảng của chúng tôi đóng vai trò là thị trường cho các nhà tổ chức sự kiện và người tham dự.\n\nNgười dùng phải ít nhất 18 tuổi hoặc có sự đồng ý của cha mẹ để sử dụng dịch vụ. Bạn chịu trách nhiệm bảo mật thông tin đăng nhập tài khoản của mình.\n\nChúng tôi có quyền sửa đổi các điều khoản này bất cứ lúc nào. Việc tiếp tục sử dụng nền tảng đồng nghĩa với việc chấp nhận các điều khoản đã cập nhật.\n\nChúng tôi cam kết bảo vệ dữ liệu cá nhân và quyền riêng tư của bạn theo các tiêu chuẩn quốc tế cao nhất. Mọi giao dịch tài chính trên ứng dụng đều được mã hóa và xử lý thông qua các cổng thanh toán uy tín để đảm bảo an toàn tuyệt đối cho người dùng.";

    private static final String TEXT_BOOKING_POLICY_TITLE = "🗓️ 2. Chính sách đặt chỗ";
    private static final String TEXT_BOOKING_POLICY_CONTENT = "Tất cả các giao dịch đặt vé sự kiện đều phải được thực hiện thông qua ứng dụng chính thức EventPass.\n\nSố lượng vé mỗi tài khoản được mua tối đa đối với một số sự kiện hot có thể bị giới hạn để tránh đầu cơ. Sau khi thanh toán thành công, mã QR Code điện tử sẽ được sinh ra và gửi trực tiếp về ví vé của bạn.\n\nMã QR Code này là duy nhất và dùng để quét check-in khi vào cổng sự kiện. Vui lòng không chia sẻ mã QR Code của bạn cho bất kỳ ai để tránh tình trạng giả mạo hoặc mất quyền tham dự.";

    private static final String TEXT_REFUND_POLICY_TITLE = "💵 3. Chính sách hoàn tiền";
    private static final String TEXT_REFUND_POLICY_CONTENT = "Yêu cầu hoàn trả tiền vé chỉ được chấp nhận đối với các sự kiện có bật tính năng hoàn vé tự động từ Ban tổ chức.\n\nBạn cần gửi yêu cầu hoàn tiền thông qua chi tiết vé tối thiểu trước 24 giờ trước khi sự kiện diễn ra. Các yêu cầu gửi muộn hơn sẽ không được giải quyết.\n\nTrong trường hợp sự kiện bị hủy hoặc thay đổi lịch trình từ phía Ban tổ chức, bạn sẽ được tự động hoàn 100% giá trị vé về tài khoản đã thanh toán ban đầu trong vòng 3-5 ngày làm việc.";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms_policy);

        initViews();
        setupEvents();
        
        // Mặc định hiển thị tab đầu tiên
        selectTab(1);
    }

    private void initViews() {
        findViewById(R.id.btn_back_custom).setOnClickListener(v -> finish());

        tvTitle = findViewById(R.id.tv_terms_policy_title);
        tvContent = findViewById(R.id.tv_terms_policy_content);

        tabGeneralTerms = findViewById(R.id.tab_general_terms);
        tabBookingPolicy = findViewById(R.id.tab_booking_policy);
        tabRefundPolicy = findViewById(R.id.tab_refund_policy);

        cbAcceptTerms = findViewById(R.id.cb_accept_terms);
        btnConfirmTerms = findViewById(R.id.btn_confirm_terms);

        // Mặc định khóa nút xác nhận
        btnConfirmTerms.setEnabled(false);
    }

    private void setupEvents() {
        tabGeneralTerms.setOnClickListener(v -> selectTab(1));
        tabBookingPolicy.setOnClickListener(v -> selectTab(2));
        tabRefundPolicy.setOnClickListener(v -> selectTab(3));

        cbAcceptTerms.setOnCheckedChangeListener((buttonView, isChecked) -> {
            btnConfirmTerms.setEnabled(isChecked);
        });

        btnConfirmTerms.setOnClickListener(v -> {
            Toast.makeText(this, "Cảm ơn bạn đã đồng ý với các điều khoản của EventPass", Toast.LENGTH_LONG).show();
            finish();
        });
    }

    private void selectTab(int tabIndex) {
        // Reset tab UI
        tabGeneralTerms.setBackgroundResource(R.drawable.bg_chip_light);
        tabGeneralTerms.setTextColor(getResources().getColor(R.color.ink_dark));
        tabGeneralTerms.setTypeface(null, android.graphics.Typeface.NORMAL);

        tabBookingPolicy.setBackgroundResource(R.drawable.bg_chip_light);
        tabBookingPolicy.setTextColor(getResources().getColor(R.color.ink_dark));
        tabBookingPolicy.setTypeface(null, android.graphics.Typeface.NORMAL);

        tabRefundPolicy.setBackgroundResource(R.drawable.bg_chip_light);
        tabRefundPolicy.setTextColor(getResources().getColor(R.color.ink_dark));
        tabRefundPolicy.setTypeface(null, android.graphics.Typeface.NORMAL);

        // Set selected tab UI and content
        if (tabIndex == 1) {
            tabGeneralTerms.setBackgroundResource(R.drawable.bg_chip);
            tabGeneralTerms.setTextColor(getResources().getColor(R.color.white));
            tabGeneralTerms.setTypeface(null, android.graphics.Typeface.BOLD);
            tvTitle.setText(TEXT_GENERAL_TERMS_TITLE);
            tvContent.setText(TEXT_GENERAL_TERMS_CONTENT);
        } else if (tabIndex == 2) {
            tabBookingPolicy.setBackgroundResource(R.drawable.bg_chip);
            tabBookingPolicy.setTextColor(getResources().getColor(R.color.white));
            tabBookingPolicy.setTypeface(null, android.graphics.Typeface.BOLD);
            tvTitle.setText(TEXT_BOOKING_POLICY_TITLE);
            tvContent.setText(TEXT_BOOKING_POLICY_CONTENT);
        } else if (tabIndex == 3) {
            tabRefundPolicy.setBackgroundResource(R.drawable.bg_chip);
            tabRefundPolicy.setTextColor(getResources().getColor(R.color.white));
            tabRefundPolicy.setTypeface(null, android.graphics.Typeface.BOLD);
            tvTitle.setText(TEXT_REFUND_POLICY_TITLE);
            tvContent.setText(TEXT_REFUND_POLICY_CONTENT);
        }
    }
}
