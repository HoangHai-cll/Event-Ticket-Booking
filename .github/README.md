🎟️ EventPass - Hệ Thống Đặt Vé & Quản Lý Sự Kiện Thông Minh
EventPass là một ứng dụng Android hiện đại, cung cấp giải pháp toàn diện cho việc đặt vé sự kiện, quản lý hóa đơn và quy trình check-in chuyên nghiệp thông qua mã QR. Với tông màu cam chủ đạo năng động, ứng dụng hướng tới trải nghiệm người dùng mượt mà và hệ thống quản trị mạnh mẽ cho ban tổ chức.
✨ Tính Năng Nổi Bật
📱 Đối Với Người Dùng (User)
•
Khám phá sự kiện: Tìm kiếm và xem thông tin chi tiết các sự kiện nổi bật, workshop, hội thảo.
•
Đặt vé nhanh chóng: Quy trình mua vé tối giản, hỗ trợ áp dụng Voucher giảm giá.
•
Hóa đơn chi tiết: Xem lại lịch sử thanh toán, chi tiết đơn giá, mã giao dịch và trạng thái vé.
•
Vé điện tử (QR Code): Nhận mã QR để vào cổng, tích hợp logic trì hoãn kích hoạt (chỉ hiển thị QR 24h trước khi sự kiện bắt đầu) để đảm bảo an toàn.
•
Hệ thống thành viên (Loyalty): Tích lũy điểm EXP khi tham gia sự kiện để thăng hạng (Đồng, Bạc, Vàng...) và nhận các đặc quyền riêng biệt.
•
Lịch sử giao dịch: Theo dõi chi tiết chi tiêu và quản lý yêu cầu hoàn tiền (Refund).
🛠️ Đối Với Quản Trị Viên (Admin)
•
Trình quét QR cao cấp: Giao diện quét mã tùy chỉnh với hiệu ứng Tia Laser và hỗ trợ đèn Flash trong môi trường thiếu sáng.
•
Check-in thông minh: Tự động nhận diện và hiển thị thông tin khách hàng + hóa đơn ngay khi quét để đối soát nhanh tại cổng.
•
Quản lý đơn hàng: Theo dõi tất cả giao dịch, phê duyệt hoàn tiền và quản lý số lượng vé theo thời gian thực.
•
Thống kê & Báo cáo: Xem biểu đồ chi tiêu và hiệu suất của các sự kiện.
🎨 Giao Diện Ứng Dụng
Trang chủ
Vé của tôi
Lịch sử
Hồ sơ thành viên
<img src="https://raw.githubusercontent.com/HoangHai-cll/Event-Ticket-Booking/main/screenshots/home.png" width="200">
<img src="https://raw.githubusercontent.com/HoangHai-cll/Event-Ticket-Booking/main/screenshots/tickets.png" width="200">
<img src="https://raw.githubusercontent.com/HoangHai-cll/Event-Ticket-Booking/main/screenshots/history.png" width="200">
<img src="https://raw.githubusercontent.com/HoangHai-cll/Event-Ticket-Booking/main/screenshots/profile.png" width="200">
🛠️ Công Nghệ Sử Dụng
•
Ngôn ngữ: Java (Android SDK).
•
Backend: Firebase (Authentication, Firestore, Storage, Remote Config).
•
Quét mã QR: ZXing Android Embedded.
•
Thư viện UI: Material Components, Glide (Load ảnh), Lottie (Animation).
•
Kiến trúc: MVC/MVVM sạch sẽ, mô-đun hóa các Controller để dễ dàng bảo trì.
🚀 Cài Đặt
1.
Clone repository:
Shell Script
git clone https://github.com/HoangHai-cll/Event-Ticket-Booking.git
2.
Mở project bằng Android Studio (Ladybug hoặc mới hơn).
3.
Kết nối với Firebase của riêng bạn (thêm file google-services.json vào thư mục app/).
4.
Bật các dịch vụ: Email Auth, Firestore Database, và Storage trên Firebase Console.
5.
Thiết lập Firebase Remote Config với key qr_release_threshold_hours (mặc định là 24).
6.
Build và chạy trên thiết bị Android (API 28+).
👤 Tác Giả
•
Hoàng Hải - GitHub Profile
