package nz.ac.auckland.concert.utility;

public class ServiceURI {

    public static final String CONCERTS = "/concerts";

    public static final String PERFORMERS = "/performers";

    public static final String PERFORMER_IMAGE = "/performers/{id}/image";

    public static final String USERS = "/users";

    public static final String AUTHENTICATE_USER = "/users/{username}/authentication";

    public static final String RESERVATIONS = "/users/{username}/reservations";

    public static final String BOOKINGS = "/users/{username}/bookings";

    public static final String BILLING = "/users/{username}/billing";

    public static final String authenticateUser(String username) {
        return AUTHENTICATE_USER.replace("{username}", username);
    }

    public static final String performerImage(Long performerId) {
        return PERFORMER_IMAGE.replace("{id}", "" + performerId);
    }

    public static final String reservations(String username) {
        return RESERVATIONS.replace("{username}", "" + username);
    }

    public static final String bookings(String username) {
        return BOOKINGS.replace("{username}", "" + username);
    }

    public static final String billing(String username) {
        return BILLING.replace("{username}", "" + username);
    }
}
