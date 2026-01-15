package de.oth.muskelmanagement.service;

import de.oth.muskelmanagement.model.entity.MembershipConfirmation;
import de.oth.muskelmanagement.model.entity.Subscription;
import de.oth.muskelmanagement.model.entity.Pricing;
import de.oth.muskelmanagement.model.entity.User;
import de.oth.muskelmanagement.model.enums.SubscriptionStatus;
import de.oth.muskelmanagement.repository.MembershipConfirmationRepository;
import de.oth.muskelmanagement.repository.SubscriptionRepository;
import de.oth.muskelmanagement.repository.UserRepository;
import de.oth.muskelmanagement.service.impl.PdfGenerationServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PdfGenerationServiceImplTest {

    @Mock
    private MembershipConfirmationRepository confirmationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private TemplateEngine templateEngine;

    @Mock
    private EmailService emailService;

    private PdfGenerationServiceImpl pdfGenerationService;

    @TempDir
    Path tempDir;

    private User testUser;
    private Pricing testPricing;
    private Subscription testSubscription;

    @BeforeEach
    void setUp() {
        pdfGenerationService = new PdfGenerationServiceImpl(
                confirmationRepository,
                userRepository,
                subscriptionRepository,
                templateEngine,
                emailService
        );

        // Set the temp directory as storage path
        ReflectionTestUtils.setField(pdfGenerationService, "pdfStoragePath", tempDir.toString());

        // Setup test data
        testUser = new User();
        testUser.setId(1L);
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("john.doe@example.com");

        testPricing = new Pricing();
        testPricing.setId(1L);
        testPricing.setName("Premium");
        testPricing.setPrice(new BigDecimal("49.99"));
        testPricing.setDurationMonths(12);

        testSubscription = new Subscription();
        testSubscription.setId(1L);
        testSubscription.setUser(testUser);
        testSubscription.setPricing(testPricing);
        testSubscription.setStartDate(LocalDate.now());
        testSubscription.setEndDate(LocalDate.now().plusMonths(12));
        testSubscription.setStatus(SubscriptionStatus.ACTIVE);
    }
    @Test
    void generateMembershipConfirmation_Success() {
        // Arrange
        Long userId = 1L;
        Long subscriptionId = 1L;
        String adminEmail = "admin@example.com";

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.of(testSubscription));
        when(templateEngine.process(eq("pdf/membership-confirmation"), any(Context.class)))
                .thenReturn("<html><body>Test PDF</body></html>");

        MembershipConfirmation savedConfirmation = new MembershipConfirmation();
        savedConfirmation.setId(1L);
        when(confirmationRepository.save(any(MembershipConfirmation.class))).thenReturn(savedConfirmation);

        // Act
        MembershipConfirmation result = pdfGenerationService.generateMembershipConfirmation(
                userId, subscriptionId, adminEmail);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());

        verify(userRepository).findById(userId);
        verify(subscriptionRepository).findById(subscriptionId);
        verify(templateEngine).process(eq("pdf/membership-confirmation"), any(Context.class));
        verify(confirmationRepository).save(any(MembershipConfirmation.class));

        // Verify that a file was created
        ArgumentCaptor<MembershipConfirmation> captor = ArgumentCaptor.forClass(MembershipConfirmation.class);
        verify(confirmationRepository).save(captor.capture());
        MembershipConfirmation captured = captor.getValue();
        
        assertEquals(testUser, captured.getUser());
        assertEquals(testSubscription, captured.getSubscription());
        assertEquals(adminEmail, captured.getGeneratedBy());
        assertNotNull(captured.getFileName());
        assertNotNull(captured.getFilePath());
    }

    @Test
    void generateMembershipConfirmation_UserNotFound() {
        // Arrange
        Long userId = 999L;
        Long subscriptionId = 1L;
        String adminEmail = "admin@example.com";

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () ->
                pdfGenerationService.generateMembershipConfirmation(userId, subscriptionId, adminEmail));

        verify(userRepository).findById(userId);
        verify(subscriptionRepository, never()).findById(any());
        verify(confirmationRepository, never()).save(any());
    }

    @Test
    void generateMembershipConfirmation_SubscriptionNotFound() {
        // Arrange
        Long userId = 1L;
        Long subscriptionId = 999L;
        String adminEmail = "admin@example.com";

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () ->
                pdfGenerationService.generateMembershipConfirmation(userId, subscriptionId, adminEmail));

        verify(userRepository).findById(userId);
        verify(subscriptionRepository).findById(subscriptionId);
        verify(confirmationRepository, never()).save(any());
    }

    @Test
    void generateMembershipConfirmation_SubscriptionNotActive() {
        // Arrange
        Long userId = 1L;
        Long subscriptionId = 1L;
        String adminEmail = "admin@example.com";

        testSubscription.setStatus(SubscriptionStatus.EXPIRED);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.of(testSubscription));

        // Act & Assert
        assertThrows(IllegalStateException.class, () ->
                pdfGenerationService.generateMembershipConfirmation(userId, subscriptionId, adminEmail));

        verify(confirmationRepository, never()).save(any());
    }

    @Test
    void generateMembershipConfirmation_SubscriptionDoesNotBelongToUser() {
        // Arrange
        Long userId = 1L;
        Long subscriptionId = 1L;
        String adminEmail = "admin@example.com";

        User differentUser = new User();
        differentUser.setId(2L);
        differentUser.setFirstName("Jane");
        differentUser.setLastName("Smith");

        testSubscription.setUser(differentUser);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.of(testSubscription));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                pdfGenerationService.generateMembershipConfirmation(userId, subscriptionId, adminEmail));

        verify(confirmationRepository, never()).save(any());
    }

    @Test
    void getAllConfirmations_Success() {
        // Arrange
        MembershipConfirmation conf1 = new MembershipConfirmation();
        conf1.setId(1L);
        MembershipConfirmation conf2 = new MembershipConfirmation();
        conf2.setId(2L);
        
        List<MembershipConfirmation> confirmations = Arrays.asList(conf1, conf2);
        when(confirmationRepository.findByDeletedFalse()).thenReturn(confirmations);

        // Act
        List<MembershipConfirmation> result = pdfGenerationService.getAllConfirmations();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(confirmationRepository).findByDeletedFalse();
    }

    @Test
    void getConfirmationsByUser_Success() {
        // Arrange
        Long userId = 1L;
        MembershipConfirmation conf1 = new MembershipConfirmation();
        conf1.setId(1L);
        
        List<MembershipConfirmation> confirmations = Arrays.asList(conf1);
        when(confirmationRepository.findByUser_IdAndDeletedFalse(userId)).thenReturn(confirmations);

        // Act
        List<MembershipConfirmation> result = pdfGenerationService.getConfirmationsByUser(userId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(confirmationRepository).findByUser_IdAndDeletedFalse(userId);
    }

    @Test
    void getConfirmationById_Success() {
        // Arrange
        Long confirmationId = 1L;
        MembershipConfirmation confirmation = new MembershipConfirmation();
        confirmation.setId(confirmationId);
        
        when(confirmationRepository.findById(confirmationId)).thenReturn(Optional.of(confirmation));

        // Act
        MembershipConfirmation result = pdfGenerationService.getConfirmationById(confirmationId);

        // Assert
        assertNotNull(result);
        assertEquals(confirmationId, result.getId());
        verify(confirmationRepository).findById(confirmationId);
    }

    @Test
    void getConfirmationById_NotFound() {
        // Arrange
        Long confirmationId = 999L;
        when(confirmationRepository.findById(confirmationId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () ->
                pdfGenerationService.getConfirmationById(confirmationId));

        verify(confirmationRepository).findById(confirmationId);
    }

    @Test
    void deleteConfirmation_Success() {
        // Arrange
        Long confirmationId = 1L;
        String adminEmail = "admin@example.com";
        
        MembershipConfirmation confirmation = new MembershipConfirmation();
        confirmation.setId(confirmationId);
        confirmation.setDeleted(false);
        
        when(confirmationRepository.findById(confirmationId)).thenReturn(Optional.of(confirmation));
        when(confirmationRepository.save(any(MembershipConfirmation.class))).thenReturn(confirmation);

        // Act
        pdfGenerationService.deleteConfirmation(confirmationId, adminEmail);

        // Assert
        ArgumentCaptor<MembershipConfirmation> captor = ArgumentCaptor.forClass(MembershipConfirmation.class);
        verify(confirmationRepository).save(captor.capture());
        MembershipConfirmation deleted = captor.getValue();
        
        assertTrue(deleted.getDeleted());
        assertEquals(adminEmail, deleted.getDeletedBy());
        assertNotNull(deleted.getDeletedAt());
    }

    @Test
    void getPdfContent_Success() throws IOException {
        // Arrange
        Long confirmationId = 1L;
        byte[] testContent = "PDF Content".getBytes();
        Path testFile = tempDir.resolve("test.pdf");
        Files.write(testFile, testContent);
        
        MembershipConfirmation confirmation = new MembershipConfirmation();
        confirmation.setId(confirmationId);
        confirmation.setFilePath(testFile.toString());
        confirmation.setDeleted(false);
        
        when(confirmationRepository.findById(confirmationId)).thenReturn(Optional.of(confirmation));

        // Act
        byte[] result = pdfGenerationService.getPdfContent(confirmationId);

        // Assert
        assertNotNull(result);
        assertArrayEquals(testContent, result);
    }

    @Test
    void getPdfContent_FileNotFound() {
        // Arrange
        Long confirmationId = 1L;
        
        MembershipConfirmation confirmation = new MembershipConfirmation();
        confirmation.setId(confirmationId);
        confirmation.setFilePath(tempDir.resolve("nonexistent.pdf").toString());
        confirmation.setDeleted(false);
        
        when(confirmationRepository.findById(confirmationId)).thenReturn(Optional.of(confirmation));

        // Act & Assert
        assertThrows(IOException.class, () ->
                pdfGenerationService.getPdfContent(confirmationId));
    }

    @Test
    void getPdfContent_DeletedConfirmation() {
        // Arrange
        Long confirmationId = 1L;
        
        MembershipConfirmation confirmation = new MembershipConfirmation();
        confirmation.setId(confirmationId);
        confirmation.setDeleted(true);
        
        when(confirmationRepository.findById(confirmationId)).thenReturn(Optional.of(confirmation));

        // Act & Assert
        assertThrows(IllegalStateException.class, () ->
                pdfGenerationService.getPdfContent(confirmationId));
    }
}

