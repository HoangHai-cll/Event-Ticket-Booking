# BÁO CÁO TỔNG HỢP LỖI VÀ NGUY CƠ CRASH 🛡️

Dưới đây là danh sách các điểm cần lưu ý và sửa chữa ngay để ứng dụng hoạt động ổn định.

## 1. Lỗi Tài nguyên (Gây lỗi Biên dịch - Build Error) ❌
*   **Trùng lặp ID trong `strings.xml`**: Hiện tại tệp `strings.xml` đang bị lặp rất nhiều mã định danh (như `admin_revenue_report`, `admin_revenue_today`, `admin_revenue_month`,...). Điều này sẽ khiến Android Studio báo lỗi **"Duplicate resources"** và không thể chạy App.
*   **Thiếu Layout Dialog**: Trong `EditProfileActivity.java` có gọi `R.layout.dialog_avatar_grid` nhưng tệp này chưa được tạo trong thư mục `layout`.
*   **Icon hệ thống**: Một số ImageButton đang dùng `android.R.drawable.ic_menu_...` có thể không hiển thị đúng màu sắc hoặc bị mất hình trên một số phiên bản Android.

## 2. Nguy cơ Crash (Lỗi Runtime - Runtime Crash) ⚠️
*   **`NullPointerException` tại Profile**: 
    *   Trong `EditProfileActivity.java` và `ProfileFragment.java`, việc gọi `user.getInterests()` nếu dữ liệu trên Firebase trống (null) sẽ gây crash khi thực hiện vòng lặp `for`.
    *   Trường `avatarName` trong Model User nếu lưu URL nhưng code lại xử lý như tên Resource (hoặc ngược lại) sẽ khiến Glide bị lỗi.
*   **Chuyển Tab trong `MainActivity`**: 
    *   Nếu người dùng nhấn "Xem vé" sau khi thanh toán thành công, `MainActivity` gọi `navigateToTab("Tickets")`. Nếu lúc đó Fragment chưa kịp khởi tạo xong (do mạng chậm hoặc logic role), ứng dụng có thể bị văng.
*   **Ép kiểu dữ liệu (Type Mismatch)**: 
    *   Trong `EventDetailActivity.java`, giá vé đang được truyền dưới dạng `double` nhưng thói quen cộng chuỗi hoặc ép kiểu sang `(long)` có thể gây lỗi định dạng chuỗi `%f` hoặc `%d`.

## 3. Lỗi Logic (Logic Bugs) 🧠
*   **Ví Voucher**: Luồng chọn Voucher từ ví người dùng hiện đang "giả lập" (mockup). Nếu ID voucher không tồn tại thật trên Firebase, bước thanh toán cuối cùng sẽ báo lỗi.
*   **Trừ kho vé**: Logic trừ `remainingTicket` trong `PaymentActivity` đang chạy độc lập. Nếu 2 người cùng mua vé cuối cùng một lúc, có thể dẫn đến tình trạng "bán lố" (Overbooking).
*   **Đăng ký Admin**: Mã bí mật `ADMIN123` đang được viết cứng trong code (Hardcoded). Nếu lộ mã này, bất kỳ ai cũng có thể chiếm quyền quản trị.

## 4. Hành động khuyến nghị (To-Do) ✅
1.  **Làm sạch `strings.xml`**: Xóa bỏ các đoạn code bị dán đè/trùng lặp ở cuối file.
2.  **Tạo tệp `dialog_avatar_grid.xml`**: Để tính năng đổi ảnh đại diện không bị lỗi.
3.  **Thêm Null-Safety**: Bổ sung `if (list != null)` trước khi duyệt mọi danh sách lấy từ Firebase.
4.  **Kiểm tra Role**: Đảm bảo `UserController` luôn trả về dữ liệu role chính xác trước khi `MainActivity` dựng thanh điều hướng.

---
*Tài liệu này được tạo tự động để hỗ trợ việc rà soát hệ thống.*
