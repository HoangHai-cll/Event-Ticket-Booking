package vn.humg.hai.event_ticket_booking_app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import vn.humg.hai.event_ticket_booking_app.R;
import vn.humg.hai.event_ticket_booking_app.model.User;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    private final List<User> users;
    private final OnUserClickListener clickListener;

    // Phase C: Interface callback khi Admin nhấp vào một User để chỉnh sửa EXP/Tier
    public interface OnUserClickListener {
        void onUserClick(User user);
    }

    public UserAdapter(List<User> users, OnUserClickListener clickListener) {
        this.users = users;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = users.get(position);
        holder.tvName.setText(user.getFullName());
        holder.tvEmail.setText(user.getEmail());
        holder.tvRole.setText(user.getRole().toUpperCase());

        // Hiển thị chữ cái đầu của tên làm avatar
        if (user.getFullName() != null && !user.getFullName().isEmpty()) {
            holder.tvAvatarChar.setText(user.getFullName().substring(0, 1).toUpperCase());
        }

        // Phase C: Hiển thị Hạng thành viên và Điểm EXP
        String tier = user.getMemberTier() != null ? user.getMemberTier() : "Thường";
        String tierIcon = getTierIcon(tier);
        holder.tvTierExp.setText(tierIcon + " " + tier + " • EXP: " + user.getExp());

        // Phase C: Gán click listener để mở dialog Quick Edit
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onUserClick(user);
        });
    }

    private String getTierIcon(String tier) {
        switch (tier) {
            case "Đồng": return "🥉";
            case "Bạc": return "🥈";
            case "Vàng": return "🥇";
            case "Thân thiết số một": return "👑";
            default: return "🏅";
        }
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAvatarChar, tvName, tvEmail, tvRole, tvTierExp;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAvatarChar = itemView.findViewById(R.id.tv_user_avatar_char);
            tvName = itemView.findViewById(R.id.tv_user_item_name);
            tvEmail = itemView.findViewById(R.id.tv_user_item_email);
            tvRole = itemView.findViewById(R.id.tv_user_item_role);
            tvTierExp = itemView.findViewById(R.id.tv_user_item_tier_exp);
        }
    }
}
