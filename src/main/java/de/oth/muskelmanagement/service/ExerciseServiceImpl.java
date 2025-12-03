package de.oth.muskelmanagement.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.oth.muskelmanagement.model.Exercise;
import de.oth.muskelmanagement.repository.ExerciseRepository;
import de.oth.muskelmanagement.service.dto.ExerciseApiResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementation of ExerciseService
 */
@Service
public class ExerciseServiceImpl implements ExerciseService {

    private static final Logger log = LoggerFactory.getLogger(ExerciseServiceImpl.class);

    private final ExerciseRepository exerciseRepository;
    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper;
    private final String imageBaseUrl;
    private final int imageResolution;

    public ExerciseServiceImpl(ExerciseRepository exerciseRepository,
                               ResourceLoader resourceLoader,
                               ObjectMapper objectMapper,
                               @Value("${exercisedb.api.image-base-url:https://exercisedb.p.rapidapi.com/image}") String imageBaseUrl,
                               @Value("${exercisedb.api.image-resolution:720}") int imageResolution) {
        this.exerciseRepository = exerciseRepository;
        this.resourceLoader = resourceLoader;
        this.objectMapper = objectMapper;
        this.imageBaseUrl = imageBaseUrl;
        this.imageResolution = imageResolution;
    }

    @Override
    public Page<Exercise> findAll(Pageable pageable) {
        return exerciseRepository.findAll(pageable);
    }

    @Override
    public List<Exercise> findAll() {
        return exerciseRepository.findAll();
    }

    @Override
    public Exercise findById(Long id) {
        return exerciseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exercise not found with id: " + id));
    }

    @Override
    public Exercise findByExternalId(String externalId) {
        return exerciseRepository.findByExternalId(externalId)
                .orElseThrow(() -> new RuntimeException("Exercise not found with external id: " + externalId));
    }

    @Override
    @Transactional
    public Exercise save(Exercise exercise) {
        return exerciseRepository.save(exercise);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        exerciseRepository.deleteById(id);
    }

    @Override
    public Page<Exercise> searchByName(String name, Pageable pageable) {
        return exerciseRepository.findByNameContainingIgnoreCase(name, pageable);
    }

    @Override
    public List<Exercise> searchByName(String name) {
        return exerciseRepository.findByNameContainingIgnoreCase(name);
    }

    @Override
    public Page<Exercise> findByBodyPart(String bodyPart, Pageable pageable) {
        return exerciseRepository.findByBodyPart(bodyPart, pageable);
    }

    @Override
    public List<Exercise> findByBodyPart(String bodyPart) {
        return exerciseRepository.findByBodyPart(bodyPart);
    }

    @Override
    public Page<Exercise> findByEquipment(String equipment, Pageable pageable) {
        return exerciseRepository.findByEquipment(equipment, pageable);
    }

    @Override
    public List<Exercise> findByEquipment(String equipment) {
        return exerciseRepository.findByEquipment(equipment);
    }

    @Override
    public Page<Exercise> findByTarget(String target, Pageable pageable) {
        return exerciseRepository.findByTarget(target, pageable);
    }

    @Override
    public List<Exercise> findByTarget(String target) {
        return exerciseRepository.findByTarget(target);
    }

    @Override
    public Page<Exercise> searchExercises(String searchTerm, Pageable pageable) {
        return exerciseRepository.searchExercises(searchTerm, pageable);
    }

    @Override
    public List<String> findDistinctBodyParts() {
        return exerciseRepository.findDistinctBodyParts();
    }

    @Override
    public List<String> findDistinctEquipment() {
        return exerciseRepository.findDistinctEquipment();
    }

    @Override
    public List<String> findDistinctTargets() {
        return exerciseRepository.findDistinctTargets();
    }

    @Override
    public long countByBodyPart(String bodyPart) {
        return exerciseRepository.countByBodyPart(bodyPart);
    }

    @Override
    public long countByEquipment(String equipment) {
        return exerciseRepository.countByEquipment(equipment);
    }

    @Override
    public long countByTarget(String target) {
        return exerciseRepository.countByTarget(target);
    }

    @Override
    public long count() {
        return exerciseRepository.count();
    }






