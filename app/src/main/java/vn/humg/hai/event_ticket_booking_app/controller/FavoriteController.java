package vn.humg.hai.event_ticket_booking_app.controller;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import vn.humg.hai.event_ticket_booking_app.model.Favorite;

public class FavoriteController {
    private static final String FAVORITES_COLLECTION = "favorites";
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    public void addToFavorite(Favorite favorite, Runnable onSuccess, Consumer<String> onError) {
        if (favorite == null || favorite.getUserId() == null || favorite.getEventId() == null) {
            onError.accept("favorite, userId or eventId is null");
            return;
        }
        firestore.collection(FAVORITES_COLLECTION)
                .document(favorite.getUserId() + "_" + favorite.getEventId())
                .set(favorite)
                .addOnSuccessListener(aVoid -> onSuccess.run())
                .addOnFailureListener(e -> onError.accept(e.getMessage()));
    }

    public void removeFromFavorite(String userId, String eventId, Runnable onSuccess, Consumer<String> onError) {
        if (userId == null || eventId == null) {
            onError.accept("userId or eventId is null");
            return;
        }
        firestore.collection(FAVORITES_COLLECTION)
                .document(userId + "_" + eventId)
                .delete()
                .addOnSuccessListener(aVoid -> onSuccess.run())
                .addOnFailureListener(e -> onError.accept(e.getMessage()));
    }

    public void getFavoritesByUser(String userId, Consumer<List<Favorite>> onSuccess, Consumer<String> onError) {
        if (userId == null) {
            onError.accept("userId is null");
            return;
        }
        firestore.collection(FAVORITES_COLLECTION)
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Favorite> favorites = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot) {
                        Favorite fav = doc.toObject(Favorite.class);
                        if (fav != null) favorites.add(fav);
                    }
                    onSuccess.accept(favorites);
                })
                .addOnFailureListener(e -> onError.accept(e.getMessage()));
    }
}