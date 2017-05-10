package demo.demo3;

import com.arjuna.ats.arjuna.AtomicAction;
import demo.domain.Booking;
import demo.domain.ServiceResult;
import demo.domain.TaxiService;
import demo.domain.TheatreService;
import io.vertx.core.AbstractVerticle;

public class TripVerticleImpl extends AbstractVerticle {

    // STM manipulation
    ServiceResult bookTrip(String serviceName, TheatreService theatreService, TaxiService taxiService, TaxiService altTaxiService) throws Exception {
        int theatreBookings;
        int taxiBookings;
        int altTaxiBookings;

        AtomicAction A = new AtomicAction();

        A.begin();
        try {
            theatreService.bookShow();
            try {
                taxiService.failToBook();
            } catch (Exception e) {
                altTaxiService.bookTaxi();
            }

            theatreBookings = theatreService.getBookings();
            taxiBookings = taxiService.getBookings();
            altTaxiBookings = altTaxiService.getBookings();
            A.commit();

            String res = String.format("%d bookings with alt taxi service", altTaxiBookings);

            return new ServiceResult(serviceName, Thread.currentThread().getName(), res, theatreBookings, taxiBookings);
        } catch (Exception e) {
            A.abort();
            throw e;
        }
    }

    int getBookings(Booking serviceClone) throws Exception {
        AtomicAction A = new AtomicAction();
        int bookings;

        A.begin();

        try {
            bookings = serviceClone.getBookings();
            A.commit();
        } catch (Exception e) {
            A.abort();
            throw e;
        }

        return bookings;
    }

    int bookShow(TheatreService service) throws Exception {
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

    int bookTaxi(TaxiService service) throws Exception {
        AtomicAction A = new AtomicAction();
        int bookings;

        A.begin();
        try {
            service.bookTaxi();
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
