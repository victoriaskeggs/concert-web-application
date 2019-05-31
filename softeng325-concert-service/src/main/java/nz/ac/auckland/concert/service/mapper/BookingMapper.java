package nz.ac.auckland.concert.service.mapper;

import nz.ac.auckland.concert.common.dto.BookingDTO;
import nz.ac.auckland.concert.service.domain.jpa.Booking;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * This class converts between Booking objects used in the domain model and BookingDTO objects send between the server
 * and clients.
 */
public class BookingMapper {

    /**
     * Converts a Booking object to an equivalent BookingDTO
     *
     * @param booking to convert
     * @return booking as a BookingDAO
     */
    public static BookingDTO toDTO(Booking booking) {

        return new BookingDTO(
                booking.getConcert().getId(),
                booking.getConcert().getTitle(),
                booking.getDateTime(),
                SeatMapper.toDTO(booking.getSeats()),
                booking.getPriceBand());
    }

    /**
     * Converts a collection of Booking objects to BookingDTOs
     *
     * @param bookings to convert
     * @return bookings as BookingDAO objects
     */
    public static Set<BookingDTO> toDTO(Collection<Booking> bookings) {
        Set<BookingDTO> bookingDTOS = new HashSet<>();
        for (Booking booking : bookings) {
            bookingDTOS.add(toDTO(booking));
        }
        return bookingDTOS;
    }
}
