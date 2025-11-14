package de.oth.muskelmanagement.config;

import de.oth.muskelmanagement.model.Role;
import de.oth.muskelmanagement.model.User;
import de.oth.muskelmanagement.repository.RoleRepository;
import de.oth.muskelmanagement.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, RoleRepository roleRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;

        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        Role adminRole = new Role("ROLE_ADMIN");

        roleRepository.save(adminRole);

        Role trainerRole = new Role("ROLE_TRAINER");

        roleRepository.save(trainerRole);

        Role memberRole = new Role("ROLE_MEMBER");
        roleRepository.save(memberRole);

        User admin = new User("Admin", "User", "admin@example.com", "Premium", passwordEncoder.encode("password"),
                Set.of(adminRole, trainerRole, memberRole));
        userRepository.save(admin);

        User trainer = new User("Trainer", "User", "trainer@example.com", "Trainer", passwordEncoder.encode("password"),
                Set.of(trainerRole, memberRole));
        userRepository.save(trainer);

        User member = new User("Member", "User", "member@example.com", "Basic", passwordEncoder.encode("password"),
                Set.of(memberRole));
        userRepository.save(member);
    }
}
