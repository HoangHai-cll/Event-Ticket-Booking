package vn.humg.hai.event_ticket_booking_app.controller;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import vn.humg.hai.event_ticket_booking_app.model.Event;
import vn.humg.hai.event_ticket_booking_app.model.TicketTier;

public class EventController {
    private static final String EVENTS_COLLECTION = "events";
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    public void saveEvent(Event event, Runnable onSuccess, Consumer<String> onError) {
        if (event == null || event.getEventId() == null) {
            onError.accept("Event or eventId is null");
            return;
        }
        firestore.collection(EVENTS_COLLECTION)
                .document(event.getEventId())
                .set(event)
                .addOnSuccessListener(aVoid -> onSuccess.run())
                .addOnFailureListener(e -> onError.accept(e.getMessage()));
    }

    public void getEventById(String eventId, Consumer<Event> onSuccess, Consumer<String> onError) {
        if (eventId == null) {
            onError.accept("eventId is null");
            return;
        }
        firestore.collection(EVENTS_COLLECTION)
                .document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Event event = documentSnapshot.toObject(Event.class);
                    onSuccess.accept(event);
                })
                .addOnFailureListener(e -> onError.accept(e.getMessage()));
    }

    public void getAllEvents(Consumer<List<Event>> onSuccess, Consumer<String> onError) {
        firestore.collection(EVENTS_COLLECTION)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Event> events = new ArrayList<>();
                    for (DocumentSnapshot document : querySnapshot) {
                        Event event = document.toObject(Event.class);
                        if (event != null) {
                            events.add(event);
                        }
                    }
                    onSuccess.accept(events);
                })
                .addOnFailureListener(e -> onError.accept(e.getMessage()));
    }

    public void updateRemainingTicket(String eventId, int quantitySold, Runnable onSuccess, Consumer<String> onError) {
        if (eventId == null) {
            onError.accept("eventId is null");
            return;
        }
        firestore.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(
                    firestore.collection(EVENTS_COLLECTION).document(eventId));
            Event event = snapshot.toObject(Event.class);
            if (event == null) return null;

            if (quantitySold > 0 && event.getRemainingTicket() < quantitySold) {
                throw new RuntimeException("Sự kiện đã bán hết vé hoặc không đủ số lượng.");
            }

            int newRemaining = Math.max(0, event.getRemainingTicket() - quantitySold);
            transaction.update(
                    firestore.collection(EVENTS_COLLECTION).document(eventId),
                    "remainingTicket", newRemaining);
            return null;
        }).addOnSuccessListener(v -> onSuccess.run())
          .addOnFailureListener(e -> onError.accept(e.getMessage()));
    }

    public void deleteEvent(String eventId, Runnable onSuccess, Consumer<String> onError) {
        if (eventId == null) {
            onError.accept("eventId is null");
            return;
        }
        firestore.collection(EVENTS_COLLECTION)
                .document(eventId)
                .delete()
                .addOnSuccessListener(aVoid -> onSuccess.run())
                .addOnFailureListener(e -> onError.accept(e.getMessage()));
    }

    public void updateRating(String eventId, float newAverage, int newCount, Runnable onSuccess, Consumer<String> onError) {
        if (eventId == null) { onError.accept("eventId is null"); return; }
        firestore.collection(EVENTS_COLLECTION)
                .document(eventId)
                .update("averageRating", newAverage, "reviewCount", newCount)
                .addOnSuccessListener(aVoid -> onSuccess.run())
                .addOnFailureListener(e -> onError.accept(e.getMessage()));
    }

    public void updateTierRemainingTicket(String eventId, String tierId, int quantitySold, Runnable onSuccess, Consumer<String> onError) {
        if (eventId == null || tierId == null) { onSuccess.run(); return; }
        firestore.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(
                    firestore.collection(EVENTS_COLLECTION).document(eventId));
            Event event = snapshot.toObject(Event.class);
            if (event == null || event.getTiers() == null) return null;

            List<TicketTier> tiers = event.getTiers();
            for (TicketTier tier : tiers) {
                if (tierId.equals(tier.getTierId())) {
                    if (quantitySold > 0 && tier.getRemainingTicket() < quantitySold) {
                        throw new RuntimeException("Hạng vé này đã bán hết hoặc không đủ số lượng.");
                    }
                    int newRemaining = tier.getRemainingTicket() - quantitySold;
                    tier.setRemainingTicket(newRemaining);
                    break;
                }
            }
            transaction.update(
                    firestore.collection(EVENTS_COLLECTION).document(eventId),
                    "tiers", tiers);
            return null;
        }).addOnSuccessListener(v -> onSuccess.run())
          .addOnFailureListener(e -> onError.accept(e.getMessage()));
    }
}