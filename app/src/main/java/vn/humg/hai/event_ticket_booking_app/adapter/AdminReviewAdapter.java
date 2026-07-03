package vn.humg.hai.event_ticket_booking_app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import vn.humg.hai.event_ticket_booking_app.R;
import vn.humg.hai.event_ticket_booking_app.model.Review;

public class AdminReviewAdapter extends RecyclerView.Adapter<AdminReviewAdapter.ViewHolder> {
    private final List<Review> reviews;
    private final OnReviewActionListener listener;

    public interface OnReviewActionListener {
        void onDelete(Review review);
    }

    public AdminReviewAdapter(List<Review> reviews, OnReviewActionListener listener) {
        this.reviews = reviews;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_review, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Review review = reviews.get(position);
        holder.tvUserName.setText(review.getUserName() != null ? review.getUserName() : "Ẩn danh");
        holder.tvComment.setText(review.getComment());
        holder.rbRating.setRating(review.getRating());
        
        if (review.getCreatedAt() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            holder.tvDate.setText(sdf.format(review.getCreatedAt().toDate()));
        }

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(review);
        });
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvComment, tvDate;
        RatingBar rbRating;
        ImageButton btnDelete;
        
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tv_review_user_name);
            tvComment = itemView.findViewById(R.id.tv_review_comment);
            tvDate = itemView.findViewById(R.id.tv_review_date);
            rbRating = itemView.findViewById(R.id.rb_review_rating);
            btnDelete = itemView.findViewById(R.id.btn_delete_review);
        }
    }
}
