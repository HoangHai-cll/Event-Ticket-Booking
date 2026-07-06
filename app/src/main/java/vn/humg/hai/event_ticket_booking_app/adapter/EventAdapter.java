package vn.humg.hai.event_ticket_booking_app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import vn.humg.hai.event_ticket_booking_app.R;
import vn.humg.hai.event_ticket_booking_app.controller.FavoriteController;
import vn.humg.hai.event_ticket_booking_app.model.Event;
import vn.humg.hai.event_ticket_booking_app.model.Favorite;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {
    private final List<Event> events;
    private final OnEventClickListener listener;
    private final FavoriteController favoriteController = new FavoriteController();
    private final Set<String> favoriteEventIds = new HashSet<>(); 
    private boolean isHorizontal = false;
    
    private OnFavoriteChangeListener favoriteListener;

    public interface OnEventClickListener {
        void onEventClick(Event event);
    }

    public interface OnFavoriteChangeListener {
        void onFavoriteChanged(String eventId, boolean isAdded);
    }

    public EventAdapter(List<Event> events, OnEventClickListener listener) {
        this.events = events;
        this.listener = listener;
    }

    public EventAdapter(List<Event> events, boolean isHorizontal, OnEventClickListener listener) {
        this.events = events;
        this.isHorizontal = isHorizontal;
        this.listener = listener;
    }

    public void setOnFavoriteChangeListener(OnFavoriteChangeListener listener) {
        this.favoriteListener = listener;
    }

    public void setFavoriteEventIds(List<String> ids) {
        this.favoriteEventIds.clear();
        this.favoriteEventIds.addAll(ids);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent, false);
        if (isHorizontal) {
            ViewGroup.LayoutParams params = view.getLayoutParams();
            params.width = (int) (parent.getResources().getDisplayMetrics().widthPixels * 0.85);
            view.setLayoutParams(params);
        }
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);
        holder.title.setText(event.getTitle());
        holder.location.setText(event.getLocation());
        holder.time.setText(String.format("⏰ %s", formatEventTime(event)));
        holder.dateTag.setText(formatDateTag(event));
        holder.price.setText(String.format(Locale.getDefault(), "%,.0fđ", event.getPrice()));
        
        // Đổ dữ liệu Đánh giá (Rating)
        holder.rbRating.setRating(event.getAverageRating());
        holder.tvReviewCount.setText(String.format(Locale.getDefault(), "(%d đánh giá)", event.getReviewCount()));

        Glide.with(holder.itemView.getContext())
                .load(event.getImage())
                .placeholder(R.drawable.img_logo_event_ticket_booking)
                .centerCrop()
                .into(holder.image);

        boolean isExpired = false;
        if (event.getDate() != null) {
            isExpired = event.getDate().getSeconds() < (System.currentTimeMillis() / 1000);
        }

        if (isExpired) {
            holder.flImageContainer.setVisibility(View.GONE);
            holder.llRatingContainer.setVisibility(View.GONE);
            holder.llActionContainer.setVisibility(View.GONE);
            holder.time.setVisibility(View.GONE);
            holder.location.setVisibility(View.GONE);
            
            holder.title.setText(event.getTitle() + " (Đã qua thời gian tổ chức)");
            holder.title.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.text_muted));
            holder.title.setTextSize(14f);
            holder.title.setTypeface(null, android.graphics.Typeface.NORMAL);
            
            holder.itemView.setAlpha(0.6f);
        } else {
            holder.flImageContainer.setVisibility(View.VISIBLE);
            holder.llRatingContainer.setVisibility(View.VISIBLE);
            holder.llActionContainer.setVisibility(View.VISIBLE);
            holder.time.setVisibility(View.VISIBLE);
            holder.location.setVisibility(View.VISIBLE);
            
            holder.title.setText(event.getTitle());
            holder.title.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.ink_dark));
            holder.title.setTextSize(18f);
            holder.title.setTypeface(null, android.graphics.Typeface.BOLD);
            
            holder.itemView.setAlpha(1.0f);
        }

        boolean isFav = favoriteEventIds.contains(event.getEventId());
        if (isFav) {
            holder.tvFavorite.setImageResource(R.drawable.ic_heart_filled);
            holder.tvFavorite.setColorFilter(0xFFFF69B4); // Pink color
        } else {
            holder.tvFavorite.setImageResource(R.drawable.ic_heart);
            holder.tvFavorite.setColorFilter(0xFFB0BEC5); // Gray color
        }

        holder.tvFavorite.setOnClickListener(v -> handleFavoriteClick(event, holder.tvFavorite));
        holder.itemView.setOnClickListener(v -> listener.onEventClick(event));
        holder.bookButton.setOnClickListener(v -> listener.onEventClick(event));
    }

    private void handleFavoriteClick(Event event, ImageView ivFav) {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) {
            Toast.makeText(ivFav.getContext(), "Vui lòng đăng nhập để thích sự kiện", Toast.LENGTH_SHORT).show();
            return;
        }

        String eventId = event.getEventId();
        if (favoriteEventIds.contains(eventId)) {
            favoriteController.removeFromFavorite(userId, eventId, () -> {
                ivFav.post(() -> {
                    favoriteEventIds.remove(eventId);
                    ivFav.setImageResource(R.drawable.ic_heart);
                    ivFav.setColorFilter(0xFFB0BEC5); // Gray color
                    Toast.makeText(ivFav.getContext(), "Đã bỏ thích sự kiện", Toast.LENGTH_SHORT).show();
                    if (favoriteListener != null) {
                        favoriteListener.onFavoriteChanged(eventId, false);
                    }
                });
            }, error -> {
                ivFav.post(() -> Toast.makeText(ivFav.getContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show());
            });
        } else {
            Favorite fav = new Favorite(userId + "_" + eventId, userId, eventId);
            favoriteController.addToFavorite(fav, () -> {
                ivFav.post(() -> {
                    favoriteEventIds.add(eventId);
                    ivFav.setImageResource(R.drawable.ic_heart_filled);
                    ivFav.setColorFilter(0xFFFF69B4); // Pink color
                    Toast.makeText(ivFav.getContext(), "Đã thích sự kiện", Toast.LENGTH_SHORT).show();
                    if (favoriteListener != null) {
                        favoriteListener.onFavoriteChanged(eventId, true);
                    }
                });
            }, error -> {
                ivFav.post(() -> Toast.makeText(ivFav.getContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show());
            });
        }
    }

    @Override
    public int getItemCount() { return events.size(); }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        final ImageView image, tvFavorite;
        final TextView title, location, time, price, dateTag, bookButton, tvReviewCount;
        final RatingBar rbRating;
        final View flImageContainer, llRatingContainer, llActionContainer;

        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.iv_item_image);
            title = itemView.findViewById(R.id.tv_item_title);
            location = itemView.findViewById(R.id.tv_item_location);
            time = itemView.findViewById(R.id.tv_item_time);
            price = itemView.findViewById(R.id.tv_item_price);
            dateTag = itemView.findViewById(R.id.tv_item_date_tag);
            bookButton = itemView.findViewById(R.id.tv_item_button);
            tvFavorite = itemView.findViewById(R.id.tv_item_favorite);
            rbRating = itemView.findViewById(R.id.rb_item_rating);
            tvReviewCount = itemView.findViewById(R.id.tv_item_review_count);
            flImageContainer = itemView.findViewById(R.id.fl_item_image_container);
            llRatingContainer = itemView.findViewById(R.id.ll_item_rating_container);
            llActionContainer = itemView.findViewById(R.id.ll_item_action_container);
        }
    }

    private String formatEventTime(Event event) {
        if (event.getDate() == null) return "Chưa xác định";
        return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(event.getDate().toDate());
    }

    private String formatDateTag(Event event) {
        if (event.getDate() == null) return "";
        return new SimpleDateFormat("dd 'Tháng' MM", Locale.getDefault()).format(event.getDate().toDate());
    }
}
