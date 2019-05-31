package nz.ac.auckland.concert.service.services;

import nz.ac.auckland.concert.common.jaxb.LocalDateAdapter;
import nz.ac.auckland.concert.common.jaxb.LocalDateTimeAdapter;
import nz.ac.auckland.concert.service.domain.jpa.Booking;
import nz.ac.auckland.concert.service.domain.jpa.Concert;
import nz.ac.auckland.concert.service.domain.jpa.Reservation;
import nz.ac.auckland.concert.service.domain.jpa.User;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ApplicationPath("/services")
public class ConcertApplication extends Application {
    private Set<Object> _singletons = new HashSet<>();
    private Set<Class<?>> _classes = new HashSet<>();

    public ConcertApplication() {
        EntityManager em = null;

        try {
            em = PersistenceManager.instance().createEntityManager();
            em.getTransaction().begin();

            // Delete all client resources
            deleteAllBookings(em);
            deleteAllReservations(em);
            deleteAllUsers(em);

            // Periodically flush and clear the persistence context .
            em.flush();
            em.clear();

            // Add the seats

            em.getTransaction().commit();

        }finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }

        _classes.add(ConcertResource.class);
        _classes.add(LocalDateAdapter.class);
        _classes.add(LocalDateTimeAdapter.class);
        _singletons.add(PersistenceManager.instance());
    }

    @Override
    public Set<Object> getSingletons()
    {
        return _singletons;
    }

    @Override
    public Set<Class<?>> getClasses()
    {
        return _classes;
    }

    private void deleteAllBookings(EntityManager em) {
        TypedQuery<Booking> query = em.createQuery("SELECT b FROM Booking b", Booking.class);
        List<Booking> bookings = query.getResultList();

        for (Booking booking: bookings) {
            em.remove(booking);
        }
    }

    private void deleteAllReservations(EntityManager em) {
        TypedQuery<Reservation> query2 = em.createQuery("SELECT r FROM Reservation r", Reservation.class);
        List<Reservation> reservations = query2.getResultList();

        for (Reservation reservation: reservations) {
            em.remove(reservation);
        }
    }

    private void deleteAllUsers(EntityManager em) {
        TypedQuery<User> query2 = em.createQuery("SELECT u FROM User u", User.class);
        List<User> users = query2.getResultList();

        for (User user: users) {
            em.remove(user);
        }
    }
}
