package vn.humg.hai.event_ticket_booking_app.dao;

import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import vn.humg.hai.event_ticket_booking_app.model.Review;

public class ReviewDao extends BaseDao {
    private static final String REVIEWS_COLLECTION = "reviews";

    public void saveReview(Review review, Runnable onSuccess, Consumer<String> onError) {
        if (review == null || review.getReviewId() == null) {
            onError.accept("Review or reviewId is null");
            return;
        }

        firestore.collection(REVIEWS_COLLECTION)
                .document(review.getReviewId())
                .set(review)
                .addOnSuccessListener(aVoid -> onSuccess.run())
                .addOnFailureListener(e -> onError.accept(e.getMessage()));
    }

    public void getAllReviews(Consumer<List<Review>> onSuccess, Consumer<String> onError) {
        firestore.collection(REVIEWS_COLLECTION)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Review> reviews = new ArrayList<>();
                    for (DocumentSnapshot document : querySnapshot) {
                        Review review = document.toObject(Review.class);
                        if (review != null) {
                            reviews.add(review);
                        }
                    }
                    onSuccess.accept(reviews);
                })
                .addOnFailureListener(e -> onError.accept(e.getMessage()));
    }

    public void getReviewsByEvent(String eventId, Consumer<List<Review>> onSuccess, Consumer<String> onError) {
        firestore.collection(REVIEWS_COLLECTION)
                .whereEqualTo("eventId", eventId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Review> reviews = new ArrayList<>();
                    for (DocumentSnapshot document : querySnapshot) {
                        Review review = document.toObject(Review.class);
                        if (review != null) {
                            reviews.add(review);
                        }
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
                .delete()
                .addOnSuccessListener(aVoid -> onSuccess.run())
                .addOnFailureListener(e -> onError.accept(e.getMessage()));
    }
}
