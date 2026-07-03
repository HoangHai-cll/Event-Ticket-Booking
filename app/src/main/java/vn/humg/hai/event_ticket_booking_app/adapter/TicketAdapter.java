package vn.humg.hai.event_ticket_booking_app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;
import vn.humg.hai.event_ticket_booking_app.R;
import vn.humg.hai.event_ticket_booking_app.controller.EventController;
import vn.humg.hai.event_ticket_booking_app.model.Booking;

public class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.TicketViewHolder> {
    private final List<Booking> bookings;
    private final EventController eventController = new EventController();

    public TicketAdapter(List<Booking> bookings) {
        this.bookings = bookings;
    }

    @NonNull
    @Override
    public TicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Sửa lỗi: Sử dụng đúng layout item_ticket
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ticket, parent, false);
        return new TicketViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TicketViewHolder holder, int position) {
        Booking booking = bookings.get(position);
        
        holder.tvId.setText(String.format("#%s", booking.getBookingId().substring(0, 8).toUpperCase()));
        holder.tvStatus.setText(booking.getStatus());
        
        // Lấy thông tin sự kiện từ ID để hiển thị tiêu đề và ảnh
        eventController.getEventById(booking.getEventId(), event -> {
            if (event != null) {
                holder.tvTitle.post(() -> {
                    holder.tvTitle.setText(event.getTitle());
                    holder.tvInfo.setText(String.format("📍 %s", event.getLocation()));
                    
                    Glide.with(holder.itemView.getContext())
                            .load(event.getImage())
                            .placeholder(R.drawable.img_logo_event_ticket_booking)
                            .into(holder.ivImage);
                });
            }
        }, error -> {});
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    static class TicketViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvTitle, tvInfo, tvStatus, tvId;

        public TicketViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_ticket_image);
            tvTitle = itemView.findViewById(R.id.tv_ticket_title);
            tvInfo = itemView.findViewById(R.id.tv_ticket_info);
            tvStatus = itemView.findViewById(R.id.tv_ticket_status);
            tvId = itemView.findViewById(R.id.tv_ticket_id);
        }
    }
}
