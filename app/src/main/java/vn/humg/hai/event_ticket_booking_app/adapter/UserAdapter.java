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

    public UserAdapter(List<User> users) {
        this.users = users;
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
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAvatarChar, tvName, tvEmail, tvRole;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAvatarChar = itemView.findViewById(R.id.tv_user_avatar_char);
            tvName = itemView.findViewById(R.id.tv_user_item_name);
            tvEmail = itemView.findViewById(R.id.tv_user_item_email);
            tvRole = itemView.findViewById(R.id.tv_user_item_role);
        }
    }
}
