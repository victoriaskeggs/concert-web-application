package nz.ac.auckland.concert.service.domain.jpa;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import javax.ws.rs.core.NewCookie;

import static nz.ac.auckland.concert.utility.Config.CLIENT_COOKIE;

/**
 * AttributeConverter class to convert SeatNumber objects to Integers, which
 * can be readily mapped to a relational schema using JPA.
 *
 */
@Converter
public class CookieConverter implements
        AttributeConverter<NewCookie, String> {

    @Override
    public String convertToDatabaseColumn(NewCookie cookie) {
        return (cookie == null ? null : cookie.getValue());
    }

    @Override
    public NewCookie convertToEntityAttribute(String value) {
        return (value == null ? null : new NewCookie(CLIENT_COOKIE, value));
    }
}

