package de.oth.muskelmanagement.service;

import de.oth.muskelmanagement.model.entity.MembershipConfirmation;

import java.io.IOException;
import java.util.List;

public interface PdfGenerationService {
    
    /**
     * Generate a membership confirmation PDF for the given user and subscription
     * 
     * @param userId The ID of the user
     * @param subscriptionId The ID of the subscription
     * @param adminEmail The email of the admin generating the PDF
     * @return The generated MembershipConfirmation entity
     */
    MembershipConfirmation generateMembershipConfirmation(Long userId, Long subscriptionId, String adminEmail);
    
    /**
     * Get all non-deleted membership confirmations
     * 
     * @return List of all non-deleted confirmations
     */
    List<MembershipConfirmation> getAllConfirmations();
    
    /**
     * Get all non-deleted membership confirmations for a specific user
     * 
     * @param userId The user ID
     * @return List of confirmations for the user
     */
    List<MembershipConfirmation> getConfirmationsByUser(Long userId);
    
    /**
     * Get a specific membership confirmation by ID
     * 
     * @param id The confirmation ID
     * @return The MembershipConfirmation entity
     */
    MembershipConfirmation getConfirmationById(Long id);
    
    /**
     * Get the PDF file content as byte array
     * 
     * @param confirmationId The confirmation ID
     * @return The PDF content as byte array
     * @throws IOException If the file cannot be read
     */
    byte[] getPdfContent(Long confirmationId) throws IOException;
    
    /**
     * Soft delete a membership confirmation
     * 
     * @param id The confirmation ID
     * @param adminEmail The email of the admin deleting the confirmation
     */
    void deleteConfirmation(Long id, String adminEmail);
}