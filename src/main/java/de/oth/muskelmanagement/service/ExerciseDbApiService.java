package de.oth.muskelmanagement.service;

import de.oth.muskelmanagement.service.dto.ExerciseApiResponseDto;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for interacting with the ExerciseDB API
 */
public interface ExerciseDbApiService {
    
    /**
     * Fetch all exercises from the ExerciseDB API
     * @return List of exercise DTOs
     */
    List<ExerciseApiResponseDto> fetchAllExercises();
    
    /**
     * Fetch a single exercise by its external ID
     * @param externalId The ExerciseDB API ID
     * @return Optional containing the exercise DTO if found
     */
    Optional<ExerciseApiResponseDto> fetchExerciseById(String externalId);
    
    /**
     * Fetch exercises by body part
     * @param bodyPart The body part to filter by (e.g., "chest", "back", "legs")
     * @return List of exercise DTOs
     */
    List<ExerciseApiResponseDto> fetchExercisesByBodyPart(String bodyPart);
    
    /**
     * Fetch exercises by equipment
     * @param equipment The equipment type (e.g., "barbell", "dumbbell", "body weight")
     * @return List of exercise DTOs
     */
    List<ExerciseApiResponseDto> fetchExercisesByEquipment(String equipment);
    
    /**
     * Fetch exercises by target muscle
     * @param target The target muscle (e.g., "biceps", "triceps", "quads")
     * @return List of exercise DTOs
     */
    List<ExerciseApiResponseDto> fetchExercisesByTarget(String target);
}