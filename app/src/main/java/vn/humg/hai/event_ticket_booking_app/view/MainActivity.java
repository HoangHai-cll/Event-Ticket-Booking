package vn.humg.hai.event_ticket_booking_app.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.LayoutInflater;
import androidx.appcompat.app.AlertDialog;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.google.firebase.auth.FirebaseAuth;
import android.widget.ImageView;
import androidx.activity.OnBackPressedCallback;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import android.widget.ImageButton;
import com.bumptech.glide.Glide;
import vn.humg.hai.event_ticket_booking_app.controller.ConfigController;
import vn.humg.hai.event_ticket_booking_app.model.User;
import android.util.Log;

import vn.humg.hai.event_ticket_booking_app.R;
import androidx.lifecycle.ViewModelProvider;
import vn.humg.hai.event_ticket_booking_app.viewmodel.AuthViewModel;
import vn.humg.hai.event_ticket_booking_app.fragments.AdminDashboardFragment;
import vn.humg.hai.event_ticket_booking_app.fragments.AdminHomeFragment;
import vn.humg.hai.event_ticket_booking_app.fragments.AdminProfileFragment;
import vn.humg.hai.event_ticket_booking_app.fragments.HistoryFragment;
import vn.humg.hai.event_ticket_booking_app.fragments.HomeFragment;
import vn.humg.hai.event_ticket_booking_app.fragments.ProfileFragment;
import vn.humg.hai.event_ticket_booking_app.fragments.TicketsFragment;
import vn.humg.hai.event_ticket_booking_app.view.SettingsActivity;
import vn.humg.hai.event_ticket_booking_app.view.HelpCenterActivity;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import androidx.activity.result.ActivityResultLauncher;
import android.widget.Toast;
import android.os.Vibrator;
import android.os.VibrationEffect;
import android.os.Build;
import android.content.Context;

public class MainActivity extends AppCompatActivity {

    private LinearLayout navHome, navAdmin, navScanQr, navTickets, navHistory, navProfile;
    private TextView tvHomeText, tvAdminText, tvScanText, tvTicketsText, tvHistoryText, tvProfileText;
    private ImageView ivHomeIcon, ivAdminIcon, ivScanIcon, ivTicketsIcon, ivHistoryIcon, ivProfileIcon;
    
    private Fragment homeFragment, adminFragment, ticketsFragment, historyFragment, profileFragment;
    private Fragment activeFragment;
    private FragmentManager fm;
    
    private DrawerLayout drawerLayout;
    private NavigationView navViewDrawer;

