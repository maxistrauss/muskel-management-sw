package de.oth.muskelmanagement.service;

import de.oth.muskelmanagement.dto.UserDto;
import de.oth.muskelmanagement.model.entity.Role;
import de.oth.muskelmanagement.model.entity.User;
import de.oth.muskelmanagement.repository.RoleRepository;
import de.oth.muskelmanagement.repository.UserRepository;
import de.oth.muskelmanagement.service.impl.UserServiceImpl;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private EmailService emailService;
    @Mock
    private SubscriptionService subscriptionService;
    @Mock
    private de.oth.muskelmanagement.repository.CourseRepository courseRepository;
    @Mock
    private de.oth.muskelmanagement.repository.EnrollmentRepository enrollmentRepository;

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
        userDto.setTwoFactorEnabled(false);
        userDto.setDeactivationReason(null);
        userDto.setRoles(new HashSet<>(Collections.singletonList("ROLE_ADMIN")));

        user = new User("Test", "User", "test@example.com", "encodedPassword",
                new HashSet<>(Collections.singletonList(memberRole)));
        user.setId(1L);
        user.setEnabled(true);
        user.setTwoFactorEnabled(false);
        user.setDeactivationReason(null);
    }

    @Test
    void save_shouldCreateUserWithMemberRoleAndEncodePassword() {
        when(roleRepository.findByName("ROLE_MEMBER")).thenReturn(memberRole);
        when(roleRepository.findByName("ROLE_ADMIN")).thenReturn(adminRole);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(
                invocation -> invocation.getArgument(0));

        User savedUser = userService.save(userDto);

        assertNotNull(savedUser);
        assertEquals("test@example.com", savedUser.getEmail());
        assertEquals("encodedPassword", savedUser.getPassword());
        assertTrue(savedUser.getRoles().contains(memberRole));
        assertTrue(savedUser.getRoles().stream().anyMatch(
                r -> r.getName().equals("ROLE_ADMIN")));
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void save_shouldSetTwoFactorEnabledCorrectly() {
        userDto.setTwoFactorEnabled(true);
        
        when(roleRepository.findByName("ROLE_MEMBER")).thenReturn(memberRole);
        when(roleRepository.findByName("ROLE_ADMIN")).thenReturn(adminRole);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(
                invocation -> invocation.getArgument(0));

        User savedUser = userService.save(userDto);

        assertNotNull(savedUser);
        assertTrue(savedUser.isTwoFactorEnabled());
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
    void updateUser_shouldUpdateUserDetails() {
        User existingUser = new User("Old", "Name", "old@example.com", "oldEncodedPassword",
                new HashSet<>(Collections.singletonList(memberRole)));
        existingUser.setId(1L);

        UserDto updateDto = new UserDto();
        updateDto.setId(1L);
        updateDto.setFirstName("New");
        updateDto.setLastName("Name");
        updateDto.setEmail("new@example.com");
        updateDto.setEnabled(false);
        updateDto.setRoles(new HashSet<>(Collections.singletonList("ROLE_TRAINER")));

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(roleRepository.findByName("ROLE_MEMBER")).thenReturn(memberRole);
        when(roleRepository.findByName("ROLE_TRAINER")).thenReturn(new Role("ROLE_TRAINER"));
        when(userRepository.save(any(User.class))).thenAnswer(
                invocation -> invocation.getArgument(0));

        userService.updateUser(updateDto);

        assertEquals("New", existingUser.getFirstName());
        assertEquals("old@example.com", existingUser.getEmail());
        assertFalse(existingUser.isEnabled());
        assertTrue(existingUser.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ROLE_MEMBER")));
        assertTrue(existingUser.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ROLE_TRAINER")));
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
        updateDto.setFirstName("Test");
        updateDto.setLastName("User");
        updateDto.setEmail("test@example.com");
        updateDto.setRoles(new HashSet<>(Collections.singletonList("ROLE_MEMBER")));

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.encode("newPassword123")).thenReturn("newEncodedPassword");
        when(roleRepository.findByName("ROLE_MEMBER")).thenReturn(memberRole);

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
        updateDto.setPassword("");
        updateDto.setFirstName("Test");
        updateDto.setLastName("User");
        updateDto.setEmail("test@example.com");
        updateDto.setRoles(new HashSet<>(Collections.singletonList("ROLE_MEMBER")));

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(roleRepository.findByName("ROLE_MEMBER")).thenReturn(memberRole);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        when(userRepository.save(userCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        userService.updateUser(updateDto);

        User savedUser = userCaptor.getValue();
        assertEquals("oldEncodedPassword", savedUser.getPassword());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void updateUser_shouldSetDeactivationReasonWhenDisabled() {
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setFirstName("Test");
        existingUser.setLastName("User");
        existingUser.setEmail("test@example.com");
        existingUser.setPassword("encodedPassword");
        existingUser.setEnabled(true);
        existingUser.setDeactivationReason(null);

        UserDto updateDto = new UserDto();
        updateDto.setId(1L);
        updateDto.setFirstName("Test");
        updateDto.setLastName("User");
        updateDto.setEmail("test@example.com");
        updateDto.setEnabled(false);
        updateDto.setDeactivationReason("Account suspended for policy violation");
        updateDto.setRoles(new HashSet<>(Collections.singletonList("ROLE_MEMBER")));

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(roleRepository.findByName("ROLE_MEMBER")).thenReturn(memberRole);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.updateUser(updateDto);

        assertFalse(existingUser.isEnabled());
        assertEquals("Account suspended for policy violation", existingUser.getDeactivationReason());
        verify(emailService, times(1)).sendAccountDeactivationEmail(
                eq("test@example.com"), 
                eq("Test User"), 
                eq("Account suspended for policy violation"));
    }

    @Test
    void updateUser_shouldClearDeactivationReasonWhenEnabled() {
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setFirstName("Test");
        existingUser.setLastName("User");
        existingUser.setEmail("test@example.com");
        existingUser.setPassword("encodedPassword");
        existingUser.setEnabled(false);
        existingUser.setDeactivationReason("Previous reason");

        UserDto updateDto = new UserDto();
        updateDto.setId(1L);
        updateDto.setFirstName("Test");
        updateDto.setLastName("User");
        updateDto.setEmail("test@example.com");
        updateDto.setEnabled(true);
        updateDto.setRoles(new HashSet<>(Collections.singletonList("ROLE_MEMBER")));

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(roleRepository.findByName("ROLE_MEMBER")).thenReturn(memberRole);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.updateUser(updateDto);

        assertTrue(existingUser.isEnabled());
        assertNull(existingUser.getDeactivationReason());
        verify(emailService, times(1)).sendAccountActivationEmail(
                eq("test@example.com"), 
                eq("Test User"));
    }

    @Test
    void updateUser_shouldUpdateTwoFactorEnabled() {
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setFirstName("Test");
        existingUser.setLastName("User");
        existingUser.setEmail("test@example.com");
        existingUser.setPassword("encodedPassword");
        existingUser.setEnabled(true);
        existingUser.setTwoFactorEnabled(false);

        UserDto updateDto = new UserDto();
        updateDto.setId(1L);
        updateDto.setFirstName("Test");
        updateDto.setLastName("User");
        updateDto.setEmail("test@example.com");
        updateDto.setEnabled(true);
        updateDto.setTwoFactorEnabled(true);
        updateDto.setRoles(new HashSet<>(Collections.singletonList("ROLE_MEMBER")));

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(roleRepository.findByName("ROLE_MEMBER")).thenReturn(memberRole);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.updateUser(updateDto);

        assertTrue(existingUser.isTwoFactorEnabled());
    }

    @Test
    void enableTwoFactor_shouldSetTwoFactorEnabledToTrue() {
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setTwoFactorEnabled(false);

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.enableTwoFactor(1L);

        assertTrue(existingUser.isTwoFactorEnabled());
        verify(userRepository, times(1)).save(existingUser);
    }

    @Test
    void disableTwoFactor_shouldSetTwoFactorEnabledToFalse() {
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setTwoFactorEnabled(true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.disableTwoFactor(1L);

        assertFalse(existingUser.isTwoFactorEnabled());
        verify(userRepository, times(1)).save(existingUser);
    }

    @Test
    void toggleTwoFactor_shouldToggleTwoFactorEnabled() {
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setTwoFactorEnabled(false);

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.toggleTwoFactor(1L);

        assertTrue(existingUser.isTwoFactorEnabled());
        verify(userRepository, times(1)).save(existingUser);
    }

    @Test
    void toggleTwoFactor_shouldToggleFromTrueToFalse() {
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setTwoFactorEnabled(true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.toggleTwoFactor(1L);

        assertFalse(existingUser.isTwoFactorEnabled());
        verify(userRepository, times(1)).save(existingUser);
    }

    @Test
    void deleteUser_shouldDeleteUser() {
        doNothing().when(userRepository).deleteById(1L);

        userService.deleteUser(1L);

        verify(userRepository, times(1)).deleteById(1L);
    }
}