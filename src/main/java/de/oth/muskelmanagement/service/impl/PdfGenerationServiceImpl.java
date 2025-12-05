package de.oth.muskelmanagement.service.impl;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import de.oth.muskelmanagement.model.entity.MembershipConfirmation;
import de.oth.muskelmanagement.model.entity.Subscription;
import de.oth.muskelmanagement.model.entity.User;
import de.oth.muskelmanagement.model.enums.SubscriptionStatus;
import de.oth.muskelmanagement.repository.MembershipConfirmationRepository;
import de.oth.muskelmanagement.repository.SubscriptionRepository;
import de.oth.muskelmanagement.repository.UserRepository;
import de.oth.muskelmanagement.service.PdfGenerationService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PdfGenerationServiceImpl implements PdfGenerationService {

    private final MembershipConfirmationRepository confirmationRepository;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final TemplateEngine templateEngine;

    @Value("${pdf.storage.path:pdf-confirmations}")
    private String pdfStoragePath;

    public PdfGenerationServiceImpl(MembershipConfirmationRepository confirmationRepository,
            UserRepository userRepository, SubscriptionRepository subscriptionRepository,
            TemplateEngine templateEngine) {
        this.confirmationRepository = confirmationRepository;
        this.userRepository = userRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.templateEngine = templateEngine;
    }

    @Override
    @Transactional
    public MembershipConfirmation generateMembershipConfirmation(Long userId, Long subscriptionId, String adminEmail) {
        // Validate user and subscription
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));

        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new EntityNotFoundException("Subscription not found with ID: " + subscriptionId));

        // Verify subscription belongs to user
        if (!subscription.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Subscription does not belong to the specified user");
        }

        // Verify subscription is active
        if (subscription.getStatus() != SubscriptionStatus.ACTIVE) {
            throw new IllegalStateException("Can only generate confirmations for active subscriptions");
        }

        try {
            // Prepare template data
            Map<String, Object> templateData = prepareTemplateData(user, subscription);

            // Render HTML from template
            String html = renderTemplate(templateData);

            // Convert HTML to PDF
            byte[] pdfBytes = convertHtmlToPdf(html);

            // Generate file name and save PDF
            String fileName = generateFileName(userId, subscriptionId);
            String filePath = savePdfToFileSystem(fileName, pdfBytes);

            // Create and save metadata
            MembershipConfirmation confirmation = new MembershipConfirmation(user, subscription, fileName, filePath,
                    adminEmail);

            return confirmationRepository.save(confirmation);

        } catch (IOException e) {
            throw new RuntimeException("Failed to generate PDF: " + e.getMessage(), e);
        }
    }

    @Override
    public List<MembershipConfirmation> getAllConfirmations() {
        return confirmationRepository.findByDeletedFalse();
    }

    @Override
    public List<MembershipConfirmation> getConfirmationsByUser(Long userId) {
        return confirmationRepository.findByUser_IdAndDeletedFalse(userId);
    }

    @Override
    public MembershipConfirmation getConfirmationById(Long id) {
        return confirmationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Membership confirmation not found with ID: " + id));
    }

    @Override
    public byte[] getPdfContent(Long confirmationId) throws IOException {
        MembershipConfirmation confirmation = getConfirmationById(confirmationId);

        if (confirmation.getDeleted()) {
            throw new IllegalStateException("Cannot retrieve deleted confirmation");
        }

        Path filePath = Paths.get(confirmation.getFilePath());

        if (!Files.exists(filePath)) {
            throw new IOException("PDF file not found: " + confirmation.getFilePath());
        }

        return Files.readAllBytes(filePath);
    }

    @Override
    @Transactional
    public void deleteConfirmation(Long id, String adminEmail) {
        MembershipConfirmation confirmation = getConfirmationById(id);

        confirmation.setDeleted(true);
        confirmation.setDeletedAt(LocalDateTime.now());
        confirmation.setDeletedBy(adminEmail);

        confirmationRepository.save(confirmation);
    }

    /**
     * Prepare data for the PDF template
     */
    private Map<String, Object> prepareTemplateData(User user, Subscription subscription) {
        Map<String, Object> data = new HashMap<>();
        data.put("membershipNumber", user.getId());
        data.put("firstName", user.getFirstName());
        data.put("lastName", user.getLastName());
        data.put("email", user.getEmail());
        data.put("startDate", subscription.getStartDate());
        data.put("endDate", subscription.getEndDate());
        data.put("membershipType", subscription.getTarif().getName());
        data.put("price", subscription.getTarif().getPrice());
        data.put("generatedDate", LocalDate.now());
        data.put("gymName", "MuskelManagement Fitness Studio");
        return data;
    }

    /**
     * Render the HTML template using Thymeleaf
     */
    private String renderTemplate(Map<String, Object> data) {
        Context context = new Context();
        context.setVariables(data);
        return templateEngine.process("pdf/membership-confirmation", context);
    }

    /**
     * Convert HTML to PDF using OpenHTMLToPDF
     */
    private byte[] convertHtmlToPdf(String html) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(outputStream);
            builder.run();

            return outputStream.toByteArray();
        }
    }

    /**
     * Generate a unique file name for the PDF
     */
    private String generateFileName(Long userId, Long subscriptionId) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return String.format("membership_confirmation_%d_%d_%s.pdf", userId, subscriptionId, timestamp);
    }

    /**
     * Save PDF to file system and return the file path
     */
    private String savePdfToFileSystem(String fileName, byte[] pdfBytes) throws IOException {
        // Create storage directory if it doesn't exist
        Path storageDir = Paths.get(pdfStoragePath);
        if (!Files.exists(storageDir)) {
            Files.createDirectories(storageDir);
        }

        // Save the PDF file
        Path filePath = storageDir.resolve(fileName);
        Files.write(filePath, pdfBytes);

        return filePath.toString();
    }
}
