package demo.domain;

import org.jboss.stm.annotations.ReadLock;
import org.jboss.stm.annotations.State;
import org.jboss.stm.annotations.WriteLock;

public class TheatreServiceImpl implements TheatreService {
    @State
    private int noOfBookings = 0;

    public TheatreServiceImpl() {
    }

    @Override
    @WriteLock
    public void init() {
    }

    @Override
    @WriteLock
    public void bookShow() {
        noOfBookings += 1;
    }

    @Override
    @ReadLock
    public int getBookings() {
        return noOfBookings;
    }
}