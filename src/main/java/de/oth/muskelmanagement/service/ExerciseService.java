package de.oth.muskelmanagement.service;

import de.oth.muskelmanagement.model.Exercise;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for managing exercises
 */
public interface ExerciseService {
    
    // CRUD Operations
    Page<Exercise> findAll(Pageable pageable);
    List<Exercise> findAll();
    Exercise findById(Long id);
    Exercise findByExternalId(String externalId);
    Exercise save(Exercise exercise);
    void deleteById(Long id);
    
    // Search and Filter Operations
    Page<Exercise> searchByName(String name, Pageable pageable);
    List<Exercise> searchByName(String name);
    Page<Exercise> findByBodyPart(String bodyPart, Pageable pageable);
    List<Exercise> findByBodyPart(String bodyPart);
    Page<Exercise> findByEquipment(String equipment, Pageable pageable);
    List<Exercise> findByEquipment(String equipment);
    Page<Exercise> findByTarget(String target, Pageable pageable);
    List<Exercise> findByTarget(String target);
    Page<Exercise> searchExercises(String searchTerm, Pageable pageable);
    
    // Distinct Values for Filters
    List<String> findDistinctBodyParts();
    List<String> findDistinctEquipment();
    List<String> findDistinctTargets();
    
    // Statistics
    long countByBodyPart(String bodyPart);
    long countByEquipment(String equipment);
    long countByTarget(String target);
    long count();
    
    // Data Loading Operations
    /**
     * Load exercises from local JSON file (data/exercises.json)
     * This is much faster than making API calls and is used during application startup.
     * @return Number of exercises loaded
     */
    int loadExercisesFromJson();
    
    // API Sync Operations
    /**
     * Sync all exercises from ExerciseDB API to local database
     * @return Number of exercises synced
     */
    int syncAllExercisesFromApi();
    
    /**
     * Sync a single exercise from ExerciseDB API by its external ID
     * @param externalId The ExerciseDB API ID
     * @return The synced exercise or null if not found
     */
    Exercise syncExerciseById(String externalId);
    
    /**
     * Sync exercises by body part from ExerciseDB API
     * @param bodyPart The body part to sync
     * @return Number of exercises synced
     */
    int syncExercisesByBodyPart(String bodyPart);
    
    /**
     * Sync exercises by equipment from ExerciseDB API
     * @param equipment The equipment type to sync
     * @return Number of exercises synced
     */
    int syncExercisesByEquipment(String equipment);
    
    /**
     * Sync exercises by target muscle from ExerciseDB API
     * @param target The target muscle to sync
     * @return Number of exercises synced
     */
    int syncExercisesByTarget(String target);
}