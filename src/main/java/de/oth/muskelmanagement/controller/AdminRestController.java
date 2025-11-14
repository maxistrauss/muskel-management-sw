package de.oth.muskelmanagement.controller;

import de.oth.muskelmanagement.service.UserService;
import de.oth.muskelmanagement.service.dto.UserDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')") // Secure all endpoints in this controller
public class AdminRestController {

    private final UserService userService;

    public AdminRestController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> findUsers(@RequestParam(required = false) String email,
            @RequestParam(required = false) String firstName, @RequestParam(required = false) String lastName) {
        List<UserDto> users = userService.findUsers(email, firstName, lastName);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        UserDto user = userService.findById(id);
        return ResponseEntity.ok(user);
    }

    @PostMapping
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserDto userDto) {
        // Password validation for creation (since @NotBlank was removed from DTO)
        if (userDto.getPassword() == null || userDto.getPassword().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password cannot be blank");
        }
        if (userDto.getPassword().length() < 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at least 8 characters long");
        }

        userService.save(userDto);
        return new ResponseEntity<>(userDto, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long id, @Valid @RequestBody UserDto userDto,
            Principal principal) {
        // Prevent admin from updating their own account via API
        UserDto existingUser = userService.findById(id);
        if (existingUser.getEmail().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Admins are not allowed to update their own account.");
        }

        userDto.setId(id); // Ensure ID from path is used
        userService.updateUser(userDto);
        return ResponseEntity.ok(userDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id, Principal principal) {
        // Prevent admin from deleting their own account via API
        UserDto userToDelete = userService.findById(id);
        if (userToDelete.getEmail().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Admins are not allowed to delete their own account.");
        }

        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
