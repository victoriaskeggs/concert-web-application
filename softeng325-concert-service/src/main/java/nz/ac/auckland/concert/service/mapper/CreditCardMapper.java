package nz.ac.auckland.concert.service.mapper;

import nz.ac.auckland.concert.common.dto.CreditCardDTO;
import nz.ac.auckland.concert.service.domain.jpa.CreditCard;

/**
 * This class converts between CreditCard objects used in the domain model and CreditCardDTO objects send between the server
 * and clients.
 */
public class CreditCardMapper {

    /**
     * Converts a CreditCard object to an equivalent CreditCardDTO
     * @param creditCard to convert
     * @return creditCard as a CreditCardDAO
     */
    public static CreditCardDTO toDTO(CreditCard creditCard) {

        return new CreditCardDTO(
                creditCard.getType(),
                creditCard.getName(),
                creditCard.getNumber(),
                creditCard.getExpiryDate());
    }

    /**
     * Converts a CreditCardDTO object to its equivalent domain model class, CreditCard
     * @param creditCardDTO to convert
     * @return converted CreditCard object
     */
    public static CreditCard toDomain(CreditCardDTO creditCardDTO) {
        return new CreditCard(
                creditCardDTO.getType(),
                creditCardDTO.getName(),
                creditCardDTO.getNumber(),
                creditCardDTO.getExpiryDate());
    }
}
