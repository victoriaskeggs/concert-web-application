package nz.ac.auckland.concert.service.domain.jpa;

import nz.ac.auckland.concert.common.types.PriceBand;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a booking
 */
@Entity
public class Booking {

    @Id
    @GeneratedValue
    private Long _id;

    @ManyToOne
    private Concert _concert;

    @Column(nullable = false)
    private LocalDateTime _dateTime;

    @ManyToMany
    private Set<Seat> _seats = new HashSet<>();

    @Column(nullable = false)
    private PriceBand _priceBand;

    public Booking(Concert concert, LocalDateTime _dateTime, Set<Seat> _seats, PriceBand _priceBand) {
        this._concert = concert;
        this._dateTime = _dateTime;
        this._seats = _seats;
        this._priceBand = _priceBand;
    }

    public Booking() {

    }

    public Long getId() {
        return _id;
    }

    public Concert getConcert() {
        return _concert;
    }

    public LocalDateTime getDateTime() {
        return _dateTime;
    }

    public Set<Seat> getSeats() {
        return _seats;
    }

    public PriceBand getPriceBand() {
        return _priceBand;
    }

    public void setId(Long _concertId) {
        this._id = _concertId;
    }

    public void setConcertTitle(Concert concert) {
        this._concert = concert;
    }

    public void setDateTime(LocalDateTime _dateTime) {
        this._dateTime = _dateTime;
    }

    public void setPriceBand(PriceBand _priceBand) {
        this._priceBand = _priceBand;
    }
}
