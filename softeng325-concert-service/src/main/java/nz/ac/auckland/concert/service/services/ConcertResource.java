package nz.ac.auckland.concert.service.services;

import nz.ac.auckland.concert.common.dto.*;
import nz.ac.auckland.concert.common.message.Messages;
import nz.ac.auckland.concert.service.domain.jpa.*;
import nz.ac.auckland.concert.service.mapper.*;
import nz.ac.auckland.concert.service.util.TheatreUtility;
import nz.ac.auckland.concert.utility.Config;
import nz.ac.auckland.concert.utility.ServiceURI;
import org.hibernate.service.spi.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.TypedQuery;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static nz.ac.auckland.concert.utility.Config.CLIENT_COOKIE;

/**
 * Class to implement a REST Web service for managing Concerts.
 */
@Produces({javax.ws.rs.core.MediaType.APPLICATION_XML})
@Consumes({javax.ws.rs.core.MediaType.APPLICATION_XML})
@Path("/concertService")
public class ConcertResource {

    private EntityManager _em;
    private Logger _logger = LoggerFactory
            .getLogger(ConcertResource.class);

    // JPQL queries
    private static final String GET_ALL_CONCERTS = "SELECT a FROM CONCERTS a";
    private static final String GET_ALL_PERFORMERS = "SELECT a FROM PERFORMERS a";
    private static final String GET_USER_BY_USERNAME = "SELECT a FROM User a WHERE a._username = ";
    private static final String GET_PERFORMER_IMAGE_NAME_BY_ID = "SELECT a._imageName FROM PERFORMERS a WHERE a._id = ";
    private static final String GET_CONCERT_BY_ID = "SELECT a FROM CONCERTS a WHERE a._id = ";
    private static final String GET_BOOKINGS_BY_CONCERT_ID =
            "SELECT b FROM Booking b WHERE b._concert._id = ";
    private static final String GET_EXPIRY_BY_RESERVATION_ID =
            "SELECT a._expiryDate FROM Reservation a WHERE a._id = ";
    private static final String GET_RESERVATIONS_BY_CONCERT_ID = "SELECT r FROM Reservation r WHERE r._request._concert._id = ";
    private static final String AUTHENTICATION_TOKEN_EXISTS = "SELECT COUNT(u) from User u WHERE u._authenticationToken = ";

    public ConcertResource() {
        _em = PersistenceManager.instance().createEntityManager();
    }

    /**
     * Retrieves all the concerts in the database. Concerts are represented as ConcertDTO objects.
     *
     * @return OK Response containing a Set of ConcertDTOs
     */
    @GET
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    @Path(ServiceURI.CONCERTS)
    public Response getConcerts() throws WebApplicationException {

        // Start the transaction
        _em.getTransaction().begin();

        // Retrieve Concert objects from the database
        TypedQuery<Concert> query = _em.createQuery(GET_ALL_CONCERTS, Concert.class);
        List<Concert> concerts = query.getResultList();

        // Convert to data transfer objects
        Set<ConcertDTO> concertDTOs = ConcertMapper.toDTO(concerts);

        // Package up the response
        GenericEntity<Set<ConcertDTO>> ge = new GenericEntity<Set<ConcertDTO>>(concertDTOs){};

        // Commit the transaction
        _em.getTransaction().commit();

        return Response.ok().entity(ge).build();
    }

    /**
     * Retrieves all the performers in the database. Performers are represented as PerformerDTO objects.
     *
     * @return OK Response containing a Set of PerformerDTOs
     */
    @GET
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    @Path(ServiceURI.PERFORMERS)
    public Response getPerformers() throws ServiceException {

        // Start the transaction
        _em.getTransaction().begin();

        // Retrieve Concert objects from the database
        TypedQuery<Performer> query = _em.createQuery(GET_ALL_PERFORMERS, Performer.class);
        List<Performer> performers = query.getResultList();

        // Convert to data transfer objects
        Set<PerformerDTO> performerDTOs = PerformerMapper.toDTO(performers);

        // Package up the response
        GenericEntity<Set<PerformerDTO>> ge = new GenericEntity<Set<PerformerDTO>>(performerDTOs){};

        // Commit the transaction
        _em.getTransaction().commit();

        return Response.ok().entity(ge).build();
    }

