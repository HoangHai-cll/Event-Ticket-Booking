package vn.humg.hai.event_ticket_booking_app.adapter;

import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import vn.humg.hai.event_ticket_booking_app.R;
import vn.humg.hai.event_ticket_booking_app.controller.EventController;
import vn.humg.hai.event_ticket_booking_app.model.Booking;
import vn.humg.hai.event_ticket_booking_app.view.ReviewSubmitActivity;
import vn.humg.hai.event_ticket_booking_app.controller.ReviewController;
import com.google.firebase.auth.FirebaseAuth;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {
    private final List<Booking> transactions;
    private final EventController eventController = new EventController();
    private final ReviewController reviewController = new ReviewController();

    public TransactionAdapter(List<Booking> transactions) {
        this.transactions = transactions;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Booking booking = transactions.get(position);
        
        // 1. Hiển thị Mã đơn và Giá tiền (Sử dụng string resource)
        holder.tvId.setText(String.format(Locale.getDefault(), "#%s", booking.getBookingId().toUpperCase()));
        
        String priceText = holder.itemView.getContext().getString(R.string.price_format, (long) booking.getTotalPrice());
        holder.tvPrice.setText(priceText);
        
        // 2. Hiển thị số lượng vé
        holder.tvQuantity.setText(String.format(Locale.getDefault(), "x%d vé", booking.getQuantity()));
        
        // 3. Định dạng ngày giờ
        if (booking.getBookingDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault());
            holder.tvTime.setText(sdf.format(booking.getBookingDate().toDate()));
        }

        // 4. Trạng thái, màu sắc và Nút đánh giá
        String status = booking.getStatus();
        boolean isCompleted = "Completed".equalsIgnoreCase(status) || "Hoàn thành".equalsIgnoreCase(status);
        String displayStatus = status;
        int statusColor = Color.parseColor("#F59E0B"); // default warning yellow
        
        if ("Completed".equalsIgnoreCase(status) || "Hoàn thành".equalsIgnoreCase(status)) {
            displayStatus = "Hoàn thành";
            statusColor = Color.parseColor("#10B981");
        } else if ("Confirmed".equalsIgnoreCase(status) || "Đã xác nhận".equalsIgnoreCase(status)) {
            displayStatus = "Đã xác nhận";
            statusColor = Color.parseColor("#10B981");
        } else if ("Cancelled".equalsIgnoreCase(status) || "Đã hủy".equalsIgnoreCase(status)) {
            displayStatus = "Đã hủy";
            statusColor = Color.parseColor("#EF4444");
        } else if ("Refund Pending".equalsIgnoreCase(status) || "Chờ hoàn tiền".equalsIgnoreCase(status)) {
            displayStatus = "Chờ hoàn tiền";
            statusColor = Color.parseColor("#F59E0B");
        } else if ("Pending Payment".equalsIgnoreCase(status) || "Chờ thanh toán".equalsIgnoreCase(status)) {
            displayStatus = "Chờ thanh toán";
            statusColor = Color.parseColor("#B45309");
        }
        
        holder.tvStatus.setTextColor(statusColor);
        holder.tvStatus.setText("● " + displayStatus);
        
        // Ẩn nút đánh giá ban đầu, chỉ hiện khi đã check điều kiện thời gian sự kiện
        holder.btnRate.setVisibility(View.GONE);

        // Xử lý sự kiện nhấn nút Đánh giá
        holder.btnRate.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), ReviewSubmitActivity.class);
            intent.putExtra("EXTRA_EVENT_ID", booking.getEventId());
            holder.itemView.getContext().startActivity(intent);
        });

        // 5. Lấy thông tin chi tiết sự kiện
        eventController.getEventById(booking.getEventId(), event -> {
            if (event != null) {
                holder.tvTitle.post(() -> {
                    holder.tvTitle.setText(event.getTitle());
                    
                    boolean isExpired = false;
                    if (event.getDate() != null) {
                        isExpired = event.getDate().getSeconds() < (System.currentTimeMillis() / 1000);
                    }
                    if (isCompleted && isExpired) {
                        String userId = FirebaseAuth.getInstance().getUid();
                        if (userId != null) {
                            reviewController.hasUserReviewedEvent(event.getEventId(), userId, hasReviewed -> {
                                holder.btnRate.post(() -> {
                                    if (!hasReviewed) {
                                        holder.btnRate.setVisibility(View.VISIBLE);
                                    } else {
                                        holder.btnRate.setVisibility(View.GONE);
                                    }
                                });
                            }, e -> {});
                        }
                    } else {
                        holder.btnRate.setVisibility(View.GONE);
                    }
                    
                    Glide.with(holder.itemView.getContext())
                            .load(event.getImage())
                            .placeholder(R.drawable.img_logo_event_ticket_booking)
                            .error(R.drawable.img_logo_event_ticket_booking)
                            .centerCrop()
                            .into(holder.ivIcon);
                });
            }
        }, error -> {
            holder.tvTitle.post(() -> holder.tvTitle.setText("Không rõ sự kiện"));
        });
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvTime, tvStatus, tvPrice, tvId, tvQuantity;
        ImageView ivIcon;
        MaterialButton btnRate;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_transaction_title);
            tvTime = itemView.findViewById(R.id.tv_transaction_time);
            tvStatus = itemView.findViewById(R.id.tv_transaction_status);
            tvPrice = itemView.findViewById(R.id.tv_transaction_price);
            tvId = itemView.findViewById(R.id.tv_transaction_id);
            tvQuantity = itemView.findViewById(R.id.tv_transaction_quantity);
            ivIcon = itemView.findViewById(R.id.iv_transaction_icon);
            btnRate = itemView.findViewById(R.id.btn_transaction_rate);
        }
    }
}
