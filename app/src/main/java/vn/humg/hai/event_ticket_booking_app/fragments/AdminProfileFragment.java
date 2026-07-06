package vn.humg.hai.event_ticket_booking_app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

import vn.humg.hai.event_ticket_booking_app.R;
import vn.humg.hai.event_ticket_booking_app.controller.UserController;
import vn.humg.hai.event_ticket_booking_app.controller.EventController;
import vn.humg.hai.event_ticket_booking_app.controller.BookingController;
import vn.humg.hai.event_ticket_booking_app.view.ChangePasswordActivity;
import vn.humg.hai.event_ticket_booking_app.view.EditProfileActivity;
import vn.humg.hai.event_ticket_booking_app.view.LoginActivity;
import vn.humg.hai.event_ticket_booking_app.view.SettingsActivity;
import vn.humg.hai.event_ticket_booking_app.view.HelpCenterActivity;
import vn.humg.hai.event_ticket_booking_app.view.AdminManageEventsActivity;
import vn.humg.hai.event_ticket_booking_app.view.AdminManageBookingsActivity;
import vn.humg.hai.event_ticket_booking_app.view.AdminDashboardActivity;

public class AdminProfileFragment extends Fragment {

    private ImageView ivAppBarAvatar, ivAdminAvatar;
    private TextView tvAdminName, tvAdminRole;
    private TextView tvStatEvents, tvStatRequests;
    private View cardStatEvents, cardStatRequests, cardStatReports;
    private MaterialButton btnLogout;
    
    private final UserController userController = new UserController();
    private final EventController eventController = new EventController();
    private final BookingController bookingController = new BookingController();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_profile, container, false);
        
        initViews(view);
        loadAdminData();
        setupListeners(view);
        
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAdminData();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            loadAdminData();
        }
    }

    private void initViews(View view) {
        ivAppBarAvatar = view.findViewById(R.id.iv_appbar_avatar);
        ivAdminAvatar = view.findViewById(R.id.iv_admin_avatar);
        tvAdminName = view.findViewById(R.id.tv_admin_name);
        tvAdminRole = view.findViewById(R.id.tv_admin_role);
        tvStatEvents = view.findViewById(R.id.tv_stat_events);
        tvStatRequests = view.findViewById(R.id.tv_stat_requests);
        btnLogout = view.findViewById(R.id.btn_admin_logout);
        
        cardStatEvents = view.findViewById(R.id.card_stat_events);
        cardStatRequests = view.findViewById(R.id.card_stat_requests);
        cardStatReports = view.findViewById(R.id.card_stat_reports);
    }
    
    private void loadAdminData() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            // Load thông tin Admin & hiển thị chức danh (Role) chuẩn xác theo accessLevel
            userController.getAdminById(uid, admin -> {
                if (admin != null && isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        tvAdminName.setText(admin.getFullName());
                        int level = admin.getAccessLevel();
                        if (level == 3) {
                            tvAdminRole.setText("Nhà phát triển 👑");
                        } else if (level == 2) {
                            tvAdminRole.setText("Quản lý cấp cao 👔");
                        } else {
                            tvAdminRole.setText("Nhân viên vận hành ⚙️");
                        }
                    });
                }
            }, e -> {});
            
            // Load số lượng sự kiện thực tế của admin này
            eventController.getAllEvents(events -> {
                if (isAdded()) {
                    long myEventsCount = events.stream()
                        .filter(e -> uid.equals(e.getCreatedByAdminId()))
                        .count();
                    requireActivity().runOnUiThread(() -> tvStatEvents.setText(String.valueOf(myEventsCount)));
                }
            }, e -> {});

            // Load số lượng yêu cầu hoàn tiền chờ xử lý của admin này (sellerId == uid && status == "Refund Pending")
            bookingController.getBookingsBySeller(uid, bookings -> {
                if (isAdded()) {
                    long pendingRefundCount = bookings.stream()
                        .filter(b -> "Refund Pending".equalsIgnoreCase(b.getStatus()))
                        .count();
                    requireActivity().runOnUiThread(() -> 
                        tvStatRequests.setText(String.format(java.util.Locale.getDefault(), "%02d", pendingRefundCount)));
                }
            }, e -> {});
        }
    }

    private void setupListeners(View view) {
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
        
        // Sự kiện click cho 3 card thống kê phía trên
        cardStatEvents.setOnClickListener(v -> 
            startActivity(new Intent(getContext(), AdminManageEventsActivity.class)));
            
        cardStatRequests.setOnClickListener(v -> 
            startActivity(new Intent(getContext(), AdminManageBookingsActivity.class)));
            
        cardStatReports.setOnClickListener(v -> 
            startActivity(new Intent(getContext(), AdminDashboardActivity.class)));

        // Cài đặt hệ thống
        view.findViewById(R.id.btn_admin_personal_info).setOnClickListener(v -> 
            startActivity(new Intent(getContext(), EditProfileActivity.class)));
            
        view.findViewById(R.id.btn_admin_permissions).setOnClickListener(v -> 
            Toast.makeText(getContext(), "Tính năng phân quyền đang phát triển", Toast.LENGTH_SHORT).show());
            
        // Sửa: Lịch sử hoạt động dẫn sang màn hình Quản lý Giao dịch của Admin
        view.findViewById(R.id.btn_admin_activity_log).setOnClickListener(v -> 
            startActivity(new Intent(getContext(), AdminManageBookingsActivity.class)));
            
        view.findViewById(R.id.btn_admin_security).setOnClickListener(v -> 
            startActivity(new Intent(getContext(), ChangePasswordActivity.class)));
            
        view.findViewById(R.id.btn_admin_app_config).setOnClickListener(v -> 
            startActivity(new Intent(getContext(), SettingsActivity.class)));
    }
}
