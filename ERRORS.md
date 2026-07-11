# Event Ticket Booking App - Error Logging & Learning

## [2026-07-11 10:43] - Lỗi thuộc tính RelativeLayout trong layout thông báo mới

- **Type**: Syntax / Resource XML Error
- **Severity**: High (Gây lỗi biên dịch hoàn toàn ứng dụng)
- **File**: `app/src/main/res/layout/activity_notification.xml:23,33`
- **Agent**: `antigravity-ide`
- **Root Cause**: Khai báo nhầm thuộc tính căn giữa dọc `android:layout_centerY="true"` (thuộc tính của ConstraintLayout) thay vì thuộc tính chuẩn của RelativeLayout là `android:layout_centerVertical="true"`. Do `compileDebugJavaWithJavac` trước đó chỉ biên dịch các file Java nên không phát hiện ra lỗi XML của AAPT2, chỉ khi chạy tác vụ `assembleDebug` (tiến trình đóng gói tài nguyên) mới báo lỗi liên kết tài nguyên (Resource linking failed).
- **Error Message**:
  ```text
  vn.humg.hai.event_ticket_booking_app-main-60:/layout/activity_notification.xml:27: error: attribute android:layout_centerY not found.
  vn.humg.hai.event_ticket_booking_app-main-60:/layout/activity_notification.xml:38: error: attribute android:layout_centerY not found.
  error: failed linking file resources.
  ```
- **Fix Applied**: Thay đổi các thuộc tính `android:layout_centerY="true"` thành `android:layout_centerVertical="true"` tại dòng 23 và 33 trong file `activity_notification.xml`.
- **Prevention**: Luôn kiểm tra kỹ các thuộc tính XML tương thích với ViewGroup cha (RelativeLayout vs ConstraintLayout) và chạy lệnh đóng gói toàn diện `./gradlew.bat assembleDebug` để kiểm thử trước khi báo cáo thành công.
- **Status**: Fixed
