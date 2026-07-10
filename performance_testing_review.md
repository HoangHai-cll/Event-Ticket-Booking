# Báo cáo Đánh giá Hiệu năng & Kiểm thử (Multi-Agent Performance & Test Review)

Tài liệu này ghi nhận kết quả đánh giá đa góc nhìn về hiệu năng và chiến lược kiểm thử cho dự án `Event_Ticket_Booking_App` được thực hiện bởi Hội đồng Tác nhân Chuyên gia (Multi-Agent Council).

---

## 1. Phân tích Tối ưu Hiệu năng (Góc nhìn từ @performance-optimizer)

### A. recyclerView Rendering & Hiệu ứng chuyển động
- **Vấn đề**: `EventAdapter` đang sử dụng `notifyDataSetChanged()` khi cập nhật danh sách ID yêu thích (`setFavoriteEventIds`). Việc này buộc RecyclerView phải vẽ lại toàn bộ danh sách (ngay cả các item không thay đổi trạng thái yêu thích), làm hao phí CPU/GPU và làm mất hiệu ứng chuyển động mượt mà.
- **Giải pháp**:
  - Tích hợp **`DiffUtil`** hoặc **`ListAdapter`** để tính toán sự khác biệt giữa hai danh sách cũ và mới, chỉ cập nhật (re-bind) những item thực sự bị thay đổi trạng thái yêu thích.

