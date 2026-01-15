package de.oth.muskelmanagement.config;

import de.oth.muskelmanagement.model.entity.*;
import de.oth.muskelmanagement.model.enums.EquipmentCategory;
import de.oth.muskelmanagement.model.enums.EquipmentStatus;
import de.oth.muskelmanagement.model.enums.ReportType;
import de.oth.muskelmanagement.model.enums.SubscriptionStatus;
import de.oth.muskelmanagement.repository.*;
import de.oth.muskelmanagement.service.ExerciseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RoomRepository roomRepository;
    private final PricingRepository pricingRepository;
    private final EquipmentRepository equipmentRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final de.oth.muskelmanagement.repository.CourseRepository courseRepository;
    private final de.oth.muskelmanagement.repository.EnrollmentRepository enrollmentRepository;
    private final ExerciseRepository exerciseRepository;
    private final ExerciseService exerciseService;
    private final PasswordEncoder passwordEncoder;
    private final TrainingPlanRepository trainingPlanRepository;
    private final FitnessMeasurementRepository fitnessMeasurementRepository;
    private final ReviewRepository reviewRepository;
    private final ReportRepository reportRepository;

    public DataInitializer(UserRepository userRepository, RoleRepository roleRepository, RoomRepository roomRepository,
            PricingRepository pricingRepository,
            EquipmentRepository equipmentRepository, SubscriptionRepository subscriptionRepository,
            de.oth.muskelmanagement.repository.CourseRepository courseRepository,
            de.oth.muskelmanagement.repository.EnrollmentRepository enrollmentRepository,
            ExerciseRepository exerciseRepository, ExerciseService exerciseService, PasswordEncoder passwordEncoder,
            TrainingPlanRepository trainingPlanRepository, FitnessMeasurementRepository fitnessMeasurementRepository,
            ReviewRepository reviewRepository, ReportRepository reportRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.roomRepository = roomRepository;
        this.pricingRepository = pricingRepository;
        this.equipmentRepository = equipmentRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.exerciseRepository = exerciseRepository;
        this.exerciseService = exerciseService;
        this.passwordEncoder = passwordEncoder;
        this.trainingPlanRepository = trainingPlanRepository;
        this.fitnessMeasurementRepository = fitnessMeasurementRepository;
        this.reviewRepository = reviewRepository;
        this.reportRepository = reportRepository;
    }

    @Override
    public void run(String... args) {
        initializeRoles();
        initializePricings();
        initializeUsers();
        initializeRooms();
        initializeEquipment();
        initializeCourses();
        initializeExercises();
        initializeSubscriptions();
        initializeTrainingPlans();
        initializeFitnessMeasurements();
        initializeReviews();
        initializeReports();
    }

    private void initializeReports() {
        if (reportRepository.count() > 0) {
            return;
        }

        reportRepository.save(new Report("Trainer Popularity", ReportType.TRAINER_POPULARITY, null, null));
        reportRepository.save(new Report("Member Growth", ReportType.MEMBER_GROWTH, null, null));
        reportRepository.save(new Report("Course Utilization Overview", ReportType.COURSE_UTILIZATION, null, null));
        reportRepository.save(new Report("Top 5 Popular Courses", ReportType.POPULAR_COURSES, null, null));
    }

    private void initializeReviews() {
        if (reviewRepository.count() > 0) {
            return;
        }

        User member = userRepository.findByEmail("member@example.com");
        Course yoga = courseRepository.findByName("Yoga Basics").stream().findFirst().orElse(null);
        Course hiit = courseRepository.findByName("HIIT Cardio").stream().findFirst().orElse(null);

        if (member != null) {
            if (yoga != null) {
                reviewRepository.save(
                        new Review(yoga, member, 5, "Absolutely loved this course! Very relaxing but effective."));
                reviewRepository.save(new Review(yoga, member, 4, "Great instructor, but the room was a bit cold."));
            }
            if (hiit != null) {
                reviewRepository.save(new Review(hiit, member, 5, "Intense workout! Exactly what I needed."));
                reviewRepository.save(new Review(hiit, member, 3, "Good exercises, but too fast-paced for beginners."));
            }
        }
    }

    private void initializePricings() {
        if (pricingRepository.count() > 0) {
            return;
        }

        Pricing basic = new Pricing("Basic", new BigDecimal("29.99"), 1, "Monthly membership with basic access", true);
        basic.setStripePriceId("price_1SpaSkKgAYD85AnvmqaYgHct");
        pricingRepository.save(basic);

        Pricing premium = new Pricing("Premium", new BigDecimal("59.99"), 12,
                "Annual membership with full access and additional services", true);
        premium.setStripePriceId("price_1SpafYKgAYD85AnvecAZOZc6");
        pricingRepository.save(premium);

        Pricing student = new Pricing("Student", new BigDecimal("19.99"), 1,
                "Discounted monthly membership for students", true);
        student.setStripePriceId("price_1SpadxKgAYD85Anv0XCU0OXc");
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
                LocalDate.now().minusMonths(3), "Technogym", EquipmentCategory.CARDIO, 90,
                LocalDate.now().minusMonths(1));
        equipmentRepository.save(treadmill1);

        Equipment treadmill2 = new Equipment("Treadmill", "TREAD-002", EquipmentStatus.AVAILABLE, cardioRoom,
                LocalDate.now().minusMonths(2), "Life Fitness", EquipmentCategory.CARDIO, 90,
                LocalDate.now().minusMonths(1));
        equipmentRepository.save(treadmill2);

        Equipment bike1 = new Equipment("Exercise Bike", "BIKE-001", EquipmentStatus.AVAILABLE, cardioRoom,
                LocalDate.now().minusMonths(1), "Peloton", EquipmentCategory.CARDIO, 120,
                LocalDate.now().minusWeeks(2));
        equipmentRepository.save(bike1);

        Equipment elliptical1 = new Equipment("Elliptical Machine", "ELLI-001", EquipmentStatus.AVAILABLE, cardioRoom,
                LocalDate.now().minusMonths(4), "Concept2", EquipmentCategory.CARDIO, 90,
                LocalDate.now().minusMonths(2));
        equipmentRepository.save(elliptical1);

        // Strength equipment for Weight Room
        Equipment bench1 = new Equipment("Flat Bench", "BENCH-01", EquipmentStatus.AVAILABLE, weightRoom,
                LocalDate.now().minusMonths(6), "Hammer Strength", EquipmentCategory.STRENGTH, 180,
                LocalDate.now().minusMonths(3));
        equipmentRepository.save(bench1);

        Equipment squatRack1 = new Equipment("Squat Rack", "SQUAT-001", EquipmentStatus.AVAILABLE, weightRoom,
                LocalDate.now().minusMonths(5), "Rogue Fitness", EquipmentCategory.STRENGTH, 180,
                LocalDate.now().minusMonths(2));
        equipmentRepository.save(squatRack1);

        // Free weights for Main Gym
        Equipment dumbbellSet1 = new Equipment("Dumbbell Set 2.5-25kg", "DUMB-001", EquipmentStatus.AVAILABLE, mainGym,
                LocalDate.now().minusMonths(2), "Multipower", EquipmentCategory.FREE_WEIGHTS, 180,
                LocalDate.now().minusMonths(1));
        equipmentRepository.save(dumbbellSet1);

        Equipment barbell1 = new Equipment("Barbell", "BARB-001", EquipmentStatus.AVAILABLE, mainGym,
                LocalDate.now().minusMonths(1), "Eleiko", EquipmentCategory.FREE_WEIGHTS, 180,
                LocalDate.now().minusWeeks(3));
        equipmentRepository.save(barbell1);

        // Additional equipment in storage as backup
        Equipment repairBike = new Equipment("Exercise Bike", "BIKE-002", EquipmentStatus.IN_MAINTENANCE, storage,
                LocalDate.now().minusMonths(12), "Schwinn", EquipmentCategory.CARDIO, 90,
                LocalDate.now().minusMonths(6));
        equipmentRepository.save(repairBike);

        Equipment oldTreadmill = new Equipment("Treadmill", "TREAD-003", EquipmentStatus.DEFECTIVE, storage,
                LocalDate.now().minusMonths(24), "ProForm", EquipmentCategory.CARDIO, 90,
                LocalDate.now().minusMonths(12));
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
                "Yoga Basics", "A gentle introduction to yoga focusing on breath and basic poses.", 20, true,
                LocalTime.of(9, 0), 60);
        if (trainer != null) {
            yoga.setTrainer(trainer);
        }
        roomRepository.findByName("Yoga Studio").ifPresent(r -> yoga.setRoom(r));
        yoga.setStartDate(LocalDate.now().minusWeeks(4));
        yoga.setEndDate(LocalDate.now().plusWeeks(8));
        yoga.setDaysOfWeek(Set.of(de.oth.muskelmanagement.model.enums.DayOfWeek.MONDAY,
                de.oth.muskelmanagement.model.enums.DayOfWeek.WEDNESDAY));
        courseRepository.save(yoga);

        de.oth.muskelmanagement.model.entity.Course hiit = new de.oth.muskelmanagement.model.entity.Course(
                "HIIT Cardio", "High intensity interval training to boost your cardio fitness.", 15, true,
                LocalTime.of(18, 0), 45);
        if (trainer != null) {
            hiit.setTrainer(trainer);
        }
        roomRepository.findByName("Cardio Room").ifPresent(r -> hiit.setRoom(r));
        hiit.setStartDate(LocalDate.now().minusWeeks(2));
        hiit.setEndDate(LocalDate.now().plusWeeks(10));
        hiit.setDaysOfWeek(Set.of(de.oth.muskelmanagement.model.enums.DayOfWeek.TUESDAY,
                de.oth.muskelmanagement.model.enums.DayOfWeek.THURSDAY,
                de.oth.muskelmanagement.model.enums.DayOfWeek.SATURDAY));
        courseRepository.save(hiit);

        de.oth.muskelmanagement.model.entity.Course strength = new de.oth.muskelmanagement.model.entity.Course(
                "Strength Training", "Build muscle and increase strength with progressive overload.", 10, true,
                LocalTime.of(17, 0), 75);
        if (trainer != null) {
            strength.setTrainer(trainer);
        }
        roomRepository.findByName("Weight Room").ifPresent(r -> strength.setRoom(r));
        strength.setStartDate(LocalDate.now().minusWeeks(1));
        strength.setEndDate(LocalDate.now().plusWeeks(11));
        strength.setDaysOfWeek(Set.of(de.oth.muskelmanagement.model.enums.DayOfWeek.MONDAY,
                de.oth.muskelmanagement.model.enums.DayOfWeek.WEDNESDAY,
                de.oth.muskelmanagement.model.enums.DayOfWeek.FRIDAY));
        courseRepository.save(strength);

        de.oth.muskelmanagement.model.entity.Course pelvicFloor = new de.oth.muskelmanagement.model.entity.Course(
                "Pelvic Floor Training", "Pelvic floor training, doesn't have to taste good but has to work.", 2, true,
                LocalTime.of(10, 0), 90);
        if (trainer != null) {
            pelvicFloor.setTrainer(trainer);
        }
        pelvicFloor.setStartDate(LocalDate.now().plusWeeks(1));
        pelvicFloor.setEndDate(LocalDate.now().plusWeeks(3));
        pelvicFloor.setDaysOfWeek(Set.of(de.oth.muskelmanagement.model.enums.DayOfWeek.SUNDAY));
        courseRepository.save(pelvicFloor);

        // Create Test Course with Admin as Trainer and 12 members
        de.oth.muskelmanagement.model.entity.User admin = userRepository.findByEmail("admin@example.com");
        de.oth.muskelmanagement.model.entity.Course testCourse = new de.oth.muskelmanagement.model.entity.Course(
                "Test Course - Pagination", 
                "This is a test course to demonstrate pagination with 12 enrolled members.", 
                15, 
                true,
                LocalTime.of(14, 0), 
                60);
        if (admin != null) {
            testCourse.setTrainer(admin);
        }
        roomRepository.findByName("Main Gym").ifPresent(r -> testCourse.setRoom(r));
        testCourse.setStartDate(LocalDate.now());
        testCourse.setEndDate(LocalDate.now().plusWeeks(8));
        testCourse.setDaysOfWeek(Set.of(de.oth.muskelmanagement.model.enums.DayOfWeek.MONDAY,
                de.oth.muskelmanagement.model.enums.DayOfWeek.WEDNESDAY,
                de.oth.muskelmanagement.model.enums.DayOfWeek.FRIDAY));
        courseRepository.save(testCourse);

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

            // Enroll 12 test members in the Test Course
            for (int i = 1; i <= 12; i++) {
                de.oth.muskelmanagement.model.entity.User testMember = userRepository.findByEmail("testmember" + i + "@example.com");
                if (testMember != null && testCourse != null) {
                    var enrollment = new de.oth.muskelmanagement.model.entity.Enrollment(testMember, testCourse);
                    enrollment.setStatus(de.oth.muskelmanagement.model.enums.EnrollmentStatus.CONFIRMED);
                    enrollmentRepository.save(enrollment);
                    testCourse.addEnrollment(enrollment);
                }
            }
            courseRepository.save(testCourse);

        } catch (Exception ex) {
            // ignore seeding enrollment errors
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

        User admin = new User("Admin", "User", "admin@example.com", passwordEncoder.encode("password"),
                Set.of(adminRole, trainerRole, memberRole));
        userRepository.save(admin);

        User trainer = new User("Trainer", "User", "trainer@example.com", passwordEncoder.encode("password"),
                Set.of(trainerRole, memberRole));
        userRepository.save(trainer);

        User member = new User("Member", "User", "member@example.com", passwordEncoder.encode("password"),
                Set.of(memberRole));
        userRepository.save(member);

        // Create 12 additional test members for pagination testing
        for (int i = 1; i <= 12; i++) {
            User testMember = new User("TestMember" + i, "User" + i, 
                    "testmember" + i + "@example.com", 
                    passwordEncoder.encode("password"),
                    Set.of(memberRole));
            userRepository.save(testMember);
        }
    }

    @Transactional
    protected void initializeRooms() {
        createRoomIfNotFound("Main Gym", 50, "Mirrors, Air conditioning, Sound system, Rubber flooring", true);
        createRoomIfNotFound("Cardio Room", 30, "Treadmills area, Bikes area, TV screens, Water dispensers", true);
        createRoomIfNotFound("Weight Room", 25, "Mirrors, Heavy-duty rubber flooring, Chalk station", true);
        createRoomIfNotFound("Yoga Studio", 20, "Mirrors, Wooden flooring, Sound system, Ambient lighting", true);
        createRoomIfNotFound("Equipment Storage", 0, "Climate controlled, Shelving units, Maintenance area", true);
        createRoomIfNotFound("Outdoor", 100, "Open air field, Natural light, Fresh air", true);
    }

    private void createRoomIfNotFound(String name, Integer capacity, String amenities, boolean active) {
        if (roomRepository.findByName(name).isEmpty()) {
            Room room = new Room(name, capacity, amenities, active);
            roomRepository.save(room);
        }
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

    private void initializeFitnessMeasurements() {
        if (fitnessMeasurementRepository.count() > 0) {
            return;
        }

        User member = userRepository.findByEmail("member@example.com");
        if (member != null) {
            // -6 months
            fitnessMeasurementRepository.save(
                    new FitnessMeasurement(member, LocalDate.now().minusMonths(6), 90.0, 25.0, 38.0,
                            "Initial assessment. Goal: Weight loss and muscle gain."));
            // -5 months
            fitnessMeasurementRepository.save(
                    new FitnessMeasurement(member, LocalDate.now().minusMonths(5), 88.5, 24.2, 38.5,
                            "Good start, diet adherence is high."));
            // -4 months
            fitnessMeasurementRepository.save(
                    new FitnessMeasurement(member, LocalDate.now().minusMonths(4), 87.2, 23.5, 39.0,
                            "Strength increasing, weight dropping steadily."));
            // -3 months
            fitnessMeasurementRepository.save(
                    new FitnessMeasurement(member, LocalDate.now().minusMonths(3), 86.0, 22.8, 39.5,
                            "Halfway check-in. Adjusting macro split."));
            // -2 months
            fitnessMeasurementRepository.save(
                    new FitnessMeasurement(member, LocalDate.now().minusMonths(2), 84.8, 21.5, 40.2,
                            "Visible definition appearing."));
            // -1 month
            fitnessMeasurementRepository.save(
                    new FitnessMeasurement(member, LocalDate.now().minusMonths(1), 83.5, 20.5, 41.0,
                            "Excellent progress. Increased cardio intensity."));
            // Current
            fitnessMeasurementRepository.save(new FitnessMeasurement(member, LocalDate.now(), 82.0, 19.5, 41.5,
                    "Hit target weight for this phase. Moving to maintenance."));
        }
    }
}
