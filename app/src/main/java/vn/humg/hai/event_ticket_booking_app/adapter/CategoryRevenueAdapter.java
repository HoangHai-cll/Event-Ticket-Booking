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

public class CategoryRevenueAdapter extends RecyclerView.Adapter<CategoryRevenueAdapter.ViewHolder> {
    private final List<Map.Entry<String, Double>> categoryData;

    public CategoryRevenueAdapter(Map<String, Double> data) {
        this.categoryData = new ArrayList<>(data.entrySet());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category_revenue, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map.Entry<String, Double> entry = categoryData.get(position);
        holder.tvCategoryName.setText(entry.getKey());
        holder.tvRevenue.setText(String.format(Locale.getDefault(), "%,.0fđ", entry.getValue()));
    }

    @Override
    public int getItemCount() {
        return categoryData.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategoryName, tvRevenue;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryName = itemView.findViewById(R.id.tv_category_name);
            tvRevenue = itemView.findViewById(R.id.tv_category_revenue);
        }
    }
}
