package vn.humg.hai.event_ticket_booking_app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import vn.humg.hai.event_ticket_booking_app.R;
import vn.humg.hai.event_ticket_booking_app.controller.BookingController;
import vn.humg.hai.event_ticket_booking_app.controller.UserController;
import vn.humg.hai.event_ticket_booking_app.controller.VoucherController;
import vn.humg.hai.event_ticket_booking_app.model.Booking;
import vn.humg.hai.event_ticket_booking_app.model.Voucher;
import vn.humg.hai.event_ticket_booking_app.adapter.VoucherAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import vn.humg.hai.event_ticket_booking_app.view.LoginActivity;
import vn.humg.hai.event_ticket_booking_app.view.MainActivity;
import vn.humg.hai.event_ticket_booking_app.view.TermsPolicyActivity;
import vn.humg.hai.event_ticket_booking_app.view.EditProfileActivity;
import vn.humg.hai.event_ticket_booking_app.view.ChangePasswordActivity;
import vn.humg.hai.event_ticket_booking_app.view.SettingsActivity;
import vn.humg.hai.event_ticket_booking_app.view.HelpCenterActivity;
import vn.humg.hai.event_ticket_booking_app.view.MembershipDetailsActivity;

public class ProfileFragment extends Fragment {

    private final UserController userController = new UserController();
    private final BookingController bookingController = new BookingController();
    private final VoucherController voucherController = new VoucherController();
    
    private ImageView ivAvatar;
    private TextView tvName, tvEmail, tvStatsJoined, tvStatsUpcoming, tvStatsPoints, tvMemberTier;
    private LinearLayout menuEdit, menuPassword, menuSettings, menuSupport, menuTerms, menuAdmin, menuMyVouchers;
    private TextView tvVoucherCount;
    private final List<Voucher> myVouchers = new ArrayList<>();
    private MaterialButton btnLogout;

