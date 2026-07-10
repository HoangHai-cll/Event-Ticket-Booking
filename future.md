# Định hướng Phát triển & Tối ưu hóa - Event Ticket Booking App

Tài liệu này vạch ra các định hướng phát triển, cải tiến hiệu năng và nâng cao tính bảo mật cho ứng dụng `Event_Ticket_Booking_App` trong tương lai để đạt chuẩn thương mại (Production-ready).

---

## 1. Lưu trữ Lịch sử sử dụng Voucher (Voucher History)

- **Hiện trạng**: Khi áp dụng voucher thành công, hệ thống xóa tệp tin voucher khỏi sub-collection `users/{userId}/user_vouchers`. Cách này làm mất lịch sử voucher đã dùng của khách hàng và chỉ lưu gián tiếp mã code trong hóa đơn đặt vé (`Booking`).
- **Định hướng tối ưu**:
  - Thay đổi cấu trúc Model [Voucher](file:///D:/Android_devpro/Android/Event_Ticket_Booking_App/app/src/main/java/vn/humg/hai/event_ticket_booking_app/model/Voucher.java): Thêm các trường `isUsed` (boolean) và `usedAt` (Timestamp).
  - Khi thanh toán thành công, thay vì gọi `removeUserVoucher`, thực hiện cập nhật tài liệu voucher:
    ```java
    voucher.setUsed(true);
    voucher.setUsedAt(Timestamp.now());
    ```
  - Cập nhật giao diện ví Voucher để chia thành 2 Tab: **Voucher khả dụng** (lọc `isUsed == false` và chưa hết hạn) và **Lịch sử Voucher** (lọc `isUsed == true` hoặc đã hết hạn).

---

## 2. Phân trang Truy vấn Firestore (Database Pagination)

- **Hiện trạng**: Phương thức `getAllEvents()` trong [EventController.java](file:///D:/Android_devpro/Android/Event_Ticket_Booking_App/app/src/main/java/vn/humg/hai/event_ticket_booking_app/controller/EventController.java) tải toàn bộ sự kiện hiện có từ Firestore. Khi lượng sự kiện lớn, truy vấn này sẽ làm tăng chi phí vận hành Firestore và gây lag giao diện của thiết bị.
- **Định hướng tối ưu**:
  - Sử dụng phân trang kết hợp cơ chế cuộn của RecyclerView (Lazy Loading):
    ```java
    // Truy vấn trang đầu tiên
    Query firstPage = firestore.collection("events")
                               .orderBy("date", Query.Direction.ASCENDING)
                               .limit(10);
    ```
  - Khi người dùng cuộn đến cuối danh sách, lấy tài liệu cuối cùng của trang trước (`lastVisibleDocument`) và thực hiện tải trang tiếp theo:
    ```java
    Query nextPage = firestore.collection("events")
                              .orderBy("date", Query.Direction.ASCENDING)
                              .startAfter(lastVisibleDocument)
                              .limit(10);
    ```

---

## 3. Thiết lập Firebase Security Rules (Bảo mật Cơ sở dữ liệu)

- **Hiện trạng**: Toàn bộ thao tác CRUD dữ liệu được thực hiện trực tiếp từ client. Cần thiết lập Rules để ngăn chặn người dùng chỉnh sửa dữ liệu trái phép.
- **Định hướng tối ưu**:
  - Định nghĩa tệp `firestore.rules` trên Firebase Console:
    ```javascript
    rules_version = '2';
    service cloud.firestore {
      match /databases/{database}/documents {
        
        // Quy tắc cho Users
        match /users/{userId} {
          allow read: if request.auth != null;
          // Người dùng chỉ được sửa thông tin cá nhân của họ, không được tự sửa Role
          allow write: if request.auth != null && request.auth.uid == userId && request.resource.data.role == resource.data.role;
          // Chỉ Admin mới được sửa mọi thông tin người dùng
          allow write: if request.auth != null && get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == "admin";
        }
        
        // Quy tắc cho Events
        match /events/{eventId} {
          allow read: if true; // Ai cũng được xem sự kiện
          // Chỉ Admin mới được thêm/sửa/xóa sự kiện
          allow write: if request.auth != null && get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == "admin";
        }
        
        // Quy tắc cho Bookings
        match /bookings/{bookingId} {
          // Người dùng chỉ được xem vé của mình
          allow read: if request.auth != null && (resource.data.userId == request.auth.uid || get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == "admin");
          // Chỉ cho phép tạo mới nếu đúng uid đăng nhập
          allow create: if request.auth != null && request.resource.data.userId == request.auth.uid;
          // Chỉ Admin mới được sửa trạng thái đơn hàng (Duyệt vé, check-in)
          allow update, delete: if request.auth != null && get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == "admin";
        }
      }
    }
    ```

---

## 4. Chuẩn hóa Tài nguyên Ngôn ngữ (Localization)

- **Hiện trạng**: Các chuỗi hiển thị (như Text hiển thị, Toast thông báo lỗi) đang bị hardcode tiếng Việt trong các file mã nguồn.
- **Định hướng tối ưu**:
  - Chuyển toàn bộ chuỗi hardcode sang tệp tài nguyên hệ thống `app/src/main/res/values/strings.xml`:
    ```xml
    <string name="error_empty_fields">Vui lòng nhập đầy đủ email và mật khẩu.</string>
    <string name="error_invalid_email">Định dạng Email không hợp lệ.</string>
    <string name="success_favorite_added">Đã thích sự kiện.</string>
    ```
  - Khi cần gọi hiển thị:
    ```java
    Toast.makeText(context, context.getString(R.string.error_invalid_email), Toast.LENGTH_SHORT).show();
    ```
  - Thiết lập thêm tệp `values-en/strings.xml` dịch các nhãn sang Tiếng Anh để ứng dụng tự động thay đổi ngôn ngữ theo cấu hình hệ điều hành của người dùng.

---

## 5. Offline Persistence & Local Caching [ĐÃ KÍCH HOẠT PERSISTENCE ✓]

- **Hiện trạng**: 
  - **Đã xử lý**: Kích hoạt cờ lưu trữ ngoại tuyến `setPersistenceEnabled(true)` của Firestore trong [SplashActivity.java](file:///D:/Android_devpro/Android/Event_Ticket_Booking_App/app/src/main/java/vn/humg/hai/event_ticket_booking_app/view/SplashActivity.java). Hệ thống hiện đã tự động cache vé đã tải về bộ nhớ cục bộ của thiết bị.
  - **Vấn đề còn lại**: Bộ nhớ đệm RAM `eventCache` trong [TicketsFragment](file:///D:/Android_devpro/Android/Event_Ticket_Booking_App/app/src/main/java/vn/humg/hai/event_ticket_booking_app/fragments/TicketsFragment.java) vẫn bị xóa khi đóng app hoàn toàn.
- **Định hướng tối ưu tiếp theo**:
  - **Tích hợp Room Database**: Thiết lập database SQLite cục bộ bằng Room để đồng bộ và lưu trữ lâu dài danh sách vé và thông tin sự kiện đi kèm, giúp ứng dụng có thể mở hiển thị QR code ngoại tuyến tuyệt đối không phụ thuộc vào cache tạm của Firestore.

---

## 6. Bảo mật mã QR Code Check-in (QR Code Check-in Security)

- **Hiện trạng**: Mã QR check-in hiện đang được tạo trực tiếp từ chuỗi ID đơn hàng (`booking.getBookingId()`). Người dùng xấu có thể dễ dàng đoán hoặc sao chép mã ID này để tự sinh mã QR giả mạo check-in trước.
- **Định hướng tối ưu**:
  - **Mã hóa AES-256 kèm Timestamp**: Thay vì truyền trực tiếp ID, hãy sử dụng thuật toán mã hóa đối xứng AES-256 để mã hóa chuỗi ID đơn hàng kết hợp với thời gian tạo mã (Timestamp) từ một Secret Key bảo mật.
  - **Sinh QR động (Dynamic QR)**: Thay đổi mã QR định kỳ mỗi 30 giây (tương tự Google Authenticator OTP) để chặn hoàn toàn hành vi gian lận bằng cách chụp ảnh màn hình vé của người khác.
  - Ứng dụng soát vé của Admin sẽ thực hiện giải mã token QR để xác minh tính hợp lệ và thời gian hiệu lực của mã trước khi xác nhận check-in.

---

## 7. Tránh rò rỉ bộ nhớ (Memory Leak Prevention)

- **Hiện trạng**: Các Fragment trong dự án giữ các tham chiếu lâu dài đến UI, Adapter và LiveData Observers mà chưa thực hiện dọn dẹp giải phóng bộ nhớ khi Fragment bị hủy.
- **Định hướng tối ưu**:
  - Thực hiện dọn dẹp các đối tượng Adapter và gán các tham chiếu view về `null` trong hàm `onDestroyView()` của Fragment:
    ```java
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        recyclerTickets.setAdapter(null);
        ticketAdapter = null;
    }
    ```
  - Đảm bảo các Listener và Callback không làm rò rỉ Context của Activity (Memory Leak).
