package de.oth.muskelmanagement.config;

import de.oth.muskelmanagement.model.entity.*;
import de.oth.muskelmanagement.model.enums.EquipmentCategory;
import de.oth.muskelmanagement.model.enums.EquipmentStatus;
import de.oth.muskelmanagement.model.enums.SubscriptionStatus;
import de.oth.muskelmanagement.repository.*;
import de.oth.muskelmanagement.service.ExerciseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RoomRepository roomRepository;
    private final MembershipRepository membershipRepository;
    private final PricingRepository pricingRepository;
    private final EquipmentRepository equipmentRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final de.oth.muskelmanagement.repository.CourseRepository courseRepository;
    private final de.oth.muskelmanagement.repository.EnrollmentRepository enrollmentRepository;
    private final ExerciseRepository exerciseRepository;
    private final ExerciseService exerciseService;
    private final PasswordEncoder passwordEncoder;
    private final TrainingPlanRepository trainingPlanRepository;

    public DataInitializer(UserRepository userRepository, RoleRepository roleRepository,
            RoomRepository roomRepository, MembershipRepository membershipRepository,
            PricingRepository pricingRepository, EquipmentRepository equipmentRepository,
            SubscriptionRepository subscriptionRepository,
            de.oth.muskelmanagement.repository.CourseRepository courseRepository,
            de.oth.muskelmanagement.repository.EnrollmentRepository enrollmentRepository,
            ExerciseRepository exerciseRepository, ExerciseService exerciseService, PasswordEncoder passwordEncoder,
            TrainingPlanRepository trainingPlanRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.roomRepository = roomRepository;
        this.membershipRepository = membershipRepository;
        this.pricingRepository = pricingRepository;
        this.equipmentRepository = equipmentRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.exerciseRepository = exerciseRepository;
        this.exerciseService = exerciseService;
        this.passwordEncoder = passwordEncoder;
        this.trainingPlanRepository = trainingPlanRepository;
    }

    @Override
    public void run(String... args) {
        initializeRoles();
        initializeMemberships();
        initializePricings();
        initializeUsers();
        migrateUsersToMemberships();
        initializeRooms();
        initializeEquipment();
        initializeCourses();
        initializeExercises();
        initializeSubscriptions();
        initializeTrainingPlans();
    }

    private void initializeTrainingPlans() {
        if (trainingPlanRepository.count() > 0) {
            return;
        }

        User trainer = userRepository.findByEmail("trainer@example.com");
        User member = userRepository.findByEmail("member@example.com");
        var exercises = exerciseRepository.findAll();

        if (trainer != null && member != null && !exercises.isEmpty()) {
            TrainingPlan beginnerPlan = new TrainingPlan("Beginner Strength",
                    "A foundational plan to build muscle and learn proper form.", member, trainer);

            // Add up to 3 exercises
            int count = 0;
            for (Exercise ex : exercises) {
                if (count >= 3)
                    break;
                TrainingPlanExercise tpe = new TrainingPlanExercise();
                tpe.setExercise(ex);
                tpe.setSets(3);
                tpe.setReps("8-12");
                tpe.setNotes("Focus on form.");
                tpe.setOrderIndex(count);
                beginnerPlan.addExercise(tpe);
                count++;
            }
            trainingPlanRepository.save(beginnerPlan);

            TrainingPlan cardioPlan = new TrainingPlan("Cardio Blast",
                    "Improve your endurance with this high-energy routine.", member, trainer);

            // Add next 2 exercises if available
            int start = count;
            for (int i = start; i < exercises.size(); i++) {
                if (i >= start + 2)
                    break;
                Exercise ex = exercises.get(i);
                TrainingPlanExercise tpe = new TrainingPlanExercise();
                tpe.setExercise(ex);
                tpe.setSets(4);
                tpe.setReps("15-20");
                tpe.setNotes("Keep heart rate up.");
                tpe.setOrderIndex(i - start);
                cardioPlan.addExercise(tpe);
            }
            trainingPlanRepository.save(cardioPlan);
        }
    }

    private void initializePricings() {
        if (pricingRepository.count() > 0) {
            return;
        }

        Pricing basic = new Pricing("Basic", new BigDecimal("29.99"), 1, "Monthly membership with basic access", true);
        pricingRepository.save(basic);

        Pricing premium = new Pricing("Premium", new BigDecimal("59.99"), 12, "Annual membership with full access and additional services", true);
        pricingRepository.save(premium);

        Pricing student = new Pricing("Student", new BigDecimal("19.99"), 1, "Discounted monthly membership for students", true);
        pricingRepository.save(student);
    }

    private void initializeEquipment() {
        if (equipmentRepository.count() > 0) {
            return;
        }

        // Get rooms if they exist
        Room cardioRoom = roomRepository.findByName("Cardio Room").orElse(null);
        Room weightRoom = roomRepository.findByName("Weight Room").orElse(null);
        Room mainGym = roomRepository.findByName("Main Gym").orElse(null);
        Room storage = roomRepository.findByName("Equipment Storage").orElse(null);

        // Cardio equipment for Cardio Room
        Equipment treadmill1 = new Equipment("Treadmill", "TREAD-001", EquipmentStatus.AVAILABLE, cardioRoom,
                LocalDate.now().minusMonths(3), "Technogym", EquipmentCategory.CARDIO, 90, LocalDate.now().minusMonths(1));
        equipmentRepository.save(treadmill1);

        Equipment treadmill2 = new Equipment("Treadmill", "TREAD-002", EquipmentStatus.AVAILABLE, cardioRoom,
                LocalDate.now().minusMonths(2), "Life Fitness", EquipmentCategory.CARDIO, 90, LocalDate.now().minusMonths(1));
        equipmentRepository.save(treadmill2);

        Equipment bike1 = new Equipment("Exercise Bike", "BIKE-001", EquipmentStatus.AVAILABLE, cardioRoom,
                LocalDate.now().minusMonths(1), "Peloton", EquipmentCategory.CARDIO, 120, LocalDate.now().minusWeeks(2));
        equipmentRepository.save(bike1);

        Equipment elliptical1 = new Equipment("Elliptical Machine", "ELLI-001", EquipmentStatus.AVAILABLE, cardioRoom,
                LocalDate.now().minusMonths(4), "Concept2", EquipmentCategory.CARDIO, 90, LocalDate.now().minusMonths(2));
        equipmentRepository.save(elliptical1);

        // Strength equipment for Weight Room
        Equipment bench1 = new Equipment("Flat Bench", "BENCH-01", EquipmentStatus.AVAILABLE, weightRoom,
                LocalDate.now().minusMonths(6), "Hammer Strength", EquipmentCategory.STRENGTH, 180, LocalDate.now().minusMonths(3));
        equipmentRepository.save(bench1);

        Equipment squatRack1 = new Equipment("Squat Rack", "SQUAT-001", EquipmentStatus.AVAILABLE, weightRoom,
                LocalDate.now().minusMonths(5), "Rogue Fitness", EquipmentCategory.STRENGTH, 180, LocalDate.now().minusMonths(2));
        equipmentRepository.save(squatRack1);

        // Free weights for Main Gym
        Equipment dumbbellSet1 = new Equipment("Dumbbell Set 2.5-25kg", "DUMB-001", EquipmentStatus.AVAILABLE, mainGym,
                LocalDate.now().minusMonths(2), "Multipower", EquipmentCategory.FREE_WEIGHTS, 180, LocalDate.now().minusMonths(1));
        equipmentRepository.save(dumbbellSet1);

        Equipment barbell1 = new Equipment("Barbell", "BARB-001", EquipmentStatus.AVAILABLE, mainGym,
                LocalDate.now().minusMonths(1), "Eleiko", EquipmentCategory.FREE_WEIGHTS, 180, LocalDate.now().minusWeeks(3));
        equipmentRepository.save(barbell1);

        // Additional equipment in storage as backup
        Equipment repairBike = new Equipment("Exercise Bike", "BIKE-002", EquipmentStatus.IN_MAINTENANCE, storage,
                LocalDate.now().minusMonths(12), "Schwinn", EquipmentCategory.CARDIO, 90, LocalDate.now().minusMonths(6));
        equipmentRepository.save(repairBike);

        Equipment oldTreadmill = new Equipment("Treadmill", "TREAD-003", EquipmentStatus.DEFECTIVE, storage,
                LocalDate.now().minusMonths(24), "ProForm", EquipmentCategory.CARDIO, 90, LocalDate.now().minusMonths(12));
        equipmentRepository.save(oldTreadmill);
    }

    private void initializeSubscriptions() {
        if (subscriptionRepository.count() > 0) {
            return;
        }

        // Find the member user and basic pricing
        User member = userRepository.findByEmail("member@example.com");
        Pricing basicPricing = pricingRepository.findByName("Basic");

        if (member != null && basicPricing != null) {
            Subscription subscription = new Subscription();
            subscription.setUser(member);
            subscription.setPricing(basicPricing);
            subscription.setStartDate(LocalDate.now());
            subscription.setEndDate(LocalDate.now().plusMonths(basicPricing.getDurationMonths()));
            subscription.setStatus(SubscriptionStatus.ACTIVE);
            subscription.setAutoRenew(false);
            subscriptionRepository.save(subscription);
        }
    }

    private void initializeCourses() {
        if (courseRepository.count() > 0) {
            return;
        }

        // Get trainer user if exists
        de.oth.muskelmanagement.model.entity.User trainer = userRepository.findByEmail("trainer@example.com");

        de.oth.muskelmanagement.model.entity.Course yoga = new de.oth.muskelmanagement.model.entity.Course(
                "Yoga Basics", "A gentle introduction to yoga focusing on breath and basic poses.", 20, true);
        if (trainer != null) {
            yoga.setTrainer(trainer);
        }
        // assign yoga studio if available
        roomRepository.findByName("Yoga Studio").ifPresent(r -> yoga.setRoom(r));
        courseRepository.save(yoga);

        de.oth.muskelmanagement.model.entity.Course hiit = new de.oth.muskelmanagement.model.entity.Course(
                "HIIT Cardio", "High intensity interval training to boost your cardio fitness.", 15, true);
        if (trainer != null) {
            hiit.setTrainer(trainer);
        }
        // assign cardio room if available
        roomRepository.findByName("Cardio Room").ifPresent(r -> hiit.setRoom(r));
        courseRepository.save(hiit);

        de.oth.muskelmanagement.model.entity.Course pelvicFloor = new de.oth.muskelmanagement.model.entity.Course(
                "Pelvic Floor Training", "Pelvic floor training, doesn't have to taste good but has to work.", 2, true);
        courseRepository.save(pelvicFloor);

        // Optionally enroll seeded users
        try {
            de.oth.muskelmanagement.model.entity.User member = userRepository.findByEmail("member@example.com");
            if (member != null) {
                var e1 = new de.oth.muskelmanagement.model.entity.Enrollment(member, yoga);
                enrollmentRepository.save(e1);
                yoga.addEnrollment(e1);
                courseRepository.save(yoga);
            }
            if (trainer != null) {
                var e2 = new de.oth.muskelmanagement.model.entity.Enrollment(trainer, hiit);
                enrollmentRepository.save(e2);
                hiit.addEnrollment(e2);
                courseRepository.save(hiit);
            }
        } catch (Exception ex) {
            // ignore seeding enrollment errors
        }
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

    private void initializeExercises() {
        if (exerciseRepository.count() > 0) {
            log.info("Exercises already initialized. Skipping exercise loading.");
            return;
        }

        log.info("Loading exercises from local JSON file (data/exercises.json)...");

        try {
            int loadedCount = exerciseService.loadExercisesFromJson();
            
            if (loadedCount > 0) {
                log.info("Successfully loaded {} exercises from JSON file", loadedCount);
            } else {
                log.warn("No exercises were loaded from JSON file.");
                log.warn("Please ensure data/exercises.json exists and contains valid exercise data.");
            }
        } catch (Exception e) {
            log.error("Error loading exercises from JSON: {}", e.getMessage());
            log.warn("Exercise initialization failed.");
            log.warn("The application will continue to run, but the exercise library will be empty.");
        }
    }
}
