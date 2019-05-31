package nz.ac.auckland.concert.service.mapper;

import nz.ac.auckland.concert.common.dto.ReservationRequestDTO;
import nz.ac.auckland.concert.service.domain.jpa.Concert;
import nz.ac.auckland.concert.service.domain.jpa.ReservationRequest;

/**
 * This class converts between ReservationRequest objects used in the domain model and ReservationRequestDTO objects send between the server
 * and clients.
 */
public class ReservationRequestMapper {

    /**
     * Converts a ReservationRequestDTO object to its equivalent domain model class, ReservationRequest
     * @param reservationRequest to convert
     * @return converted ReservationRequest object
     */
    public static ReservationRequest toDomain(ReservationRequestDTO reservationRequest, Concert concert) {
        return new ReservationRequest(reservationRequest.getNumberOfSeats(),
                reservationRequest.getSeatType(),
                concert,
                reservationRequest.getDate());
    }
}
