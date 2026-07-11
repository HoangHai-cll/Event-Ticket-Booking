package vn.humg.hai.event_ticket_booking_app.model;

import com.google.firebase.Timestamp;

public class User {
    private String uid;
    private String fullName;
    private String email;
    private String phone;
    private String role; // "user" hoặc "admin"
    private Timestamp createdAt;
    private Timestamp lastLogin;

    // --- Additional Fields for Interactive Profile ---
    private String birthday;
    private String gender;
    private String avatarName; // e.g. "avatar_1", "avatar_2"
    private java.util.List<String> interests;

    private long exp = 0;
    private String memberTier = "Thường";
    private String fcmToken;

    public User() {
        this.role = "user"; // Mặc định là user
        this.interests = new java.util.ArrayList<>();
        this.exp = 0;
        this.memberTier = "Thường";
    }

    public String getFcmToken() { return fcmToken; }
    public void setFcmToken(String fcmToken) { this.fcmToken = fcmToken; }

    public long getExp() { return exp; }
    public void setExp(long exp) { this.exp = exp; }

    public String getMemberTier() { return memberTier; }
    public void setMemberTier(String memberTier) { this.memberTier = memberTier; }

    public static String computeTier(long expPoints) {
        if (expPoints < 500) return "Thường";
        if (expPoints < 1500) return "Đồng";
        if (expPoints < 3500) return "Bạc";
        if (expPoints < 7500) return "Vàng";
        return "Thân thiết số một";
    }

    public static int getTierLevel(String tier) {
        if (tier == null) return 0;
        switch (tier) {
            case "Đồng": return 1;
            case "Bạc": return 2;
            case "Vàng": return 3;
            case "Thân thiết số một": return 4;
            default: return 0; // "Thường"
        }
    }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getLastLogin() { return lastLogin; }
    public void setLastLogin(Timestamp lastLogin) { this.lastLogin = lastLogin; }

    public String getBirthday() { return birthday; }
    public void setBirthday(String birthday) { this.birthday = birthday; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getAvatarName() { return avatarName; }
    public void setAvatarName(String avatarName) { this.avatarName = avatarName; }

    public java.util.List<String> getInterests() { return interests; }
    public void setInterests(java.util.List<String> interests) { this.interests = interests; }
}
