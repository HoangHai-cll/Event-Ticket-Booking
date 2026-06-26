package vn.humg.hai.event_ticket_booking_app.adapter;

import android.content.Intent;

public class AuthResultAdapter {
    public static Intent createResult(String email, String password, boolean success) {
        Intent data = new Intent();
        if (email != null) data.putExtra("prefill_email", email);
        if (password != null) data.putExtra("prefill_password", password);
        data.putExtra("registration_success", success);
        return data;
    }

    public static String getPrefillEmail(Intent data) {
        return data != null ? data.getStringExtra("prefill_email") : null;
    }

    public static String getPrefillPassword(Intent data) {
        return data != null ? data.getStringExtra("prefill_password") : null;
    }

    public static boolean isRegistrationSuccess(Intent data) {
        return data != null && data.getBooleanExtra("registration_success", false);
    }
}
