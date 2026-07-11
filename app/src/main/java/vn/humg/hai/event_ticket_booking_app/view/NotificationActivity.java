package vn.humg.hai.event_ticket_booking_app.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import vn.humg.hai.event_ticket_booking_app.R;
import vn.humg.hai.event_ticket_booking_app.adapter.NotificationAdapter;
import vn.humg.hai.event_ticket_booking_app.model.NotificationModel;
import vn.humg.hai.event_ticket_booking_app.utils.LocalNotificationDbHelper;
import vn.humg.hai.event_ticket_booking_app.utils.MyFirebaseMessagingService;

public class NotificationActivity extends AppCompatActivity implements NotificationAdapter.OnNotificationClickListener {

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private List<NotificationModel> notificationList = new ArrayList<>();
    private LocalNotificationDbHelper dbHelper;

    private ImageView btnBack;
    private TextView tvMarkAllRead;
    private ImageView btnClearAll;
    private View layoutEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        dbHelper = LocalNotificationDbHelper.getInstance(this);

        initViews();
        setupRecyclerView();
        loadNotifications();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        tvMarkAllRead = findViewById(R.id.tv_mark_all_read);
        btnClearAll = findViewById(R.id.btn_clear_all);
        recyclerView = findViewById(R.id.recycler_notifications);
        layoutEmpty = findViewById(R.id.layout_empty_notifications);

        btnBack.setOnClickListener(v -> finish());

        tvMarkAllRead.setOnClickListener(v -> markAllNotificationsAsRead());

        btnClearAll.setOnClickListener(v -> confirmClearAllNotifications());
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter(notificationList, this);
        recyclerView.setAdapter(adapter);
    }

    private void loadNotifications() {
        List<NotificationModel> list = dbHelper.getAllNotifications();
        notificationList.clear();
        notificationList.addAll(list);
        adapter.notifyDataSetChanged();

        checkEmptyState();
    }

    private void checkEmptyState() {
        if (notificationList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
            tvMarkAllRead.setVisibility(View.GONE);
            btnClearAll.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);
            tvMarkAllRead.setVisibility(View.VISIBLE);
            btnClearAll.setVisibility(View.VISIBLE);
        }
    }

    private void markAllNotificationsAsRead() {
        if (notificationList.isEmpty()) return;
        dbHelper.markAllAsRead();
        loadNotifications();
        notifyUnreadChange();
        Toast.makeText(this, "Đã đánh dấu đã đọc tất cả", Toast.LENGTH_SHORT).show();
    }

    private void confirmClearAllNotifications() {
        new AlertDialog.Builder(this)
                .setTitle("Xóa tất cả thông báo?")
                .setMessage("Hành động này sẽ xóa vĩnh viễn tất cả lịch sử thông báo của bạn.")
                .setPositiveButton("Xóa tất cả", (dialog, which) -> {
                    dbHelper.clearAllNotifications();
                    loadNotifications();
                    notifyUnreadChange();
                    Toast.makeText(this, "Đã xóa toàn bộ thông báo", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onItemClick(NotificationModel notification) {
        // 1. Đánh dấu đã đọc trong SQLite nếu chưa đọc
        if (notification.getIsRead() == 0) {
            dbHelper.markAsRead(notification.getId());
            notification.setIsRead(1);
            adapter.notifyDataSetChanged();
            notifyUnreadChange();
        }

        // 2. Hiển thị chi tiết thông báo trong Dialog sang trọng
        new AlertDialog.Builder(this)
                .setTitle(notification.getTitle())
                .setMessage(notification.getBody())
                .setPositiveButton("Đóng", null)
                .show();
    }

    @Override
    public void onDeleteClick(NotificationModel notification) {
        dbHelper.deleteNotification(notification.getId());
        int position = notificationList.indexOf(notification);
        if (position != -1) {
            notificationList.remove(position);
            adapter.notifyItemRemoved(position);
            checkEmptyState();
        }
        notifyUnreadChange();
    }

    private void notifyUnreadChange() {
        Intent intent = new Intent(MyFirebaseMessagingService.ACTION_UPDATE_UNREAD);
        sendBroadcast(intent);
    }
}
