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
import vn.humg.hai.event_ticket_booking_app.model.Booking;
import vn.humg.hai.event_ticket_booking_app.view.LoginActivity;
import vn.humg.hai.event_ticket_booking_app.view.MainActivity;
import vn.humg.hai.event_ticket_booking_app.view.TermsPolicyActivity;
import vn.humg.hai.event_ticket_booking_app.view.EditProfileActivity;
import vn.humg.hai.event_ticket_booking_app.view.ChangePasswordActivity;
import vn.humg.hai.event_ticket_booking_app.view.SettingsActivity;
import vn.humg.hai.event_ticket_booking_app.view.HelpCenterActivity;

public class ProfileFragment extends Fragment {

    private final UserController userController = new UserController();
    private final BookingController bookingController = new BookingController();
    
    private ImageView ivAvatar;
    private TextView tvName, tvEmail, tvStatsJoined, tvStatsUpcoming, tvStatsPoints;
    private LinearLayout menuEdit, menuPassword, menuSettings, menuSupport, menuTerms, menuAdmin;
    private MaterialButton btnLogout;

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
        
        tvStatsJoined = view.findViewById(R.id.tv_stats_joined);
        tvStatsUpcoming = view.findViewById(R.id.tv_stats_upcoming);
        tvStatsPoints = view.findViewById(R.id.tv_stats_points);
        
        menuEdit = view.findViewById(R.id.menu_edit_profile);
        menuPassword = view.findViewById(R.id.menu_change_password);
        menuSettings = view.findViewById(R.id.menu_settings);
        menuSupport = view.findViewById(R.id.menu_support);
        menuTerms = view.findViewById(R.id.menu_terms);
        menuAdmin = view.findViewById(R.id.menu_admin);
        
        btnLogout = view.findViewById(R.id.btn_logout);
    }

    private void initEvents() {
        menuEdit.setOnClickListener(v -> startActivity(new Intent(getContext(), EditProfileActivity.class)));
        menuPassword.setOnClickListener(v -> startActivity(new Intent(getContext(), ChangePasswordActivity.class)));
        menuSettings.setOnClickListener(v -> startActivity(new Intent(getContext(), SettingsActivity.class)));
        menuSupport.setOnClickListener(v -> startActivity(new Intent(getContext(), HelpCenterActivity.class)));
        menuTerms.setOnClickListener(v -> startActivity(new Intent(getContext(), TermsPolicyActivity.class)));
        
        if (menuAdmin != null) {
            menuAdmin.setOnClickListener(v -> {
                // Chuyển sang Tab Admin trên MainActivity thay vì mở Activity mới
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).navigateToTab("Admin");
                }
            });
        }

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(getContext(), "Đã đăng xuất", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            if (getActivity() != null) getActivity().finish();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserProfile();
        calculateUserStats();
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
                        
                        if (menuAdmin != null) {
                            menuAdmin.setVisibility("admin".equalsIgnoreCase(user.getRole()) ? View.VISIBLE : View.GONE);
                        }

                        String avatarUrl = user.getAvatarName();
                        if (avatarUrl != null && !avatarUrl.isEmpty()) {
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
    }

    private void calculateUserStats() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        bookingController.getBookingsByUser(uid, bookings -> {
            if (!isAdded() || getActivity() == null) return;
            
            int joined = 0;
            int upcoming = 0;
            double totalPoints = 0;

            for (vn.humg.hai.event_ticket_booking_app.model.Booking b : bookings) {
                String status = b.getStatus();
                if ("Completed".equalsIgnoreCase(status) || "Hoàn thành".equalsIgnoreCase(status)) {
                    joined++;
                    totalPoints += (b.getTotalPrice() / 10000);
                } else if ("Confirmed".equalsIgnoreCase(status)) {
                    upcoming++;
                }
            }

            final int finalJoined = joined;
            final int finalUpcoming = upcoming;
            final int finalPoints = (int) totalPoints;

            getActivity().runOnUiThread(() -> {
                tvStatsJoined.setText(String.valueOf(finalJoined));
                tvStatsUpcoming.setText(String.valueOf(finalUpcoming));
                tvStatsPoints.setText(String.valueOf(finalPoints));
            });
        }, error -> {});
    }
}
