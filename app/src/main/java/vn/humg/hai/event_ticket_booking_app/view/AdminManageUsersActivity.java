package vn.humg.hai.event_ticket_booking_app.view;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import vn.humg.hai.event_ticket_booking_app.R;
import vn.humg.hai.event_ticket_booking_app.adapter.UserAdapter;
import vn.humg.hai.event_ticket_booking_app.controller.UserController;
import vn.humg.hai.event_ticket_booking_app.model.Admin;
import vn.humg.hai.event_ticket_booking_app.model.User;

public class AdminManageUsersActivity extends AppCompatActivity implements UserAdapter.OnUserClickListener {

    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private final List<User> userList = new ArrayList<>();
    private final UserController userController = new UserController();

    /**
     * Phase C: Quyền quản trị tài khoản.
     * Chỉ Manager (accessLevel >= 2) và Developer (accessLevel == 3) mới được phép.
     */
    private boolean canManageUsers = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage_users);

        initViews();
        setupRecyclerView();
        checkAdminPermission(); // Kiểm tra quyền trước, sau đó tải danh sách
    }

    private void initViews() {
        findViewById(R.id.btn_back_custom).setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        recyclerView = findViewById(R.id.recycler_admin_users);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserAdapter(userList, this);
        recyclerView.setAdapter(adapter);
    }

    /**
     * Phase C: Load profile Admin đang đăng nhập để kiểm tra accessLevel.
     * Chỉ Manager (cấp 2) và Developer (cấp 3) mới có quyền quản lý người dùng/phân quyền.
     */
    private void checkAdminPermission() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            loadAllUsers();
            return;
        }

        userController.getAdminById(uid, admin -> {
            if (admin != null && admin.getAccessLevel() >= 2) {
                canManageUsers = true;
            }
            loadAllUsers();
        }, err -> loadAllUsers()); // Nếu lỗi → mặc định không có quyền, vẫn hiện danh sách
    }

    private void loadAllUsers() {
        userController.getAllUsers(users -> {
            runOnUiThread(() -> {
                userList.clear();
                userList.addAll(users);
                adapter.notifyDataSetChanged();
            });
        }, error -> runOnUiThread(() ->
                Toast.makeText(this, "Lỗi: " + error, Toast.LENGTH_SHORT).show()));
    }

    // Phase C: Callback khi Admin nhấp vào một User item
    @Override
    public void onUserClick(User user) {
        if (!canManageUsers) {
            // Admin thông thường: chỉ hiển thị thông tin
            showUserInfoDialog(user);
        } else {
            // Được quyền quản trị: fetch full admin details để lấy accessLevel chính xác
            if ("admin".equalsIgnoreCase(user.getRole())) {
                userController.getAdminById(user.getUid(), admin -> {
                    if (admin != null) {
                        runOnUiThread(() -> showQuickEditDialog(admin));
                    } else {
                        runOnUiThread(() -> showQuickEditDialog(user));
                    }
                }, err -> runOnUiThread(() -> showQuickEditDialog(user)));
            } else {
                showQuickEditDialog(user);
            }
        }
    }

    /** Dialog chỉ xem thông tin — dành cho Nhân viên thường (accessLevel == 1) hoặc khi lỗi */
    private void showUserInfoDialog(User user) {
        String tier = user.getMemberTier() != null ? user.getMemberTier() : "Thường";
        String displayRole = "Thành viên thường";
        if ("admin".equalsIgnoreCase(user.getRole())) {
            displayRole = "Quản trị viên";
        }
        
        new AlertDialog.Builder(this)
                .setTitle("Thông tin người dùng")
                .setMessage(
                        "👤 " + user.getFullName()
                        + "\n📧 " + user.getEmail()
                        + "\n🏷️ Vai trò: " + displayRole
                        + "\n🏅 Hạng: " + tier
                        + "\n⭐ EXP: " + user.getExp()
                        + "\n\n🔒 Bạn không có quyền quản lý tài khoản & phân quyền.\n"
                        + "Liên hệ Quản lý hoặc Nhà phát triển.")
                .setPositiveButton("Đóng", null)
                .show();
    }

    /** Dialog chỉnh sửa EXP & Phân quyền Admin — Chỉ dành cho Manager & Dev (accessLevel >= 2) */
    private void showQuickEditDialog(User user) {
        float dp = getResources().getDisplayMetrics().density;
        int padding = (int) (20 * dp);

        // ── Root layout ──
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(padding, padding, padding, (int) (8 * dp));

        // ── Tên & email ──
        TextView tvName = new TextView(this);
        tvName.setText("👤 " + user.getFullName() + "\n" + user.getEmail());
        tvName.setTextSize(13f);
        layout.addView(tvName);

        // ── EXP hiện tại ──
        TextView tvCurrentExp = new TextView(this);
        tvCurrentExp.setText("\nEXP hiện tại: " + user.getExp()
                + "  |  Hạng: " + (user.getMemberTier() != null ? user.getMemberTier() : "Thường"));
        tvCurrentExp.setTextSize(12f);
        tvCurrentExp.setTypeface(null, android.graphics.Typeface.BOLD);
        layout.addView(tvCurrentExp);

        // ── Label thao tác EXP ──
        TextView tvExpLabel = new TextView(this);
        tvExpLabel.setText("\nThao tác EXP:");
        tvExpLabel.setTextSize(13f);
        layout.addView(tvExpLabel);

        // ── RadioGroup: Cộng / Trừ ──
        RadioGroup expRadioGroup = new RadioGroup(this);
        expRadioGroup.setOrientation(RadioGroup.HORIZONTAL);

        RadioButton rbAdd = new RadioButton(this);
        rbAdd.setText("➕  Cộng EXP");
        rbAdd.setId(View.generateViewId());
        rbAdd.setChecked(true);

        RadioButton rbSub = new RadioButton(this);
        rbSub.setText("➖  Trừ EXP");
        rbSub.setId(View.generateViewId());

        expRadioGroup.addView(rbAdd);
        expRadioGroup.addView(rbSub);
        layout.addView(expRadioGroup);

        // ── Ô nhập số lượng EXP (chỉ số dương) ──
        EditText edtExpAmount = new EditText(this);
        edtExpAmount.setInputType(InputType.TYPE_CLASS_NUMBER);
        edtExpAmount.setHint("Nhập số EXP (ví dụ: 100)");
        LinearLayout.LayoutParams edtParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        edtParams.topMargin = (int) (6 * dp);
        edtExpAmount.setLayoutParams(edtParams);
        layout.addView(edtExpAmount);

        // ── Phân quyền & Vai trò ──
        TextView tvRoleLabel = new TextView(this);
        tvRoleLabel.setText("\nVai trò hệ thống:");
        tvRoleLabel.setTextSize(13f);
        layout.addView(tvRoleLabel);

        RadioGroup roleRadioGroup = new RadioGroup(this);
        roleRadioGroup.setOrientation(RadioGroup.HORIZONTAL);

        RadioButton rbUser = new RadioButton(this);
        rbUser.setText("Thành viên");
        rbUser.setId(View.generateViewId());

        RadioButton rbAdmin = new RadioButton(this);
        rbAdmin.setText("Quản trị viên");
        rbAdmin.setId(View.generateViewId());

        boolean isInitiallyAdmin = "admin".equalsIgnoreCase(user.getRole());
        if (isInitiallyAdmin) {
            rbAdmin.setChecked(true);
        } else {
            rbUser.setChecked(true);
        }

        roleRadioGroup.addView(rbUser);
        roleRadioGroup.addView(rbAdmin);
        layout.addView(roleRadioGroup);

        // ── Lựa chọn Cấp độ Admin (chỉ hiện khi chọn Quản trị viên) ──
        LinearLayout layoutAdminLevel = new LinearLayout(this);
        layoutAdminLevel.setOrientation(LinearLayout.VERTICAL);
        layoutAdminLevel.setVisibility(isInitiallyAdmin ? View.VISIBLE : View.GONE);
        
        TextView tvLevelLabel = new TextView(this);
        tvLevelLabel.setText("Chọn Cấp độ Admin:");
        tvLevelLabel.setTextSize(13f);
        layoutAdminLevel.addView(tvLevelLabel);

        Spinner spinnerLevel = new Spinner(this);
        String[] adminLevels = {"Cấp 1: Nhân viên (Staff)", "Cấp 2: Quản lý (Manager)"};
        ArrayAdapter<String> levelAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, adminLevels);
        levelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLevel.setAdapter(levelAdapter);

        // Đọc accessLevel hiện tại của admin để gán
        final int currentAccessLevel = (user instanceof Admin) ? ((Admin) user).getAccessLevel() : 1;
        
        if (currentAccessLevel == 2) {
            spinnerLevel.setSelection(1); // Manager
        } else {
            spinnerLevel.setSelection(0); // Staff
        }
        layoutAdminLevel.addView(spinnerLevel);
        layout.addView(layoutAdminLevel);

        // Điều khiển ẩn/hiện Layout Cấp độ Admin dựa vào việc chọn vai trò
        roleRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == rbAdmin.getId()) {
                layoutAdminLevel.setVisibility(View.VISIBLE);
            } else {
                layoutAdminLevel.setVisibility(View.GONE);
            }
        });

        // ── Kiểm tra nếu là Nhà phát triển (Cấp 3) thì KHÔNG cho phép hạ cấp/sửa vai trò ──
        if (currentAccessLevel == 3 && isInitiallyAdmin) {
            rbUser.setEnabled(false);
            rbAdmin.setEnabled(false);
            spinnerLevel.setEnabled(false);
            TextView tvDevWarning = new TextView(this);
            tvDevWarning.setText("⚠️ Tài khoản Nhà phát triển (Cấp 3) - Không thể thay đổi vai trò.");
            tvDevWarning.setTextColor(0xFFEF4444); // Màu đỏ cảnh báo
            tvDevWarning.setTextSize(11f);
            layoutAdminLevel.addView(tvDevWarning);
        }

        new AlertDialog.Builder(this)
                .setTitle("⚙️ Quản lý vai trò & EXP")
                .setView(layout)
                .setPositiveButton("Cập nhật", (dialog, which) -> {
                    String input = edtExpAmount.getText().toString().trim();
                    boolean makeAdmin = rbAdmin.isChecked();
                    int selectedLevelIndex = spinnerLevel.getSelectedItemPosition();
                    int newAccessLevel = selectedLevelIndex + 1; // 1: Staff, 2: Manager

                    long delta = 0;
                    if (!input.isEmpty()) {
                        try {
                            long amount = Long.parseLong(input);
                            delta = rbSub.isChecked() ? -amount : amount;
                        } catch (NumberFormatException e) {
                            Toast.makeText(this, "Số EXP không hợp lệ", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    long oldExp = user.getExp();
                    long newExp = Math.max(0, oldExp + delta);
                    
                    // Tạo đối tượng User/Admin phù hợp để lưu
                    User finalUserToSave;
                    if (makeAdmin) {
                        Admin adminUser = new Admin();
                        // Copy data từ user cũ sang Admin
                        adminUser.setUid(user.getUid());
                        adminUser.setFullName(user.getFullName());
                        adminUser.setEmail(user.getEmail());
                        adminUser.setPhone(user.getPhone());
                        adminUser.setCreatedAt(user.getCreatedAt());
                        adminUser.setExp(newExp);
                        adminUser.setMemberTier(User.computeTier(newExp));
                        adminUser.setRole("admin");
                        
                        // Nếu là cấp 3 (Developer) đã được bảo vệ, giữ nguyên cấp độ của họ
                        if (currentAccessLevel == 3) {
                            adminUser.setAccessLevel(3);
                        } else {
                            adminUser.setAccessLevel(newAccessLevel);
                        }
                        finalUserToSave = adminUser;
                    } else {
                        // Trở thành user thường
                        User normalUser = new User();
                        normalUser.setUid(user.getUid());
                        normalUser.setFullName(user.getFullName());
                        normalUser.setEmail(user.getEmail());
                        normalUser.setPhone(user.getPhone());
                        normalUser.setCreatedAt(user.getCreatedAt());
                        normalUser.setExp(newExp);
                        normalUser.setMemberTier(User.computeTier(newExp));
                        normalUser.setRole("user");
                        finalUserToSave = normalUser;
                    }

                    final User userToSave = finalUserToSave;
                    final long finalDelta = delta;
                    final long finalOldExp = oldExp;
                    final long finalNewExp = newExp;

                    userController.saveUserProfile(userToSave, () -> runOnUiThread(() -> {
                        String sign = finalDelta >= 0 ? "+" : "";
                        String roleText = makeAdmin ? "Admin (Cấp " + ((Admin) userToSave).getAccessLevel() + ")" : "User thường";
                        Toast.makeText(this,
                                "✅ " + userToSave.getFullName()
                                        + "\nEXP: " + finalOldExp + " → " + finalNewExp + " (" + sign + finalDelta + ")"
                                        + "\nVai trò: " + roleText,
                                Toast.LENGTH_LONG).show();
                        loadAllUsers();
                    }), err -> runOnUiThread(() ->
                            Toast.makeText(this, "Lỗi cập nhật: " + err, Toast.LENGTH_SHORT).show()));
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
