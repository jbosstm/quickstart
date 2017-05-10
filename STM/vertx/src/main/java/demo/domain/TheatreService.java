package demo.domain;

import org.jboss.stm.annotations.Transactional;

@Transactional
public interface TheatreService extends Booking {
    void bookShow();
}
