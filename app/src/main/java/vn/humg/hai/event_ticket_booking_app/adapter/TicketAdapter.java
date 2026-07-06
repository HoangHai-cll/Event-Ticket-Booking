package vn.humg.hai.event_ticket_booking_app.adapter;

import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import java.util.List;
import vn.humg.hai.event_ticket_booking_app.R;
import vn.humg.hai.event_ticket_booking_app.controller.EventController;
import vn.humg.hai.event_ticket_booking_app.controller.ReviewController;
import vn.humg.hai.event_ticket_booking_app.model.Booking;
import vn.humg.hai.event_ticket_booking_app.view.ReviewSubmitActivity;

import java.util.Map;
import vn.humg.hai.event_ticket_booking_app.model.Event;

public class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.TicketViewHolder> {
    private final List<Booking> bookings;
    private final Map<String, Event> eventCache;
    private final ReviewController reviewController = new ReviewController();
    private OnTicketClickListener listener;

    public interface OnTicketClickListener {
        void onTicketClick(Booking booking);
    }

    public TicketAdapter(List<Booking> bookings, Map<String, Event> eventCache, OnTicketClickListener listener) {
        this.bookings = bookings;
        this.eventCache = eventCache;
        this.listener = listener;
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
        
        Event event = eventCache != null ? eventCache.get(booking.getEventId()) : null;
        
        if (event != null) {
            boolean isExpired = event.getDate() != null
                    && event.getDate().getSeconds() < (System.currentTimeMillis() / 1000);

            // Load ảnh chung
            Glide.with(holder.itemView.getContext())
                    .load(event.getImage())
                    .placeholder(R.drawable.img_logo_event_ticket_booking)
                    .into(holder.ivImage);

            // Luôn hiện đủ các thành phần
            holder.tvTitle.setText(event.getTitle());
            holder.tvCategory.setVisibility(View.VISIBLE);
            holder.tvCategory.setText(event.getCategory() == null || event.getCategory().isEmpty() ? "Sự kiện" : event.getCategory());
            holder.tvDate.setVisibility(View.VISIBLE);
            holder.tvLocation.setVisibility(View.VISIBLE);
            holder.vDivider.setVisibility(View.VISIBLE);
            holder.llStatusContainer.setVisibility(View.VISIBLE);
            
            String dateStr = "Chưa xác định";
            if (event.getDate() != null) {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy • HH:mm", java.util.Locale.getDefault());
                dateStr = sdf.format(event.getDate().toDate());
            }
            holder.tvDate.setText(dateStr);
            holder.tvLocation.setText(event.getLocation());
            
            holder.tvId.setVisibility(View.VISIBLE);
            holder.tvId.setText(String.format("#%s", booking.getBookingId().substring(0, Math.min(8, booking.getBookingId().length())).toUpperCase()));
            holder.tvStatus.setText(booking.getStatus());

            if (isExpired) {
                // ── HẾT HẠN: giữ layout, chuyển xám, hiện badge ──
                holder.viewExpiredOverlay.setVisibility(View.VISIBLE);
                holder.tvExpiredBadge.setVisibility(View.VISIBLE);

                // Xám hóa ảnh
                ColorMatrix cm = new ColorMatrix();
                cm.setSaturation(0);
                holder.ivImage.setColorFilter(new ColorMatrixColorFilter(cm));

                // Xám hóa chữ
                int gray = ContextCompat.getColor(holder.itemView.getContext(), R.color.text_muted);
                holder.tvTitle.setTextColor(gray);
                holder.tvCategory.setTextColor(gray);
                holder.tvDate.setTextColor(gray);
                holder.tvLocation.setTextColor(gray);
                holder.tvId.setTextColor(gray);
                holder.tvStatus.setTextColor(gray);
                holder.tvStatus.setBackgroundColor(0xFFE5E7EB);
                holder.itemView.setAlpha(0.75f);
                holder.btnWriteReview.setVisibility(View.GONE);

                // Kiểm tra có thể viết đánh giá không
                boolean isConfirmed = "Confirmed".equalsIgnoreCase(booking.getStatus());
                if (isConfirmed) {
                    String userId = FirebaseAuth.getInstance().getUid();
                    if (userId != null) {
                        reviewController.hasUserReviewedEvent(event.getEventId(), userId, hasReviewed -> {
                            holder.btnWriteReview.post(() ->
                                holder.btnWriteReview.setVisibility(!hasReviewed ? View.VISIBLE : View.GONE)
                            );
                        }, e -> {});
                    }
                }
                holder.btnWriteReview.setOnClickListener(v -> {
                    Intent intent = new Intent(holder.itemView.getContext(), ReviewSubmitActivity.class);
                    intent.putExtra("EXTRA_EVENT_ID", event.getEventId());
                    holder.itemView.getContext().startActivity(intent);
                });

                holder.itemView.setOnClickListener(v -> {
                    Intent intent = new Intent(holder.itemView.getContext(),
                            vn.humg.hai.event_ticket_booking_app.view.EventDetailActivity.class);
                    intent.putExtra("EXTRA_EVENT_ID", event.getEventId());
                    holder.itemView.getContext().startActivity(intent);
                });

            } else {
                // ── CÒN HẠN: hiển thị bình thường ──
                holder.viewExpiredOverlay.setVisibility(View.GONE);
                holder.tvExpiredBadge.setVisibility(View.GONE);

                // Khôi phục màu sắc
                holder.ivImage.setColorFilter(null);
                holder.tvTitle.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.ink_dark));
                holder.tvCategory.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.brand_primary));
                int defaultColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.text_muted);
                holder.tvDate.setTextColor(defaultColor);
                holder.tvLocation.setTextColor(defaultColor);
                holder.tvId.setTextColor(defaultColor);
                holder.itemView.setAlpha(1.0f);
                holder.btnWriteReview.setVisibility(View.GONE);

                // Phase B: Hiển thị nhãn và màu theo trạng thái đặt vé
                String bookingStatus = booking.getStatus() != null ? booking.getStatus() : "";
                switch (bookingStatus) {
                    case "Refund Pending":
                        holder.tvStatus.setText("Chờ hoàn tiền");
                        holder.tvStatus.setTextColor(Color.parseColor("#D97706"));
                        holder.tvStatus.setBackgroundColor(Color.parseColor("#FEF3C7"));
                        break;
                    case "Cancelled":
                        holder.tvStatus.setText("Đã hủy");
                        holder.tvStatus.setTextColor(Color.parseColor("#6B7280"));
                        holder.tvStatus.setBackgroundColor(Color.parseColor("#F3F4F6"));
                        break;
                    case "Pending Payment":
                        holder.tvStatus.setText("Chờ thanh toán");
                        holder.tvStatus.setTextColor(Color.parseColor("#B45309"));
                        holder.tvStatus.setBackgroundColor(Color.parseColor("#FEF3C7"));
                        break;
                    default: // Confirmed
                        holder.tvStatus.setText("Đã xác nhận");
                        holder.tvStatus.setTextColor(0xFF10B981);
                        holder.tvStatus.setBackgroundColor(0xFFD1FAE5);
                        break;
                }

                holder.itemView.setOnClickListener(v -> {
                    if (listener != null) listener.onTicketClick(booking);
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    static class TicketViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage, ivQr;
        TextView tvTitle, tvDate, tvLocation, tvStatus, tvId, tvCategory;
        TextView tvExpiredBadge;
        View vDivider, viewExpiredOverlay;
        View llStatusContainer;
        MaterialButton btnWriteReview;

        public TicketViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_ticket_image);
            ivQr = itemView.findViewById(R.id.iv_ticket_qr);
            tvTitle = itemView.findViewById(R.id.tv_ticket_title);
            tvDate = itemView.findViewById(R.id.tv_ticket_date);
            tvLocation = itemView.findViewById(R.id.tv_ticket_location);
            tvStatus = itemView.findViewById(R.id.tv_ticket_status);
            tvId = itemView.findViewById(R.id.tv_ticket_id);
            tvCategory = itemView.findViewById(R.id.tv_ticket_category);
            vDivider = itemView.findViewById(R.id.v_ticket_divider);
            llStatusContainer = itemView.findViewById(R.id.ll_ticket_status_container);
            btnWriteReview = itemView.findViewById(R.id.btn_ticket_write_review);
            tvExpiredBadge = itemView.findViewById(R.id.tv_expired_badge);
            viewExpiredOverlay = itemView.findViewById(R.id.view_expired_overlay);
        }
    }
}
