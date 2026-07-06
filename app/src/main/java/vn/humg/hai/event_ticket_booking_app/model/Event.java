package vn.humg.hai.event_ticket_booking_app.model;

import com.google.firebase.Timestamp;
import java.util.ArrayList;
import java.util.List;
import vn.humg.hai.event_ticket_booking_app.model.TicketTier;

public class Event {
    private String eventId;
    private String title;
    private String description;
    private String location;
    private String googleMapsUrl;
    private Timestamp date;       // Start Date & Time
    private Timestamp endDate;    // End Date & Time
    private double price;
    private int totalTicket;
    private int remainingTicket;
    private String image;
    private String category;
    private boolean isHot;
    private boolean isFree;
    private float averageRating; 
    private int reviewCount;     
    private String createdByAdminId; 
    private List<String> tags;
    private List<String> speakers;
    private String status;        // "Active", "Draft", "Completed"
    
    // New fields
    private String organizerName;
    private String organizerImage;
    private String artistName;
    private String artistImage;
    private List<TicketTier> tiers;  // Hạng vé
    private String requiredTier; // Yêu cầu hạng thành viên để mua vé
    private int creatorAccessLevel = 1; // Cấp độ admin tạo: 1: Staff, 2: Manager, 3: Developer

    public Event() {
        this.averageRating = 0;
        this.reviewCount = 0;
        this.isFree = false;
        this.tags = new ArrayList<>();
        this.speakers = new ArrayList<>();
        this.tiers = new ArrayList<>();
        this.status = "Draft";
        this.creatorAccessLevel = 1; // Mặc định là Staff
    }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getGoogleMapsUrl() { return googleMapsUrl; }
    public void setGoogleMapsUrl(String googleMapsUrl) { this.googleMapsUrl = googleMapsUrl; }

    public Timestamp getDate() { return date; }
    public void setDate(Timestamp date) { this.date = date; }

    public Timestamp getEndDate() { return endDate; }
    public void setEndDate(Timestamp endDate) { this.endDate = endDate; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getTotalTicket() { return totalTicket; }
    public void setTotalTicket(int totalTicket) { this.totalTicket = totalTicket; }

    public int getRemainingTicket() { return remainingTicket; }
    public void setRemainingTicket(int remainingTicket) { this.remainingTicket = remainingTicket; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public boolean isHot() { return isHot; }
    public void setHot(boolean hot) { isHot = hot; }

    public boolean isFree() { return isFree; }
    public void setFree(boolean free) { isFree = free; }

    public float getAverageRating() { return averageRating; }
    public void setAverageRating(float averageRating) { this.averageRating = averageRating; }

    public int getReviewCount() { return reviewCount; }
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }

    public String getCreatedByAdminId() { return createdByAdminId; }
    public void setCreatedByAdminId(String createdByAdminId) { this.createdByAdminId = createdByAdminId; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public List<String> getSpeakers() { return speakers; }
    public void setSpeakers(List<String> speakers) { this.speakers = speakers; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getOrganizerName() { return organizerName; }
    public void setOrganizerName(String organizerName) { this.organizerName = organizerName; }

    public String getOrganizerImage() { return organizerImage; }
    public void setOrganizerImage(String organizerImage) { this.organizerImage = organizerImage; }

    public String getArtistName() { return artistName; }
    public void setArtistName(String artistName) { this.artistName = artistName; }

    public String getArtistImage() { return artistImage; }
    public void setArtistImage(String artistImage) { this.artistImage = artistImage; }

    public List<TicketTier> getTiers() { return tiers; }
    public void setTiers(List<TicketTier> tiers) { this.tiers = tiers; }

    public boolean hasTiers() {
        return tiers != null && !tiers.isEmpty();
    }

    public String getRequiredTier() { return requiredTier; }
    public void setRequiredTier(String requiredTier) { this.requiredTier = requiredTier; }

    public int getCreatorAccessLevel() { return creatorAccessLevel; }
    public void setCreatorAccessLevel(int creatorAccessLevel) { this.creatorAccessLevel = creatorAccessLevel; }
}
