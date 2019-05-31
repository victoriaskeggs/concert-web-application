package nz.ac.auckland.concert.service.mapper;

import nz.ac.auckland.concert.common.dto.UserDTO;
import nz.ac.auckland.concert.service.domain.jpa.User;

import javax.ws.rs.core.NewCookie;

/**
 * This class converts between User objects used in the domain model and UserDTO objects send between the server
 * and clients.
 */
public class UserMapper {

    /**
     * Converts a UserDTO object to its equivalent domain model class, User
     * @param userDTO to convert
     * @return converted User object
     */
    public static User toDomain(UserDTO userDTO, NewCookie authenticationToken) {
        return new User(
                userDTO.getUsername(),
                userDTO.getPassword(),
                userDTO.getLastname(),
                userDTO.getFirstname(),
                authenticationToken);
    }

    /**
     * Converts a User object to an equivalent UserDTO
     * @param user to convert
     * @return user as a UserDTO
     */
    public static UserDTO toDTO(User user) {

        return new UserDTO(
                user.getUsername(),
                user.getPassword(),
                user.getLastname(),
                user.getFirstname());
    }
}
