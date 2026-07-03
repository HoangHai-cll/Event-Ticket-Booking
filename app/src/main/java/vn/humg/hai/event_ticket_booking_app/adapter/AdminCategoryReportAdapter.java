package vn.humg.hai.event_ticket_booking_app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import vn.humg.hai.event_ticket_booking_app.R;

public class AdminCategoryReportAdapter extends RecyclerView.Adapter<AdminCategoryReportAdapter.ViewHolder> {
    
    public static class CategoryStats {
        public String name;
        public double revenue;
        public float avgRating;
        public int reviewCount;

        public CategoryStats(String name) {
            this.name = name;
            this.revenue = 0;
            this.avgRating = 0;
            this.reviewCount = 0;
        }
    }

    private final List<CategoryStats> statsList;

    public AdminCategoryReportAdapter(Map<String, CategoryStats> data) {
        this.statsList = new ArrayList<>(data.values());
        // Sắp xếp theo doanh thu giảm dần
        this.statsList.sort((s1, s2) -> Double.compare(s2.revenue, s1.revenue));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_category_report, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CategoryStats stats = statsList.get(position);
        holder.tvName.setText(stats.name);
        holder.tvRevenue.setText(String.format(Locale.getDefault(), "%,.0fđ", stats.revenue));
        
        if (stats.reviewCount > 0) {
            holder.tvRating.setText(String.format(Locale.getDefault(), "⭐ %.1f", stats.avgRating));
            holder.tvRating.setVisibility(View.VISIBLE);
        } else {
            holder.tvRating.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return statsList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvRating, tvRevenue;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_report_category_name);
            tvRating = itemView.findViewById(R.id.tv_report_category_rating);
            tvRevenue = itemView.findViewById(R.id.tv_report_category_revenue);
        }
    }
}
