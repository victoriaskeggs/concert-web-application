package nz.ac.auckland.concert.service.mapper;

import nz.ac.auckland.concert.common.dto.SeatDTO;
import nz.ac.auckland.concert.service.domain.jpa.Seat;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * This class converts between Seat objects used in the domain model and SeatDTO objects send between the server
 * and clients.
 */
public class SeatMapper {

    /**
     * Converts a Seat object to an equivalent SeatDTO
     * @param seat to convert
     * @return seat as a SeatDAO
     */
    public static SeatDTO toDTO(Seat seat) {

        return new SeatDTO(
                seat.getRow(),
                seat.getNumber());
    }

    /**
     * Converts a collection of Seat objects to SeatDTOs
     * @param seats to convert
     * @return seats as SeatDTO objects
     */
    public static Set<SeatDTO> toDTO(Collection<Seat> seats) {
        Set<SeatDTO> seatDTOS = new HashSet<>();
        for (Seat seat: seats) {
            seatDTOS.add(toDTO(seat));
        }
        return seatDTOS;
    }

    /**
     * Converts a SeatDTO object to its equivalent domain model class, Seat
     * @param seatDTO to convert
     * @return converted Seat object
     */
    public static Seat toDomain(SeatDTO seatDTO) {
        return new Seat(
                seatDTO.getRow(),
                seatDTO.getNumber());
    }

    /**
     * Converts a collection of SeatDTOs to Seats
     * @param seatDTOs to convert
     * @return seats as Seat objects
     */
    public static Set<Seat> toDomain(Collection<SeatDTO> seatDTOs) {
        Set<Seat> domainSeats = new HashSet<>();
        for (SeatDTO seatDTO: seatDTOs) {
            domainSeats.add(toDomain(seatDTO));
        }
        return domainSeats;
    }
}
