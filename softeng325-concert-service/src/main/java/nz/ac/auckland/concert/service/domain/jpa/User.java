package nz.ac.auckland.concert.service.domain.jpa;

import javax.persistence.*;
import javax.ws.rs.core.NewCookie;
import java.util.HashSet;
import java.util.Set;

@Entity
public class User {

    /**
     * All users must have a unique username
     */
    @Id
    @Column(nullable = false, unique = true)
    private String _username;

    @Column(nullable = false)
    private String _password;

    private String _lastname;

    @Column(nullable = false)
    private String _firstname;

    @Convert(converter = CookieConverter.class)
    private NewCookie _authenticationToken;

    @Embedded
    private CreditCard _creditCard;

    @OneToMany
    private Set<Booking> _bookings = new HashSet<>();

    public User(String username, String password, String lastname, String firstname, NewCookie authenticationToken) {
        _username = username;
        _password = password;
        _firstname = firstname;
        _lastname = lastname;
        _authenticationToken = authenticationToken;
    }

    public User() {}

    public String getUsername() {
        return _username;
    }

    public void setUsername(String username) {
        this._username = username;
    }

    public String getPassword() {
        return _password;
    }

    public void setPassword(String password) {
        this._password = password;
    }

    public String getFirstname() {
        return _firstname;
    }

    public void setFirstname(String firstname) {
        this._firstname = firstname;
    }

    public String getLastname() {
        return _lastname;
    }

    public void setLastname(String lastname) {
        this._lastname = lastname;
    }

    public NewCookie getAuthenticationToken() {
        return _authenticationToken;
    }

    public void setAuthenticationToken(NewCookie authenticationToken) {
        this._authenticationToken = authenticationToken;
    }

    public CreditCard getCreditCard() {
        return _creditCard;
    }

    public void setCreditCard(CreditCard creditCard) {
        this._creditCard = creditCard;
    }

    public Set<Booking> getBookings() {
        return _bookings;
    }

    public void setBookings(Set<Booking> _bookings) {
        this._bookings = _bookings;
    }

}
