# 🎟️ EventPass - Smart Event Ticketing & Management System

[Tiếng Việt](#tiếng-việt) | [English](#english)

---

## Tiếng Việt

**EventPass** là một ứng dụng Android hiện đại cung cấp giải pháp toàn diện cho việc đặt vé sự kiện, quản lý hóa đơn và quy trình check-in chuyên nghiệp bằng mã QR. Với bộ nhận diện thương hiệu màu cam năng động, ứng dụng tập trung vào trải nghiệm người dùng mượt mà và hệ thống kiểm soát quản trị mạnh mẽ.

### ✨ Các Tính Năng Chính

#### 📱 Dành cho Người dùng
*   **Khám phá sự kiện:** Duyệt và tìm kiếm các sự kiện nổi bật, workshop và hội thảo với hình ảnh chất lượng cao.
*   **Đặt vé nhanh chóng:** Quy trình mua vé tối giản tích hợp hỗ trợ Voucher giảm giá.
*   **Hóa đơn chi tiết:** Xem đầy đủ thông tin thanh toán, bao gồm đơn giá, giảm giá, mã giao dịch và phương thức thanh toán.
*   **Vé điện tử (QR Code):** Vào cổng an toàn với mã QR tích hợp **Logic Trì hoãn Kích hoạt** (Mã QR tự động hiển thị 24 giờ trước khi sự kiện bắt đầu).
*   **Hệ thống Thành viên (EXP):** Tích lũy điểm EXP cho mỗi sự kiện tham gia để thăng hạng (Đồng, Bạc, Vàng...) và mở khóa các phần thưởng độc quyền.
*   **Lịch sử giao dịch:** Theo dõi chi tiêu và quản lý yêu cầu hoàn tiền dễ dàng.

#### 🛠️ Dành cho Quản trị viên
*   **Trình quét QR Cao cấp:** Giao diện quét mã tùy chỉnh hiệu suất cao với **Hiệu ứng Laser** và hỗ trợ đèn flash.
*   **Check-in Tức thì:** Truy xuất dữ liệu thời gian thực hiển thị hồ sơ khách hàng và tình trạng hóa đơn ngay khi quét.
*   **Quản lý Đơn hàng:** Theo dõi tất cả giao dịch, phê duyệt/từ chối hoàn tiền và quản lý kho vé thời gian thực.
*   **Thống kê:** Xem phân tích chi tiêu và báo cáo hiệu suất sự kiện.

### 🎨 Ảnh Chụp Giao Diện (Screenshots)

| Trang chủ | Vé của tôi | Lịch sử giao dịch | Hồ sơ & Thành viên |
|:---:|:---:|:---:|:---:|
| <img src="screenshots/home.png" width="200"> | <img src="screenshots/tickets.png" width="200"> | <img src="screenshots/history.png" width="200"> | <img src="screenshots/profile.png" width="200"> |

> *Lưu ý: Vui lòng lưu các ảnh chụp màn hình vào thư mục `/screenshots` trong repository để hiển thị.*

### 🛠️ Công Nghệ Sử Dụng
*   **Ngôn ngữ:** Java (Android SDK)
*   **Backend:** Google Firebase (Authentication, Firestore, Storage, Remote Config)
*   **Quét mã:** ZXing Android Embedded (Customized)
*   **UI/UX:** Material Components 3, Glide, Custom Vector Graphics

---

## English

**EventPass** is a modern Android application providing a comprehensive solution for event ticket booking, invoice management, and professional QR-code-based check-in. Featuring a vibrant orange brand identity, the app focuses on seamless user experiences and robust administrative controls.

### ✨ Key Features

#### 📱 For Users
*   **Event Discovery:** Browse and search for featured events, workshops, and seminars with high-quality visuals.
*   **Rapid Booking:** Streamlined ticket purchase flow with integrated Voucher support.
*   **Detailed Invoices:** Review complete billing details, including unit prices, discounts, transaction IDs, and payment methods.
*   **Digital Tickets (QR Code):** Secure QR entry with **Delayed Release logic** (QR codes activate automatically 24h before the event).
*   **Loyalty System (EXP):** Earn EXP points for every event attended to level up (Bronze, Silver, Gold, etc.) and unlock exclusive rewards.
*   **Transaction History:** Monitor spending and manage refund requests easily.

### 🛠️ For Administrators
*   **Premium QR Scanner:** Custom high-performance scanning interface with **Laser animation** and flashlight support.
*   **Instant Check-in:** Real-time data retrieval showing customer profiles and invoice status immediately upon scanning.
*   **Order Management:** Track all transactions, approve/reject refunds, and manage ticket stock in real-time.
*   **Statistics:** View spending analytics and event performance reports.

---

## 🚀 Bắt đầu / Getting Started

1.  **Clone repository:**
    ```bash
    git clone https://github.com/HoangHai-cll/Event-Ticket-Booking.git
    ```
2.  **Mở trong Android Studio / Open in Android Studio:** Recommended version **Ladybug 2024.2.1** or newer.
3.  **Firebase Setup:**
    *   Thêm / Add `google-services.json` vào thư mục `app/`.
    *   Bật / Enable: Email Auth, Firestore, and Storage.
4.  **Remote Config:** Thêm tham số / Add parameter `qr_release_threshold_hours` (Default: `24`).

---

## 👤 Tác giả / Author
*   **Hoàng Hải** - [HoangHai-cll](https://github.com/HoangHai-cll)
