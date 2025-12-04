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
    
}