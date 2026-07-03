package vn.humg.hai.event_ticket_booking_app.controller;

import java.util.List;
import java.util.function.Consumer;
import vn.humg.hai.event_ticket_booking_app.dao.ReviewDao;
import vn.humg.hai.event_ticket_booking_app.model.Review;

public class ReviewController {
    private final ReviewDao reviewDao;
    private final EventController eventController;

    public ReviewController() {
        this.reviewDao = new ReviewDao();
        this.eventController = new EventController();
    }

    public void saveReview(Review review, Runnable onSuccess, Consumer<String> onError) {
        reviewDao.saveReview(review, () -> {
            // Tự động cập nhật điểm trung bình cho sự kiện sau khi gửi review thành công
            updateEventRating(review.getEventId());
            onSuccess.run();
        }, onError);
    }

    public void getAllReviews(Consumer<List<Review>> onSuccess, Consumer<String> onError) {
        reviewDao.getAllReviews(onSuccess, onError);
    }

    public void getReviewsByEvent(String eventId, Consumer<List<Review>> onSuccess, Consumer<String> onError) {
        reviewDao.getReviewsByEvent(eventId, onSuccess, onError);
    }

    public void deleteReview(String reviewId, Runnable onSuccess, Consumer<String> onError) {
        reviewDao.deleteReview(reviewId, onSuccess, onError);
    }

    private void updateEventRating(String eventId) {
        reviewDao.getReviewsByEvent(eventId, reviews -> {
            if (reviews == null || reviews.isEmpty()) return;

            float sum = 0;
            for (Review r : reviews) {
                sum += r.getRating();
            }
            float average = sum / reviews.size();
            eventController.updateRating(eventId, average, reviews.size(), () -> {}, e -> {});
        }, error -> {});
    }
}
