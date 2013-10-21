package org.jboss.narayana.quickstarts.compensationsApi.hotel;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Date;

/**
 * @author paul.robinson@redhat.com, 2012-05-03
 *         <p/>
 *         JPA entiry to represent the reservation.
 */
@Entity
public class HotelBooking implements Serializable {

    private Integer id;
    private String name;
    private Date date;
    private BookingStatus status;

    public HotelBooking() {

        this.status = BookingStatus.PENDING;
    }

    @Id
    @GeneratedValue
    public Integer getId() {

        return id;
    }

    public void setId(Integer id) {

        this.id = id;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public BookingStatus getStatus() {

        return status;
    }

    public void setStatus(BookingStatus status) {

        this.status = status;
    }

    public Date getDate() {

        return date;
    }

    public void setDate(Date date) {

        this.date = date;
    }
}