package vn.humg.hai.event_ticket_booking_app.controller;

import java.util.List;
import java.util.function.Consumer;
import vn.humg.hai.event_ticket_booking_app.dao.FavoriteDao;
import vn.humg.hai.event_ticket_booking_app.model.Favorite;

public class FavoriteController {
    private final FavoriteDao favoriteDao;

    public FavoriteController() {
        this.favoriteDao = new FavoriteDao();
    }

    public void addToFavorite(Favorite favorite, Runnable onSuccess, Consumer<String> onError) {
        favoriteDao.addToFavorite(favorite, onSuccess, onError);
    }

    public void removeFromFavorite(String userId, String eventId, Runnable onSuccess, Consumer<String> onError) {
        favoriteDao.removeFromFavorite(userId, eventId, onSuccess, onError);
    }

    public void getFavoritesByUser(String userId, Consumer<List<Favorite>> onSuccess, Consumer<String> onError) {
        favoriteDao.getFavoritesByUser(userId, onSuccess, onError);
    }
}
