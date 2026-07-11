package vn.humg.hai.event_ticket_booking_app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import vn.humg.hai.event_ticket_booking_app.R;
import vn.humg.hai.event_ticket_booking_app.model.Event;

public class AdminEventAdapter extends RecyclerView.Adapter<AdminEventAdapter.ViewHolder> {
    private final List<Event> events;
    private final OnEventActionListener listener;

    public interface OnEventActionListener {
        void onDelete(Event event);
        void onEdit(Event event);
        void onViewStats(Event event);
        void onShare(Event event);
        void onArchive(Event event);
    }

    public AdminEventAdapter(List<Event> events, OnEventActionListener listener) {
        this.events = events;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_event_manage, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event event = events.get(position);
        holder.tvTitle.setText(event.getTitle());
        
        if (event.getDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH);
            holder.tvDate.setText(sdf.format(event.getDate().toDate()));
        } else {
            holder.tvDate.setText("Draft (No date set)");
        }

        String status = event.getStatus() != null ? event.getStatus().toUpperCase() : "DRAFT";
        long now = System.currentTimeMillis();
        
        if (!"DRAFT".equals(status)) {
            if (event.getDate() != null && event.getDate().toDate().getTime() < now) {
                status = "COMPLETED";
            } else {
                status = "ACTIVE";
            }
        }

        holder.tvStatus.setText(status);
        holder.tvAttendance.setVisibility(View.GONE);

        holder.btnAnalytics.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), R.color.text_muted));
        holder.btnEdit.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), R.color.text_muted));
        
        if ("ACTIVE".equals(status)) {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_chip);
            holder.tvStatus.setTextColor(0xFFFFFFFF);
            holder.btnAnalytics.setImageResource(android.R.drawable.ic_menu_sort_by_size);
            holder.btnEdit.setImageResource(android.R.drawable.ic_menu_edit);
            holder.btnDelete.setVisibility(View.VISIBLE);
        } else if ("DRAFT".equals(status)) {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_chip_light);
            holder.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.ink_dark));
            holder.btnAnalytics.setImageResource(android.R.drawable.ic_menu_view); 
            holder.btnEdit.setImageResource(android.R.drawable.ic_menu_edit);
            holder.btnDelete.setVisibility(View.VISIBLE);
        } else if ("COMPLETED".equals(status)) {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_chip_light);
            holder.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.text_muted));
            
            int attended = event.getTotalTicket() - event.getRemainingTicket();
            holder.tvAttendance.setVisibility(View.VISIBLE);
            holder.tvAttendance.setText(String.format(Locale.getDefault(), "%d Attended", attended));
            
            // Fixed: use available drawable icon
            holder.btnAnalytics.setImageResource(android.R.drawable.ic_dialog_info);
            holder.btnEdit.setImageResource(android.R.drawable.ic_menu_save); 
            holder.btnDelete.setImageResource(android.R.drawable.ic_menu_share); 
            holder.btnDelete.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), R.color.brand_primary));
        }

        Glide.with(holder.itemView.getContext())
                .load(event.getImage())
                .placeholder(R.drawable.img_logo_event_ticket_booking)
                .centerCrop()
                .into(holder.ivImage);

        // --- Hot Timer Logic (Admin Only) ---
        if (event.isHot() && event.getHotSetAt() != null) {
            long nowSeconds = System.currentTimeMillis() / 1000L;
            long hotAgeSeconds = nowSeconds - event.getHotSetAt().getSeconds();
            long duration = event.isAutoHot() ? (24 * 60 * 60) : (3 * 24 * 60 * 60);
            long remainingSeconds = duration - hotAgeSeconds;

            if (remainingSeconds > 0) {
                holder.tvHotTimer.setVisibility(View.VISIBLE);
                long hours = remainingSeconds / 3600;
                if (hours >= 24) {
                    holder.tvHotTimer.setText(String.format(Locale.getDefault(), "🔥 Còn %d ngày", hours / 24));
                } else {
                    holder.tvHotTimer.setText(String.format(Locale.getDefault(), "🔥 Còn %dh", hours));
                }
            } else {
                holder.tvHotTimer.setVisibility(View.GONE);
            }
        } else {
            holder.tvHotTimer.setVisibility(View.GONE);
        }

        holder.btnEdit.setOnClickListener(v -> {
            if ("COMPLETED".equals(event.getStatus())) {
                listener.onArchive(event);
            } else {
                listener.onEdit(event);
            }
        });

        holder.btnAnalytics.setOnClickListener(v -> listener.onViewStats(event));

        holder.btnDelete.setOnClickListener(v -> {
            if ("COMPLETED".equals(event.getStatus())) {
                listener.onShare(event);
            } else {
                listener.onDelete(event);
            }
        });
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvTitle, tvDate, tvStatus, tvAttendance, tvHotTimer;
        ImageButton btnAnalytics, btnEdit, btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_event_image);
            tvTitle = itemView.findViewById(R.id.tv_event_title);
            tvDate = itemView.findViewById(R.id.tv_event_date);
            tvStatus = itemView.findViewById(R.id.tv_event_status);
            tvAttendance = itemView.findViewById(R.id.tv_event_attendance);
            tvHotTimer = itemView.findViewById(R.id.tv_event_hot_timer);
            btnAnalytics = itemView.findViewById(R.id.btn_analytics);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}
