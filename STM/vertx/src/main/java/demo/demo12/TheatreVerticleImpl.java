package demo.demo12;

import com.arjuna.ats.arjuna.AtomicAction;
import demo.domain.Booking;
import demo.domain.TheatreService;
import io.vertx.core.AbstractVerticle;

/**
 * The base class encapsulates the domain and STM specific logic
 */
class TheatreVerticleImpl extends AbstractVerticle {
    // STM manipulation
    int getBookings(Booking service) throws Exception {
        AtomicAction A = new AtomicAction();
        int bookings;

        A.begin();
        try {
            bookings = service.getBookings();
            A.commit();
        } catch (Exception e) {
            A.abort();
            throw e;
        }

        return bookings;
    }

    int makeBooking(TheatreService service) throws Exception {
        AtomicAction A = new AtomicAction();
        int bookings;

        A.begin();
        try {
            service.bookShow();
            bookings = service.getBookings();
            A.commit();
        } catch (Exception e) {
            A.abort();
            throw e;
        }

        return bookings;
    }

    // workaround for JBTM-1732
    static void initSTMMemory(Booking service) {
        AtomicAction A = new AtomicAction();

        A.begin();
        service.init();
        A.commit();
    }

}
