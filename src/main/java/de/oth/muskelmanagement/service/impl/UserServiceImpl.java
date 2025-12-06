package de.oth.muskelmanagement.service.impl;

import de.oth.muskelmanagement.dto.RegistrationDto;
import de.oth.muskelmanagement.dto.UserDto;
import de.oth.muskelmanagement.model.entity.Role;
import de.oth.muskelmanagement.model.entity.User;
import de.oth.muskelmanagement.repository.RoleRepository;
import de.oth.muskelmanagement.repository.UserRepository;
import de.oth.muskelmanagement.service.EmailService;
import de.oth.muskelmanagement.service.UserService;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final de.oth.muskelmanagement.repository.CourseRepository courseRepository;
    private final de.oth.muskelmanagement.repository.EnrollmentRepository enrollmentRepository;
    private final de.oth.muskelmanagement.repository.MembershipRepository membershipRepository;

    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository,
            PasswordEncoder passwordEncoder, EmailService emailService,
            de.oth.muskelmanagement.repository.CourseRepository courseRepository,
            de.oth.muskelmanagement.repository.EnrollmentRepository enrollmentRepository,
            de.oth.muskelmanagement.repository.MembershipRepository membershipRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.membershipRepository = membershipRepository;
    }

    @Override
    public User save(UserDto userDto) {
        User user = new User();
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setEmail(userDto.getEmail());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setEnabled(userDto.isEnabled());
        user.setTwoFactorEnabled(userDto.isTwoFactorEnabled());

        if (userDto.getMembershipId() != null) {
            user.setMembership(membershipRepository.findById(userDto.getMembershipId()).orElse(null));
        }

        Set<Role> roles = userDto.getRoles().stream().map(roleName -> {
            Role role = roleRepository.findByName(roleName);
            if (role == null) {
                role = new Role(roleName);
                roleRepository.save(role);
            }
            return role;
        }).collect(Collectors.toSet());

        Role memberRole = roleRepository.findByName("ROLE_MEMBER");
        if (memberRole == null) {
            memberRole = new Role("ROLE_MEMBER");
            roleRepository.save(memberRole);
        }
        roles.add(memberRole);
        user.setRoles(roles);

        return userRepository.save(user);
    }

    @Override
    public User registerUser(RegistrationDto registrationDto) {
        User user = new User();
        user.setFirstName(registrationDto.getFirstName());
        user.setLastName(registrationDto.getLastName());
        user.setEmail(registrationDto.getEmail());
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        user.setEnabled(true); // New registrations are enabled by default

        if (registrationDto.getMembershipId() != null) {
            user.setMembership(membershipRepository.findById(registrationDto.getMembershipId()).orElse(null));
        }

        // Only assign ROLE_MEMBER for self-registration
        Role memberRole = roleRepository.findByName("ROLE_MEMBER");
        if (memberRole == null) {
            memberRole = new Role("ROLE_MEMBER");
            roleRepository.save(memberRole);
        }

        Set<Role> roles = new HashSet<>();
        roles.add(memberRole);
        user.setRoles(roles);

        return userRepository.save(user);
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public Page<UserDto> findAll(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::convertToDto);
    }

    @Override
    public User findEntityById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public UserDto findById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        return convertToDto(user);
    }

    @Override
    public void updateUser(UserDto userDto) {
        User user = userRepository.findById(userDto.getId()).orElseThrow(() -> new RuntimeException("User not found"));

        // Track if enabled status changed
        boolean wasEnabled = user.isEnabled();
        boolean willBeEnabled = userDto.isEnabled();
        boolean statusChanged = wasEnabled != willBeEnabled;

        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setEmail(userDto.getEmail());
        user.setEnabled(userDto.isEnabled());
        user.setTwoFactorEnabled(userDto.isTwoFactorEnabled());

        if (userDto.getMembershipId() != null) {
            user.setMembership(membershipRepository.findById(userDto.getMembershipId()).orElse(null));
        } else {
            user.setMembership(null);
        }

        // Handle deactivation reason
        if (!willBeEnabled) {
            user.setDeactivationReason(userDto.getDeactivationReason());
        } else {
            // Clear deactivation reason when account is activated
            user.setDeactivationReason(null);
        }

        // Only update password if it's provided in the DTO
        if (userDto.getPassword() != null && !userDto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        }

        Set<Role> roles = userDto.getRoles().stream().map(roleName -> {
            Role role = roleRepository.findByName(roleName);
            if (role == null) {
                role = new Role(roleName);
                roleRepository.save(role);
            }
            return role;
        }).collect(Collectors.toSet());

        Role memberRole = roleRepository.findByName("ROLE_MEMBER");
        if (memberRole == null) {
            memberRole = new Role("ROLE_MEMBER");
            roleRepository.save(memberRole);
        }
        roles.add(memberRole);
        user.setRoles(roles);

        userRepository.save(user);

        // Send email notification if status changed
        if (statusChanged) {
            String userName = user.getFirstName() + " " + user.getLastName();
            if (!willBeEnabled) {
                // Account was deactivated
                emailService.sendAccountDeactivationEmail(user.getEmail(), userName, user.getDeactivationReason());
            } else {
                // Account was activated
                emailService.sendAccountActivationEmail(user.getEmail(), userName);
            }
        }
    }

    @Override
    public void deleteUser(Long id) {
        // Before deleting user, remove as trainer from any courses and delete enrollments
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            // Unassign trainer role from courses
            List<de.oth.muskelmanagement.model.entity.Course> courses = courseRepository.findByTrainer(user);
            for (de.oth.muskelmanagement.model.entity.Course c : courses) {
                c.setTrainer(null);
                courseRepository.save(c);
            }

            // Remove enrollments
            var enrollments = enrollmentRepository.findByUser(user);
            for (var e : enrollments) {
                enrollmentRepository.delete(e);
            }
        }
        userRepository.deleteById(id);
    }

    @Override
    public void changeUserPassword(UserDto user, String newPassword) {
        User userFromDb = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        userFromDb.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(userFromDb);
    }

    @Override
    public Page<UserDto> findUsers(String email, String firstName, String lastName, String membershipName,
            Pageable pageable) {
        Specification<User> spec = (Root<User> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(email)) {
                predicates.add(cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase() + "%"));
            }
            if (StringUtils.hasText(firstName)) {
                predicates.add(cb.like(cb.lower(root.get("firstName")), "%" + firstName.toLowerCase() + "%"));
            }
            if (StringUtils.hasText(lastName)) {
                predicates.add(cb.like(cb.lower(root.get("lastName")), "%" + lastName.toLowerCase() + "%"));
            }
            if (StringUtils.hasText(membershipName)) {
                predicates.add(cb.like(cb.lower(root.get("membership").get("name")),
                        "%" + membershipName.toLowerCase() + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return userRepository.findAll(spec, pageable).map(this::convertToDto);
    }

    private UserDto convertToDto(User user) {
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setFirstName(user.getFirstName());
        userDto.setLastName(user.getLastName());
        userDto.setEmail(user.getEmail());

        if (user.getMembership() != null) {
            userDto.setMembershipName(user.getMembership().getName());
            userDto.setMembershipId(user.getMembership().getId());
        }
        
        userDto.setEnabled(user.isEnabled());
        userDto.setDeactivationReason(user.getDeactivationReason());
        userDto.setTwoFactorEnabled(user.isTwoFactorEnabled());
        userDto.setRoles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()));
        return userDto;
    }

    @Override
    public void enableTwoFactor(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        user.setTwoFactorEnabled(true);
        userRepository.save(user);
    }

    @Override
    public void disableTwoFactor(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        user.setTwoFactorEnabled(false);
        userRepository.save(user);
    }

    @Override
    public void toggleTwoFactor(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        user.setTwoFactorEnabled(!user.isTwoFactorEnabled());
        userRepository.save(user);
    }

    @Override
    public Page<UserDto> findMembersOnly(Pageable pageable) {
        return userRepository.findPureMembers(pageable).map(this::convertToDto);
    }

    @Override
    public Page<UserDto> findNonAdmins(Pageable pageable) {
        return userRepository.findNonAdmins(pageable).map(this::convertToDto);
    }
}
