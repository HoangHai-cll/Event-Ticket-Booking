package vn.humg.hai.event_ticket_booking_app.utils;

import android.util.Patterns;

public class ValidationUtils {

    /**
     * Kiểm tra định dạng Email hợp lệ.
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * Kiểm tra Mật khẩu hợp lệ (tối thiểu 6 ký tự).
     */
    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }
}
