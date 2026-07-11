package vn.humg.hai.event_ticket_booking_app.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import vn.humg.hai.event_ticket_booking_app.R;
import vn.humg.hai.event_ticket_booking_app.model.NotificationModel;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    public interface OnNotificationClickListener {
        void onItemClick(NotificationModel notification);
        void onDeleteClick(NotificationModel notification);
    }

    private final List<NotificationModel> notifications;
    private final OnNotificationClickListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm, dd/MM/yyyy", Locale.getDefault());

    public NotificationAdapter(List<NotificationModel> notifications, OnNotificationClickListener listener) {
        this.notifications = notifications;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NotificationModel notification = notifications.get(position);
        holder.bind(notification, listener);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final View layoutRoot;
        private final View layoutIconContainer;
        private final ImageView ivIcon;
        private final TextView tvTitle;
        private final TextView tvBody;
        private final TextView tvTime;
        private final View viewUnreadDot;
        private final ImageView btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutRoot = itemView.findViewById(R.id.layout_item_root);
            layoutIconContainer = itemView.findViewById(R.id.layout_icon_container);
            ivIcon = itemView.findViewById(R.id.iv_notification_type_icon);
            tvTitle = itemView.findViewById(R.id.tv_notification_title);
            tvBody = itemView.findViewById(R.id.tv_notification_body);
            tvTime = itemView.findViewById(R.id.tv_notification_time);
            viewUnreadDot = itemView.findViewById(R.id.view_unread_dot);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }

        public void bind(NotificationModel notification, OnNotificationClickListener listener) {
            Context context = itemView.getContext();

            tvTitle.setText(notification.getTitle());
            tvBody.setText(notification.getBody());

            // Định dạng thời gian nhận
            long timeMs = notification.getTimestamp();
            String relativeTime = DateUtils.getRelativeTimeSpanString(timeMs, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS).toString();
            tvTime.setText(relativeTime);

            // Xử lý Trạng thái Đọc / Chưa Đọc
            if (notification.getIsRead() == 0) {
                tvTitle.setTypeface(null, Typeface.BOLD);
                viewUnreadDot.setVisibility(View.VISIBLE);
                layoutRoot.setBackgroundColor(Color.parseColor("#FFFDF2")); // Màu vàng nhạt sang trọng
            } else {
                tvTitle.setTypeface(null, Typeface.NORMAL);
                viewUnreadDot.setVisibility(View.GONE);
                layoutRoot.setBackgroundColor(Color.parseColor("#FFFFFF")); // Trắng chuẩn
            }

            // Xử lý Icon và màu nền theo loại thông báo
            String type = notification.getType() != null ? notification.getType().toUpperCase() : "SYSTEM";
            switch (type) {
                case "TICKET":
                    ivIcon.setImageResource(R.drawable.ic_nav_tickets);
                    ivIcon.setImageTintList(ColorStateList.valueOf(Color.parseColor("#0284C7")));
                    layoutIconContainer.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E0F2FE")));
                    break;
                case "VOUCHER":
                    ivIcon.setImageResource(R.drawable.ic_bell); // Dùng ic_bell làm fallback
                    ivIcon.setImageTintList(ColorStateList.valueOf(Color.parseColor("#E11D48")));
                    layoutIconContainer.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFE4E6")));
                    break;
                case "SYSTEM":
                default:
                    ivIcon.setImageResource(R.drawable.ic_settings);
                    ivIcon.setImageTintList(ColorStateList.valueOf(Color.parseColor("#7C3AED")));
                    layoutIconContainer.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F5F3FF")));
                    break;
            }

            // Click Item
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(notification);
                }
            });

            // Click Delete
            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(notification);
                }
            });
        }
    }
}