    /**
     * Attempts to create a new user. When successful, the new user is
     * automatically authenticated and logged into the remote service.
     *
     * @param newUser a description of the new user. The following
     * properties are expected to be set: username, password, firstname
     * and lastname.
     *
     * @return OK Response containing a new UserDTO object, whose identity property is also set, and an
     * authentication token
     *
     * @throws BadRequestException if the expected UserDTO attributes are not set.
     * Messages.CREATE_USER_WITH_MISSING_FIELD
     *
     * @throws BadRequestException if the supplied username is already taken.
     * Messages.CREATE_USER_WITH_NON_UNIQUE_NAME
     *
     */
    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    @Path(ServiceURI.USERS)
    public Response createUser(UserDTO newUser) throws ServiceException {

        // Check the expected user attributes have been set
        if (newUser == null || newUser.getFirstname() == null || newUser.getLastname() == null ||
        newUser.getUsername() == null || newUser.getPassword() == null) {
            throw new BadRequestException(Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(Messages.CREATE_USER_WITH_MISSING_FIELDS)
                    .build());
        }

        // Start the transaction
        _em.getTransaction().begin();

        // Check for users with the same username
        if (isUserWithUsername(_em, newUser.getUsername())) {
            throw new BadRequestException(Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(Messages.CREATE_USER_WITH_NON_UNIQUE_NAME)
                    .build());
        }

        // Create an authentication token for the user
        NewCookie authenticationToken = makeCookie();

        // Convert to domain model
        User domainUser = UserMapper.toDomain(newUser, authenticationToken);

        // Persist new user to the database
        _em.persist(domainUser);
        _em.flush();
        _em.clear();

        Response response = Response
                .ok(UserMapper.toDTO(domainUser))
                .cookie(authenticationToken)
                .build();

        // Commit the transaction
        _em.getTransaction().commit();

        return response;
    }

    /**
     * Attempts to authenticate an existing user and log them into the
     * service.
     *
     * @param user stores the user's authentication credentials. Properties
     * username and password must be set.
     *
     * @return OK Response containing an authentication token and a UserDTO whose properties are all set.
     *
     * @throws BadRequestException if the UserDTO parameter doesn't have values for username and/or
     * password.
     * Messages.AUTHENTICATE_USER_WITH_MISSING_FIELDS
     *
     * @throws NotFoundException if the remote service doesn't have a record of a user with the
     * specified username.
     * Messages.AUTHENTICATE_NON_EXISTENT_USER
     *
     * @throws BadRequestException if the given user can't be authenticated because their password
     * doesn't match what's stored in the remote service.
     * Messages.AUTHENTICATE_USER_WITH_ILLEGAL_PASSWORD
     *
     */
    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    @Path(ServiceURI.AUTHENTICATE_USER)
    public Response authenticateUser(UserDTO user) throws ServiceException {

        // Check the user has a username and password
        if (user.getUsername() == null || user.getPassword() == null) {
            throw new BadRequestException(Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(Messages.AUTHENTICATE_USER_WITH_MISSING_FIELDS)
                    .build());
        }

        // Start the transaction
        _em.getTransaction().begin();

        // Check the user exists
        if (!isUserWithUsername(_em, user.getUsername())) {
            throw new NotFoundException(Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(Messages.AUTHENTICATE_NON_EXISTENT_USER)
                    .build());
        }

        // Check that the user's password matches the one stored in the database
        User domainUser = retrieveUserByUsername(_em, user.getUsername());

        if (!domainUser.getPassword().equals(user.getPassword())) {
            throw new BadRequestException(Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(Messages.AUTHENTICATE_USER_WITH_ILLEGAL_PASSWORD)
                    .build());

        }

        // Convert to a data transfer object
        UserDTO userDTO = UserMapper.toDTO(domainUser);

        // Commit the transaction
        _em.getTransaction().commit();

        // Add the authentication token to the response
        return Response.ok(userDTO)
                .cookie(domainUser.getAuthenticationToken())
                .build();
    }

