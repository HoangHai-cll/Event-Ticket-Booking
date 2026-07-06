package vn.humg.hai.event_ticket_booking_app.controller;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class AuthController {
    private final FirebaseAuth auth;
    private final FirebaseFirestore firestore;

    public AuthController() {
        this.auth = FirebaseAuth.getInstance();
        this.firestore = FirebaseFirestore.getInstance();
    }

    public FirebaseAuth getAuth() { return auth; }
    public FirebaseFirestore getFirestore() { return firestore; }

    public void saveUserProfile(String uid, String fullName, String email, String phone, Runnable onSuccess, java.util.function.Consumer<String> onError) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("fullName", fullName);
        userData.put("email", email);
        userData.put("phone", phone);
        userData.put("createdAt", FieldValue.serverTimestamp());
        userData.put("lastLogin", FieldValue.serverTimestamp());

        firestore.collection("users").document(uid)
                .set(userData)
                .addOnSuccessListener(aVoid -> onSuccess.run())
                .addOnFailureListener(e -> onError.accept(e.getMessage()));
    }
}