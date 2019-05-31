package nz.ac.auckland.concert.service.domain.jpa;

import nz.ac.auckland.concert.common.types.PriceBand;

import javax.persistence.*;
import java.time.LocalDateTime;

@Embeddable
public class ReservationRequest {

    @Column(nullable = false)
    private int _numberOfSeats;

    @Enumerated
    @Column(nullable = false)
    private PriceBand _seatType;

    @ManyToOne(
            optional = false)
    @JoinColumn(name = "CONCERT_ID",
                nullable = false)
    private Concert _concert;

    @Column(nullable = false)
    private LocalDateTime _date;

    public ReservationRequest(int _numberOfSeats, PriceBand _seatType, Concert _concert, LocalDateTime _date) {
        this._numberOfSeats = _numberOfSeats;
        this._seatType = _seatType;
        this._concert = _concert;
        this._date = _date;
    }

    public ReservationRequest() {}

    public int getNumberOfSeats() {
        return _numberOfSeats;
    }

    public void setNumberOfSeats(int numberOfSeats) {
        this._numberOfSeats = numberOfSeats;
    }

    public PriceBand getSeatType() {
        return _seatType;
    }

    public void setSeatType(PriceBand seatType) {
        this._seatType = seatType;
    }

    public Concert getConcert() {
        return _concert;
    }

    public void setConcert(Concert concert) {
        this._concert = concert;
    }

    public LocalDateTime getDate() {
        return _date;
    }

    public void setDate(LocalDateTime date) {
        this._date = date;
    }
}