    private AuthViewModel authViewModel;
    private boolean isUserAdmin = false;
    private String pendingTargetTab = null;

    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(),
            result -> {
                if (result.getContents() == null) {
                    Toast.makeText(MainActivity.this, "Đã hủy quét mã QR", Toast.LENGTH_SHORT).show();
                } else {
                    vibrateOnScan();
                    Toast.makeText(this, "Đã đọc mã thành công!", Toast.LENGTH_SHORT).show();
                    // Khi quét từ màn hình chính, chúng ta chuyển sang tab Admin và xử lý
                    navigateToTab("Admin");
                    if (adminFragment instanceof AdminDashboardFragment) {
                        // Có thể thêm logic xử lý trực tiếp ở đây hoặc gửi sang AdminDashboardFragment
                        Intent intent = new Intent(this, AdminManageBookingsActivity.class);
                        intent.putExtra("SCANNED_QR_ID", result.getContents());
                        startActivity(intent);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        setupObservers(savedInstanceState);

        fm = getSupportFragmentManager();
        initViews();
        setupNavigation();
        ConfigController.getInstance().fetchAndActivate();
        checkUserRoleAndSetupFragments(savedInstanceState);
        
        handleIntent(getIntent());
    }

    private void initViews() {
        navHome = findViewById(R.id.nav_home);
        navAdmin = findViewById(R.id.nav_admin);
        navScanQr = findViewById(R.id.nav_scan_qr);
        navTickets = findViewById(R.id.nav_tickets);
        navHistory = findViewById(R.id.nav_history);
        navProfile = findViewById(R.id.nav_profile);

        tvHomeText = findViewById(R.id.tv_nav_home_text);
        tvAdminText = findViewById(R.id.tv_nav_admin_text);
        tvScanText = findViewById(R.id.tv_nav_scan_text);
        tvTicketsText = findViewById(R.id.tv_nav_tickets_text);
        tvHistoryText = findViewById(R.id.tv_nav_history_text);
        tvProfileText = findViewById(R.id.tv_nav_profile_text);

        ivHomeIcon = findViewById(R.id.iv_nav_home_icon);
        ivAdminIcon = findViewById(R.id.iv_nav_admin_icon);
        ivScanIcon = findViewById(R.id.iv_nav_scan_icon);
        ivTicketsIcon = findViewById(R.id.iv_nav_tickets_icon);
        ivHistoryIcon = findViewById(R.id.iv_nav_history_icon);
        ivProfileIcon = findViewById(R.id.iv_nav_profile_icon);

        drawerLayout = findViewById(R.id.drawer_layout);
        navViewDrawer = findViewById(R.id.nav_view_drawer);
    }

    private void setupObservers(Bundle savedInstanceState) {
        authViewModel.getUserProfileState().observe(this, user -> {
            if (user != null) {
                isUserAdmin = "admin".equalsIgnoreCase(user.getRole());
                setupUIForRole(isUserAdmin, savedInstanceState);
                syncFcmToken(user);
                
                if (navViewDrawer != null) {
                    View headerView = navViewDrawer.getHeaderView(0);
                    if (headerView != null) {
                        TextView tvName = headerView.findViewById(R.id.tv_nav_header_name);
                        TextView tvRole = headerView.findViewById(R.id.tv_nav_header_role);
                        ImageView ivAvatar = headerView.findViewById(R.id.iv_nav_header_avatar);
                        
                        if (tvName != null) {
                            String name = user.getFullName();
                            tvName.setText(name != null ? name : "Người dùng");
                        }
                        if (tvRole != null) {
                            tvRole.setText(isUserAdmin ? "Quản trị viên" : "Hạng: " + (user.getMemberTier() != null ? user.getMemberTier() : "Thường"));
                            tvRole.setOnClickListener(v -> {
                                if (!isUserAdmin) {
                                    startActivity(new Intent(MainActivity.this, MembershipDetailsActivity.class));
                                    drawerLayout.closeDrawer(GravityCompat.START);
                                }
                            });
                        }
                        
                        if (ivAvatar != null) {
                            String avatar = user.getAvatarName();
                            if (avatar != null && !avatar.isEmpty()) {
                                Glide.with(MainActivity.this)
                                    .load(avatar)
                                    .circleCrop()
                                    .placeholder(R.drawable.img_logo_event_ticket_booking)
                                    .into(ivAvatar);
                            }
                        }
                    }
                }
            }
        });
        
        authViewModel.getErrorState().observe(this, error -> {
            if (error != null) {
                setupUIForRole(false, savedInstanceState);
            }
        });
    }

    private void checkUserRoleAndSetupFragments(Bundle savedInstanceState) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            setupUIForRole(false, savedInstanceState);
            setupGuestDrawerHeader();
            return;
        }
        authViewModel.getUserProfile(uid);
    }

    private void setupGuestDrawerHeader() {
        if (navViewDrawer != null) {
            View headerView = navViewDrawer.getHeaderView(0);
            if (headerView != null) {
                TextView tvName = headerView.findViewById(R.id.tv_nav_header_name);
                TextView tvRole = headerView.findViewById(R.id.tv_nav_header_role);
                ImageView ivAvatar = headerView.findViewById(R.id.iv_nav_header_avatar);
                
                if (tvName != null) {
                    tvName.setText("Khách");
                }
                if (tvRole != null) {
                    tvRole.setText("Chưa đăng nhập - Chạm để đăng nhập");
                    tvRole.setOnClickListener(v -> {
                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                        drawerLayout.closeDrawer(GravityCompat.START);
                    });
                }
                if (ivAvatar != null) {
                    ivAvatar.setOnClickListener(v -> {
                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                        drawerLayout.closeDrawer(GravityCompat.START);
                    });
                }
            }
        }
    }

