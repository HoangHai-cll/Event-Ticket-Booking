package vn.humg.hai.event_ticket_booking_app.model;

public class NotificationModel {
    private int id;
    private String title;
    private String body;
    private long timestamp;
    private String type; // TICKET, VOUCHER, SYSTEM
    private int isRead; // 0 for unread, 1 for read

    public NotificationModel() {
    }

    public NotificationModel(int id, String title, String body, long timestamp, String type, int isRead) {
        this.id = id;
        this.title = title;
        this.body = body;
        this.timestamp = timestamp;
        this.type = type;
        this.isRead = isRead;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getIsRead() {
        return isRead;
    }

    public void setIsRead(int isRead) {
        this.isRead = isRead;
    }
}
