package de.oth.muskelmanagement.service.impl;

import de.oth.muskelmanagement.dto.ReportDataDto;
import de.oth.muskelmanagement.model.entity.Course;
import de.oth.muskelmanagement.model.entity.Report;
import de.oth.muskelmanagement.model.entity.User;
import de.oth.muskelmanagement.model.enums.ReportType;
import de.oth.muskelmanagement.repository.*;
import de.oth.muskelmanagement.service.ReportService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;

    public ReportServiceImpl(ReportRepository reportRepository, CourseRepository courseRepository,
            EnrollmentRepository enrollmentRepository, UserRepository userRepository,
            SubscriptionRepository subscriptionRepository) {
        this.reportRepository = reportRepository;
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.userRepository = userRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    @Override
    public Page<Report> findAll(Pageable pageable) {
        return reportRepository.findAll(pageable);
    }

    @Override
    public Report findById(Long id) {
        return reportRepository.findById(id).orElseThrow(() -> new RuntimeException("Report not found"));
    }

    @Override
    public Report save(Report report) {
        return reportRepository.save(report);
    }

    @Override
    public void delete(Long id) {
        reportRepository.deleteById(id);
    }

    @Override
    public ReportDataDto generateReportData(Report report) {
        if (report.getType() == ReportType.COURSE_UTILIZATION) {
            return generateCourseUtilizationData(report);
        } else if (report.getType() == ReportType.MEMBER_STATISTICS) {
            return generateMemberStatisticsData(report);
        } else if (report.getType() == ReportType.TRAINER_POPULARITY) {
            return generateTrainerPopularityData(report);
        } else if (report.getType() == ReportType.MEMBER_GROWTH) {
            return generateMemberGrowthData(report);
        } else if (report.getType() == ReportType.POPULAR_COURSES) {
            return generatePopularCoursesData(report);
        }
        return new ReportDataDto(List.of(), List.of(), "No Data");
    }

    private ReportDataDto generateTrainerPopularityData(Report report) {
        List<String> labels = new ArrayList<>();
        List<Number> data = new ArrayList<>();

        List<Course> courses = courseRepository.findAll();
        Map<User, Long> trainerEnrollments = new HashMap<>();

        for (Course c : courses) {
            if (c.getTrainer() != null) {
                long count = enrollmentRepository.countByCourseAndStatus(c,
                        de.oth.muskelmanagement.model.enums.EnrollmentStatus.CONFIRMED);
                trainerEnrollments.put(c.getTrainer(), trainerEnrollments.getOrDefault(c.getTrainer(), 0L) + count);
            }
        }

        // Sort by count descending
        var sorted = trainerEnrollments.entrySet().stream()
                .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue())).collect(Collectors.toList());

        for (var entry : sorted) {
            labels.add(entry.getKey().getFirstName() + " " + entry.getKey().getLastName());
            data.add(entry.getValue());
        }

        return new ReportDataDto(labels, data, "Total Students Taught");
    }

    private ReportDataDto generateMemberGrowthData(Report report) {
        List<String> labels = new ArrayList<>();
        List<Number> data = new ArrayList<>();

        List<User> users = userRepository.findAll();
        Map<String, Long> growthByMonth = new TreeMap<>(); // TreeMap to keep sorted by key (YYYY-MM)

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");

        for (User u : users) {
            if (u.getCreatedAt() != null) {
                String monthKey = u.getCreatedAt().format(formatter);
                growthByMonth.put(monthKey, growthByMonth.getOrDefault(monthKey, 0L) + 1);
            }
        }

        // Convert to cumulative or just monthly? User asked for growth, monthly registration count is good.
        // Let's do monthly registrations.

        for (Map.Entry<String, Long> entry : growthByMonth.entrySet()) {
            labels.add(entry.getKey());
            data.add(entry.getValue());
        }

        return new ReportDataDto(labels, data, "New Members per Month");
    }

    // ... (other methods)

    private ReportDataDto generatePopularCoursesData(Report report) {
        // Top 5 most enrolled courses
        List<String> labels = new ArrayList<>();
        List<Number> data = new ArrayList<>();

        List<Course> courses = courseRepository.findAll();
        Map<Course, Long> enrollmentCounts = new HashMap<>();

        for (Course c : courses) {
            long count = enrollmentRepository.countByCourseAndStatus(c,
                    de.oth.muskelmanagement.model.enums.EnrollmentStatus.CONFIRMED);
            enrollmentCounts.put(c, count);
        }

        // Sort by count descending and limit to 5
        var sorted = enrollmentCounts.entrySet().stream().sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
                .limit(5).collect(Collectors.toList());

        for (var entry : sorted) {
            labels.add(entry.getKey().getName());
            data.add(entry.getValue());
        }

        return new ReportDataDto(labels, data, "Top 5 Popular Courses");
    }

    private ReportDataDto generateCourseUtilizationData(Report report) {
        // If a specific course is selected, show detailed history (mocked mostly as we don't have history table yet)
        // If no course is selected, show comparison between all courses

        List<String> labels = new ArrayList<>();
        List<Number> data = new ArrayList<>();

        if (report.getCourse() != null) {
            // Specific course detail
            labels.add("Capacity");
            labels.add("Enrolled");
            labels.add("Waitlist");

            Course c = report.getCourse();
            long enrolled = enrollmentRepository.countByCourseAndStatus(c,
                    de.oth.muskelmanagement.model.enums.EnrollmentStatus.CONFIRMED);
            long waitlisted = enrollmentRepository.countByCourseAndStatus(c,
                    de.oth.muskelmanagement.model.enums.EnrollmentStatus.WAITLISTED);

            data.add(c.getCapacity());
            data.add(enrolled);
            data.add(waitlisted);

            return new ReportDataDto(labels, data, "Utilization for " + c.getName());
        } else {
            // All courses comparison
            List<Course> courses = courseRepository.findAll();
            for (Course c : courses) {
                if (!c.isActive())
                    continue;
                labels.add(c.getName());
                // Calculate percentage
                long enrolled = enrollmentRepository.countByCourseAndStatus(c,
                        de.oth.muskelmanagement.model.enums.EnrollmentStatus.CONFIRMED);
                double utilization = (double) enrolled / c.getCapacity() * 100;
                data.add(utilization);
            }
            return new ReportDataDto(labels, data, "Course Utilization (%)");
        }
    }

    private ReportDataDto generateMemberStatisticsData(Report report) {
        List<String> labels = new ArrayList<>();
        List<Number> data = new ArrayList<>();

        // Simple breakdown by role
        labels.add("Admins");
        labels.add("Trainers");
        labels.add("Members");

        List<User> users = userRepository.findAll();
        long admins = users.stream().filter(u -> u.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_ADMIN")))
                .count();
        long trainers = users.stream()
                .filter(u -> u.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_TRAINER"))).count();
        long members = users.stream()
                .filter(u -> u.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_MEMBER"))).count();

        data.add(admins);
        data.add(trainers);
        data.add(members);

        return new ReportDataDto(labels, data, "User Distribution");
    }
}
