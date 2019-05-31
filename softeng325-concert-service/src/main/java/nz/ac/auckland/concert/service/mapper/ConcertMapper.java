package nz.ac.auckland.concert.service.mapper;

import nz.ac.auckland.concert.common.dto.ConcertDTO;
import nz.ac.auckland.concert.service.domain.jpa.Concert;
import nz.ac.auckland.concert.service.domain.jpa.Performer;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * This class converts between Concert objects used in the domain model and ConcertDTO objects send between the server
 * and clients.
 */
public class ConcertMapper {

    /**
     * Converts a Concert object to an equivalent ConcertDTO
     * @param concert to convert
     * @return concert as a ConcertDAO
     */
    public static ConcertDTO toDTO(Concert concert) {

        // Find IDs of performers performing at the concert
        Set<Long> performerIds = new HashSet<>();
        for (Performer performer: concert.getPerformers()) {
            performerIds.add(performer.getId());
        }

        return new ConcertDTO(
                concert.getId(),
                concert.getTitle(),
                concert.getDates(),
                concert.getTicketPrices(),
                performerIds);
    }

    /**
     * Converts a collection of Concert objects to ConcertDTOs
     * @param concerts to convert
     * @return concerts as ConcertDAO objects
     */
    public static Set<ConcertDTO> toDTO(Collection<Concert> concerts) {
        Set<ConcertDTO> concertDTOS = new HashSet<>();
        for (Concert concert: concerts) {
            concertDTOS.add(toDTO(concert));
        }
        return concertDTOS;
    }
}