### B. Network Latency & Tần suất gọi API dư thừa
- **Vấn đề**: Tệp [TicketsFragment.java](file:///D:/Android_devpro/Android/Event_Ticket_Booking_App/app/src/main/java/vn/humg/hai/event_ticket_booking_app/fragments/TicketsFragment.java) đang thực hiện gọi hàm `loadMyTickets()` lặp đi lặp lại trong cả 3 hàm lifecycle: `onCreateView()`, `onHiddenChanged(false)` (khi chuyển qua lại giữa các tab) và `onResume()`. Điều này khiến ứng dụng thực hiện các truy vấn mạng trùng lặp liên tục lên Firestore, gây tốn băng thông di động của người dùng và tăng độ trễ giao diện.
- **Giải pháp**:
  - Đưa dữ liệu danh sách vé (`bookings`) vào lưu trữ trong `BookingViewModel` sử dụng LiveData.
  - Chỉ gọi tải dữ liệu mới từ mạng khi ViewModel xác định dữ liệu chưa có (hoặc khi người dùng chủ động thực hiện hành động kéo-để-làm-mới - SwipeRefreshLayout).

---

## 2. Thiết kế Cơ sở dữ liệu & Chi phí Truy vấn (Góc nhìn từ @database-architect)

### A. Lỗi N+1 Query kinh điển trên Firestore
- **Vấn đề**: Trong phương thức `loadMyTickets()` của `TicketsFragment`, khi phát hiện các sự kiện liên kết với vé chưa được lưu trong cache, ứng dụng chạy một vòng lặp `for` và thực hiện các cuộc gọi API `eventController.getEventById(id)` đơn lẻ cho từng sự kiện:
  ```java
  for (String id : missingEventIds) {
      eventController.getEventById(id, ...)
  }
  ```
  Nếu người dùng có 10 vé của 10 sự kiện khác nhau, ứng dụng sẽ thực hiện **10 truy vấn mạng độc lập** đến Firestore. Điều này làm tăng chi phí vận hành Firestore (Firestore Reads Billing) và kéo dài thời gian tải vé của người dùng.
- **Giải pháp**:
  - Sử dụng toán tử truy vấn gộp **`whereIn`** của Firestore để lấy toàn bộ thông tin các sự kiện trong một truy vấn duy nhất:
    ```java
    firestore.collection("events")
             .whereIn(FieldPath.documentId(), missingEventIds)
             .get()
    ```
    *(Lưu ý: Firestore hỗ trợ gộp tối đa 30 ID trong toán tử `whereIn` trên một truy vấn).*

### B. Tính nhất quán giao dịch (Firestore Transaction)
- **Đánh giá tích cực**: Hệ thống đã xử lý rất tốt việc cập nhật số lượng vé còn lại của sự kiện thông qua cơ chế Transaction (`firestore.runTransaction`) ở `EventController.java`. Điều này đảm bảo tính nhất quán của dữ liệu, ngăn chặn tuyệt đối lỗi Overbooking (bán vượt quá số vé hiện có) khi có nhiều người dùng thanh toán cùng một thời điểm.

---

## 3. Chiến lược Kiểm thử & Đảm bảo Chất lượng (Góc nhìn từ @test-engineer)

### A. Hiện trạng Kiểm thử (Test Coverage)
- **Vấn đề**: Hiện tại dự án hoàn toàn chưa có bất kỳ tệp tin kiểm thử tự động nào (Unit Test trong thư mục `test/` hoặc Instrumented Test trong thư mục `androidTest/`). Điều này làm tăng khả năng phát sinh lỗi hồi quy (regression bugs) khi tiến hành refactor hoặc nâng cấp các logic nghiệp vụ sau này.

### B. Đề xuất Kế hoạch xây dựng Test Suite
1. **Unit Testing (Logic nghiệp vụ thuần túy)**:
   - Viết các test case JUnit để kiểm thử helper class [ValidationUtils](file:///D:/Android_devpro/Android/Event_Ticket_Booking_App/app/src/main/java/vn/humg/hai/event_ticket_booking_app/utils/ValidationUtils.java) (xác thực email, password).
   - Viết Unit Test cho logic tính toán phân hạng thành viên dựa trên EXP trong `User.computeTier(exp)`.
2. **ViewModel & Controller Testing (Mockito Mocking)**:
   - Nhờ việc refactor chuyển sang Dependency Injection nhẹ (cho phép truyền controller qua constructor của ViewModel), ta có thể sử dụng thư viện **Mockito** để mock các Controller (ví dụ `EventController`, `AuthController`) và viết Unit Test độc lập cho các ViewModel (`EventViewModel`, `AuthViewModel`) mà không cần kết nối mạng hoặc cơ sở dữ liệu thật.
3. **UI/UX Integration Testing (Espresso)**:
   - Viết các bản test tự động bằng **Espresso** để mô phỏng hành vi của người dùng: nhập email/mật khẩu sai ➔ kiểm tra hiển thị thông báo lỗi; thực hiện luồng đặt vé ➔ kiểm tra chuyển màn hình thành công.

---

## 4. Bảng Tổng hợp & Trạng thái Cải tiến

| Hạng mục cải tiến | Thuộc vai trò | Mức độ ưu tiên | Trạng thái | Tác động dự kiến |
| :--- | :--- | :---: | :--- | :--- |
| Khắc phục lỗi N+1 Query bằng truy vấn `whereIn` | `@database-architect` | **High (Cao)** | **Đã sửa ✓** | Tăng tốc độ tải trang vé gấp 3-5 lần, giảm đáng kể chi phí Firebase. |
| Bật Offline Persistence (Xem vé ngoại tuyến) | `@performance-optimizer` | **High (Cao)** | **Đã sửa ✓** | Cho phép hiển thị QR code check-in ngay cả khi mất mạng tại rạp. |
| Giảm tần suất gọi API trùng lặp trong tab vé | `@performance-optimizer` | **Medium (Trung bình)** | *Chờ tối ưu ⏳* | Giảm tải băng thông mạng và độ giật của UI khi chuyển đổi tab. |
| Áp dụng `DiffUtil` cho `EventAdapter` | `@performance-optimizer` | **Medium (Trung bình)** | *Chờ tối ưu ⏳* | Đảm bảo RecyclerView cuộn mượt mà (60/120 FPS), tối ưu hóa hiệu năng render. |
| Xây dựng Test Suite cơ bản (Unit Test cho Utils) | `@test-engineer` | **Low (Thấp)** | *Chờ tối ưu ⏳* | Tạo nền tảng để kiểm thử tự động, tránh lỗi hồi quy logic. |
