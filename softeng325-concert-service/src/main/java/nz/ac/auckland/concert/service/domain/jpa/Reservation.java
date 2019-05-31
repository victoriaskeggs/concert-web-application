package nz.ac.auckland.concert.service.domain.jpa;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * Represents a reservation
 */
@Entity
public class Reservation {

    @Id
    @GeneratedValue
    private Long _id;

    @Embedded
    private ReservationRequest _request;

    @ManyToMany
    private Set<Seat> _seats;

    private LocalDateTime _expiryDate;

    @Version
    private Long _version;

    public Reservation(ReservationRequest request, Set<Seat> seats, LocalDateTime expiryDate) {
        this._request = request;
        this._seats = seats;
        _expiryDate = expiryDate;
    }

    public Reservation() {}

    public Long getId() {
        return _id;
    }

    public ReservationRequest getRequest() {
        return _request;
    }

    public Set<Seat> getSeats() {
        return _seats;
    }

    public void setId(Long id) {
        this._id = id;
    }

    public void setRequest(ReservationRequest request) {
        this._request = request;
    }

    public LocalDateTime getExpiryDate() {
        return _expiryDate;
    }

    public void setExpiryDate(LocalDateTime _expiryDate) {
        this._expiryDate = _expiryDate;
    }

    public Long getVersion() {
        return _version;
    }

    public void setVersion(Long _version) {
        this._version = _version;
    }
}
