package demo.domain;

import org.jboss.stm.annotations.ReadLock;
import org.jboss.stm.annotations.State;
import org.jboss.stm.annotations.WriteLock;

public class TaxiServiceImpl implements TaxiService {
    @State
    private int noOfBookings = 0;

    public TaxiServiceImpl() {
    }

    @Override
    @WriteLock
    public void failToBook() throws Exception {
        throw new Exception();
    }

    @Override
    @WriteLock
    public void init() {
    }

    @Override
    @WriteLock
    public void bookTaxi() {
        noOfBookings += 1;
    }

    @Override
    @ReadLock
    public int getBookings() {
        return noOfBookings;
    }
}
