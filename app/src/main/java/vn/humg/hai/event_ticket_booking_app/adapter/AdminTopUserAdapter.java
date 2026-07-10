package vn.humg.hai.event_ticket_booking_app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import vn.humg.hai.event_ticket_booking_app.R;

public class AdminTopUserAdapter extends RecyclerView.Adapter<AdminTopUserAdapter.ViewHolder> {
    private final List<Map.Entry<String, Double>> topUsers;
    private final Map<String, String> userNames;

    public AdminTopUserAdapter(List<Map.Entry<String, Double>> topUsers, Map<String, String> userNames) {
        this.topUsers = topUsers;
        this.userNames = userNames;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_top_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map.Entry<String, Double> entry = topUsers.get(position);
        holder.tvRank.setText(String.valueOf(position + 1));
        
        String name = userNames.get(entry.getKey());
        String userId = entry.getKey();
        holder.tvName.setText(name != null ? name : "User #" + (userId.length() > 5 ? userId.substring(0, 5) : userId));
        
        holder.tvTotalSpent.setText(String.format(Locale.getDefault(), "%,.0fđ", entry.getValue()));
    }

    @Override
    public int getItemCount() {
        return topUsers.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank, tvName, tvTotalSpent;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tv_user_rank);
            tvName = itemView.findViewById(R.id.tv_user_display_name);
            tvTotalSpent = itemView.findViewById(R.id.tv_user_total_spent);
        }
    }
}
