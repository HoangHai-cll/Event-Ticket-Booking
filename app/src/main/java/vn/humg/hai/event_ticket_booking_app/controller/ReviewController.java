package vn.humg.hai.event_ticket_booking_app.controller;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import vn.humg.hai.event_ticket_booking_app.model.Review;

public class ReviewController {
    private static final String REVIEWS_COLLECTION = "reviews";
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    public void saveReview(Review review, Runnable onSuccess, Consumer<String> onError) {
        if (review == null || review.getReviewId() == null) {
            onError.accept("Review or reviewId is null");
            return;
        }
        firestore.collection(REVIEWS_COLLECTION)
                .document(review.getReviewId())
                .set(review)
                .addOnSuccessListener(aVoid -> {
                    updateEventRating(review.getEventId());
                    onSuccess.run();
                })
                .addOnFailureListener(e -> onError.accept(e.getMessage()));
    }

    public void getAllReviews(Consumer<List<Review>> onSuccess, Consumer<String> onError) {
        firestore.collection(REVIEWS_COLLECTION)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Review> reviews = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot) {
                        Review r = doc.toObject(Review.class);
                        if (r != null) reviews.add(r);
                    }
                    onSuccess.accept(reviews);
                })
                .addOnFailureListener(e -> onError.accept(e.getMessage()));
    }

    public void getReviewsByEvent(String eventId, Consumer<List<Review>> onSuccess, Consumer<String> onError) {
        if (eventId == null) {
            onError.accept("eventId is null");
            return;
        }
        firestore.collection(REVIEWS_COLLECTION)
                .whereEqualTo("eventId", eventId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Review> reviews = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot) {
                        Review r = doc.toObject(Review.class);
                        if (r != null) reviews.add(r);
                    }
                    onSuccess.accept(reviews);
                })
                .addOnFailureListener(e -> onError.accept(e.getMessage()));
    }

    public void deleteReview(String reviewId, Runnable onSuccess, Consumer<String> onError) {
        if (reviewId == null) {
            onError.accept("reviewId is null");
            return;
        }
        firestore.collection(REVIEWS_COLLECTION)
                .document(reviewId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Review review = documentSnapshot.toObject(Review.class);
                    firestore.collection(REVIEWS_COLLECTION)
                            .document(reviewId)
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                if (review != null) {
                                    updateEventRating(review.getEventId());
                                }
                                onSuccess.run();
                            })
                            .addOnFailureListener(e -> onError.accept(e.getMessage()));
                })
                .addOnFailureListener(e -> onError.accept(e.getMessage()));
    }

    private void updateEventRating(String eventId) {
        getReviewsByEvent(eventId, reviews -> {
            if (reviews == null || reviews.isEmpty()) return;

            float sum = 0;
            for (Review r : reviews) {
                sum += r.getRating();
            }
            float average = sum / reviews.size();
            new EventController().updateRating(eventId, average, reviews.size(), () -> {}, e -> {});
        }, error -> {});
    }

    public void hasUserReviewedEvent(String eventId, String userId, Consumer<Boolean> onResult, Consumer<String> onError) {
        if (eventId == null || userId == null) {
            onResult.accept(false);
            return;
        }
        firestore.collection(REVIEWS_COLLECTION)
                .whereEqualTo("eventId", eventId)
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> onResult.accept(!querySnapshot.isEmpty()))
                .addOnFailureListener(e -> onError.accept(e.getMessage()));
    }
}