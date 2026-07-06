package vn.humg.hai.event_ticket_booking_app.controller;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;
import java.util.function.Consumer;
import vn.humg.hai.event_ticket_booking_app.model.Admin;
import vn.humg.hai.event_ticket_booking_app.model.User;
import vn.humg.hai.event_ticket_booking_app.model.Voucher;

public class UserController {
    private static final String USERS_COLLECTION = "users";
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    public void getAdminById(String uid, Consumer<Admin> onSuccess, Consumer<String> onError) {
        if (uid == null) { onError.accept("uid is null"); return; }
        firestore.collection(USERS_COLLECTION)
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> onSuccess.accept(doc.toObject(Admin.class)))
                .addOnFailureListener(e -> onError.accept(e.getMessage()));
    }

    public void saveUserProfile(User user, Runnable onSuccess, Consumer<String> onError) {
        if (user == null || user.getUid() == null) {
            onError.accept("User or uid is null");
            return;
        }
        if (user.getCreatedAt() == null) {
            user.setCreatedAt(Timestamp.now());
        }
        if (user.getLastLogin() == null) {
            user.setLastLogin(Timestamp.now());
        }
        firestore.collection(USERS_COLLECTION)
                .document(user.getUid())
                .set(user)
                .addOnSuccessListener(aVoid -> onSuccess.run())
                .addOnFailureListener(e -> onError.accept(e.getMessage()));
    }

    public void getUserById(String uid, Consumer<User> onSuccess, Consumer<String> onError) {
        if (uid == null) { onError.accept("uid is null"); return; }
        firestore.collection(USERS_COLLECTION)
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    User user = documentSnapshot.toObject(User.class);
                    onSuccess.accept(user);
                })
                .addOnFailureListener(e -> onError.accept(e.getMessage()));
    }

    public void getAllUsers(Consumer<List<User>> onSuccess, Consumer<String> onError) {
        firestore.collection(USERS_COLLECTION)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<User> users = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot) {
                        User user = doc.toObject(User.class);
                        if (user != null) users.add(user);
                    }
                    onSuccess.accept(users);
                })
                .addOnFailureListener(e -> onError.accept(e.getMessage()));
    }

    public void updateLastLogin(String uid, Runnable onSuccess, Consumer<String> onError) {
        if (uid == null) { onError.accept("uid is null"); return; }
        firestore.collection(USERS_COLLECTION)
                .document(uid)
                .update("lastLogin", com.google.firebase.firestore.FieldValue.serverTimestamp())
                .addOnSuccessListener(aVoid -> onSuccess.run())
                .addOnFailureListener(e -> onError.accept(e.getMessage()));
    }

    public void addExp(String uid, long amount, Consumer<String> onLevelUp, Runnable onSuccess, Consumer<String> onError) {
        getUserById(uid, user -> {
            if (user == null) {
                onError.accept("User not found");
                return;
            }
            long oldExp = user.getExp();
            long newExp = oldExp + amount;
            user.setExp(newExp);
            
            String oldTier = user.getMemberTier() != null ? user.getMemberTier() : "Thường";
            String newTier = User.computeTier(newExp);
            user.setMemberTier(newTier);
            
            saveUserProfile(user, () -> {
                if (!newTier.equalsIgnoreCase(oldTier)) {
                    grantLevelUpVouchers(uid, newTier);
                    onLevelUp.accept(newTier);
                } else {
                    onSuccess.run();
                }
            }, onError);
        }, onError);
    }

    private void grantLevelUpVouchers(String uid, String newTier) {
        VoucherController voucherController = new VoucherController();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, 30);
        
        List<Voucher> giftVouchers = new ArrayList<>();
        if ("Đồng".equalsIgnoreCase(newTier)) {
            giftVouchers.add(new Voucher(UUID.randomUUID().toString(), "DONG5", "Đặc quyền hạng Đồng 5%", "PERCENT", 5.0, 0.0, 50000.0, new Timestamp(cal.getTime()), false));
            giftVouchers.add(new Voucher(UUID.randomUUID().toString(), "DONG10", "Đặc quyền hạng Đồng 10%", "PERCENT", 10.0, 100000.0, 80000.0, new Timestamp(cal.getTime()), false));
        } else if ("Bạc".equalsIgnoreCase(newTier)) {
            giftVouchers.add(new Voucher(UUID.randomUUID().toString(), "BAC10", "Đặc quyền hạng Bạc 10%", "PERCENT", 10.0, 0.0, 100000.0, new Timestamp(cal.getTime()), false));
            giftVouchers.add(new Voucher(UUID.randomUUID().toString(), "BAC15", "Đặc quyền hạng Bạc 15%", "PERCENT", 15.0, 150000.0, 120000.0, new Timestamp(cal.getTime()), false));
        } else if ("Vàng".equalsIgnoreCase(newTier)) {
            giftVouchers.add(new Voucher(UUID.randomUUID().toString(), "VANG15", "Đặc quyền hạng Vàng 15%", "PERCENT", 15.0, 0.0, 150000.0, new Timestamp(cal.getTime()), false));
            giftVouchers.add(new Voucher(UUID.randomUUID().toString(), "VANG20", "Đặc quyền hạng Vàng 20%", "PERCENT", 20.0, 200000.0, 200000.0, new Timestamp(cal.getTime()), false));
        } else if ("Thân thiết số một".equalsIgnoreCase(newTier)) {
            giftVouchers.add(new Voucher(UUID.randomUUID().toString(), "VIP20", "Đặc quyền hạng VIP 20%", "PERCENT", 20.0, 0.0, 250000.0, new Timestamp(cal.getTime()), false));
            giftVouchers.add(new Voucher(UUID.randomUUID().toString(), "VIP50", "Đặc quyền hạng VIP 50%", "PERCENT", 50.0, 300000.0, 500000.0, new Timestamp(cal.getTime()), false));
        }
        
        for (Voucher v : giftVouchers) {
            voucherController.addUserVoucher(uid, v, () -> {}, e -> {});
        }
    }

    public void incrementAdminBookingCount(String adminId, int ticketQuantity, Consumer<Admin> onUpdate, Consumer<String> onError) {
        getAdminById(adminId, admin -> {
            if (admin == null) {
                onError.accept("Admin not found");
                return;
            }
            int newCount = admin.getBookingsConfirmedCount() + ticketQuantity;
            admin.setBookingsConfirmedCount(newCount);
            
            if (admin.getAccessLevel() == 1 && newCount >= 10) {
                admin.setAccessLevel(2);
            }
            
            saveUserProfile(admin, () -> onUpdate.accept(admin), onError);
        }, onError);
    }
}