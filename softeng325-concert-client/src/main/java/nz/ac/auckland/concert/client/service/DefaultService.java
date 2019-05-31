package nz.ac.auckland.concert.client.service;

import nz.ac.auckland.concert.common.dto.*;
import nz.ac.auckland.concert.common.message.Messages;
import nz.ac.auckland.concert.utility.Config;
import nz.ac.auckland.concert.utility.ServiceURI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.*;
import java.awt.*;
import java.util.Map;
import java.util.Set;

/**
 * Clients use this class to communicate with the server.
 */
public class DefaultService implements ConcertService {

    private static String WEB_SERVICE_URI = "http://localhost:10000/services/concertService";

    private Logger _logger = LoggerFactory
            .getLogger(DefaultService.class);

    private Client _client;

    // The most recently authenticated user stays logged in
    private Cookie _cookie;
    private String _username;

    public DefaultService() {

        // Use ClientBuilder to create a new client that can be used to create
        // connections to the Web service.
        _client = ClientBuilder.newClient();
    }

    public void closeConnection() {
        _client.close();
    }

    /**
     * Returns a Set of ConcertDTO objects, where each ConcertDTO instance
     * describes a concert.
     *
     * @throws ServiceException if there's an error communicating with the
     * service. The exception's message is Messages.SERVICE_COMMUNICATION_ERROR.
     *
     */
    @Override
    public Set<ConcertDTO> getConcerts() throws ServiceException {
        return ((Set<ConcertDTO>)get(ServiceURI.CONCERTS, new GenericType<Set<ConcertDTO>>() {
                }));
    }

    /**
     * Returns a Set of PerformerDTO objects. Each member of the Set describes
     * a Performer.
     *
     * @throws ServiceException if there's an error communicating with the
     * service. The exception's message is Messages.SERVICE_COMMUNICATION_ERROR.
     *
     */
    @Override
    public Set<PerformerDTO> getPerformers() throws ServiceException {
        return ((Set<PerformerDTO>)get(ServiceURI.PERFORMERS, new GenericType<Set<PerformerDTO>>() {
        }));
    }

    /**
     * Attempts to create a new user. When successful, the new user is
     * automatically authenticated and logged into the remote service.
     *
     * @param newUser a description of the new user. The following
     * properties are expected to be set: username, password, firstname
     * and lastname.
     *
     * @return a new UserDTO object, whose identity property is also set.
     *
     * @throws ServiceException in response to any of the following conditions.
     * The exception's message is defined in
     * class nz.ac.auckland.concert.common.Messages.
     *
     * Condition: the expected UserDTO attributes are not set.
     * Messages.CREATE_USER_WITH_MISSING_FIELD
     *
     * Condition: the supplied username is already taken.
     * Messages.CREATE_USER_WITH_NON_UNIQUE_NAME
     *
     * Condition: there is a communication error.
     * Messages.SERVICE_COMMUNICATION_ERROR
     *
     */
    @Override
    public UserDTO createUser(UserDTO newUser) throws ServiceException {

        Response response = null;

        try {
            Invocation.Builder builder = buildRequest(ServiceURI.USERS);

            response = builder.post(Entity.entity(newUser, MediaType.APPLICATION_XML));

            // Get the response code from the Response object.
            int responseCode = response.getStatus();

            // Process the response
            switch (responseCode) {
                case 200: // TODO: change to 201
                    // Created
                    UserDTO user = response.readEntity(UserDTO.class);
                    _username = user.getUsername();
                    processCookieFromResponse(response);
                    return user;

                case 400:
                    // Bad request
                    throw new ServiceException(response.readEntity(String.class));
                default:
                    // There was a problem with the server
                    throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
            }
        } finally {

            // Always close the response
            response.close();
        }
    }

    /**
     * Attempts to authenticate an existing user and log them into the remote
     * service.
     *
     * @param user stores the user's authentication credentials. Properties
     * username and password must be set.
     *
     * @return a UserDTO whose properties are all set.
     *
     * @throws ServiceException in response to any of the following conditions.
     * The exception's message is defined in
     * class nz.ac.auckland.concert.common.Messages.
     *
     * Condition: the UserDTO parameter doesn't have values for username and/or
     * password.
     * Messages.AUTHENTICATE_USER_WITH_MISSING_FIELDS
     *
     * Condition: the remote service doesn't have a record of a user with the
     * specified username.
     * Messages.AUTHENTICATE_NON_EXISTENT_USER
     *
     * Condition: the given user can't be authenticated because their password
     * doesn't match what's stored in the remote service.
     * Messages.AUTHENTICATE_USER_WITH_ILLEGAL_PASSWORD
     *
     * Condition: there is a communication error.
     * Messages.SERVICE_COMMUNICATION_ERROR
     *
     */
    @Override
    public UserDTO authenticateUser(UserDTO user) throws ServiceException {
        return (UserDTO) post(user, ServiceURI.authenticateUser(user.getUsername()), UserDTO.class);
    }

