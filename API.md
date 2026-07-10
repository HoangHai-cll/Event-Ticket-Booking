# Danh sách API & Tương tác Dữ liệu - Event Ticket Booking App

Tài liệu này tổng hợp toàn bộ các API (hàm truy vấn dữ liệu CRUD) hiện có trong ứng dụng di động `Event_Ticket_Booking_App`, tương tác với Firebase Authentication và Firebase Cloud Firestore.

---

## 1. Nhóm API Xác thực & Người dùng (AuthController & UserController)

Quản lý thông tin tài khoản, hồ sơ cá nhân và cấp bậc thành viên. Tương tác với Firebase Authentication và Collection `users`.

| Tên Hàm / API | Mô tả Chức năng | Firestore Collection / Document |
| :--- | :--- | :--- |
| `saveUserProfile(...)` | Tạo mới hoặc cập nhật hồ sơ người dùng cơ bản khi đăng ký. | `users/{uid}` |
| `getUserById(...)` | Lấy thông tin chi tiết của một tài khoản User. | `users/{uid}` |
| `getAdminById(...)` | Lấy thông tin tài khoản Admin (quyền hạn, bậc Admin). | `users/{uid}` |
| `getAllUsers(...)` | Lấy danh sách toàn bộ người dùng trong hệ thống (dành cho Admin). | `users` |
| `updateLastLogin(...)` | Cập nhật thời gian đăng nhập cuối cùng của người dùng. | `users/{uid}` |
| `addExp(...)` | Tích lũy EXP mua vé và tự động nâng hạng thành viên (Đồng, Bạc, Vàng, VIP) kèm tặng Voucher thăng hạng. | `users/{uid}` |
| `incrementAdminBookingCount(...)` | Tăng số vé Admin đã duyệt và nâng cấp quyền duyệt vé tự động. | `users/{adminId}` |

---

## 2. Nhóm API Quản lý Sự kiện (EventController)

Quản lý thông tin các sự kiện âm nhạc, workshop, hội thảo. Tương tác với Collection `events`.

| Tên Hàm / API | Mô tả Chức năng | Firestore Collection / Document |
| :--- | :--- | :--- |
| `saveEvent(...)` | Thêm mới sự kiện hoặc chỉnh sửa sự kiện cũ (dành cho Admin). | `events/{eventId}` |
| `getEventById(...)` | Lấy thông tin chi tiết của một sự kiện cụ thể. | `events/{eventId}` |
| `getEventsByIds(...)` | Truy vấn gộp thông tin danh sách sự kiện hàng loạt bằng `whereIn` (giải quyết N+1 query). | `events` |
| `getAllEvents(...)` | Lấy danh sách toàn bộ sự kiện đang hoạt động trên hệ thống. | `events` |
| `deleteEvent(...)` | Xóa sự kiện khỏi hệ thống (dành cho Admin). | `events/{eventId}` |
| `updateRemainingTicket(...)` | Cập nhật số lượng vé tổng còn lại bằng Firestore Transaction. | `events/{eventId}` |
| `updateTierRemainingTicket(...)` | Cập nhật số lượng vé còn lại của một hạng vé cụ thể (VIP, Standard) bằng Transaction. | `events/{eventId}` |
| `updateRating(...)` | Cập nhật điểm đánh giá trung bình và số lượt đánh giá sự kiện. | `events/{eventId}` |

---

## 3. Nhóm API Đặt vé & Giao dịch (BookingController)

Quản lý hóa đơn đặt vé và lịch sử soát vé. Tương tác với Collection `bookings`.

| Tên Hàm / API | Mô tả Chức năng | Firestore Collection / Document |
| :--- | :--- | :--- |
| `saveBooking(...)` | Tạo mới đơn đặt vé (lưu thông tin thanh toán, số lượng, hạng vé). | `bookings/{bookingId}` |
| `getBookingById(...)` | Lấy chi tiết hóa đơn đặt vé bằng ID. | `bookings/{bookingId}` |
| `getBookingsByUser(...)` | Lấy danh sách các vé đã mua của người dùng cụ thể. | `bookings` (query by `userId`) |
| `getBookingsBySeller(...)` | Lấy danh sách vé do một seller (người bán) phụ trách. | `bookings` (query by `sellerId`) |
| `getAllBookings(...)` | Lấy toàn bộ danh sách vé đặt trên hệ thống (dành cho Admin). | `bookings` |
| `updateBookingStatus(...)` | Cập nhật trạng thái vé (`Confirmed`, `Used`, `Cancelled`, `Refund Pending`). | `bookings/{bookingId}` |

