package vn.humg.hai.event_ticket_booking_app.dao;

import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import vn.humg.hai.event_ticket_booking_app.model.Favorite;

public class FavoriteDao extends BaseDao {
    private static final String FAVORITES_COLLECTION = "favorites";

    public void addToFavorite(Favorite favorite, Runnable onSuccess, Consumer<String> onError) {
        firestore.collection(FAVORITES_COLLECTION)
                .document(favorite.getUserId() + "_" + favorite.getEventId())
                .set(favorite)
                .addOnSuccessListener(aVoid -> onSuccess.run())
                .addOnFailureListener(e -> onError.accept(e.getMessage()));
    }

    public void removeFromFavorite(String userId, String eventId, Runnable onSuccess, Consumer<String> onError) {
        firestore.collection(FAVORITES_COLLECTION)
                .document(userId + "_" + eventId)
                .delete()
                .addOnSuccessListener(aVoid -> onSuccess.run())
                .addOnFailureListener(e -> onError.accept(e.getMessage()));
    }

    public void getFavoritesByUser(String userId, Consumer<List<Favorite>> onSuccess, Consumer<String> onError) {
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
