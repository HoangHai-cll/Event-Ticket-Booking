package vn.humg.hai.event_ticket_booking_app.model;

import com.google.firebase.Timestamp;

public class Booking {
    private String bookingId;
    private String userId;
    private String sellerId;
    private String eventId;
    private String tierId;          // ID hạng vé
    private String tierName;        // Tên hạng vé (VIP, Standard...)
    private double pricePerTicket;  // Giá một vé của hạng này
    private int quantity;
    private double totalPrice;
    private double discount;
    private String voucherCode;
    private Timestamp bookingDate;
    private String status;

    // --- CÁC TRƯỜNG KIỂM SOÁT ADMIN & VẬN HÀNH ---
    private String processedBy;     // UID của Admin xử lý đơn hàng
    private String adminNote;       // Ghi chú nội bộ của Admin
    private Timestamp updatedAt;    // Lần cuối cập nhật trạng thái
    
    private String paymentMethod;   // Momo, VNPAY, Bank Transfer...
    private String paymentId;       // Mã giao dịch bên thứ 3
    
    private boolean isCheckedIn;    // Trạng thái quét vé tại sự kiện
    private Timestamp checkInAt;    // Thời gian quét vé
    private String checkInBy;       // UID của nhân viên/Admin thực hiện quét vé

    public Booking() {
        this.isCheckedIn = false;
        this.discount = 0;
    }

    // Getters and Setters
    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getSellerId() { return sellerId; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getTierId() { return tierId; }
    public void setTierId(String tierId) { this.tierId = tierId; }

    public String getTierName() { return tierName; }
    public void setTierName(String tierName) { this.tierName = tierName; }

    public double getPricePerTicket() { return pricePerTicket; }
    public void setPricePerTicket(double pricePerTicket) { this.pricePerTicket = pricePerTicket; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    public double getDiscount() { return discount; }
    public void setDiscount(double discount) { this.discount = discount; }

    public String getVoucherCode() { return voucherCode; }
    public void setVoucherCode(String voucherCode) { this.voucherCode = voucherCode; }

    public Timestamp getBookingDate() { return bookingDate; }
    public void setBookingDate(Timestamp bookingDate) { this.bookingDate = bookingDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getProcessedBy() { return processedBy; }
    public void setProcessedBy(String processedBy) { this.processedBy = processedBy; }

    public String getAdminNote() { return adminNote; }
    public void setAdminNote(String adminNote) { this.adminNote = adminNote; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

    public boolean isCheckedIn() { return isCheckedIn; }
    public void setCheckedIn(boolean checkedIn) { isCheckedIn = checkedIn; }

    public Timestamp getCheckInAt() { return checkInAt; }
    public void setCheckInAt(Timestamp checkInAt) { this.checkInAt = checkInAt; }

    public String getCheckInBy() { return checkInBy; }
    public void setCheckInBy(String checkInBy) { this.checkInBy = checkInBy; }
}