    /**
     * Retrieves a user from the database
     * @param em
     * @param username for the user
     * @return the user
     */
    private User retrieveUserByUsername(EntityManager em, String username) {
        TypedQuery<User> query = em.createQuery(getUserQuery(username), User.class);
        return query.getSingleResult();
    }

    /**
     * Checks if a user with a specified username exists in the database
     * @param em
     * @param username of the user
     * @return true if a user does exist, false otherwise
     */
    private boolean isUserWithUsername(EntityManager em, String username) {
        TypedQuery<User> query = em.createQuery(getUserQuery(username), User.class);
        List<User> usernames = query.getResultList();
        return !usernames.isEmpty();
    }

    /**
     * Creates a query to select a user from the database given the username
     * @param username
     * @return query to select the user
     */
    private String getUserQuery(String username) {
        return GET_USER_BY_USERNAME + "'" + username + "'";
    }

    /**
     * Returns the name of an image for a given performer.
     *
     * @return OK Response containing the image name
     */
    @GET
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    @Path(ServiceURI.PERFORMER_IMAGE)
    public Response getImageForPerformer(@PathParam("id")Long performerId) throws ServiceException {

        // Start the transaction
        _em.getTransaction().begin();

        // Retrieve name of image for performer from the database
        TypedQuery<String> query = _em.createQuery(getPerformerImageNameQuery(performerId), String.class);
        String imageName = query.getSingleResult();

        // Commit the transaction
        _em.getTransaction().commit();

        return Response.ok(imageName)
                .build();
    }

    /**
     * Creates a query to select the name of a performer's image the database given the performer ID
     * @param performerId
     * @return query to select the image name
     */
    private String getPerformerImageNameQuery(Long performerId) {
        return GET_PERFORMER_IMAGE_NAME_BY_ID + "'" + performerId + "'";
    }

