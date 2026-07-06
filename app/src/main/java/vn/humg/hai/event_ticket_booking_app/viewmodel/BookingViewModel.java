package vn.humg.hai.event_ticket_booking_app.viewmodel;

import vn.humg.hai.event_ticket_booking_app.controller.*;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.firebase.Timestamp;
import java.util.List;
import java.util.ArrayList;
import vn.humg.hai.event_ticket_booking_app.model.Booking;
import vn.humg.hai.event_ticket_booking_app.model.Event;
import vn.humg.hai.event_ticket_booking_app.model.Voucher;





public class BookingViewModel extends ViewModel {
    private final BookingController bookingRepository;
    private final EventController eventRepository;
    private final UserController userRepository;
    private final VoucherController voucherRepository;

    public enum BookingStatus { IDLE, LOADING, SUCCESS, ERROR, LEVEL_UP }

    public static class BookingResult {
        public BookingStatus status;
        public String bookingId;
        public String errorMessage;
        public String newTier;

        public BookingResult(BookingStatus status, String bookingId, String errorMessage, String newTier) {
            this.status = status;
            this.bookingId = bookingId;
            this.errorMessage = errorMessage;
            this.newTier = newTier;
        }
    }

    private final MutableLiveData<Boolean> _loadingState = new MutableLiveData<>(false);
    private final MutableLiveData<String> _errorState = new MutableLiveData<>(null);
    private final MutableLiveData<List<Voucher>> _userVouchersState = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<BookingResult> _bookingResultState = new MutableLiveData<>(new BookingResult(BookingStatus.IDLE, null, null, null));
    private final MutableLiveData<List<Booking>> _bookingsState = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Booking> _bookingDetailState = new MutableLiveData<>(null);

    public BookingViewModel() {
        this.bookingRepository = new BookingController();
        this.eventRepository = new EventController();
        this.userRepository = new UserController();
        this.voucherRepository = new VoucherController();
    }

    public LiveData<Boolean> getLoadingState() { return _loadingState; }
    public LiveData<String> getErrorState() { return _errorState; }
    public LiveData<List<Voucher>> getUserVouchersState() { return _userVouchersState; }
    public LiveData<BookingResult> getBookingResultState() { return _bookingResultState; }
    public LiveData<List<Booking>> getBookingsState() { return _bookingsState; }
    public LiveData<Booking> getBookingDetailState() { return _bookingDetailState; }

    public void loadUserVouchers(String userId) {
        _loadingState.setValue(true);
        voucherRepository.getUserVouchers(userId,
                vouchers -> {
                    _userVouchersState.postValue(vouchers);
                    _loadingState.postValue(false);
                },
                error -> {
                    _errorState.postValue(error);
                    _loadingState.postValue(false);
                });
    }

    public void checkAndGiveFirstPurchaseVoucher(String userId) {
        voucherRepository.checkAndGiveFirstPurchaseVoucher(userId, () -> {
            loadUserVouchers(userId);
        });
    }

    public void checkoutBooking(Booking booking, Event event, String userVoucherId) {
        _bookingResultState.setValue(new BookingResult(BookingStatus.LOADING, null, null, null));

        Runnable saveBookingAction = () -> {
            bookingRepository.saveBooking(booking, () -> {
                // Remove voucher if applied
                if (userVoucherId != null && !userVoucherId.isEmpty()) {
                    voucherRepository.removeUserVoucher(booking.getUserId(), userVoucherId, () -> {}, e -> {});
                }

                // Add EXP and complete booking process
                long gainedExp = booking.getQuantity() * (50 + (long) (Math.random() * 151));
                userRepository.addExp(booking.getUserId(), gainedExp,
                        newTier -> _bookingResultState.postValue(new BookingResult(BookingStatus.LEVEL_UP, booking.getBookingId(), null, newTier)),
                        () -> _bookingResultState.postValue(new BookingResult(BookingStatus.SUCCESS, booking.getBookingId(), null, null)),
                        error -> _bookingResultState.postValue(new BookingResult(BookingStatus.SUCCESS, booking.getBookingId(), null, null))
                );
            }, error -> {
                _bookingResultState.postValue(new BookingResult(BookingStatus.ERROR, null, error, null));
            });
        };

        // Deduct ticket count first (tier-specific or total)
        if (booking.getTierId() != null && !booking.getTierId().isEmpty() && event.hasTiers()) {
            eventRepository.updateTierRemainingTicket(event.getEventId(), booking.getTierId(), booking.getQuantity(), () -> {
                eventRepository.updateRemainingTicket(event.getEventId(), booking.getQuantity(), saveBookingAction, error -> {
                    _bookingResultState.postValue(new BookingResult(BookingStatus.ERROR, null, error, null));
                });
            }, error -> {
                _bookingResultState.postValue(new BookingResult(BookingStatus.ERROR, null, error, null));
            });
        } else {
            eventRepository.updateRemainingTicket(event.getEventId(), booking.getQuantity(), saveBookingAction, error -> {
                _bookingResultState.postValue(new BookingResult(BookingStatus.ERROR, null, error, null));
            });
        }
    }

    public void loadUserBookings(String userId) {
        _loadingState.setValue(true);
        bookingRepository.getBookingsByUser(userId,
                bookings -> {
                    _bookingsState.postValue(bookings);
                    _loadingState.postValue(false);
                },
                error -> {
                    _errorState.postValue(error);
                    _loadingState.postValue(false);
                });
    }

    public void loadBookingDetail(String bookingId) {
        _loadingState.setValue(true);
        bookingRepository.getBookingById(bookingId,
                booking -> {
                    _bookingDetailState.postValue(booking);
                    _loadingState.postValue(false);
                },
                error -> {
                    _errorState.postValue(error);
                    _loadingState.postValue(false);
                });
    }
}
