package vn.humg.hai.event_ticket_booking_app.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import vn.humg.hai.event_ticket_booking_app.R;
import vn.humg.hai.event_ticket_booking_app.controller.EventController;
import vn.humg.hai.event_ticket_booking_app.controller.UserController;
import vn.humg.hai.event_ticket_booking_app.model.Booking;

public class AdminBookingAdapter extends RecyclerView.Adapter<AdminBookingAdapter.ViewHolder> {
    private final List<Booking> bookings;
    private final OnBookingActionListener listener;
    private final EventController eventController = new EventController();
    private final UserController userController = new UserController();

    public interface OnBookingActionListener {
        void onConfirm(Booking booking);
        void onCancel(Booking booking);
    }

    public AdminBookingAdapter(List<Booking> bookings, OnBookingActionListener listener) {
        this.bookings = bookings;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_booking, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Booking booking = bookings.get(position);

        holder.tvBookingId.setText("Mã: #" + booking.getBookingId().toUpperCase());
        holder.tvPrice.setText("Giá: " + String.format(Locale.getDefault(), "%,.0fđ", booking.getTotalPrice()));
        
        // Reset text để tránh lỗi hiển thị sai khi recycle view
        holder.tvUser.setText("Khách: Đang tải...");
        holder.tvEvent.setText("Đang tải sự kiện...");

        if (booking.getBookingDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            holder.tvDate.setText(sdf.format(booking.getBookingDate().toDate()));
        } else {
            holder.tvDate.setText("--/--/----");
        }

        // Trạng thái và màu sắc
        String status = booking.getStatus();
        holder.tvStatus.setText(status != null ? status : "Pending");
        
        if ("Completed".equalsIgnoreCase(status) || "Confirmed".equalsIgnoreCase(status)) {
            holder.tvStatus.setTextColor(Color.parseColor("#10B981")); // Xanh lá
            holder.btnConfirm.setVisibility(View.GONE);
            holder.btnCancel.setVisibility(View.GONE);
        } else if ("Cancelled".equalsIgnoreCase(status)) {
            holder.tvStatus.setTextColor(Color.parseColor("#EF4444")); // Đỏ
            holder.btnConfirm.setVisibility(View.GONE);
            holder.btnCancel.setVisibility(View.GONE);
        } else {
            holder.tvStatus.setTextColor(Color.parseColor("#F59E0B")); // Cam (Pending)
            holder.btnConfirm.setVisibility(View.VISIBLE);
            holder.btnCancel.setVisibility(View.VISIBLE);
        }

        // Tải tên khách hàng
        userController.getUserById(booking.getUserId(), user -> {
            if (user != null) {
                holder.tvUser.post(() -> holder.tvUser.setText("Khách: " + user.getFullName()));
            } else {
                holder.tvUser.post(() -> holder.tvUser.setText("Khách: Không xác định"));
            }
        }, e -> holder.tvUser.post(() -> holder.tvUser.setText("Khách: Lỗi tải")));

        // Tải tên sự kiện
        eventController.getEventById(booking.getEventId(), event -> {
            if (event != null) {
                holder.tvEvent.post(() -> holder.tvEvent.setText(event.getTitle()));
            } else {
                holder.tvEvent.post(() -> holder.tvEvent.setText("Sự kiện đã bị xóa"));
            }
        }, e -> holder.tvEvent.post(() -> holder.tvEvent.setText("Lỗi tải sự kiện")));

        holder.btnConfirm.setOnClickListener(v -> listener.onConfirm(booking));
        holder.btnCancel.setOnClickListener(v -> listener.onCancel(booking));
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvBookingId, tvUser, tvStatus, tvEvent, tvPrice, tvDate;
        MaterialButton btnCancel, btnConfirm;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBookingId = itemView.findViewById(R.id.tv_admin_booking_id);
            tvUser = itemView.findViewById(R.id.tv_admin_booking_user);
            tvStatus = itemView.findViewById(R.id.tv_admin_booking_status);
            tvEvent = itemView.findViewById(R.id.tv_admin_booking_event);
            tvPrice = itemView.findViewById(R.id.tv_admin_booking_price);
            tvDate = itemView.findViewById(R.id.tv_admin_booking_date);
            btnCancel = itemView.findViewById(R.id.btn_admin_cancel_booking);
            btnConfirm = itemView.findViewById(R.id.btn_admin_confirm_booking);
        }
    }
}
