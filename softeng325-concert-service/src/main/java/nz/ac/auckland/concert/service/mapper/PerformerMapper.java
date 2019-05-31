package nz.ac.auckland.concert.service.mapper;

import nz.ac.auckland.concert.common.dto.ConcertDTO;
import nz.ac.auckland.concert.common.dto.PerformerDTO;
import nz.ac.auckland.concert.service.domain.jpa.Concert;
import nz.ac.auckland.concert.service.domain.jpa.Performer;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * This class converts between Performer objects used in the domain model and PerformerDTO objects send between the
 * server and clients.
 */
public class PerformerMapper {

    /**
     * Converts a Performer object to an equivalent PerformerDTO
     * @param performer to convert
     * @return performer as a PerformerDAO
     */
    public static PerformerDTO toDTO(Performer performer) {

        // Find IDs of concerts the performer has performed at
        Set<Long> concertIds = new HashSet<>();
        for (Concert concert: performer.getConcerts()) {
            concertIds.add(concert.getId());
        }

        return new PerformerDTO(
                performer.getId(),
                performer.getName(),
                performer.getImageName(),
                performer.getGenre(),
                concertIds);
    }

    /**
     * Converts a collection of Performer objects to PerformerDTOs
     * @param performers to convert
     * @return performers as DAOs
     */
    public static Set<PerformerDTO> toDTO(Collection<Performer> performers) {
        Set<PerformerDTO> performerDTOs = new HashSet<>();
        for (Performer performerDTO: performers) {
            performerDTOs.add(toDTO(performerDTO));
        }
        return performerDTOs;
    }
}

