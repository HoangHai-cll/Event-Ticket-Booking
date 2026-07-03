package vn.humg.hai.event_ticket_booking_app.model;

import com.google.firebase.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Model Admin kế thừa từ User, bổ sung các trường kiểm soát và quản trị chuyên sâu.
 */
public class Admin extends User {
    
    // --- PHÂN CẤP VÀ QUYỀN HẠN ---
    private int accessLevel;             // 1: Super Admin (Toàn quyền), 2: Manager (Quản lý), 3: Staff (Nhân viên)
    private List<String> specificPermissions; // Danh sách mã quyền cụ thể (Vd: "DELETE_EVENT")
    
    // --- KIỂM SOÁT NHANH (FLAGS) ---
    private boolean canManageUsers;      // Quyền quản lý danh sách người dùng
    private boolean canManageEvents;     // Quyền đăng/sửa/xóa sự kiện
    private boolean canManageBookings;   // Quyền xác nhận/hủy giao dịch
    private boolean canAccessReports;    // Quyền xem báo cáo và thống kê
    
    // --- QUẢN TRỊ NỘI BỘ (INTERNAL AUDIT) ---
    private String accountStatus;        // "Active", "Locked", "Pending_Approval"
    private Timestamp promotedAt;        // Ngày được cấp quyền quản trị
    private String promotedBy;           // UID của Admin cấp trên đã thực hiện duyệt
    private String internalNote;         // Ghi chú nội bộ về nhân sự quản trị này
    
    // --- THEO DÕI HOẠT ĐỘNG VÀ HIỆU SUẤT ---
    private int totalActionsPerformed;   // Tổng số thao tác quản trị đã thực hiện
    private Timestamp lastAdminActionAt; // Thời điểm thực hiện thao tác cuối cùng
    private int bookingsConfirmedCount;  // Số lượng đơn hàng đã xác nhận thành công
    private int bookingsCancelledCount;  // Số lượng đơn hàng đã thực hiện lệnh hủy

    public Admin() {
        super();
        setRole("admin"); // Luôn mặc định role là admin
        this.specificPermissions = new ArrayList<>();
        this.accountStatus = "Active";
        this.accessLevel = 3; // Mặc định khởi tạo là nhân viên (Staff)
        this.totalActionsPerformed = 0;
        this.bookingsConfirmedCount = 0;
        this.bookingsCancelledCount = 0;
    }

    // Getters and Setters
    public int getAccessLevel() { return accessLevel; }
    public void setAccessLevel(int accessLevel) { this.accessLevel = accessLevel; }

    public List<String> getSpecificPermissions() { return specificPermissions; }
    public void setSpecificPermissions(List<String> specificPermissions) { this.specificPermissions = specificPermissions; }

    public boolean isCanManageUsers() { return canManageUsers; }
    public void setCanManageUsers(boolean canManageUsers) { this.canManageUsers = canManageUsers; }

    public boolean isCanManageEvents() { return canManageEvents; }
    public void setCanManageEvents(boolean canManageEvents) { this.canManageEvents = canManageEvents; }

    public boolean isCanManageBookings() { return canManageBookings; }
    public void setCanManageBookings(boolean canManageBookings) { this.canManageBookings = canManageBookings; }

    public boolean isCanAccessReports() { return canAccessReports; }
    public void setCanAccessReports(boolean canAccessReports) { this.canAccessReports = canAccessReports; }

    public String getAccountStatus() { return accountStatus; }
    public void setAccountStatus(String accountStatus) { this.accountStatus = accountStatus; }

    public Timestamp getPromotedAt() { return promotedAt; }
    public void setPromotedAt(Timestamp promotedAt) { this.promotedAt = promotedAt; }

    public String getPromotedBy() { return promotedBy; }
    public void setPromotedBy(String promotedBy) { this.promotedBy = promotedBy; }

    public String getInternalNote() { return internalNote; }
    public void setInternalNote(String internalNote) { this.internalNote = internalNote; }

    public int getTotalActionsPerformed() { return totalActionsPerformed; }
    public void setTotalActionsPerformed(int totalActionsPerformed) { this.totalActionsPerformed = totalActionsPerformed; }

    public Timestamp getLastAdminActionAt() { return lastAdminActionAt; }
    public void setLastAdminActionAt(Timestamp lastAdminActionAt) { this.lastAdminActionAt = lastAdminActionAt; }

    public int getBookingsConfirmedCount() { return bookingsConfirmedCount; }
    public void setBookingsConfirmedCount(int bookingsConfirmedCount) { this.bookingsConfirmedCount = bookingsConfirmedCount; }

    public int getBookingsCancelledCount() { return bookingsCancelledCount; }
    public void setBookingsCancelledCount(int bookingsCancelledCount) { this.bookingsCancelledCount = bookingsCancelledCount; }
}
