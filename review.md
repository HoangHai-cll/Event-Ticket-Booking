# Đánh giá Mã nguồn & Kiến trúc Tích hợp API - Event Ticket Booking App

Tài liệu này phân tích chi tiết về kiến trúc mã nguồn hiện tại của dự án Android `Event_Ticket_Booking_App` và đánh giá tính khả thi của việc tích hợp các API dịch vụ bên thứ ba (Thanh toán, Thông báo, Gửi mail/SMS).

---

## 1. Phân tích Kiến trúc & Luồng dữ liệu (Architecture & Data Flow)

Dự án được xây dựng theo mô hình kiến trúc **MVVM (Model-View-ViewModel)** kết hợp cấu trúc **Controller-Service** để tương tác với Firebase.

### Sơ đồ Luồng dữ liệu:

```mermaid
graph TD
    subgraph Tầng Giao diện (Views/UI)
        A[Activity / Fragment] -->|1. Lắng nghe trạng thái LiveData| B(ViewModel)
        A -->|2. Gọi các hàm xử lý hành động| B
    end

    subgraph Tầng Nghiệp vụ (ViewModels)
        B -->|3. Yêu cầu CRUD dữ liệu| C(Controller / Repository)
        C -->|4. Phản hồi kết quả dữ liệu/lỗi| B
    end

    subgraph Tầng Dữ liệu (Backend / Services)
        C -->|5. Đọc/Ghi trực tiếp| D[Firebase Firestore & Auth]
    end
    
    style A fill:#e1f5fe,stroke:#01579b,stroke-width:2px
    style B fill:#fff9c4,stroke:#fbc02d,stroke-width:2px
    style C fill:#e8f5e9,stroke:#2e7d32,stroke-width:2px
    style D fill:#ffebee,stroke:#c62828,stroke-width:2px
```

### Chi tiết ánh xạ giữa các thành phần:

