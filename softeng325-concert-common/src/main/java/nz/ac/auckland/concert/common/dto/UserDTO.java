package nz.ac.auckland.concert.common.dto;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * DTO class to represent users. 
 * 
 * A UserDTO describes a user in terms of:
 * _username  the user's unique username.
 * _password  the user's password.
 * _firstname the user's first name.
 * _lastname  the user's family name.
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class UserDTO {

    @XmlElement
	private String _username;

    @XmlElement
	private String _password;

    @XmlElement
	private String _firstname;

    @XmlElement
	private String _lastname;
	
	public UserDTO() {}
	
	public UserDTO(String username, String password, String lastname, String firstname) {
		_username = username;
		_password = password;
		_lastname = lastname;
		_firstname = firstname;
	}
	
	public UserDTO(String username, String password) {
		this(username, password, null, null);
	}
	
	public String getUsername() {
		return _username;
	}
	
	public String getPassword() {
		return _password;
	}
	
	public String getFirstname() {
		return _firstname;
	}
	
	public String getLastname() {
		return _lastname;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof UserDTO))
            return false;
        if (obj == this)
            return true;

        UserDTO rhs = (UserDTO) obj;
        boolean isEquals = new EqualsBuilder().
            append(_username, rhs._username).
            append(_password, rhs._password).
            append(_firstname, rhs._firstname).
            append(_lastname, rhs._lastname).
            isEquals();
        return isEquals;
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 31). 
	            append(_username).
	            append(_password).
	            append(_firstname).
	            append(_password).
	            hashCode();
	}
}
