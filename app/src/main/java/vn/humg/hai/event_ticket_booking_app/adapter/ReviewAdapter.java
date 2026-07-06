package vn.humg.hai.event_ticket_booking_app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import vn.humg.hai.event_ticket_booking_app.R;
import vn.humg.hai.event_ticket_booking_app.controller.UserController;
import vn.humg.hai.event_ticket_booking_app.model.Review;
import vn.humg.hai.event_ticket_booking_app.model.User;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {
    private final List<Review> reviews;

    public ReviewAdapter(List<Review> reviews) {
        this.reviews = reviews;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Review review = reviews.get(position);
        holder.tvName.setText(review.getUserName() != null ? review.getUserName() : "Người dùng");
        holder.tvComment.setText(review.getComment());
        holder.rbRating.setRating(review.getRating());
        
        if (review.getCreatedAt() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            holder.tvDate.setText(sdf.format(review.getCreatedAt().toDate()));
        }
        
        if (review.getImageUrl() != null && !review.getImageUrl().isEmpty()) {
            holder.cardImage.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView.getContext())
                    .load(review.getImageUrl())
                    .centerCrop()
                    .into(holder.ivImage);
        } else {
            holder.cardImage.setVisibility(View.GONE);
        }

        // Load reviewer's profile avatar dynamically
        String reviewerUid = review.getUserId();
        if (reviewerUid != null && !reviewerUid.isEmpty()) {
            UserController userController = new UserController();
            userController.getUserById(reviewerUid, user -> {
                if (user != null) {
                    String avatarName = user.getAvatarName();
                    holder.ivUserAvatar.post(() -> {
                        if (avatarName != null && !avatarName.isEmpty()) {
                            Glide.with(holder.itemView.getContext())
                                    .load(avatarName)
                                    .circleCrop()
                                    .placeholder(R.drawable.img_logo_event_ticket_booking)
                                    .into(holder.ivUserAvatar);
                        } else {
                            Glide.with(holder.itemView.getContext())
                                    .load(R.drawable.img_logo_event_ticket_booking)
                                    .circleCrop()
                                    .into(holder.ivUserAvatar);
                        }
                    });
                }
            }, error -> {});
        } else {
            Glide.with(holder.itemView.getContext())
                    .load(R.drawable.img_logo_event_ticket_booking)
                    .circleCrop()
                    .into(holder.ivUserAvatar);
        }
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvComment, tvDate;
        RatingBar rbRating;
        ImageView ivImage, ivUserAvatar;
        MaterialCardView cardImage;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_review_item_name);
            tvComment = itemView.findViewById(R.id.tv_review_item_comment);
            tvDate = itemView.findViewById(R.id.tv_review_item_date);
            rbRating = itemView.findViewById(R.id.rb_review_item_rating);
            ivImage = itemView.findViewById(R.id.iv_review_item_image);
            cardImage = itemView.findViewById(R.id.card_review_item_image);
            ivUserAvatar = itemView.findViewById(R.id.iv_review_user_avatar);
        }
    }
}