---

## 4. Nhóm API Đánh giá & Phản hồi (ReviewController)

Quản lý các bài đánh giá, nhận xét của người dùng sau khi tham gia sự kiện. Tương tác với Collection `reviews`.

| Tên Hàm / API | Mô tả Chức năng | Firestore Collection / Document |
| :--- | :--- | :--- |
| `saveReview(...)` | Lưu đánh giá sự kiện mới (tự động kích hoạt tính toán lại rating sự kiện). | `reviews/{reviewId}` |
| `getAllReviews(...)` | Lấy toàn bộ danh sách các đánh giá trên hệ thống. | `reviews` |
| `getReviewsByEvent(...)` | Lấy danh sách các đánh giá thuộc về một sự kiện cụ thể. | `reviews` (query by `eventId`) |
| `deleteReview(...)` | Xóa đánh giá vi phạm (dành cho Admin). | `reviews/{reviewId}` |
| `hasUserReviewedEvent(...)` | Kiểm tra xem người dùng đã từng viết đánh giá cho sự kiện này chưa. | `reviews` (query by `eventId` & `userId`) |

---

## 5. Nhóm API Yêu thích (FavoriteController)

Quản lý danh sách sự kiện yêu thích để hiển thị cá nhân hóa trên trang chủ. Tương tác với Collection `favorites`.

| Tên Hàm / API | Mô tả Chức năng | Firestore Collection / Document |
| :--- | :--- | :--- |
| `addToFavorite(...)` | Thêm một sự kiện vào danh sách yêu thích cá nhân. | `favorites/{userId_eventId}` |
| `removeFromFavorite(...)` | Xóa sự kiện khỏi danh sách yêu thích cá nhân. | `favorites/{userId_eventId}` |
| `getFavoritesByUser(...)` | Lấy toàn bộ danh sách ID sự kiện yêu thích của người dùng. | `favorites` (query by `userId`) |

---

## 6. Nhóm API Quản lý Khuyến mãi (VoucherController)

Quản lý mã giảm giá của hệ thống và ví khuyến mãi của cá nhân người dùng.

| Tên Hàm / API | Mô tả Chức năng | Firestore Collection / Document |
| :--- | :--- | :--- |
| `addSystemVoucher(...)` | Tạo mã giảm giá dùng chung cho toàn hệ thống (dành cho Admin). | `system_vouchers/{voucherId}` |
| `getAllSystemVouchers(...)` | Lấy toàn bộ danh sách mã giảm giá của hệ thống. | `system_vouchers` |
| `deleteSystemVoucher(...)` | Xóa mã giảm giá hệ thống (dành cho Admin). | `system_vouchers/{voucherId}` |
| `getUserVouchers(...)` | Lấy danh sách các voucher hiện có trong ví của người dùng. | `users/{userId}/user_vouchers` |
| `addUserVoucher(...)` | Tặng thêm voucher vào ví cá nhân người dùng. | `users/{userId}/user_vouchers/{voucherId}` |
| `removeUserVoucher(...)` | Xóa voucher khỏi ví của người dùng (gọi sau khi đã áp dụng thanh toán). | `users/{userId}/user_vouchers/{voucherId}` |
| `checkAndGiveFirstPurchaseVoucher(...)` | Kiểm tra lịch sử đơn hàng để tự động tặng voucher chào mừng `WELCOME50` (giảm 50.000đ) cho giao dịch đầu tiên. | `bookings` & `users/{userId}/user_vouchers` |

---

## 7. Nhóm API Cấu hình Hệ thống (ConfigController)

Sử dụng dịch vụ **Firebase Remote Config** để quản trị các tham số cấu hình động của ứng dụng từ xa.

*   **`fetchAndActivate()`**: Tải và áp dụng các cấu hình mới nhất từ Firebase Console.
*   **`getQrReleaseThresholdHours()`**: Lấy số giờ cấu hình quy định thời gian cho phép hiển thị mã QR check-in trước khi sự kiện diễn ra (mặc định là `24` giờ).
*   **`getQrReleaseThresholdMs()`**: Chuyển đổi số giờ cấu hình sang mili-giây để tính toán so sánh thời gian thực.
