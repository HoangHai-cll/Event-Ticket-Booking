package vn.humg.hai.event_ticket_booking_app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import vn.humg.hai.event_ticket_booking_app.R;
import vn.humg.hai.event_ticket_booking_app.model.Voucher;

public class VoucherAdapter extends RecyclerView.Adapter<VoucherAdapter.VoucherViewHolder> {

    public interface OnVoucherClickListener {
        void onVoucherClick(Voucher voucher);
        void onDeleteClick(Voucher voucher);
    }

    private final List<Voucher> vouchers;
    private final boolean isAdminMode;
    private boolean isViewOnly = false;
    private Voucher selectedVoucher;
    private final OnVoucherClickListener listener;

    public VoucherAdapter(List<Voucher> vouchers, boolean isAdminMode, Voucher selectedVoucher, OnVoucherClickListener listener) {
        this.vouchers = vouchers;
        this.isAdminMode = isAdminMode;
        this.selectedVoucher = selectedVoucher;
        this.listener = listener;
        this.isViewOnly = false;
    }

    public VoucherAdapter(List<Voucher> vouchers) {
        this.vouchers = vouchers;
        this.isAdminMode = false;
        this.selectedVoucher = null;
        this.listener = null;
        this.isViewOnly = true;
    }

    public void setSelectedVoucher(Voucher voucher) {
        this.selectedVoucher = voucher;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VoucherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_voucher_select, parent, false);
        return new VoucherViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VoucherViewHolder holder, int position) {
        Voucher voucher = vouchers.get(position);

        holder.tvTitle.setText(voucher.getTitle());

        String discountDesc = "";
        if ("PERCENT".equalsIgnoreCase(voucher.getDiscountType())) {
            discountDesc = "Mức giảm tối đa: " + formatPrice(voucher.getMaxDiscountAmount());
        } else {
            discountDesc = "Giảm giá trực tiếp: " + formatPrice(voucher.getDiscountValue());
        }
        holder.tvCondition.setText(String.format("Đơn tối thiểu từ %s • %s", 
                formatPrice(voucher.getMinOrderValue()), discountDesc));

        if (voucher.getExpiryDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm 'ngày' dd/MM/yyyy", Locale.getDefault());
            holder.tvExpiry.setText("HSD: " + sdf.format(voucher.getExpiryDate().toDate()));
        } else {
            holder.tvExpiry.setText("Không giới hạn HSD");
        }

        if (isViewOnly) {
            holder.btnDelete.setVisibility(View.GONE);
            holder.rbSelect.setVisibility(View.GONE);
            holder.itemView.setClickable(false);
        } else if (isAdminMode) {
            holder.btnDelete.setVisibility(View.VISIBLE);
            holder.rbSelect.setVisibility(View.GONE);
            holder.btnDelete.setOnClickListener(v -> {
                if (listener != null) listener.onDeleteClick(voucher);
            });
        } else {
            holder.btnDelete.setVisibility(View.GONE);
            holder.rbSelect.setVisibility(View.VISIBLE);

            boolean isSelected = selectedVoucher != null && selectedVoucher.getVoucherId().equals(voucher.getVoucherId());
            holder.rbSelect.setChecked(isSelected);

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onVoucherClick(voucher);
            });
        }
    }

    @Override
    public int getItemCount() {
        return vouchers.size();
    }

    private String formatPrice(double price) {
        return String.format(Locale.getDefault(), "%,.0fđ", price);
    }

    static class VoucherViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvCondition, tvExpiry;
        RadioButton rbSelect;
        MaterialButton btnDelete;

        public VoucherViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_voucher_title);
            tvCondition = itemView.findViewById(R.id.tv_voucher_condition);
            tvExpiry = itemView.findViewById(R.id.tv_voucher_expiry);
            rbSelect = itemView.findViewById(R.id.rb_voucher_select);
            btnDelete = itemView.findViewById(R.id.btn_delete_voucher);
        }
    }
}
