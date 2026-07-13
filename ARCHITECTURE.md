# 🗺️ Sơ đồ Kiến trúc & Luồng Dữ liệu - EventPass

Tài liệu này chứa các sơ đồ luồng trực quan mô tả cách các thành phần trong ứng dụng EventPass tương tác với nhau và với Firebase Backend.

---

## 1. Sơ đồ Kiến trúc Hệ thống (MVC + Firebase LiveData)

Sơ đồ dưới đây mô tả cấu trúc phân tầng của ứng dụng Android và cách thức giao tiếp reactive với Firebase Services:

```mermaid
graph TD
    subgraph Client [Android Client Application]
        View[59 XML Layouts & Activities] <--> VM[ViewModel & LiveData]
        VM <--> Controller[7 Controller Classes]
        Model[Model Classes: User, Event, Booking...] <--> Controller
    end

    subgraph Firebase [Google Firebase Platform]
        Auth[Firebase Authentication]
        DB[(Cloud Firestore - NoSQL)]
        Storage[Firebase Storage - Image Hosting]
        Config[Firebase Remote Config]
    end

    Controller <-->|Xác thực| Auth
    Controller <-->|Đọc/Ghi Real-time| DB
    Controller <-->|Tải banner sự kiện| Storage
    Controller -->|Đọc tham số cấu hình| Config
    
    DB -.->|addSnapshotListener| VM
```

---

## 2. Sơ đồ Luồng Đặt vé & Thanh toán (Booking & Payment)

Mô tả tuần tự cách thức thu thập dữ liệu từ giao diện người dùng, áp dụng voucher, chạy giao dịch Firestore và cập nhật số lượng vé:

```mermaid
sequenceDiagram
    autonumber
    actor User as Người dùng
    participant UI as EventDetailActivity
    participant BC as BookingController
    participant VC as VoucherController
    participant EC as EventController
    participant DB as Cloud Firestore

    User->>UI: Chọn hạng vé (VIP/Std) & Số lượng
    UI->>VC: Lấy danh sách Voucher khả dụng
    VC-->>UI: Hiển thị Voucher & Tính tiền chiết khấu
    User->>UI: Bấm "Thanh toán"
    UI->>BC: saveBooking(bookingData)
    activate BC
    BC->>DB: Khởi chạy Firestore Transaction
    activate DB
    DB->>EC: Kiểm tra số lượng vé còn lại trong kho
    alt Vé hợp lệ
        DB->>DB: Trừ số lượng vé (Event document)
        DB->>DB: Tạo tài liệu hóa đơn mới (Status: Confirmed)
        DB-->>BC: Giao dịch thành công
        BC->>VC: removeUserVoucher() (Xóa voucher đã dùng)
        BC-->>UI: Xác nhận đặt vé thành công
    else Hết vé hoặc lỗi
        DB-->>BC: Giao dịch thất bại
        deactivate DB
        BC-->>UI: Hiển thị lỗi hết vé
    end
    deactivate BC
```

---

## 3. Sơ đồ Luồng Quét QR Code & Check-in (Admin)

Mô tả luồng so sánh thời gian thực để kích hoạt mã QR trên app người dùng, và quy trình soát vé của quản trị viên:

```mermaid
sequenceDiagram
    autonumber
    actor User as Người mua vé
    actor Admin as Quản trị viên
    participant UserUI as MyTickets (User)
    participant Config as Remote Config
    participant Cam as QR Scanner (Admin)
    participant BC as BookingController
    participant UC as UserController
    participant DB as Cloud Firestore

    %% Luồng hiển thị QR phía User
    User->>UserUI: Mở xem vé điện tử
    UserUI->>Config: Lấy ngưỡng qr_release_threshold_hours (24h)
    Note over UserUI: Tính: EventStartTime - CurrentTime
    alt Thời gian còn lại <= 24h
        UserUI->>UserUI: Sinh mã QR (bookingId) bằng ZXing
        UserUI-->>User: Hiển thị mã QR vé điện tử
    else Thời gian còn lại > 24h
        UserUI-->>User: Ẩn mã QR, hiển thị đếm ngược bảo mật
    end

    %% Luồng Quét mã phía Admin
    Admin->>Cam: Mở trình quét CustomScannerActivity (hiệu ứng Laser)
    User->>Cam: Xuất trình QR Code
    Cam->>Cam: Quét và giải mã lấy chuỗi "bookingId"
    Cam->>BC: getBookingById(bookingId)
    activate BC
    BC->>DB: Kiểm tra trạng thái hóa đơn
    DB-->>BC: Trả về trạng thái (Confirmed / Used / Cancelled)
    alt Trạng thái là "Confirmed" (Hợp lệ)
        BC->>DB: updateBookingStatus(bookingId, "Used")
        BC->>UC: addExp(userId, expAmount) (Tích lũy EXP thăng hạng)
        BC-->>Cam: Check-in thành công (Bíp bíp, màn hình xanh)
    else Vé đã quét hoặc không hợp lệ
        BC-->>Cam: Check-in thất bại (Màn hình đỏ cảnh báo)
    end
    deactivate BC
```