    // New profile menu views
    private LinearLayout menuMyTickets, menuNotifications, menuTransactionHistory, menuMyReviews;
    private View viewNotificationDot;
    private View cardLogout;
    private View viewAdminDivider;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        initViews(view);
        initEvents();
        loadUserProfile();
        calculateUserStats();
        return view;
    }

    private void initViews(View view) {
        ivAvatar = view.findViewById(R.id.iv_user_avatar);
        tvName = view.findViewById(R.id.tv_user_name);
        tvEmail = view.findViewById(R.id.tv_user_email);
        tvMemberTier = view.findViewById(R.id.tv_profile_member_tier);
        
        tvStatsJoined = view.findViewById(R.id.tv_stats_joined);
        tvStatsUpcoming = view.findViewById(R.id.tv_stats_upcoming);
        tvStatsPoints = view.findViewById(R.id.tv_stats_points);
        
        menuEdit = view.findViewById(R.id.menu_edit_profile);
        menuPassword = view.findViewById(R.id.menu_change_password);
        menuSettings = view.findViewById(R.id.menu_settings);
        menuSupport = view.findViewById(R.id.menu_support);
        menuTerms = view.findViewById(R.id.menu_terms);
        menuAdmin = view.findViewById(R.id.menu_admin);
        
        // New views
        menuMyTickets = view.findViewById(R.id.menu_my_tickets);
        menuNotifications = view.findViewById(R.id.menu_notifications);
        menuTransactionHistory = view.findViewById(R.id.menu_transaction_history);
        menuMyReviews = view.findViewById(R.id.menu_my_reviews);
        viewNotificationDot = view.findViewById(R.id.view_notification_dot);
        cardLogout = view.findViewById(R.id.card_logout);
        viewAdminDivider = view.findViewById(R.id.view_admin_divider);
    }

    private void initEvents() {
        menuEdit.setOnClickListener(v -> startActivity(new Intent(getContext(), EditProfileActivity.class)));
        menuPassword.setOnClickListener(v -> startActivity(new Intent(getContext(), ChangePasswordActivity.class)));
        menuSettings.setOnClickListener(v -> startActivity(new Intent(getContext(), SettingsActivity.class)));
        menuSupport.setOnClickListener(v -> startActivity(new Intent(getContext(), HelpCenterActivity.class)));
        menuTerms.setOnClickListener(v -> startActivity(new Intent(getContext(), TermsPolicyActivity.class)));
        
        if (menuMyTickets != null) {
            menuMyTickets.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).navigateToTab("Tickets");
                }
            });
        }
        
        if (menuNotifications != null) {
            menuNotifications.setOnClickListener(v -> {
                startActivity(new Intent(getContext(), vn.humg.hai.event_ticket_booking_app.view.NotificationActivity.class));
            });
        }
        
        if (menuTransactionHistory != null) {
            menuTransactionHistory.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).navigateToTab("History");
                }
            });
        }
        
        if (menuMyReviews != null) {
            menuMyReviews.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Tính năng Đánh giá của tôi đang được phát triển", Toast.LENGTH_SHORT).show();
            });
        }
        
        if (tvMemberTier != null) {
            tvMemberTier.setOnClickListener(v -> startActivity(new Intent(getContext(), MembershipDetailsActivity.class)));
        }
        
        if (menuAdmin != null) {
            menuAdmin.setOnClickListener(v -> {
                // Chuyển sang Tab Admin trên MainActivity thay vì mở Activity mới
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).navigateToTab("Admin");
                }
            });
        }

        if (cardLogout != null) {
            cardLogout.setOnClickListener(v -> {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(getContext(), "Đã đăng xuất", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                if (getActivity() != null) getActivity().finish();
            });
        }
    }

    private final android.content.BroadcastReceiver notificationReceiver = new android.content.BroadcastReceiver() {
        @Override
        public void onReceive(android.content.Context context, Intent intent) {
            updateNotificationDot();
        }
    };

    private void updateNotificationDot() {
        if (viewNotificationDot != null && getContext() != null) {
            int count = vn.humg.hai.event_ticket_booking_app.utils.LocalNotificationDbHelper.getInstance(getContext()).getUnreadCount();
            if (count > 0) {
                viewNotificationDot.setVisibility(View.VISIBLE);
            } else {
                viewNotificationDot.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getContext() != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                getContext().registerReceiver(notificationReceiver, 
                    new android.content.IntentFilter("vn.humg.hai.event_ticket_booking_app.UPDATE_UNREAD_COUNT"), 
                    android.content.Context.RECEIVER_NOT_EXPORTED);
            } else {
                getContext().registerReceiver(notificationReceiver, 
                    new android.content.IntentFilter("vn.humg.hai.event_ticket_booking_app.UPDATE_UNREAD_COUNT"));
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (getContext() != null) {
            try {
                getContext().unregisterReceiver(notificationReceiver);
            } catch (Exception ignored) {}
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserProfile();
        calculateUserStats();
        updateNotificationDot();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            loadUserProfile();
            calculateUserStats();
            updateNotificationDot();
        }
    }

    private void loadUserProfile() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        userController.getUserById(uid, user -> {
            if (isAdded() && getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (user != null) {
                        tvName.setText(user.getFullName());
                        tvEmail.setText(user.getEmail());
                        if (tvMemberTier != null) {
                            tvMemberTier.setText("Hạng: " + (user.getMemberTier() != null ? user.getMemberTier() : "Thường"));
                        }
                        if (tvStatsPoints != null) {
                            tvStatsPoints.setText(String.valueOf(user.getExp()));
                        }
                        
                        int adminVisibility = "admin".equalsIgnoreCase(user.getRole()) ? View.VISIBLE : View.GONE;
                        if (menuAdmin != null) {
                            menuAdmin.setVisibility(adminVisibility);
                        }
                        if (viewAdminDivider != null) {
                            viewAdminDivider.setVisibility(adminVisibility);
                        }

                        String avatarUrl = user.getAvatarName();
                        if (avatarUrl != null && !avatarUrl.isEmpty() && avatarUrl.startsWith("http")) {
                            Glide.with(ProfileFragment.this)
                                    .load(avatarUrl)
                                    .circleCrop()
                                    .placeholder(R.drawable.img_logo_event_ticket_booking)
                                    .into(ivAvatar);
                        } else {
                            Glide.with(ProfileFragment.this)
                                    .load(R.drawable.img_logo_event_ticket_booking)
                                    .circleCrop()
                                    .into(ivAvatar);
                        }
                    }
                });
            }
        }, error -> {
            if (isAdded() && getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                        tvEmail.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
                    }
                    tvName.setText("Người dùng EventPass");
                });
            }
        });

        // Tải danh sách Voucher của User
        voucherController.getUserVouchers(uid, vouchers -> {
            if (isAdded() && getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    myVouchers.clear();
                    myVouchers.addAll(vouchers);
                    if (tvVoucherCount != null) {
                        tvVoucherCount.setText(String.valueOf(vouchers.size()));
                    }
                });
            }
        }, error -> {});
    }

    private void calculateUserStats() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        bookingController.getBookingsByUser(uid, bookings -> {
            if (!isAdded() || getActivity() == null) return;
            
            int joined = 0;
            int upcoming = 0;

            for (vn.humg.hai.event_ticket_booking_app.model.Booking b : bookings) {
                String status = b.getStatus();
                if ("Completed".equalsIgnoreCase(status) || "Hoàn thành".equalsIgnoreCase(status)) {
                    joined++;
                } else if ("Confirmed".equalsIgnoreCase(status)) {
                    upcoming++;
                }
            }

            final int finalJoined = joined;
            final int finalUpcoming = upcoming;

            getActivity().runOnUiThread(() -> {
                tvStatsJoined.setText(String.valueOf(finalJoined));
                tvStatsUpcoming.setText(String.valueOf(finalUpcoming));
            });
        }, error -> {});
    }

    private void showMyVouchersDialog() {
        if (getContext() == null) return;

        com.google.android.material.bottomsheet.BottomSheetDialog bottomSheetDialog = 
            new com.google.android.material.bottomsheet.BottomSheetDialog(requireContext());

        View sheetView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_my_vouchers, null);

        TextView tvTitle = sheetView.findViewById(R.id.tv_sheet_title);
        RecyclerView rvVouchers = sheetView.findViewById(R.id.recycler_sheet_vouchers);
        TextView tvEmpty = sheetView.findViewById(R.id.tv_sheet_empty);

        if (tvTitle != null) {
            tvTitle.setText("Voucher của tôi (" + myVouchers.size() + ")");
        }

        if (myVouchers.isEmpty()) {
            if (tvEmpty != null) tvEmpty.setVisibility(View.VISIBLE);
            if (rvVouchers != null) rvVouchers.setVisibility(View.GONE);
        } else {
            if (tvEmpty != null) tvEmpty.setVisibility(View.GONE);
            if (rvVouchers != null) {
                rvVouchers.setVisibility(View.VISIBLE);
                rvVouchers.setLayoutManager(new LinearLayoutManager(getContext()));
                VoucherAdapter adapter = new VoucherAdapter(myVouchers);
                rvVouchers.setAdapter(adapter);
            }
        }

        bottomSheetDialog.setContentView(sheetView);
        bottomSheetDialog.show();
    }
}