    /**
     * Returns an Image for a given performer.
     *
     * @param performer the performer for whom an Image is required.
     *
     * @return an Image instance.
     *
     * @throws ServiceException in response to any of the following conditions.
     * The exception's message is defined in
     * class nz.ac.auckland.concert.common.Messages.
     *
     * Condition: there is no image for the specified performer.
     * Messages.NO_IMAGE_FOR_PERFORMER
     *
     * Condition: there is a communication error.
     * Messages.SERVICE_COMMUNICATION_ERROR
     *
     */
    @Override
    public Image getImageForPerformer(PerformerDTO performer) throws ServiceException {
        String imageName = (String)get(ServiceURI.performerImage(performer.getId()), String.class);

        // Retrieve the image from AWS
        AWSClient client = new AWSClient();
        return client.retrieveImage(imageName);
    }

    /**
     * Attempts to reserve seats for a concert. The reservation is valid for a
     * short period that is determine by the remote service.
     *
     * @param reservationRequest a description of the reservation, including
     * number of seats, price band, concert identifier, and concert date. All
     * fields are expected to be filled.
     *
     * @return a ReservationDTO object that describes the reservation. This
     * includes the original ReservationDTO parameter plus the seats (a Set of
     * SeatDTO objects) that have been reserved.
     *
     * @throws ServiceException in response to any of the following conditions.
     * The exception's message is defined in
     * class nz.ac.auckland.concert.common.Messages.
     *
     * Condition: the request is made by an unauthenticated user.
     * Messages.UNAUTHENTICATED_REQUEST
     *
     * Condition: the request includes an authentication token but it's not
     * recognised by the remote service.
     * Messages.BAD_AUTHENTICATON_TOKEN
     *
     * Condition: the ReservationRequestDTO parameter is incomplete.
     * Messages.RESERVATION_REQUEST_WITH_MISSING_FIELDS
     *
     * Condition: the ReservationRequestDTO parameter specifies a reservation
     * date/time for when the concert is not scheduled.
     * Messages.CONCERT_NOT_SCHEDULED_ON_RESERVATION_DATE
     *
     * Condition: the reservation request is unsuccessful because the number of
     * seats within the required price band are unavailable.
     * Messages.INSUFFICIENT_SEATS_AVAILABLE_FOR_RESERVATION
     *
     * Condition: there is a communication error.
     * Messages.SERVICE_COMMUNICATION_ERROR
     *
     */
    @Override
    public ReservationDTO reserveSeats(ReservationRequestDTO reservationRequest) throws ServiceException {
        return (ReservationDTO) post(reservationRequest, ServiceURI.reservations(_username), ReservationDTO.class);
    }

    /**
     * Confirms a reservation. Prior to calling this method, a successful
     * reservation request should have been made via a call to reserveSeats(),
     * returning a ReservationDTO.
     *
     * @param reservation a description of the reservation to confirm.
     *
     * @throws ServiceException in response to any of the following conditions.
     * The exception's message is defined in
     * class nz.ac.auckland.concert.common.Messages.
     *
     * Condition: the request is made by an unauthenticated user.
     * Messages.UNAUTHENTICATED_REQUEST
     *
     * Condition: the request includes an authentication token but it's not
     * recognised by the remote service.
     * Messages.BAD_AUTHENTICATON_TOKEN
     *
     * Condition: the reservation has expired.
     * Messages.EXPIRED_RESERVATION
     *
     * Condition: the user associated with the request doesn't have a credit
     * card registered with the remote service.
     * Messages.CREDIT_CARD_NOT_REGISTERED
     *
     * Condition: there is a communication error.
     * Messages.SERVICE_COMMUNICATION_ERROR
     *
     */
    @Override
    public void confirmReservation(ReservationDTO reservation) throws ServiceException {
        post(reservation, ServiceURI.bookings(_username), ReservationDTO.class);
    }

