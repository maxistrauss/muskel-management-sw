package de.oth.muskelmanagement.service;

import de.oth.muskelmanagement.model.User;
import de.oth.muskelmanagement.service.dto.UserDto;

import java.util.List;

public interface UserService {
    User save(UserDto userDto);

    User findByEmail(String email);

    List<UserDto> findAll();

    UserDto findById(Long id);

    void updateUser(UserDto userDto);

    void deleteUser(Long id);

    void changeUserPassword(UserDto user, String newPassword);

    List<UserDto> findUsers(String email, String firstName, String lastName);
}
