package vn.humg.hai.event_ticket_booking_app.model;

public class Favorite {
    private String favoriteId;
    private String userId;
    private String eventId;

    public Favorite() {}

    public Favorite(String favoriteId, String userId, String eventId) {
        this.favoriteId = favoriteId;
        this.userId = userId;
        this.eventId = eventId;
    }

    public String getFavoriteId() { return favoriteId; }
    public void setFavoriteId(String favoriteId) { this.favoriteId = favoriteId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
}