1. **Authentication (Xác thực người dùng)**:
   - **Views**: [LoginActivity](file:///D:/Android_devpro/Android/Event_Ticket_Booking_App/app/src/main/java/vn/humg/hai/event_ticket_booking_app/view/LoginActivity.java) và [RegisterActivity](file:///D:/Android_devpro/Android/Event_Ticket_Booking_App/app/src/main/java/vn/humg/hai/event_ticket_booking_app/view/RegisterActivity.java).
   - **ViewModel**: [AuthViewModel](file:///D:/Android_devpro/Android/Event_Ticket_Booking_App/app/src/main/java/vn/humg/hai/event_ticket_booking_app/viewmodel/AuthViewModel.java).
   - **Controllers**: [AuthController](file:///D:/Android_devpro/Android/Event_Ticket_Booking_App/app/src/main/java/vn/humg/hai/event_ticket_booking_app/controller/AuthController.java) & [UserController](file:///D:/Android_devpro/Android/Event_Ticket_Booking_App/app/src/main/java/vn/humg/hai/event_ticket_booking_app/controller/UserController.java).
   - **Firestore**: Lưu trữ thông tin tài khoản ở collection `users`.

2. **Events (Sự kiện)**:
   - **Views**: [HomeFragment](file:///D:/Android_devpro/Android/Event_Ticket_Booking_App/app/src/main/java/vn/humg/hai/event_ticket_booking_app/fragments/HomeFragment.java) và [EventDetailActivity](file:///D:/Android_devpro/Android/Event_Ticket_Booking_App/app/src/main/java/vn/humg/hai/event_ticket_booking_app/view/EventDetailActivity.java).
   - **ViewModel**: [EventViewModel](file:///D:/Android_devpro/Android/Event_Ticket_Booking_App/app/src/main/java/vn/humg/hai/event_ticket_booking_app/viewmodel/EventViewModel.java).
   - **Controllers**: [EventController](file:///D:/Android_devpro/Android/Event_Ticket_Booking_App/app/src/main/java/vn/humg/hai/event_ticket_booking_app/controller/EventController.java).
   - **Firestore**: Lưu trữ thông tin sự kiện ở collection `events`.

3. **Bookings (Đặt vé & Lịch sử giao dịch)**:
   - **Views**: [SelectTicketActivity](file:///D:/Android_devpro/Android/Event_Ticket_Booking_App/app/src/main/java/vn/humg/hai/event_ticket_booking_app/view/SelectTicketActivity.java) và [PaymentActivity](file:///D:/Android_devpro/Android/Event_Ticket_Booking_App/app/src/main/java/vn/humg/hai/event_ticket_booking_app/view/PaymentActivity.java).
   - **ViewModel**: `BookingViewModel` & `EventViewModel`.
   - **Controllers**: `BookingController`.
   - **Firestore**: Lưu trữ ở collection `bookings`. Model `Booking` đã thiết kế sẵn các thuộc tính quan trọng phục vụ thanh toán trực tuyến:
     - `paymentMethod`: Phương thức thanh toán (Momo, VNPAY, Bank...).
     - `paymentId`: ID giao dịch từ bên thứ ba dùng để đối soát.
     - `status`: Trạng thái vé (`PENDING`, `PAID`, `CANCELLED`).

---

## 2. Đánh giá tính khả thi tích hợp API ngoài (External APIs Integration)

Việc tích hợp thêm các API ngoài vào ứng dụng là **Hoàn toàn khả thi** và có thể thực hiện một cách an toàn nhờ cấu trúc mã nguồn đã được module hóa.

### A. Tích hợp cổng thanh toán trực tuyến (Momo, VNPAY, ZaloPay)
- **Cơ chế Momo SDK (App-to-App)**:
  - Tích hợp thư viện Momo SDK vào dự án. Khi người dùng xác nhận thanh toán tại [PaymentActivity](file:///D:/Android_devpro/Android/Event_Ticket_Booking_App/app/src/main/java/vn/humg/hai/event_ticket_booking_app/view/PaymentActivity.java), gọi phương thức SDK để mở trực tiếp app Momo của khách hàng. Sau khi khách hàng hoàn tất giao dịch trên Momo, app Momo sẽ trả kết quả thanh toán về qua callback.
- **Cơ chế VNPAY (Payment URL qua WebView/Custom Chrome Tabs)**:
  - Gọi API từ Backend riêng hoặc Firebase Cloud Functions để gửi thông tin đơn hàng và nhận về một URL thanh toán bảo mật từ VNPAY.
  - Sử dụng Custom Chrome Tabs để mở trang thanh toán VNPAY trong ứng dụng. Sau khi thanh toán xong, trang web sẽ redirect về ứng dụng thông qua **Deep Link** (ví dụ: `eventbooking://payment_callback`), ứng dụng sẽ phân tích tham số trả về để cập nhật trạng thái đơn hàng.

### B. Tích hợp API SMS & Email (Twilio, SendGrid) để gửi vé điện tử (E-Ticket)
- **Quy trình tối ưu và an toàn**:
  - Không nên gọi trực tiếp API của Twilio hay SendGrid từ ứng dụng Android do nguy cơ bị dịch ngược mã nguồn (Decompile) làm lộ thông tin API Key.
  - **Khuyên dùng**: Sử dụng **Firebase Cloud Functions** (Node.js). Cloud Functions sẽ lắng nghe thay đổi của collection `bookings`. Khi trạng thái vé chuyển sang `status = "PAID"`, Cloud Function sẽ tự động kích hoạt:
    1. Sinh mã QR độc nhất chứa thông tin `bookingId`.
    2. Gọi API của Twilio (gửi SMS) hoặc SendGrid (gửi Email kèm file đính kèm chứa mã QR).

### C. Tích hợp Firebase Cloud Messaging (FCM) để gửi thông báo đẩy
- Tận dụng dịch vụ Firebase có sẵn trong dự án.
- Tạo một lớp kế thừa từ `FirebaseMessagingService` để nhận thông báo đẩy khi Admin duyệt vé, có sự kiện mới, hoặc khi cập nhật hạng thành viên của người dùng.

---

## 3. Nhật ký cải tiến chất lượng (Refactoring & Performance Logs)

Dưới đây là các phần cấu trúc đã được tối ưu hóa trực tiếp trong dự án để nâng cao chất lượng code và hiệu năng:

*   **Giảm Coupling & Tách biệt trách nhiệm (SRP)**:
    - Di chuyển toàn bộ logic tương tác Firestore (`FavoriteController`) và xác thực người dùng (`FirebaseAuth`) ra khỏi [EventAdapter](file:///D:/Android_devpro/Android/Event_Ticket_Booking_App/app/src/main/java/vn/humg/hai/event_ticket_booking_app/adapter/EventAdapter.java).
    - Adapter bây giờ chỉ giữ vai trò hiển thị và callback sự kiện click nút tim thông qua `OnFavoriteClickListener` về cho [HomeFragment](file:///D:/Android_devpro/Android/Event_Ticket_Booking_App/app/src/main/java/vn/humg/hai/event_ticket_booking_app/fragments/HomeFragment.java) điều phối.
*   **Loại bỏ Hardcode Màu sắc**:
    - Chuyển đổi mã màu tim cứng (`0xFFFF69B4`, `0xFFB0BEC5`) thành màu tài nguyên hệ thống cấu hình ở `colors.xml` (`color_favorite`, `color_favorite_inactive`).
*   **Giải quyết lỗi N+1 Query (Tối ưu số lượng request)**:
    - Viết mới hàm truy vấn hàng loạt bằng `whereIn` trong `EventController.java` (`getEventsByIds`).
    - Cập nhật [TicketsFragment](file:///D:/Android_devpro/Android/Event_Ticket_Booking_App/app/src/main/java/vn/humg/hai/event_ticket_booking_app/fragments/TicketsFragment.java) gọi truy vấn gộp này, giúp giảm số lượng kết nối mạng khi tải tab vé xuống còn **1 request duy nhất**.
*   **Chuẩn hóa Validation & Bảo mật đầu vào**:
    - Tạo tệp [ValidationUtils](file:///D:/Android_devpro/Android/Event_Ticket_Booking_App/app/src/main/java/vn/humg/hai/event_ticket_booking_app/utils/ValidationUtils.java) để thống nhất kiểm tra định dạng email và độ dài mật khẩu (áp dụng tại `LoginActivity` và `RegisterActivity`).
*   **Kích hoạt Offline Persistence (Truy cập ngoại tuyến)**:
    - Bật cờ `setPersistenceEnabled(true)` trong [SplashActivity](file:///D:/Android_devpro/Android/Event_Ticket_Booking_App/app/src/main/java/vn/humg/hai/event_ticket_booking_app/view/SplashActivity.java), đảm bảo người dùng có thể hiển thị mã QR check-in kể cả khi mất mạng tại sự kiện.
*   **Dọn dẹp Dependency & Hiện đại hóa Layout (Dependency Cleanup)**:
    - Loại bỏ hoàn toàn thư viện lỗi thời không còn được Google hỗ trợ `androidx.cardview:cardview:1.0.0` khỏi [build.gradle.kts](file:///D:/Android_devpro/Android/Event_Ticket_Booking_App/app/build.gradle.kts).
    - Thay thế toàn bộ các thẻ `<androidx.cardview.widget.CardView>` cũ thành `<com.google.android.material.card.MaterialCardView>` trong 5 tệp thiết kế giao diện: `item_event.xml`, `item_ticket.xml`, `item_transaction.xml`, `fragment_admin_profile.xml` và `activity_event_detail.xml` để tận dụng bo góc, stroke viền và đổ bóng chuẩn UI/UX của Google Material Design.

---

## 4. Kết luận & Đề xuất

| Loại dịch vụ | Giải pháp đề xuất | Mức độ khả thi | Ghi chú |
| :--- | :--- | :--- | :--- |
| **Thanh toán trực tuyến** | Momo SDK hoặc VNPAY WebView Redirect | **Rất cao** | Cần thiết lập Backend bảo mật để tính toán mã băm chữ ký (Signature). |
| **Gửi vé E-Ticket (Email/SMS)** | Firebase Cloud Functions + SendGrid/Twilio | **Rất cao** | Đảm bảo an toàn tuyệt đối cho API Key và giảm tải tác vụ cho Client Android. |
| **Thông báo đẩy (Notification)** | Firebase Cloud Messaging (FCM) | **Rất cao** | Có sẵn dịch vụ Firebase trong dự án, cấu hình nhanh chóng. |
