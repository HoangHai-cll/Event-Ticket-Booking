package vn.humg.hai.event_ticket_booking_app.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.Timestamp;
import java.util.ArrayList;
import java.util.List;
import vn.humg.hai.event_ticket_booking_app.R;
import vn.humg.hai.event_ticket_booking_app.adapter.VoucherAdapter;
import vn.humg.hai.event_ticket_booking_app.controller.VoucherController;
import vn.humg.hai.event_ticket_booking_app.model.Voucher;

public class VoucherSelectDialog extends Dialog implements VoucherAdapter.OnVoucherClickListener {

    public interface OnVoucherSelectedListener {
        void onVoucherSelected(Voucher voucher);
    }

    private final String userId;
    private final double currentOrderPrice;
    private Voucher selectedVoucher;
    private Voucher tempSelectedVoucher;
    private final OnVoucherSelectedListener selectListener;

    private RecyclerView recyclerView;
    private VoucherAdapter adapter;
    private final List<Voucher> voucherList = new ArrayList<>();
    private final VoucherController voucherController = new VoucherController();

    public VoucherSelectDialog(@NonNull Context context, String userId, double currentOrderPrice, 
                               Voucher selectedVoucher, OnVoucherSelectedListener selectListener) {
        super(context);
        this.userId = userId;
        this.currentOrderPrice = currentOrderPrice;
        this.selectedVoucher = selectedVoucher;
        this.tempSelectedVoucher = selectedVoucher;
        this.selectListener = selectListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_voucher_select, null);
        setContentView(view);

        recyclerView = view.findViewById(R.id.recycler_dialog_vouchers);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new VoucherAdapter(voucherList, false, tempSelectedVoucher, this);
        recyclerView.setAdapter(adapter);

        view.findViewById(R.id.btn_cancel_select).setOnClickListener(v -> {
            if (selectListener != null) selectListener.onVoucherSelected(null);
            dismiss();
        });

        view.findViewById(R.id.btn_apply_select).setOnClickListener(v -> {
            if (selectListener != null) selectListener.onVoucherSelected(tempSelectedVoucher);
            dismiss();
        });

        loadUserVouchers();
    }

    private void loadUserVouchers() {
        voucherController.getUserVouchers(userId, list -> {
            List<Voucher> validVouchers = new ArrayList<>();
            long nowSec = System.currentTimeMillis() / 1000;

            for (Voucher v : list) {
                // Lọc voucher hết hạn
                if (v.getExpiryDate() != null && v.getExpiryDate().getSeconds() < nowSec) {
                    continue;
                }
                // Chỉ hiển thị các voucher thỏa điều kiện đơn hàng tối thiểu
                if (currentOrderPrice >= v.getMinOrderValue()) {
                    validVouchers.add(v);
                }
            }

            if (validVouchers.isEmpty()) {
                recyclerView.post(() -> Toast.makeText(getContext(), "Bạn không có voucher khả dụng cho đơn hàng này", Toast.LENGTH_SHORT).show());
            }

            recyclerView.post(() -> {
                voucherList.clear();
                voucherList.addAll(validVouchers);
                adapter.notifyDataSetChanged();
            });
        }, e -> recyclerView.post(() -> 
            Toast.makeText(getContext(), "Lỗi tải voucher: " + e, Toast.LENGTH_SHORT).show()
        ));
    }

    @Override
    public void onVoucherClick(Voucher voucher) {
        tempSelectedVoucher = voucher;
        adapter.setSelectedVoucher(tempSelectedVoucher);
    }

    @Override
    public void onDeleteClick(Voucher voucher) {
        // Không dùng
    }
}