    private void setupUIForRole(boolean isAdmin, Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            if (isAdmin) {
                homeFragment = new AdminHomeFragment();
                profileFragment = new AdminProfileFragment();
            } else {
                homeFragment = new HomeFragment();
                profileFragment = new ProfileFragment();
            }
            activeFragment = homeFragment;

            if (isAdmin) {
                // Giao diện cho Admin
                navAdmin.setVisibility(View.VISIBLE);
                navScanQr.setVisibility(View.VISIBLE);
                navTickets.setVisibility(View.GONE);
                navHistory.setVisibility(View.GONE);
                
                adminFragment = new AdminDashboardFragment();
                fm.beginTransaction()
                        .add(R.id.fragment_container, profileFragment, "Profile").hide(profileFragment)
                        .add(R.id.fragment_container, adminFragment, "Admin").hide(adminFragment)
                        .add(R.id.fragment_container, homeFragment, "Home")
                        .commit();
                updateNavUI("Home");
            } else {
                // Giao diện cho User
                navAdmin.setVisibility(View.GONE);
                navScanQr.setVisibility(View.GONE);
                navTickets.setVisibility(View.VISIBLE);
                navHistory.setVisibility(View.VISIBLE);
                
                ticketsFragment = new TicketsFragment();
                historyFragment = new HistoryFragment();
                fm.beginTransaction()
                        .add(R.id.fragment_container, profileFragment, "Profile").hide(profileFragment)
                        .add(R.id.fragment_container, historyFragment, "History").hide(historyFragment)
                        .add(R.id.fragment_container, ticketsFragment, "Tickets").hide(ticketsFragment)
                        .add(R.id.fragment_container, homeFragment, "Home")
                        .commit();
                updateNavUI("Home");
            }

            if (pendingTargetTab != null) {
                navigateToTab(pendingTargetTab);
                pendingTargetTab = null;
            }
        } else {
            // Khôi phục trạng thái khi xoay màn hình
            homeFragment = fm.findFragmentByTag("Home");
            profileFragment = fm.findFragmentByTag("Profile");
            adminFragment = fm.findFragmentByTag("Admin");
            ticketsFragment = fm.findFragmentByTag("Tickets");
            historyFragment = fm.findFragmentByTag("History");

            for (Fragment f : fm.getFragments()) {
                if (f != null && !f.isHidden()) {
                    activeFragment = f;
                    updateNavUI(f.getTag());
                    break;
                }
            }
        }
    }

    private void setupNavigation() {
        navHome.setOnClickListener(v -> navigateToTab("Home"));
        navAdmin.setOnClickListener(v -> navigateToTab("Admin"));
        navScanQr.setOnClickListener(v -> {
            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                Toast.makeText(this, "Vui lòng đăng nhập để sử dụng tính năng này", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, LoginActivity.class));
                return;
            }
            ScanOptions options = new ScanOptions();
            options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
            options.setPrompt("Quét mã vé để check-in");
            options.setCameraId(0);
            options.setBeepEnabled(true);
            options.setOrientationLocked(true);
            options.setCaptureActivity(CustomScannerActivity.class);
            barcodeLauncher.launch(options);
        });
        navTickets.setOnClickListener(v -> navigateToTab("Tickets"));
        navHistory.setOnClickListener(v -> navigateToTab("History"));
        navProfile.setOnClickListener(v -> navigateToTab("Profile"));

        // Drawer Menu Item clicks
        navViewDrawer.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home_drawer) {
                navigateToTab("Home");
            } else if (id == R.id.nav_notifications_drawer) {
                if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                    Toast.makeText(this, "Vui lòng đăng nhập để xem thông báo", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, LoginActivity.class));
                    return true;
                }
                startActivity(new Intent(this, NotificationActivity.class));
            } else if (id == R.id.nav_favorites_drawer) {
                if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                    Toast.makeText(this, "Vui lòng đăng nhập để xem danh sách yêu thích", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, LoginActivity.class));
                    return true;
                }
                navigateToTab("Home");
                if (homeFragment instanceof HomeFragment) {
                    ((HomeFragment) homeFragment).filterByFavoritesOnly();
                }
            } else if (id == R.id.nav_vouchers_drawer) {
                navigateToTab("Tickets");
            } else if (id == R.id.nav_settings_drawer) {
                if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                    Toast.makeText(this, "Vui lòng đăng nhập để thay đổi cài đặt", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, LoginActivity.class));
                    return true;
                }
                startActivity(new Intent(this, SettingsActivity.class));
            } else if (id == R.id.nav_support_drawer) {
                startActivity(new Intent(this, HelpCenterActivity.class));
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        View headerView = navViewDrawer.getHeaderView(0);
        if (headerView != null) {
            ImageButton btnClose = headerView.findViewById(R.id.btn_close_drawer);
            if (btnClose != null) {
                btnClose.setOnClickListener(v -> {
                    if (drawerLayout != null) drawerLayout.closeDrawer(GravityCompat.START);
                });
            }
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else if (activeFragment != homeFragment && activeFragment != adminFragment) {
                    navigateToTab(isUserAdmin ? "Admin" : "Home");
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    public void openDrawer() {
        if (drawerLayout != null) {
            drawerLayout.openDrawer(GravityCompat.START);
        }
    }

    public void navigateToTab(String tag) {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            if ("Tickets".equals(tag) || "History".equals(tag) || "Profile".equals(tag) || "Admin".equals(tag)) {
                Toast.makeText(this, "Vui lòng đăng nhập để sử dụng tính năng này", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, LoginActivity.class));
                return;
            }
        }

        Fragment target;
        switch (tag) {
            case "Admin": target = adminFragment; break;
            case "Tickets": target = ticketsFragment; break;
            case "History": target = historyFragment; break;
            case "Profile": target = profileFragment; break;
            default: target = homeFragment; break;
        }

        if (target == null || activeFragment == null || activeFragment == target) return;

        fm.beginTransaction().hide(activeFragment).show(target).commit();
        activeFragment = target;
        updateNavUI(tag);
    }

    private void updateNavUI(String tag) {
        if (tag == null) return;

        navHome.setBackground(null);
        navAdmin.setBackground(null);
        navScanQr.setBackground(null);
        navTickets.setBackground(null);
        navHistory.setBackground(null);
        navProfile.setBackground(null);

        int mutedColor = ContextCompat.getColor(this, R.color.text_muted);
        tvHomeText.setTextColor(mutedColor);
        tvAdminText.setTextColor(mutedColor);
        if (tvScanText != null) tvScanText.setTextColor(mutedColor);
        tvTicketsText.setTextColor(mutedColor);
        tvHistoryText.setTextColor(mutedColor);
        tvProfileText.setTextColor(mutedColor);

        if (ivHomeIcon != null) ivHomeIcon.setColorFilter(mutedColor);
        if (ivAdminIcon != null) ivAdminIcon.setColorFilter(mutedColor);
        if (ivScanIcon != null) ivScanIcon.setColorFilter(mutedColor);
        if (ivTicketsIcon != null) ivTicketsIcon.setColorFilter(mutedColor);
        if (ivHistoryIcon != null) ivHistoryIcon.setColorFilter(mutedColor);
        if (ivProfileIcon != null) ivProfileIcon.setColorFilter(mutedColor);

        int primaryColor = ContextCompat.getColor(this, R.color.brand_primary);
        switch (tag) {
            case "Home":
                navHome.setBackgroundResource(R.drawable.bg_nav_active);
                tvHomeText.setTextColor(primaryColor);
                if (ivHomeIcon != null) ivHomeIcon.setColorFilter(primaryColor);
                break;
            case "Admin":
                navAdmin.setBackgroundResource(R.drawable.bg_nav_active);
                tvAdminText.setTextColor(primaryColor);
                if (ivAdminIcon != null) ivAdminIcon.setColorFilter(primaryColor);
                break;
            case "Tickets":
                navTickets.setBackgroundResource(R.drawable.bg_nav_active);
                tvTicketsText.setTextColor(primaryColor);
                if (ivTicketsIcon != null) ivTicketsIcon.setColorFilter(primaryColor);
                break;
            case "History":
                navHistory.setBackgroundResource(R.drawable.bg_nav_active);
                tvHistoryText.setTextColor(primaryColor);
                if (ivHistoryIcon != null) ivHistoryIcon.setColorFilter(primaryColor);
                break;
            case "Profile":
                navProfile.setBackgroundResource(R.drawable.bg_nav_active);
                tvProfileText.setTextColor(primaryColor);
                if (ivProfileIcon != null) ivProfileIcon.setColorFilter(primaryColor);
                break;
        }
    }

    private void handleIntent(Intent intent) {
        if (intent != null) {
            String targetTab = intent.getStringExtra("TARGET_TAB");
            if (targetTab != null) {
                if (activeFragment == null) {
                    pendingTargetTab = targetTab;
                } else {
                    navigateToTab(targetTab);
                }
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void showNotificationsDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_notifications, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();
        View btnClose = dialogView.findViewById(R.id.btn_close_notif);
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> dialog.dismiss());
        }
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }
        dialog.show();
    }

    private void vibrateOnScan() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(150);
            }
        }
    }

    private void syncFcmToken(User user) {
        if (user == null || user.getUid() == null) return;
        com.google.firebase.messaging.FirebaseMessaging.getInstance().getToken()
            .addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    Log.w("MainActivity", "Fetching FCM registration token failed", task.getException());
                    return;
                }
                String token = task.getResult();
                if (token != null && !token.equals(user.getFcmToken())) {
                    // Update locally
                    user.setFcmToken(token);
                    // Update in Firestore
                    com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(user.getUid())
                        .update("fcmToken", token)
                        .addOnSuccessListener(aVoid -> Log.d("MainActivity", "FCM Token successfully synced to Firestore"))
                        .addOnFailureListener(e -> Log.e("MainActivity", "Failed to sync FCM Token to Firestore", e));
                }
            });
    }
}
