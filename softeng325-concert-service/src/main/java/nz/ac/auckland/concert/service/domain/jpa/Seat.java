package nz.ac.auckland.concert.service.domain.jpa;

import nz.ac.auckland.concert.common.types.SeatNumber;
import nz.ac.auckland.concert.common.types.SeatRow;

import javax.persistence.*;

@Entity
public class Seat {

    @Id
    @GeneratedValue
    private Long _id;

    @Enumerated
    @Column(nullable = false)
    private SeatRow _row;

    @Convert(converter = SeatNumberConverter.class)
    @Column(nullable = false)
    private SeatNumber _number;

    public Seat(SeatRow _row, SeatNumber _number) {
        this._row = _row;
        this._number = _number;
    }

    public Seat() {}

    public SeatRow getRow() {
        return _row;
    }

    public void setRow(SeatRow row) {
        this._row = row;
    }

    public SeatNumber getNumber() {
        return _number;
    }

    public void setNumber(SeatNumber number) {
        this._number = number;
    }
}
