package de.oth.muskelmanagement.config;

import de.oth.muskelmanagement.model.Role;
import de.oth.muskelmanagement.model.Room;
import de.oth.muskelmanagement.model.User;
import de.oth.muskelmanagement.model.Membership;
import de.oth.muskelmanagement.repository.RoleRepository;
import de.oth.muskelmanagement.repository.RoomRepository;
import de.oth.muskelmanagement.repository.UserRepository;
import de.oth.muskelmanagement.repository.MembershipRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RoomRepository roomRepository;
    private final MembershipRepository membershipRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, RoleRepository roleRepository,
            RoomRepository roomRepository, MembershipRepository membershipRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.roomRepository = roomRepository;
        this.membershipRepository = membershipRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        initializeRoles();
        initializeMemberships();
        initializeUsers();
        migrateUsersToMemberships();
        initializeRooms();
    }

    private void initializeMemberships() {
        if (membershipRepository.count() > 0) {
            return;
        }

        Membership basic = new Membership("Basic");
        basic.setDurationMonths(1);
        basic.setPrice(19.99);
        basic.setDescription("Basic membership");
        membershipRepository.save(basic);

        Membership premium = new Membership("Premium");
        premium.setDurationMonths(12);
        premium.setPrice(199.99);
        premium.setDescription("Premium membership with full access");
        membershipRepository.save(premium);

        Membership trainer = new Membership("Trainer");
        trainer.setDurationMonths(12);
        trainer.setPrice(0.0);
        trainer.setDescription("Trainer internal membership");
        membershipRepository.save(trainer);
    }

    private void migrateUsersToMemberships() {
        Iterable<User> users = userRepository.findAll();
        for (User u : users) {
            if (u.getMembership() == null && u.getMembershipType() != null) {
                String mt = u.getMembershipType();
                Membership m = membershipRepository.findByName(mt);
                if (m == null) {
                    m = new Membership(mt);
                    membershipRepository.save(m);
                }
                u.setMembership(m);
                userRepository.save(u);
            }
        }
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
