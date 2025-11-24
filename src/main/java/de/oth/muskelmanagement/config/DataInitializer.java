package de.oth.muskelmanagement.config;

import de.oth.muskelmanagement.model.Role;
import de.oth.muskelmanagement.model.Room;
import de.oth.muskelmanagement.model.User;
import de.oth.muskelmanagement.repository.RoleRepository;
import de.oth.muskelmanagement.repository.RoomRepository;
import de.oth.muskelmanagement.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RoomRepository roomRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, RoleRepository roleRepository,
            RoomRepository roomRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.roomRepository = roomRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        initializeRoles();
        initializeUsers();
        initializeRooms();
    }

    private void initializeRoles() {
        if (roleRepository.count() > 0) {
            return;
        }

        Role adminRole = new Role("ROLE_ADMIN");
        roleRepository.save(adminRole);

        Role trainerRole = new Role("ROLE_TRAINER");
        roleRepository.save(trainerRole);

        Role memberRole = new Role("ROLE_MEMBER");
        roleRepository.save(memberRole);
    }

    private void initializeUsers() {
        if (userRepository.count() > 0) {
            return;
        }

        Role adminRole = roleRepository.findByName("ROLE_ADMIN");
        Role trainerRole = roleRepository.findByName("ROLE_TRAINER");
        Role memberRole = roleRepository.findByName("ROLE_MEMBER");

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

    private void initializeRooms() {
        if (roomRepository.count() > 0) {
            return;
        }

        Room mainGym = new Room("Main Gym", 50, "Mirrors, Air conditioning, Sound system, Rubber flooring", true);
        roomRepository.save(mainGym);

        Room cardioRoom = new Room("Cardio Room", 30, "Treadmills area, Bikes area, TV screens, Water dispensers", true);
        roomRepository.save(cardioRoom);

        Room weightRoom = new Room("Weight Room", 25, "Mirrors, Heavy-duty rubber flooring, Chalk station", true);
        roomRepository.save(weightRoom);

        Room yogaStudio = new Room("Yoga Studio", 20, "Mirrors, Wooden flooring, Sound system, Ambient lighting", true);
        roomRepository.save(yogaStudio);

        Room storage = new Room("Equipment Storage", 0, "Climate controlled, Shelving units, Maintenance area", true);
        roomRepository.save(storage);
    }
}