    /**
     * Registers a credit card for the currently logged in user.
     *
     * @param creditCard a description of the credit card.
     *
     * @throws ServiceException in response to any of the following conditions.
     * The exception's message is defined in
     * class nz.ac.auckland.concert.common.Messages.
     *
     * Condition: the request is made by an unauthenticated user.
     * Messages.UNAUTHENTICATED_REQUEST
     *
     * Condition: the request includes an authentication token but it's not
     * recognised by the remote service.
     * Messages.BAD_AUTHENTICATON_TOKEN
     *
     * Condition: there is a communication error.
     * Messages.SERVICE_COMMUNICATION_ERROR
     *
     */
    @Override
    public void registerCreditCard(CreditCardDTO creditCard) throws ServiceException {
        post(creditCard, ServiceURI.billing(_username), CreditCardDTO.class);
    }

    @Override
    public Set<BookingDTO> getBookings() throws ServiceException {
        Object bookings = get(ServiceURI.bookings(_username), new GenericType<Set<BookingDTO>>() {
        });
        if (bookings == null) {
            return null;
        }
        return (Set<BookingDTO>) bookings;
    }



    private Object post(Object toPost, String uriExtension, Class responseEntityType){

        // TODO: return null or URI string? Make another POST method?

        Response response = null;

        try {
            Invocation.Builder builder = buildRequest(uriExtension);
            addCookieToInvocation(builder);

            response = builder.post(Entity.entity(toPost, MediaType.APPLICATION_XML));

            // Process the response
            return processResponse(response, responseEntityType);

        } finally {

            // Always close the response
            response.close();
        }
    }

    private Object get(String uriExtension, Class responseEntityType){

        // TODO: return null or URI string? Make another POST method?

        Response response = null;

        try {
            Invocation.Builder builder = buildRequest(uriExtension);
            addCookieToInvocation(builder);

            response = builder.get();

            // Process the response
            return processResponse(response, responseEntityType);

        } finally {

            // Always close the response
            response.close();
        }
    }

    private Object get(String uriExtension, GenericType responseEntityType){

        // TODO: return null or URI string? Make another POST method?

        Response response = null;

        try {
            Invocation.Builder builder = buildRequest(uriExtension);
            addCookieToInvocation(builder);

            response = builder.get();

            // Process the response
            return processResponse(response, responseEntityType);

        } finally {

            // Always close the response
            response.close();
        }
    }

    private Invocation.Builder buildRequest(String uriExtension) {
        return _client
                .target(WEB_SERVICE_URI + uriExtension)
                .request()
                .accept(MediaType.APPLICATION_XML);
    }

    private Object processResponse(Response response, Class responseEntityType) {

        // Get the response code from the Response object.
        int responseCode = response.getStatus();

        switch (responseCode) {
            case 200:
                // OK
                return response.readEntity(responseEntityType);
            case 201:
                // Created TODO: logging, return get()
                _logger.info("Created new resource at: " + response.getLocation().getPath());
                return null;
            case 204:
                // No content
                return null;
            case 400: // Bad request
            case 401: // Not authenticated
            case 404: // Not found
                throw new ServiceException(response.readEntity(String.class));
            default:
                // There was a problem with the server
                throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
        }
    }

    private Object processResponse(Response response, GenericType responseEntityType) {

        // Get the response code from the Response object.
        int responseCode = response.getStatus();

        switch (responseCode) {
            case 200:
                // OK
                return response.readEntity(responseEntityType);
            case 201:
                // Created
                _logger.info("Created new resource at: " + response.getLocation().getPath());
                return null;
            case 204:
                // No content
                return null;
            case 400: // Bad request
            case 401: // Not authenticated
            case 404: // Not found
                throw new ServiceException(response.readEntity(String.class));
            default:
                // There was a problem with the server
                throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
        }
    }

    private void addCookieToInvocation(Invocation.Builder builder) {
        if (_cookie != null) {
            builder.cookie(_cookie);
        }
    }

    /**
     * Method to extract any cookie from a Response object received from the
     * Web service. If there is a cookie named clientId (Config.CLIENT_COOKIE)
     * the client stores the cookie.
     */
    private void processCookieFromResponse(Response response) {
        Map<String, NewCookie> cookies = response.getCookies();

        if(cookies.containsKey(Config.CLIENT_COOKIE)) {
            _cookie = cookies.get(Config.CLIENT_COOKIE);
        }
    }


}
