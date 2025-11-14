package de.oth.muskelmanagement.service;

import de.oth.muskelmanagement.model.Role;
import de.oth.muskelmanagement.model.User;
import de.oth.muskelmanagement.repository.RoleRepository;
import de.oth.muskelmanagement.repository.UserRepository;
import de.oth.muskelmanagement.service.dto.UserDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private UserDto userDto;
    private User user;
    private Role memberRole;
    private Role adminRole;

    @BeforeEach
    void setUp() {
        memberRole = new Role("ROLE_MEMBER");
        memberRole.setId(1L);

        adminRole = new Role("ROLE_ADMIN");
        adminRole.setId(2L);

        userDto = new UserDto();
        userDto.setId(1L);
        userDto.setFirstName("Test");
        userDto.setLastName("User");
        userDto.setEmail("test@example.com");
        userDto.setPassword("password123");
        userDto.setEnabled(true);
        userDto.setRoles(new HashSet<>(Collections.singletonList("ROLE_ADMIN")));

        user = new User("Test", "User", "test@example.com", "encodedPassword",
                new HashSet<>(Collections.singletonList(memberRole)));
        user.setId(1L);
        user.setEnabled(true);
    }

    @Test
    void save_shouldCreateUserWithMemberRoleAndEncodePassword() {
        when(roleRepository.findByName("ROLE_MEMBER")).thenReturn(memberRole);
        when(roleRepository.findByName("ROLE_ADMIN")).thenReturn(adminRole);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(
                invocation -> invocation.getArgument(0)); // Return the argument passed to save

        User savedUser = userService.save(userDto);

        assertNotNull(savedUser);
        assertEquals("test@example.com", savedUser.getEmail());
        assertEquals("encodedPassword", savedUser.getPassword());
        assertTrue(savedUser.getRoles().contains(memberRole));
        assertTrue(savedUser.getRoles().stream().anyMatch(
                r -> r.getName().equals("ROLE_ADMIN"))); // Check by name as role object might be different instance
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void findByEmail_shouldReturnUserIfExists() {
        when(userRepository.findByEmail(anyString())).thenReturn(user);

        User foundUser = userService.findByEmail("test@example.com");

        assertNotNull(foundUser);
        assertEquals("test@example.com", foundUser.getEmail());
    }

    @Test
    void findByEmail_shouldReturnNullIfUserDoesNotExist() {
        when(userRepository.findByEmail(anyString())).thenReturn(null);

        User foundUser = userService.findByEmail("nonexistent@example.com");

        assertNull(foundUser);
    }

    @Test
    void updateUser_shouldUpdateUserDetailsAndKeepMemberRole() {
        User existingUser = new User("Old", "Name", "old@example.com", "oldEncodedPassword",
                new HashSet<>(Collections.singletonList(memberRole)));
        existingUser.setId(1L);

        UserDto updateDto = new UserDto();
        updateDto.setId(1L);
        updateDto.setFirstName("New");
        updateDto.setLastName("Name");
        updateDto.setEmail("new@example.com");
        updateDto.setEnabled(false);
        updateDto.setRoles(new HashSet<>(Collections.singletonList("ROLE_TRAINER"))); // Try to remove member role

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(roleRepository.findByName("ROLE_MEMBER")).thenReturn(memberRole);
        when(roleRepository.findByName("ROLE_TRAINER")).thenReturn(new Role("ROLE_TRAINER"));
        when(userRepository.save(any(User.class))).thenAnswer(
                invocation -> invocation.getArgument(0)); // Return the argument passed to save

        userService.updateUser(updateDto);

        assertEquals("New", existingUser.getFirstName());
        assertEquals("new@example.com", existingUser.getEmail());
        assertFalse(existingUser.isEnabled());
        assertTrue(existingUser.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ROLE_MEMBER"))); // Member role should still be there
        assertTrue(existingUser.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ROLE_TRAINER"))); // Trainer role should be added
        verify(userRepository, times(1)).save(existingUser);
    }

    @Test
    void updateUser_shouldUpdatePasswordIfProvided() {
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setFirstName("Test");
        existingUser.setLastName("User");
        existingUser.setEmail("test@example.com");
        existingUser.setPassword("oldEncodedPassword");
        existingUser.setEnabled(true);

        UserDto updateDto = new UserDto();
        updateDto.setId(1L);
        updateDto.setPassword("newPassword123");
        updateDto.setFirstName("Test"); // Required for validation
        updateDto.setLastName("User"); // Required for validation
        updateDto.setEmail("test@example.com"); // Required for validation
        updateDto.setRoles(new HashSet<>(Collections.singletonList("ROLE_MEMBER"))); // Required for validation

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.encode("newPassword123")).thenReturn("newEncodedPassword");
        when(roleRepository.findByName("ROLE_MEMBER")).thenReturn(memberRole); // Mock member role for conditional logic

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        when(userRepository.save(userCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        userService.updateUser(updateDto);

        User savedUser = userCaptor.getValue();
        assertEquals("newEncodedPassword", savedUser.getPassword());
    }

    @Test
    void updateUser_shouldNotUpdatePasswordIfNotProvided() {
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setFirstName("Test");
        existingUser.setLastName("User");
        existingUser.setEmail("test@example.com");
        existingUser.setPassword("oldEncodedPassword");
        existingUser.setEnabled(true);

        UserDto updateDto = new UserDto();
        updateDto.setId(1L);
        updateDto.setPassword(""); // Empty password
        updateDto.setFirstName("Test"); // Required for validation
        updateDto.setLastName("User"); // Required for validation
        updateDto.setEmail("test@example.com"); // Required for validation
        updateDto.setRoles(new HashSet<>(Collections.singletonList("ROLE_MEMBER"))); // Required for validation

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(roleRepository.findByName("ROLE_MEMBER")).thenReturn(memberRole);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        when(userRepository.save(userCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        userService.updateUser(updateDto);

        User savedUser = userCaptor.getValue();
        assertEquals("oldEncodedPassword", savedUser.getPassword()); // Password should remain unchanged
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void deleteUser_shouldDeleteUser() {
        // No need to stub existsById as it's not called by the service method
        doNothing().when(userRepository).deleteById(1L);

        userService.deleteUser(1L);

        verify(userRepository, times(1)).deleteById(1L);
    }
}

