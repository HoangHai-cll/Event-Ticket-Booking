package vn.humg.hai.event_ticket_booking_app.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import java.util.Map;
import vn.humg.hai.event_ticket_booking_app.R;
import vn.humg.hai.event_ticket_booking_app.view.NotificationActivity;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "FCMService";
    private static final String CHANNEL_ID = "event_ticket_notification_channel";
    private static final String CHANNEL_NAME = "Thông báo ứng dụng";
    public static final String ACTION_UPDATE_UNREAD = "vn.humg.hai.event_ticket_booking_app.UPDATE_UNREAD_COUNT";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d(TAG, "From: " + remoteMessage.getFrom());

        String title = "";
        String body = "";
        String type = "SYSTEM"; // Default type

        // Check if message contains data payload
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            Map<String, String> data = remoteMessage.getData();
            title = data.get("title");
            body = data.get("body");
            if (data.containsKey("type")) {
                type = data.get("type");
            }
        }

        // Check if message contains notification payload (fallback)
        if (remoteMessage.getNotification() != null) {
            if (title == null || title.isEmpty()) {
                title = remoteMessage.getNotification().getTitle();
            }
            if (body == null || body.isEmpty()) {
                body = remoteMessage.getNotification().getBody();
            }
        }

        if (title == null || title.isEmpty()) {
            title = "Thông báo mới";
        }
        if (body == null || body.isEmpty()) {
            body = "Bạn có một thông báo mới từ ứng dụng.";
        }

        // 1. Ghi nhận thông báo vào SQLite
        LocalNotificationDbHelper dbHelper = LocalNotificationDbHelper.getInstance(this);
        dbHelper.insertNotification(title, body, type);

        // 2. Phát Broadcast nội bộ để cập nhật Badge
        Intent broadcastIntent = new Intent(ACTION_UPDATE_UNREAD);
        sendBroadcast(broadcastIntent);

        // 3. Kiểm tra cấu hình tắt/bật thông báo của người dùng
        SharedPreferences sharedPreferences = getSharedPreferences("app_settings", MODE_PRIVATE);
        boolean isNotifEnabled = sharedPreferences.getBoolean("app_notif", true);

        if (isNotifEnabled) {
            sendNotification(title, body);
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "Refreshed token: " + token);
        // Lưu token hoặc gửi lên Firestore nếu cần
        SharedPreferences.Editor editor = getSharedPreferences("app_settings", MODE_PRIVATE).edit();
        editor.putString("fcm_token", token);
        editor.apply();
    }

    private void sendNotification(String title, String messageBody) {
        Intent intent = new Intent(this, NotificationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_profile_nav) // Sẽ tự map sang icon chuẩn khi build
                        .setContentTitle(title)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent)
                        .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Đăng ký Notification Channel cho Android Oreo trở lên
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Cập nhật sự kiện, thông tin thanh toán và khuyến mại");
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        if (notificationManager != null) {
            notificationManager.notify((int) System.currentTimeMillis(), notificationBuilder.build());
        }
    }
}
