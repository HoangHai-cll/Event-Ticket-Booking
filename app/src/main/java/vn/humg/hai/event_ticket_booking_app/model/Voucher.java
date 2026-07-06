package vn.humg.hai.event_ticket_booking_app.model;

import com.google.firebase.Timestamp;

public class Voucher {
    private String voucherId;
    private String code;
    private String title;
    private String discountType;       // "PERCENT" or "FIXED"
    private double discountValue;
    private double minOrderValue;
    private double maxDiscountAmount;
    private Timestamp expiryDate;
    private boolean isSystemDefault;

    public Voucher() {}

    public Voucher(String voucherId, String code, String title, String discountType, 
                   double discountValue, double minOrderValue, double maxDiscountAmount, 
                   Timestamp expiryDate, boolean isSystemDefault) {
        this.voucherId = voucherId;
        this.code = code;
        this.title = title;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.minOrderValue = minOrderValue;
        this.maxDiscountAmount = maxDiscountAmount;
        this.expiryDate = expiryDate;
        this.isSystemDefault = isSystemDefault;
    }

    public String getVoucherId() { return voucherId; }
    public void setVoucherId(String voucherId) { this.voucherId = voucherId; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDiscountType() { return discountType; }
    public void setDiscountType(String discountType) { this.discountType = discountType; }

    public double getDiscountValue() { return discountValue; }
    public void setDiscountValue(double discountValue) { this.discountValue = discountValue; }

    public double getMinOrderValue() { return minOrderValue; }
    public void setMinOrderValue(double minOrderValue) { this.minOrderValue = minOrderValue; }

    public double getMaxDiscountAmount() { return maxDiscountAmount; }
    public void setMaxDiscountAmount(double maxDiscountAmount) { this.maxDiscountAmount = maxDiscountAmount; }

    public Timestamp getExpiryDate() { return expiryDate; }
    public void setExpiryDate(Timestamp expiryDate) { this.expiryDate = expiryDate; }

    public boolean isSystemDefault() { return isSystemDefault; }
    public void setSystemDefault(boolean systemDefault) { isSystemDefault = systemDefault; }
}
