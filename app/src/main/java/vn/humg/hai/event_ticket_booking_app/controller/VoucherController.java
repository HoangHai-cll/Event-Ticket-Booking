package vn.humg.hai.event_ticket_booking_app.controller;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import vn.humg.hai.event_ticket_booking_app.model.Voucher;

public class VoucherController {
    private static final String SYSTEM_VOUCHERS_COLLECTION = "system_vouchers";
    private static final String USER_VOUCHERS_COLLECTION = "user_vouchers";
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    public void addSystemVoucher(Voucher voucher, Runnable onSuccess, Consumer<String> onError) {
        if (voucher == null || voucher.getVoucherId() == null) {
            onError.accept("Voucher or voucherId is null");
            return;
        }
        firestore.collection(SYSTEM_VOUCHERS_COLLECTION)
                .document(voucher.getVoucherId())
                .set(voucher)
                .addOnSuccessListener(aVoid -> onSuccess.run())
                .addOnFailureListener(e -> onError.accept(e.getMessage()));
    }

    public void getAllSystemVouchers(Consumer<List<Voucher>> onSuccess, Consumer<String> onError) {
        firestore.collection(SYSTEM_VOUCHERS_COLLECTION)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Voucher> vouchers = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot) {
                        Voucher v = doc.toObject(Voucher.class);
                        if (v != null) vouchers.add(v);
                    }
                    onSuccess.accept(vouchers);
                })
                .addOnFailureListener(e -> onError.accept(e.getMessage()));
    }

    public void deleteSystemVoucher(String voucherId, Runnable onSuccess, Consumer<String> onError) {
        if (voucherId == null) {
            onError.accept("voucherId is null");
            return;
        }
        firestore.collection(SYSTEM_VOUCHERS_COLLECTION)
                .document(voucherId)
                .delete()
                .addOnSuccessListener(aVoid -> onSuccess.run())
                .addOnFailureListener(e -> onError.accept(e.getMessage()));
    }

    public void getUserVouchers(String userId, Consumer<List<Voucher>> onSuccess, Consumer<String> onError) {
        if (userId == null) {
            onError.accept("userId is null");
            return;
        }
        firestore.collection("users")
                .document(userId)
                .collection(USER_VOUCHERS_COLLECTION)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Voucher> vouchers = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot) {
                        Voucher v = doc.toObject(Voucher.class);
                        if (v != null) vouchers.add(v);
                    }
                    onSuccess.accept(vouchers);
                })
                .addOnFailureListener(e -> onError.accept(e.getMessage()));
    }

    public void addUserVoucher(String userId, Voucher voucher, Runnable onSuccess, Consumer<String> onError) {
        if (userId == null || voucher == null || voucher.getVoucherId() == null) {
            onError.accept("userId, voucher or voucherId is null");
            return;
        }
        firestore.collection("users")
                .document(userId)
                .collection(USER_VOUCHERS_COLLECTION)
                .document(voucher.getVoucherId())
                .set(voucher)
                .addOnSuccessListener(aVoid -> onSuccess.run())
                .addOnFailureListener(e -> onError.accept(e.getMessage()));
    }

    public void removeUserVoucher(String userId, String voucherId, Runnable onSuccess, Consumer<String> onError) {
        if (userId == null || voucherId == null) {
            onError.accept("userId or voucherId is null");
            return;
        }
        firestore.collection("users")
                .document(userId)
                .collection(USER_VOUCHERS_COLLECTION)
                .document(voucherId)
                .delete()
                .addOnSuccessListener(aVoid -> onSuccess.run())
                .addOnFailureListener(e -> onError.accept(e.getMessage()));
    }

    public void checkAndGiveFirstPurchaseVoucher(String userId, Runnable onComplete) {
        if (userId == null) { onComplete.run(); return; }
        firestore.collection("bookings")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        firestore.collection("users")
                                .document(userId)
                                .collection(USER_VOUCHERS_COLLECTION)
                                .whereEqualTo("code", "WELCOME50")
                                .get()
                                .addOnSuccessListener(vouchersSnapshot -> {
                                    if (vouchersSnapshot.isEmpty()) {
                                        Calendar cal = Calendar.getInstance();
                                        cal.add(Calendar.DAY_OF_YEAR, 30);
                                        Voucher welcomeVoucher = new Voucher(
                                                UUID.randomUUID().toString(),
                                                "WELCOME50",
                                                "Chào mừng bạn mới - Giảm 50k",
                                                "FIXED",
                                                50000.0,
                                                100000.0,
                                                50000.0,
                                                new Timestamp(cal.getTime()),
                                                false
                                        );
                                        addUserVoucher(userId, welcomeVoucher, onComplete, e -> onComplete.run());
                                    } else {
                                        onComplete.run();
                                    }
                                })
                                .addOnFailureListener(e -> onComplete.run());
                    } else {
                        onComplete.run();
                    }
                })
                .addOnFailureListener(e -> onComplete.run());
    }
}