package de.oth.muskelmanagement.service;

import de.oth.muskelmanagement.model.User;
import de.oth.muskelmanagement.service.dto.RegistrationDto;
import de.oth.muskelmanagement.service.dto.UserDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    User save(UserDto userDto);

    User registerUser(RegistrationDto registrationDto);

    User findByEmail(String email);

    Page<UserDto> findAll(Pageable pageable);

    UserDto findById(Long id);

    void updateUser(UserDto userDto);

    void deleteUser(Long id);

    void changeUserPassword(UserDto user, String newPassword);

    Page<UserDto> findUsers(String email, String firstName, String lastName, String membershipType, Pageable pageable);
}
