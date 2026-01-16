package de.oth.muskelmanagement.service;

import de.oth.muskelmanagement.dto.RegistrationDto;
import de.oth.muskelmanagement.dto.UserDto;
import de.oth.muskelmanagement.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    User save(UserDto userDto);

    User registerUser(RegistrationDto registrationDto);

    User findByEmail(String email);

    User findEntityById(Long id);

    Page<UserDto> findAll(Pageable pageable);

    UserDto findById(Long id);

    void updateUser(UserDto userDto);

    void deleteUser(Long id);

    void changeUserPassword(UserDto user, String newPassword);

    void enableTwoFactor(Long userId);

    void disableTwoFactor(Long userId);

    void toggleTwoFactor(Long userId); // Re-added

    void changeMyPassword(Long userId, String oldPassword, String newPassword);

    Page<UserDto> findUsers(String email, String firstName, String lastName, String activePlan, Pageable pageable);

    Page<UserDto> findAllNonAdmins(Pageable pageable);
}