    /**
     * Load exercises from local JSON file (data/exercises.json)
     * This is much faster than making API calls during application startup.
     *
     * @return Number of exercises loaded from JSON
     */
    @Transactional
    public int loadExercisesFromJson() {
        log.info("Loading exercises from local JSON file: data/exercises.json");
        
        try {
            Resource resource = resourceLoader.getResource("file:data/exercises.json");
            
            if (!resource.exists()) {
                log.warn("JSON file not found at: data/exercises.json");
                log.warn("Please ensure the exercises.json file exists in the data directory");
                return 0;
            }
            
            try (InputStream inputStream = resource.getInputStream()) {
                // Configure ObjectMapper to skip empty strings
                ObjectMapper jsonMapper = objectMapper.copy();
                jsonMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
                
                List<ExerciseApiResponseDto> exercises = jsonMapper.readValue(
                    inputStream,
                    new TypeReference<List<ExerciseApiResponseDto>>() {}
                );
                
                log.info("Successfully parsed {} exercises from JSON file", exercises.size());
                
                int syncedCount = 0;
                int skippedCount = 0;
                
                for (ExerciseApiResponseDto dto : exercises) {
                    // Skip empty entries (the JSON has some empty string entries)
                    if (dto == null || dto.getId() == null || dto.getId().isBlank()) {
                        skippedCount++;
                        continue;
                    }
                    
                    try {
                        syncExerciseFromDto(dto);
                        syncedCount++;
                        
                        if (syncedCount % 10 == 0) {
                            log.debug("Loaded {} exercises so far...", syncedCount);
                        }
                    } catch (Exception e) {
                        log.error("Error loading exercise {}: {}", dto.getId(), e.getMessage());
                        skippedCount++;
                    }
                }
                
                log.info("Successfully loaded {} exercises from JSON ({} skipped/invalid)",
                    syncedCount, skippedCount);
                return syncedCount;
                
            } catch (IOException e) {
                log.error("Error reading JSON file: {}", e.getMessage());
                throw new RuntimeException("Failed to load exercises from JSON file", e);
            }
            
        } catch (Exception e) {
            log.error("Error loading exercises from JSON: {}", e.getMessage());
            throw new RuntimeException("Failed to load exercises from JSON", e);
        }
    }

    /**
     * Helper method to convert API DTO to Exercise entity and save/update
     */
    private Exercise syncExerciseFromDto(ExerciseApiResponseDto dto) {
        // Validate required fields
        if (dto.getId() == null || dto.getId().isBlank()) {
            log.warn("Skipping exercise with null/empty ID");
            return null;
        }
        if (dto.getName() == null || dto.getName().isBlank()) {
            log.warn("Skipping exercise {} with null/empty name", dto.getId());
            return null;
        }
        
        Exercise exercise = exerciseRepository.findByExternalId(dto.getId())
                .orElse(new Exercise());
        
        // Use local proxy endpoint for images (browsers can't send API auth headers)
        String gifUrl = String.format("/exercise-image?exerciseId=%s&resolution=%d",
            dto.getId(), imageResolution);
        
        exercise.setExternalId(dto.getId());
        exercise.setName(dto.getName());
        exercise.setGifUrl(gifUrl);
        exercise.setBodyPart(dto.getBodyPart());
        exercise.setEquipment(dto.getEquipment());
        exercise.setTarget(dto.getTarget());
        exercise.setSecondaryMuscles(dto.getSecondaryMuscles() != null ? dto.getSecondaryMuscles() : new java.util.ArrayList<>());
        exercise.setInstructions(dto.getInstructions() != null ? dto.getInstructions() : new java.util.ArrayList<>());
        exercise.setLastSyncedAt(LocalDateTime.now());
        
        try {
            Exercise saved = exerciseRepository.save(exercise);
            log.debug("Synced exercise: {} (ID: {}) with GIF URL: {}", saved.getName(), saved.getExternalId(), gifUrl);
            return saved;
        } catch (Exception e) {
            log.error("Failed to save exercise {} ({}): {}", dto.getId(), dto.getName(), e.getMessage());
            return null;
        }
    }
}