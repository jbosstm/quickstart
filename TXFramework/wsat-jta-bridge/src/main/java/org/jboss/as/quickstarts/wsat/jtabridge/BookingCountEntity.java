package org.jboss.as.quickstarts.wsat.jtabridge;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * @author paul.robinson@redhat.com, 2012-05-03
 */
@Entity
public class BookingCountEntity implements Serializable {
    
    private int id;
    private int bookingCount;

    public BookingCountEntity() {
    }

    @Id
    @GeneratedValue
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBookingCount() {
        return bookingCount;
    }

    public void setBookingCount(int counter) {
        this.bookingCount = counter;
    }

    public void addBookings(int howMany) {
        setBookingCount(getBookingCount() + howMany);
    }
}