# 📖 Hướng Dẫn Thiết Lập Hệ Thống Firebase - EventPass

Tài liệu này hướng dẫn các bước chi tiết để liên kết và cấu hình Google Firebase cho dự án Android **EventPass**.

---

## Bước 1: Khởi tạo dự án trên Firebase Console
1. Truy cập [Firebase Console](https://console.firebase.google.com/) và đăng nhập tài khoản Google.
2. Bấm **Add Project**, đặt tên dự án là `EventPass` và bấm **Continue**.
3. (Tùy chọn) Bật **Google Analytics** cho dự án và chọn tài khoản cấu hình tương ứng. Bấm **Create Project** để hoàn tất.

---

## Bước 2: Tích hợp ứng dụng Android
1. Trên màn hình chính của Firebase Console, bấm vào biểu tượng **Android** để thêm ứng dụng.
2. Điền thông tin gói (Package Name): `vn.humg.hai.event_ticket_booking_app` (Bắt buộc phải khớp với `applicationId` trong file [build.gradle.kts](file:///d:/Android_devpro/Android/Event_Ticket_Booking_App/app/build.gradle.kts)).
3. Điền chứng chỉ ký SHA-1 (Tùy chọn - Cần thiết cho tính năng đăng nhập Google/OTP).
4. Bấm **Register app**, sau đó tải file **`google-services.json`** về.
5. Sao chép file `google-services.json` vừa tải và dán vào thư mục **`app/`** của dự án ([d:\Android_devpro\Android\Event_Ticket_Booking_App\app\google-services.json](file:///d:/Android_devpro/Android/Event_Ticket_Booking_App/app/)).

---

## Bước 3: Cấu hình các dịch vụ Firebase

### 1. Firebase Authentication (Xác thực người dùng)
*   Tại thanh menu bên trái Firebase Console, chọn **Build > Authentication**.
*   Bấm **Get Started**.
*   Trong tab **Sign-in method**, bấm vào **Email/Password**, chọn **Enable** và bấm **Save**.

### 2. Cloud Firestore (Cơ sở dữ liệu NoSQL)
*   Chọn **Build > Cloud Firestore** từ menu trái.
*   Bấm **Create database**, chọn vị trí server (Ví dụ: `asia-southeast1` tại Singapore để có tốc độ kết nối nhanh nhất về Việt Nam) và chọn chế độ **Start in test mode** (Chúng ta sẽ cập nhật rules bảo mật ở bước sau).
*   Sau khi cơ sở dữ liệu được tạo, chọn tab **Rules** trên Firestore Dashboard.
*   Sao chép nội dung từ file [firestore.rules](file:///d:/Android_devpro/Android/Event_Ticket_Booking_App/firestore.rules) của dự án, dán đè vào khung soạn thảo và bấm **Publish**.

### 3. Firebase Storage (Lưu trữ hình ảnh)
*   Chọn **Build > Storage** từ menu trái.
*   Bấm **Get Started**, chọn vị trí server tương tự như Firestore và bấm **Done**.
*   (Tùy chọn) Để cho phép ứng dụng đọc/ghi hình ảnh banner sự kiện, vào tab **Rules** của Storage và đổi quyền thành:
    ```javascript
    rules_version = '2';
    service firebase.storage {
      match /b/{bucket}/o {
        match /{allPaths=**} {
          allow read, write: if request.auth != null;
        }
      }
    }
    ```

### 4. Firebase Remote Config (Cấu hình động từ xa)
*   Chọn **Build > Remote Config** từ menu trái.
*   Bấm **Create configuration**.
*   Thêm tham số mới:
    *   **Parameter key**: `qr_release_threshold_hours`
    *   **Value type**: `Number`
    *   **Default value**: `24` (Thời gian hiển thị mã QR trước khi sự kiện diễn ra, tính bằng giờ).
*   Bấm **Save** và bấm **Publish changes** để kích hoạt tham số.

---

## Bước 4: Biên dịch và chạy thử ứng dụng
1. Mở dự án bằng **Android Studio Ladybug (2024.2.1)** hoặc mới hơn.
2. Đợi Gradle đồng bộ và tải các dependencies (Firebase BOM, ZXing, Glide, Lottie).
3. Kết nối thiết bị thật (bật USB Debugging) hoặc máy ảo Android (yêu cầu API cấp 28 trở lên).
4. Bấm nút **Run** (biểu tượng Play màu xanh) trên thanh công cụ để build ứng dụng và cài đặt lên thiết bị.