    /**
     * Attempts to reserve seats for a concert. The reservation is valid for a
     * short period (5 seconds).
     *
     * @param reservationRequest a description of the reservation, including
     * number of seats, price band, concert identifier, and concert date. All
     * fields are expected to be filled.
     *
     * @return an OK Response containing a ReservationDTO object that describes the reservation. This
     * includes the original ReservationDTO parameter plus the seats (a Set of
     * SeatDTO objects) that have been reserved.
     *
     * @throws NotAuthorizedException if the request is made by an unauthenticated user.
     * Messages.UNAUTHENTICATED_REQUEST
     *
     * @throws NotAuthorizedException if the request includes an authentication token but it's not
     * recognised by the remote service.
     * Messages.BAD_AUTHENTICATON_TOKEN
     *
     * @throws BadRequestException if the ReservationRequestDTO parameter is incomplete.
     * Messages.RESERVATION_REQUEST_WITH_MISSING_FIELDS
     *
     * @throws BadRequestException if the ReservationRequestDTO parameter specifies a reservation
     * date/time for when the concert is not scheduled.
     * Messages.CONCERT_NOT_SCHEDULED_ON_RESERVATION_DATE
     *
     * @throws NotFoundException if the reservation request is unsuccessful because the number of
     * seats within the required price band are unavailable.
     * Messages.INSUFFICIENT_SEATS_AVAILABLE_FOR_RESERVATION
     *
     */
    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    @Path(ServiceURI.RESERVATIONS)
    public Response reserveSeats(ReservationRequestDTO reservationRequest,
                                 @PathParam("username") String username,
                                 @CookieParam(CLIENT_COOKIE) Cookie authenticationToken) throws ServiceException {

        // Check the client has an authentication token
        checkUnauthenticatedRequest(authenticationToken);

        // Check reservation request has all required information
        if (reservationRequest.getConcertId() == null || reservationRequest.getDate() == null ||
        reservationRequest.getSeatType() == null) {
            throw new BadRequestException(Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(Messages.RESERVATION_REQUEST_WITH_MISSING_FIELDS)
                    .build());
        }

        // Start the transaction
        _em.getTransaction().begin();

        // Check the client's authentication token is valid
        checkBadAuthenticatedToken(_em, authenticationToken);

        _em.flush();

        // Retrieve the concert from the database
        Concert concert = retrieveConcertById(_em, reservationRequest.getConcertId());

        _em.flush();

        // Check concert is scheduled on reservation date
        if (!concert.getDates().contains(reservationRequest.getDate())) {
            throw new BadRequestException(Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(Messages.CONCERT_NOT_SCHEDULED_ON_RESERVATION_DATE)
                    .build());
        }

        // Retrieve bookings
        TypedQuery<Booking> query = _em.createQuery(getBookingsByConcertIdQuery(reservationRequest.getConcertId()),
                Booking.class);
        List<Booking> bookings = query.getResultList();

        _em.flush();

        // Retrieve reservations
        TypedQuery<Reservation> query2 = _em.createQuery
                (getReservationsByConcertIdQuery(reservationRequest.getConcertId()), Reservation.class);
        query2.setLockMode(LockModeType.OPTIMISTIC); // do not want other clients editing these reservations
        List<Reservation> reservations = query2.getResultList();

        _em.flush();

        // Free up seats from expired reservations
        for (Reservation reservation: reservations) {
            if (reservation.getExpiryDate().isBefore(LocalDateTime.now())) {
                reservations.remove(reservation);
                _em.remove(reservation);
                _em.flush();
            }
        }

        // Booked or reserved seats are not available
        Set<Seat> takenSeats = new HashSet<>();
        for (Booking booking: bookings) {
            takenSeats.addAll(booking.getSeats());
        }
        for (Reservation reservation: reservations) {
            takenSeats.addAll(reservation.getSeats());
        }

        // Find available seats for the client
        Set<SeatDTO> availableSeats = TheatreUtility.findAvailableSeats(reservationRequest.getNumberOfSeats(),
                reservationRequest.getSeatType(), SeatMapper.toDTO(takenSeats));
        for (Seat seat: SeatMapper.toDomain(availableSeats)) {
            _em.lock(seat, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
        }

        // Check there are enough seats for the client
        if (availableSeats.isEmpty()) {
            throw new NotFoundException(Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(Messages.INSUFFICIENT_SEATS_AVAILABLE_FOR_RESERVATION)
                    .build());
        }

        // Create a reservation to add to the database
        // Reservations last 5 seconds
        Reservation domainReservation = new Reservation(ReservationRequestMapper.toDomain(reservationRequest,
                concert), SeatMapper.toDomain(availableSeats),
                LocalDateTime.now().plusSeconds(Config.RESERVATION_EXPIRY_TIME_IN_SECONDS));

        // Persist the client's reservation to the database
        _em.persist(domainReservation);
        _em.flush();
        _em.clear();

        // Retrieve the reservation ID
        Long id = domainReservation.getId();

        // Create a reservation to send back to the client
        ReservationDTO reservationDTO = new ReservationDTO(id, reservationRequest, availableSeats);

        _em.getTransaction().commit();

        // Package up and send the response
        return Response
                .ok(reservationDTO)
                .build();

    }

    private void checkUnauthenticatedRequest(Cookie authenticationToken) {
        if (authenticationToken == null) {
            throw new NotAuthorizedException(Response
                    .status(Response.Status.UNAUTHORIZED)
                    .entity(Messages.UNAUTHENTICATED_REQUEST)
                    .build());
        }
    }

    private void checkBadAuthenticatedToken(EntityManager em, Cookie authenticationToken) {

        TypedQuery<Long> query = em.createQuery(getAuthenticationTokenExistsQuery(authenticationToken.getValue()),
                Long.class);

        if (query.getResultList().size() == 0) {
            throw new NotAuthorizedException(Response
                    .status(Response.Status.UNAUTHORIZED)
                    .entity(Messages.BAD_AUTHENTICATON_TOKEN)
                    .build());
        }
    }

    private String getAuthenticationTokenExistsQuery(String tokenValue) {
        return AUTHENTICATION_TOKEN_EXISTS + "'" + tokenValue + "'";
    }

    /**
     * Retrieves a concert from the database
     * @param em
     * @param concertId ID of the concert
     * @return the concert
     */
    private Concert retrieveConcertById(EntityManager em, Long concertId) {
        TypedQuery<Concert> query = em.createQuery(getConcertByIdQuery(concertId),
                Concert.class);
        return query.getSingleResult();
    }

    /**
     * Produces a query to retrieve a concert in the database
     * @param id of the concert
     * @return query string
     */
    private String getConcertByIdQuery(Long id) {
        return GET_CONCERT_BY_ID + "'" + id + "'";
    }

    /**
     * Produces a query to retrieve the booked seats of a specified concert
     * @param id of the concert
     * @return query string
     */
    private String getBookingsByConcertIdQuery(Long id) {
        return GET_BOOKINGS_BY_CONCERT_ID + "'" + id + "'";
    }

    private String getReservationsByConcertIdQuery(Long id) {
        return GET_RESERVATIONS_BY_CONCERT_ID + "'" + id + "'";
    }

    /**
     * Confirms a reservation by creating a booking. Prior to calling this method, a successful
     * reservation request should have been made via a call to reserveSeats(),
     * returning a ReservationDTO.
     *
     * @param reservationDTO a description of the reservation to confirm.
     *
     * @return Created Response with the URI of the created booking
     *
     * @throws NotAuthorizedException if the request is made by an unauthenticated user.
     * Messages.UNAUTHENTICATED_REQUEST
     *
     * @throws NotAuthorizedException if the request includes an authentication token but it's not
     * recognised by the remote service.
     * Messages.BAD_AUTHENTICATON_TOKEN
     *
     * @throws BadRequestException if the reservation has expired.
     * Messages.EXPIRED_RESERVATION
     *
     * @throws BadRequestException if the user associated with the request doesn't have a credit
     * card registered with the remote service.
     * Messages.CREDIT_CARD_NOT_REGISTERED
     *
     */
    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    @Path(ServiceURI.BOOKINGS)
    public Response confirmReservation(ReservationDTO reservationDTO, @PathParam("username") String username,
                                       @CookieParam(CLIENT_COOKIE) Cookie authenticationToken)
            throws ServiceException {

        checkUnauthenticatedRequest(authenticationToken);

        // Start the transaction
        _em.getTransaction().begin();

        checkBadAuthenticatedToken(_em, authenticationToken);

        _em.flush();

        // Check the user has a credit card registered
        User user = retrieveUserByUsername(_em, username);
        if (user.getCreditCard() == null) {
            throw new BadRequestException(Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(Messages.CREDIT_CARD_NOT_REGISTERED)
                    .build());
        }

        _em.flush();

        // Get the expiry date
        TypedQuery<LocalDateTime> query = _em.createQuery(getExpiryByReservationIdQuery(reservationDTO.getId()),
                LocalDateTime.class);
        LocalDateTime expiryDate = query.getSingleResult();

        _em.flush();

        // Retrieve the concert
        Concert concert = retrieveConcertById(_em, reservationDTO.getReservationRequest().getConcertId());

        _em.flush();

        // Convert SeatDTOs representing reserved seats into domain model objects
        Set<Seat> seats = SeatMapper.toDomain(reservationDTO.getSeats());
        _em.lock(seats, LockModeType.OPTIMISTIC_FORCE_INCREMENT);

        // Check the reservation has not expired
        if (expiryDate.isBefore(LocalDateTime.now())) {
            throw new BadRequestException(Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(Messages.EXPIRED_RESERVATION)
                    .build());
        }

        // Create a booking for the client
        Booking booking = new Booking(concert,
                reservationDTO.getReservationRequest().getDate(),
                seats,
                reservationDTO.getReservationRequest().getSeatType());

        // Persist the booking to the database
        _em.persist(booking);
        _em.flush();
        _em.clear();

        // Add the booking to the user
        User user2 = retrieveUserByUsername(_em, username);
        user2.getBookings().add(booking);

        // Retrieve the booking ID
        Long id = booking.getId();

        // Commit the transaction
        _em.getTransaction().commit();

        // Package up and send the response
        return Response
                .created(UriBuilder.fromUri("/users/" + username + "/bookings/" + id).build())
                .build();

    }

    private String getExpiryByReservationIdQuery(Long id) {
        return GET_EXPIRY_BY_RESERVATION_ID + "'" + id + "'";
    }

    /**
     * Registers a credit card for a given user.
     *
     * @param creditCard a description of the credit card.
     *
     * @return no content Response
     *
     * @throws NotAuthorizedException if the request is made by an unauthenticated user.
     * Messages.UNAUTHENTICATED_REQUEST
     *
     * @throws NotAuthorizedException if the request includes an authentication token but it's not
     * recognised by the remote service.
     * Messages.BAD_AUTHENTICATON_TOKEN
     *
     */
    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    @Path(ServiceURI.BILLING)
    public Response registerCreditCard(CreditCardDTO creditCard, @PathParam("username") String username,
                                       @CookieParam(CLIENT_COOKIE) Cookie cookie)
            throws ServiceException {

        checkUnauthenticatedRequest(cookie);

        // Start the transaction
        _em.getTransaction().begin();

        checkBadAuthenticatedToken(_em, cookie);

        // Retrieve the user from the database
        User user = retrieveUserByUsername(_em, username);

        // Add credit card to the user
        user.setCreditCard(CreditCardMapper.toDomain(creditCard));

        // Commit the transaction
        _em.getTransaction().commit();

        // Package up and send the response
        return Response.noContent().build();
    }

    /**
     * Retrieves the bookings (confirmed reservations) for a given user.
     *
     * @return a Response containing a Set of BookingDTOs describing the bookings. Each BookingDTO
     * includes concert-identifying information, booking date, seats booked and
     * their price band. Set<BookingDTO>
     *
     * @throws NotAuthorizedException if the request is made by an unauthenticated user.
     * Messages.UNAUTHENTICATED_REQUEST
     *
     * @throws NotAuthorizedException if the request includes an authentication token but it's not
     * recognised by the remote service.
     * Messages.BAD_AUTHENTICATON_TOKEN
     *
     */
    @GET
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    @Path("/users/{username}/bookings")
    public Response getBookings(@PathParam("username") String username,
                                @CookieParam(CLIENT_COOKIE) Cookie cookie) throws ServiceException {

        checkUnauthenticatedRequest(cookie);

        // Start the transaction
        _em.getTransaction().begin();

        checkBadAuthenticatedToken(_em, cookie);

        // Retrieve user's bookings from database
        User user = retrieveUserByUsername(_em, username);
        Set<Booking> bookings = user.getBookings();

        // Convert to data transfer objects
        Set<BookingDTO> bookingDTOs = BookingMapper.toDTO(bookings);

        // Package up and send the response
        GenericEntity<Set<BookingDTO>> ge = new GenericEntity<Set<BookingDTO>>(bookingDTOs){};

        // Commit the transaction
        _em.getTransaction().commit();

        return Response.ok().entity(ge).build();

    }

    /**
     * Allows a client to subscribe to news or updates about a performer.
     */
    @GET
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    @Path("/performers/{id}/subscribe")
    public Response subscribeToPerformer() throws ServiceException {
        // TODO
        return null;
    }

    /**
     * Allows a client to subscribe to news or updates about a concert, such as ticket sales.
     */
    @GET
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    @Path("/concerts/{id}/subscribe")
    public Response subscribeToConcert() throws ServiceException {
        // TODO
        return null;
    }

    /**
     * Helper method that can be called from every service method to generate a
     * NewCookie instance.
     *
     * @return a NewCookie object, with a generated UUID value
     */
    private NewCookie makeCookie(){
        NewCookie token = new NewCookie(CLIENT_COOKIE, UUID.randomUUID().toString());

        _logger.info("Generated cookie: " + token.getValue());

        return token;
    }

    /**
     * Closes the entity manager
     */
    @Override
    public void finalize() {
        _em.close();
    }
}