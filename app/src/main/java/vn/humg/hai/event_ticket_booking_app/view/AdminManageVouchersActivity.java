package vn.humg.hai.event_ticket_booking_app.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;
import vn.humg.hai.event_ticket_booking_app.R;
import vn.humg.hai.event_ticket_booking_app.adapter.VoucherAdapter;
import vn.humg.hai.event_ticket_booking_app.controller.VoucherController;
import vn.humg.hai.event_ticket_booking_app.model.Voucher;

public class AdminManageVouchersActivity extends AppCompatActivity implements VoucherAdapter.OnVoucherClickListener {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private FloatingActionButton fabAdd;
    private VoucherAdapter adapter;
    private final List<Voucher> voucherList = new ArrayList<>();
    private final VoucherController voucherController = new VoucherController();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage_vouchers);

        initViews();
        setupEvents();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadVouchers();
    }

    private void initViews() {
        findViewById(R.id.btn_back_custom).setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        recyclerView = findViewById(R.id.recycler_vouchers);
        swipeRefresh = findViewById(R.id.swipe_refresh_vouchers);
        fabAdd = findViewById(R.id.fab_add_voucher);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new VoucherAdapter(voucherList, true, null, this);
        recyclerView.setAdapter(adapter);
    }

    private void setupEvents() {
        swipeRefresh.setOnRefreshListener(this::loadVouchers);
        fabAdd.setOnClickListener(v -> startActivity(new Intent(this, AdminAddVoucherActivity.class)));
    }

    private void loadVouchers() {
        swipeRefresh.setRefreshing(true);
        voucherController.getAllSystemVouchers(list -> runOnUiThread(() -> {
            swipeRefresh.setRefreshing(false);
            voucherList.clear();
            voucherList.addAll(list);
            adapter.notifyDataSetChanged();
        }), error -> runOnUiThread(() -> {
            swipeRefresh.setRefreshing(false);
            Toast.makeText(this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
        }));
    }

    @Override
    public void onVoucherClick(Voucher voucher) {
        // Không dùng trong Admin
    }

    @Override
    public void onDeleteClick(Voucher voucher) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa Voucher")
                .setMessage("Bạn có chắc chắn muốn xóa mã Voucher " + voucher.getCode() + " khỏi hệ thống?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    voucherController.deleteSystemVoucher(voucher.getVoucherId(), () -> runOnUiThread(() -> {
                        Toast.makeText(this, "Đã xóa voucher thành công", Toast.LENGTH_SHORT).show();
                        loadVouchers();
                    }), error -> runOnUiThread(() -> 
                        Toast.makeText(this, "Lỗi khi xóa: " + error, Toast.LENGTH_SHORT).show()
                    ));
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
