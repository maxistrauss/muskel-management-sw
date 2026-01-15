package de.oth.muskelmanagement.service;

import de.oth.muskelmanagement.dto.RegistrationDto;
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
class ProfileManagementTest {

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

    private User testUser;
    private Role memberRole;
    private RegistrationDto registrationDto;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        memberRole = new Role("ROLE_MEMBER");
        memberRole.setId(1L);

        testUser = new User("John", "Doe", "john.doe@example.com", "encodedPassword",
                new HashSet<>(Collections.singletonList(memberRole)));
        testUser.setId(1L);
        testUser.setEnabled(true);
        testUser.setTwoFactorEnabled(false);

        registrationDto = new RegistrationDto();
        registrationDto.setFirstName("Jane");
        registrationDto.setLastName("Smith");
        registrationDto.setEmail("jane.smith@example.com");
        registrationDto.setPassword("securePassword123");


        userDto = new UserDto();
        userDto.setId(1L);
        userDto.setFirstName("UpdatedJohn");
        userDto.setLastName("UpdatedDoe");
        userDto.setEmail("john.doe@example.com");
        userDto.setTwoFactorEnabled(false);
        userDto.setRoles(new HashSet<>(Collections.singletonList("ROLE_MEMBER")));
    }

    // --- Registrieren (Register) Tests ---
    @Test
    void registerUser_shouldCreateNewUserWithMemberRole() {
        when(userRepository.findByEmail(anyString())).thenReturn(null); // User does not exist
        when(roleRepository.findByName("ROLE_MEMBER")).thenReturn(memberRole);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedSecurePassword123");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(2L); // Simulate ID being set by DB
            return user;
        });

        User newUser = userService.registerUser(registrationDto);

        assertNotNull(newUser);
        assertNotNull(newUser.getId());
        assertEquals(registrationDto.getFirstName(), newUser.getFirstName());
        assertEquals(registrationDto.getLastName(), newUser.getLastName());
        assertEquals(registrationDto.getEmail(), newUser.getEmail());
        assertEquals("encodedSecurePassword123", newUser.getPassword());
        assertTrue(newUser.getRoles().contains(memberRole));
        assertTrue(newUser.isEnabled());
        assertFalse(newUser.isTwoFactorEnabled());

        verify(userRepository, times(1)).findByEmail(registrationDto.getEmail());
        verify(roleRepository, times(1)).findByName("ROLE_MEMBER");
        verify(passwordEncoder, times(1)).encode(registrationDto.getPassword());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void registerUser_shouldThrowExceptionIfEmailExists() {
        when(userRepository.findByEmail(anyString())).thenReturn(testUser); // User already exists

        assertThrows(IllegalStateException.class, () ->
                userService.registerUser(registrationDto));

        verify(userRepository, times(1)).findByEmail(registrationDto.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    // --- Daten einsehen (View Data) Tests ---
    @Test
    void getUserById_shouldReturnUserIfExists() {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));

        User foundUser = userService.findEntityById(testUser.getId());

        assertNotNull(foundUser);
        assertEquals(testUser.getId(), foundUser.getId());
        assertEquals(testUser.getEmail(), foundUser.getEmail());
        verify(userRepository, times(1)).findById(testUser.getId());
    }

    @Test
    void getUserById_shouldThrowExceptionIfUserDoesNotExist() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> // findEntityById throws RuntimeException
                userService.findEntityById(99L));

        verify(userRepository, times(1)).findById(anyLong());
    }

    @Test
    void getUserByEmail_shouldReturnUserIfExists() {
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(testUser);

        User foundUser = userService.findByEmail(testUser.getEmail());

        assertNotNull(foundUser);
        assertEquals(testUser.getEmail(), foundUser.getEmail());
        verify(userRepository, times(1)).findByEmail(testUser.getEmail());
    }

    @Test
    void getUserByEmail_shouldReturnNullIfUserDoesNotExist() {
        when(userRepository.findByEmail(anyString())).thenReturn(null);

        User foundUser = userService.findByEmail("nonexistent@example.com");

        assertNull(foundUser);
        verify(userRepository, times(1)).findByEmail(anyString());
    }

    // --- Daten ändern (Change Data) Tests ---
    @Test
    void updateProfile_shouldUpdateOnlyEditableUserDetails() {
        User existingUser = new User("Old", "User", "john.doe@example.com", "oldEncodedPassword",
                new HashSet<>(Collections.singletonList(memberRole)));
        existingUser.setId(1L);
        existingUser.setEnabled(true);
        existingUser.setTwoFactorEnabled(false);

        UserDto updatedProfileDto = new UserDto();
        updatedProfileDto.setId(existingUser.getId());
        updatedProfileDto.setFirstName("NewFirstName");
        updatedProfileDto.setLastName("NewLastName");
        updatedProfileDto.setEmail("john.doe@example.com"); // Email should not change via profile update
        updatedProfileDto.setEnabled(true); // Maintain enabled status
        updatedProfileDto.setTwoFactorEnabled(false); // Can be changed separately, but here it's not the focus
        updatedProfileDto.setRoles(new HashSet<>(Collections.singletonList("ROLE_MEMBER"))); // Roles not changeable by user

        when(userRepository.findById(existingUser.getId())).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(roleRepository.findByName("ROLE_MEMBER")).thenReturn(memberRole);

        userService.updateUser(updatedProfileDto);

        assertEquals("NewFirstName", existingUser.getFirstName());
        assertEquals("NewLastName", existingUser.getLastName());
        assertEquals("john.doe@example.com", existingUser.getEmail()); // Email remains the same
        assertEquals("oldEncodedPassword", existingUser.getPassword()); // Password remains the same
        assertTrue(existingUser.getRoles().contains(memberRole)); // Roles remain the same
        verify(userRepository, times(1)).findById(existingUser.getId());
        verify(userRepository, times(1)).save(existingUser);
        verify(passwordEncoder, never()).encode(anyString()); // Password encoder should not be called
    }

    @Test
    void updateProfile_shouldNotAllowEmailChange() {
        User existingUser = new User("John", "Doe", "john.doe@example.com", "encodedPassword",
                new HashSet<>(Collections.singletonList(memberRole)));
        existingUser.setId(1L);

        UserDto updatedProfileDto = new UserDto();
        updatedProfileDto.setId(existingUser.getId());
        updatedProfileDto.setFirstName("John");
        updatedProfileDto.setLastName("Doe");
        updatedProfileDto.setEmail("new.email@example.com"); // Attempt to change email
        updatedProfileDto.setEnabled(true); // Added enabled for updateUser
        updatedProfileDto.setRoles(new HashSet<>(Collections.singletonList("ROLE_MEMBER"))); // Required by updateUser

        when(userRepository.findById(existingUser.getId())).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.updateUser(updatedProfileDto);

        assertEquals("john.doe@example.com", existingUser.getEmail()); // Email should not have changed
        verify(userRepository, times(1)).findById(existingUser.getId());
        verify(userRepository, times(1)).save(existingUser);
    }
    
    // --- Konto löschen (Delete Account) Tests ---
    @Test
    void deactivateAccount_shouldSetUserToDisabledAndAddReason() {
        testUser.setEnabled(true); // Ensure user is enabled initially
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(roleRepository.findByName("ROLE_MEMBER")).thenReturn(memberRole); // Mock role for updateUser

        UserDto deactivationDto = new UserDto();
        deactivationDto.setId(testUser.getId());
        deactivationDto.setFirstName(testUser.getFirstName());
        deactivationDto.setLastName(testUser.getLastName());
        deactivationDto.setEmail(testUser.getEmail());
        deactivationDto.setEnabled(false); // Deactivate
        deactivationDto.setDeactivationReason("User requested deactivation");
        deactivationDto.setTwoFactorEnabled(testUser.isTwoFactorEnabled());
        deactivationDto.setRoles(new HashSet<>(Collections.singletonList("ROLE_MEMBER"))); // Required by updateUser


        userService.updateUser(deactivationDto);

        assertFalse(testUser.isEnabled());
        assertEquals("User requested deactivation", testUser.getDeactivationReason());
        // No getDeactivatedAt() in User entity, so remove this assertion
        // assertNotNull(testUser.getDeactivatedAt()); 
        verify(userRepository, times(1)).findById(testUser.getId());
        verify(userRepository, times(1)).save(testUser);
        verify(emailService, times(1)).sendAccountDeactivationEmail(
                eq(testUser.getEmail()),
                eq(testUser.getFirstName() + " " + testUser.getLastName()),
                eq("User requested deactivation"));
    }

    @Test
    void deactivateAccount_shouldThrowExceptionIfUserNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        UserDto deactivationDto = new UserDto();
        deactivationDto.setId(99L); // ID of non-existent user
        deactivationDto.setEnabled(false);
        deactivationDto.setDeactivationReason("Reason");
        deactivationDto.setRoles(new HashSet<>(Collections.singletonList("ROLE_MEMBER"))); // Required by updateUser


        assertThrows(RuntimeException.class, () -> // updateUser throws RuntimeException
                userService.updateUser(deactivationDto));

        verify(userRepository, times(1)).findById(anyLong());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void changePassword_shouldThrowExceptionForIncorrectOldPassword() {
        String oldPassword = "oldSecurePassword";
        String wrongOldPassword = "wrongPassword";
        String newPassword = "newSecurePassword";

        User existingUser = new User("John", "Doe", "john.doe@example.com", oldPassword,
                new HashSet<>(Collections.singletonList(memberRole)));
        existingUser.setId(1L);

        when(userRepository.findById(existingUser.getId())).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(wrongOldPassword, existingUser.getPassword())).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () ->
                userService.changeMyPassword(existingUser.getId(), wrongOldPassword, newPassword));

        verify(userRepository, times(1)).findById(existingUser.getId());
        verify(passwordEncoder, times(1)).matches(wrongOldPassword, existingUser.getPassword());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void changePassword_shouldThrowExceptionIfUserNotFound() {
        String oldPassword = "oldSecurePassword";
        String newPassword = "newSecurePassword";

        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> // changeMyPassword throws RuntimeException
                userService.changeMyPassword(99L, oldPassword, newPassword));

        verify(userRepository, times(1)).findById(anyLong());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }
}
