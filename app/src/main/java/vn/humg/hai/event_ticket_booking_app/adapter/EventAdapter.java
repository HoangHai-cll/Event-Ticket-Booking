package vn.humg.hai.event_ticket_booking_app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import vn.humg.hai.event_ticket_booking_app.R;
import vn.humg.hai.event_ticket_booking_app.model.Event;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {
    
    public static final int VIEW_TYPE_HOT = 1;
    public static final int VIEW_TYPE_STANDARD = 2;
    public static final int VIEW_TYPE_COMPACT = 3;

    private final List<Event> events;
    private final OnEventClickListener listener;
    private final OnFavoriteClickListener favoriteClickListener;
    private final Set<String> favoriteEventIds = new HashSet<>(); 
    private final boolean isHotSection;
    private boolean useCompactLayout = false;
    
    private OnFavoriteChangeListener favoriteListener;

    public interface OnEventClickListener {
        void onEventClick(Event event, ImageView imageView);
    }

    public interface OnFavoriteChangeListener {
        void onFavoriteChanged(String eventId, boolean isAdded);
    }

    public interface OnFavoriteClickListener {
        void onFavoriteClick(Event event);
    }

    public EventAdapter(List<Event> events, OnEventClickListener listener, OnFavoriteClickListener favoriteClickListener) {
        this(events, false, listener, favoriteClickListener);
    }

    public EventAdapter(List<Event> events, boolean isHotSection, OnEventClickListener listener, OnFavoriteClickListener favoriteClickListener) {
        this.events = events;
        this.isHotSection = isHotSection;
        this.listener = listener;
        this.favoriteClickListener = favoriteClickListener;
    }

    public void setUseCompactLayout(boolean useCompact) {
        this.useCompactLayout = useCompact;
        notifyDataSetChanged();
    }

    public void setOnFavoriteChangeListener(OnFavoriteChangeListener listener) {
        this.favoriteListener = listener;
    }

    public void setFavoriteEventIds(List<String> ids) {
        this.favoriteEventIds.clear();
        this.favoriteEventIds.addAll(ids);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (isHotSection) return VIEW_TYPE_HOT;
        return useCompactLayout ? VIEW_TYPE_COMPACT : VIEW_TYPE_STANDARD;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutId;
        if (viewType == VIEW_TYPE_HOT) {
            layoutId = R.layout.item_event_hot;
        } else if (viewType == VIEW_TYPE_COMPACT) {
            layoutId = R.layout.item_event_compact;
        } else {
            layoutId = R.layout.item_event;
        }

        View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        
        if (viewType == VIEW_TYPE_HOT) {
            ViewGroup.LayoutParams params = view.getLayoutParams();
            params.width = (int) (parent.getResources().getDisplayMetrics().widthPixels * 0.7);
            view.setLayoutParams(params);
        }
        
        return new EventViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);
        
        if (holder.title != null) holder.title.setText(event.getTitle());
        if (holder.location != null) holder.location.setText(event.getLocation());
        if (holder.time != null) holder.time.setText(String.format("⏰ %s", formatEventTime(event)));
        if (holder.dateTag != null) holder.dateTag.setText(formatDateTag(event));
        if (holder.price != null) holder.price.setText(String.format(Locale.getDefault(), "%,.0fđ", event.getPrice()));
        
        if (holder.rbRating != null) holder.rbRating.setRating(event.getAverageRating());
        if (holder.tvReviewCount != null) holder.tvReviewCount.setText(String.format(Locale.getDefault(), "(%d đánh giá)", event.getReviewCount()));

        if (holder.image != null) {
            Glide.with(holder.itemView.getContext())
                    .load(event.getImage())
                    .placeholder(R.drawable.img_logo_event_ticket_booking)
                    .centerCrop()
                    .into(holder.image);
            androidx.core.view.ViewCompat.setTransitionName(holder.image, "event_image_" + event.getEventId() + "_" + holder.viewType);
        }

        if (holder.tvFavorite != null) {
            boolean isFav = favoriteEventIds.contains(event.getEventId());
            if (isFav) {
                holder.tvFavorite.setImageResource(R.drawable.ic_heart_filled);
                holder.tvFavorite.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), R.color.brand_primary));
            } else {
                holder.tvFavorite.setImageResource(R.drawable.ic_heart);
                holder.tvFavorite.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), R.color.text_muted));
            }
            holder.tvFavorite.setOnClickListener(v -> {
                if (favoriteClickListener != null) favoriteClickListener.onFavoriteClick(event);
            });
        }

        holder.itemView.setOnClickListener(v -> listener.onEventClick(event, holder.image));
        if (holder.bookButton != null) {
            holder.bookButton.setOnClickListener(v -> listener.onEventClick(event, holder.image));
        }
    }

    @Override
    public int getItemCount() { return events.size(); }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        final int viewType;
        ImageView image, tvFavorite;
        TextView title, location, time, price, dateTag, bookButton, tvReviewCount;
        RatingBar rbRating;
        View flImageContainer, llRatingContainer, llActionContainer;

        EventViewHolder(@NonNull View itemView, int viewType) {
            super(itemView);
            this.viewType = viewType;
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
