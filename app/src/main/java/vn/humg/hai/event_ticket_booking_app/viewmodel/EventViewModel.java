package vn.humg.hai.event_ticket_booking_app.viewmodel;

import vn.humg.hai.event_ticket_booking_app.controller.*;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.List;
import java.util.ArrayList;
import vn.humg.hai.event_ticket_booking_app.model.Event;
import vn.humg.hai.event_ticket_booking_app.model.Favorite;
import vn.humg.hai.event_ticket_booking_app.model.Review;




public class EventViewModel extends ViewModel {
    private final EventController eventRepository;
    private final FavoriteController favoriteRepository;
    private final ReviewController reviewRepository;

    private final MutableLiveData<Boolean> _loadingState = new MutableLiveData<>(false);
    private final MutableLiveData<String> _errorState = new MutableLiveData<>(null);
    private final MutableLiveData<List<Event>> _eventsState = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Event> _eventDetailState = new MutableLiveData<>(null);
    private final MutableLiveData<List<Review>> _reviewsState = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> _isFavoriteState = new MutableLiveData<>(false);
    private final MutableLiveData<List<Favorite>> _userFavoritesState = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> _reviewSuccessState = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> _hasReviewedState = new MutableLiveData<>(false);

    public EventViewModel() {
        this.eventRepository = new EventController();
        this.favoriteRepository = new FavoriteController();
        this.reviewRepository = new ReviewController();
    }

    public LiveData<Boolean> getLoadingState() { return _loadingState; }
    public LiveData<String> getErrorState() { return _errorState; }
    public LiveData<List<Event>> getEventsState() { return _eventsState; }
    public LiveData<Event> getEventDetailState() { return _eventDetailState; }
    public LiveData<List<Review>> getReviewsState() { return _reviewsState; }
    public LiveData<Boolean> getIsFavoriteState() { return _isFavoriteState; }
    public LiveData<List<Favorite>> getUserFavoritesState() { return _userFavoritesState; }
    public LiveData<Boolean> getReviewSuccessState() { return _reviewSuccessState; }
    public LiveData<Boolean> getHasReviewedState() { return _hasReviewedState; }

    public void loadAllEvents() {
        _loadingState.setValue(true);
        eventRepository.getAllEvents(
                events -> {
                    _eventsState.postValue(events);
                    _loadingState.postValue(false);
                },
                error -> {
                    _errorState.postValue(error);
                    _loadingState.postValue(false);
                });
    }

    public void loadEventById(String eventId) {
        _loadingState.setValue(true);
        eventRepository.getEventById(eventId,
                event -> {
                    _eventDetailState.postValue(event);
                    _loadingState.postValue(false);
                },
                error -> {
                    _errorState.postValue(error);
                    _loadingState.postValue(false);
                });
    }

    public void loadReviewsForEvent(String eventId) {
        _loadingState.setValue(true);
        reviewRepository.getReviewsByEvent(eventId,
                reviews -> {
                    _reviewsState.postValue(reviews);
                    _loadingState.postValue(false);
                },
                error -> {
                    _errorState.postValue(error);
                    _loadingState.postValue(false);
                });
    }

    public void submitReview(Review review) {
        _loadingState.setValue(true);
        _reviewSuccessState.setValue(false);
        reviewRepository.saveReview(review,
                () -> {
                    _reviewSuccessState.postValue(true);
                    _loadingState.postValue(false);
                },
                error -> {
                    _errorState.postValue(error);
                    _loadingState.postValue(false);
                });
    }

    public void checkHasUserReviewed(String eventId, String userId) {
        reviewRepository.hasUserReviewedEvent(eventId, userId,
                hasReviewed -> _hasReviewedState.postValue(hasReviewed),
                error -> _errorState.postValue(error));
    }

    public void loadUserFavorites(String userId) {
        _loadingState.setValue(true);
        favoriteRepository.getFavoritesByUser(userId,
                favorites -> {
                    _userFavoritesState.postValue(favorites);
                    _loadingState.postValue(false);
                },
                error -> {
                    _errorState.postValue(error);
                    _loadingState.postValue(false);
                });
    }

    public void checkFavoriteStatus(String userId, String eventId) {
        favoriteRepository.getFavoritesByUser(userId,
                favorites -> {
                    boolean isFav = false;
                    if (favorites != null) {
                        for (Favorite f : favorites) {
                            if (f.getEventId() != null && f.getEventId().equals(eventId)) {
                                isFav = true;
                                break;
                            }
                        }
                    }
                    _isFavoriteState.postValue(isFav);
                },
                error -> _errorState.postValue(error));
    }

    public void toggleFavorite(String userId, Event event) {
        favoriteRepository.getFavoritesByUser(userId,
                favorites -> {
                    boolean isFav = false;
                    if (favorites != null) {
                        for (Favorite f : favorites) {
                            if (f.getEventId() != null && f.getEventId().equals(event.getEventId())) {
                                isFav = true;
                                break;
                            }
                        }
                    }

                    if (isFav) {
                        favoriteRepository.removeFromFavorite(userId, event.getEventId(),
                                () -> _isFavoriteState.postValue(false),
                                error -> _errorState.postValue(error));
                    } else {
                        Favorite fav = new Favorite(userId + "_" + event.getEventId(), userId, event.getEventId());
                        favoriteRepository.addToFavorite(fav,
                                () -> _isFavoriteState.postValue(true),
                                error -> _errorState.postValue(error));
                    }
                },
                error -> _errorState.postValue(error));
    }
}
