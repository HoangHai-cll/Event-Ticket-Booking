package vn.humg.hai.event_ticket_booking_app.model;

public class TicketTier {
    private String tierId;
    private String tierName;       // "VIP", "Standard", "Economy"...
    private double price;          // Giá vé hạng này
    private int totalTicket;       // Tổng số vé hạng này
    private int remainingTicket;   // Số vé còn lại
    private String seatMapImageUrl; // Ảnh sơ đồ chỗ ngồi (optional)

    public TicketTier() {}

    public TicketTier(String tierId, String tierName, double price, int totalTicket, String seatMapImageUrl) {
        this.tierId = tierId;
        this.tierName = tierName;
        this.price = price;
        this.totalTicket = totalTicket;
        this.remainingTicket = totalTicket;
        this.seatMapImageUrl = seatMapImageUrl;
    }

    public String getTierId() { return tierId; }
    public void setTierId(String tierId) { this.tierId = tierId; }

    public String getTierName() { return tierName; }
    public void setTierName(String tierName) { this.tierName = tierName; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getTotalTicket() { return totalTicket; }
    public void setTotalTicket(int totalTicket) { this.totalTicket = totalTicket; }

    public int getRemainingTicket() { return remainingTicket; }
    public void setRemainingTicket(int remainingTicket) { this.remainingTicket = remainingTicket; }

    public String getSeatMapImageUrl() { return seatMapImageUrl; }
    public void setSeatMapImageUrl(String seatMapImageUrl) { this.seatMapImageUrl = seatMapImageUrl; }
}
