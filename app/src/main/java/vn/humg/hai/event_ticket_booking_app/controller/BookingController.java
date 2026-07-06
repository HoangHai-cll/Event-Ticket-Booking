package vn.humg.hai.event_ticket_booking_app.controller;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import vn.humg.hai.event_ticket_booking_app.model.Booking;

public class BookingController {
    private static final String BOOKINGS_COLLECTION = "bookings";
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    public void saveBooking(Booking booking, Runnable onSuccess, Consumer<String> onError) {
        if (booking == null || booking.getBookingId() == null) {
            onError.accept("Booking or bookingId is null");
            return;
        }
        firestore.collection(BOOKINGS_COLLECTION)
                .document(booking.getBookingId())
                .set(booking)
                .addOnSuccessListener(aVoid -> onSuccess.run())
                .addOnFailureListener(e -> onError.accept(e.getMessage()));
    }

    public void getBookingById(String bookingId, Consumer<Booking> onSuccess, Consumer<String> onError) {
        if (bookingId == null) {
            onError.accept("bookingId is null");
            return;
        }
        firestore.collection(BOOKINGS_COLLECTION)
                .document(bookingId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Booking booking = documentSnapshot.toObject(Booking.class);
                    onSuccess.accept(booking);
                })
                .addOnFailureListener(e -> onError.accept(e.getMessage()));
    }

    public void getBookingsByUser(String userId, Consumer<List<Booking>> onSuccess, Consumer<String> onError) {
        if (userId == null) {
            onError.accept("userId is null");
            return;
        }
        firestore.collection(BOOKINGS_COLLECTION)
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Booking> bookings = new ArrayList<>();
                    for (DocumentSnapshot document : querySnapshot) {
                        Booking booking = document.toObject(Booking.class);
                        if (booking != null) {
                            bookings.add(booking);
                        }
                    }
                    onSuccess.accept(bookings);
                })
                .addOnFailureListener(e -> onError.accept(e.getMessage()));
    }

    public void getBookingsBySeller(String sellerId, Consumer<List<Booking>> onSuccess, Consumer<String> onError) {
        if (sellerId == null) {
            onError.accept("sellerId is null");
            return;
        }
        firestore.collection(BOOKINGS_COLLECTION)
                .whereEqualTo("sellerId", sellerId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Booking> bookings = new ArrayList<>();
                    for (DocumentSnapshot document : querySnapshot) {
                        Booking booking = document.toObject(Booking.class);
                        if (booking != null) {
                            bookings.add(booking);
                        }
                    }
                    onSuccess.accept(bookings);
                })
                .addOnFailureListener(e -> onError.accept(e.getMessage()));
    }

    public void getAllBookings(Consumer<List<Booking>> onSuccess, Consumer<String> onError) {
        firestore.collection(BOOKINGS_COLLECTION)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Booking> bookings = new ArrayList<>();
                    for (DocumentSnapshot document : querySnapshot) {
                        Booking booking = document.toObject(Booking.class);
                        if (booking != null) {
                            bookings.add(booking);
                        }
                    }
                    onSuccess.accept(bookings);
                })
                .addOnFailureListener(e -> onError.accept(e.getMessage()));
    }

    public void updateBookingStatus(String bookingId, String status, Runnable onSuccess, Consumer<String> onError) {
        if (bookingId == null) {
            onError.accept("bookingId is null");
            return;
        }
        firestore.collection(BOOKINGS_COLLECTION)
                .document(bookingId)
                .update("status", status)
                .addOnSuccessListener(aVoid -> onSuccess.run())
                .addOnFailureListener(e -> onError.accept(e.getMessage()));
    }
}