package vn.humg.hai.event_ticket_booking_app.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;
import vn.humg.hai.event_ticket_booking_app.R;
import vn.humg.hai.event_ticket_booking_app.model.Booking;
import vn.humg.hai.event_ticket_booking_app.model.Event;
import vn.humg.hai.event_ticket_booking_app.view.NotificationActivity;

public class EventReminderReceiver extends BroadcastReceiver {
    private static final String TAG = "EventReminderReceiver";
    private static final String CHANNEL_ID = "event_reminder_notification_channel";
    private static final String CHANNEL_NAME = "Nhắc nhở sự kiện sắp diễn ra";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Alarm triggered! Checking upcoming events...");

        // 1. Kiểm tra cấu hình cài đặt xem người dùng có tắt thông báo không
        SharedPreferences appPrefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE);
        boolean isNotifEnabled = appPrefs.getBoolean("app_notif", true);
        if (!isNotifEnabled) {
            Log.d(TAG, "Notifications are disabled in app settings. Skipping.");
            return;
        }

        // 2. Lấy UID của người dùng đang đăng nhập
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            Log.d(TAG, "No user logged in. Skipping background check.");
            return;
        }

        // 3. Truy vấn Firestore tìm bookings & events
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("bookings")
                .whereEqualTo("userId", uid)
                .get()
                .addOnSuccessListener(bookingSnapshots -> {
                    List<Booking> bookings = bookingSnapshots.toObjects(Booking.class);
                    if (bookings.isEmpty()) {
                        Log.d(TAG, "User has no bookings.");
                        return;
                    }

                    long nowSec = System.currentTimeMillis() / 1000L;
                    long limit24hSec = nowSec + (24 * 60 * 60);

                    db.collection("events")
                            .whereGreaterThan("date", new Timestamp(nowSec, 0))
                            .get()
                            .addOnSuccessListener(eventSnapshots -> {
                                List<Event> upcomingEvents = eventSnapshots.toObjects(Event.class);
                                if (upcomingEvents.isEmpty()) {
                                    Log.d(TAG, "No upcoming events found.");
                                    return;
                                }

                                for (Event event : upcomingEvents) {
                                    if (event.getDate() == null) continue;
                                    long eventTimeSec = event.getDate().getSeconds();

                                    // Kiểm tra xem sự kiện có diễn ra trong vòng 24 tiếng tới không
                                    if (eventTimeSec <= limit24hSec) {
                                        // Kiểm tra xem user có mua vé sự kiện này không
                                        for (Booking booking : bookings) {
                                            if (booking.getEventId().equals(event.getEventId()) 
                                                    && !"Cancelled".equalsIgnoreCase(booking.getStatus())) {
                                                
                                                // Tính khoảng cách thời gian còn lại
                                                long diffSeconds = eventTimeSec - nowSec;
                                                long diffHours = diffSeconds / 3600;
                                                long diffMinutes = (diffSeconds % 3600) / 60;
                                                
                                                String timeRemainingStr = diffHours > 0 
                                                        ? diffHours + " giờ " + diffMinutes + " phút"
                                                        : diffMinutes + " phút";

                                                // Tránh spam: Kiểm tra xem đã gửi thông báo cho sự kiện này trong vòng 2 giờ qua chưa
                                                SharedPreferences reminderPrefs = context.getSharedPreferences("EventReminderPrefs", Context.MODE_PRIVATE);
                                                String lastNotifiedKey = "last_notified_" + event.getEventId();
                                                long lastNotifiedTimeMs = reminderPrefs.getLong(lastNotifiedKey, 0);

                                                // 2 giờ = 2 * 60 * 60 * 1000 = 7,200,000 ms (trừ hao 5 phút offset = 7,000,000 ms)
                                                if (System.currentTimeMillis() - lastNotifiedTimeMs > 7000000L) {
                                                    // Đẩy thông báo
                                                    String title = "Sắp đến giờ diễn ra sự kiện!";
                                                    String message = "Sự kiện \"" + event.getTitle() + "\" của bạn sẽ bắt đầu sau " + timeRemainingStr + " nữa. Hãy chuẩn bị sẵn sàng vé của bạn nhé!";
                                                    
                                                    triggerNotification(context, title, message, event.getEventId());

                                                    // Lưu mốc thời gian đã gửi
                                                    reminderPrefs.edit().putLong(lastNotifiedKey, System.currentTimeMillis()).apply();
                                                }
                                                break;
                                            }
                                        }
                                    }
                                }
                            })
                            .addOnFailureListener(e -> Log.e(TAG, "Error fetching events: ", e));
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error fetching bookings: ", e));
    }

    private void triggerNotification(Context context, String title, String body, String eventId) {
        // 1. Lưu thông báo vào SQLite cục bộ
        LocalNotificationDbHelper.getInstance(context).insertNotification(title, body, "TICKET");

        // 2. Phát broadcast cập nhật Badge trên màn hình chính
        Intent updateIntent = new Intent(MyFirebaseMessagingService.ACTION_UPDATE_UNREAD);
        context.sendBroadcast(updateIntent);

        // 3. Hiển thị thông báo hệ thống lên thanh trạng thái (cho dù ứng dụng đã đóng)
        Intent intent = new Intent(context, NotificationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), intent,
                    PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_MUTABLE);
        } else {
            pendingIntent = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), intent,
                    PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
        }

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_nav_tickets)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Cập nhật thời gian nhắc nhở sự kiện gần nhất");
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        if (manager != null) {
            manager.notify(eventId.hashCode(), builder.build());
        }
    }
}
