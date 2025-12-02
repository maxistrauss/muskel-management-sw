package de.oth.muskelmanagement.service;

import de.oth.muskelmanagement.service.dto.ExerciseApiResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of ExerciseDbApiService using WebClient for HTTP communication
 */
@Service
public class ExerciseDbApiServiceImpl implements ExerciseDbApiService {

    private static final Logger log = LoggerFactory.getLogger(ExerciseDbApiServiceImpl.class);

    private final WebClient webClient;
    private final int maxRetries;
    private final Duration timeout;
    private final String apiKey;

    public ExerciseDbApiServiceImpl(
            @Value("${exercisedb.api.base-url}") String baseUrl,
            @Value("${exercisedb.api.key:}") String apiKey,
            @Value("${exercisedb.api.timeout-seconds:10}") int timeoutSeconds,
            @Value("${exercisedb.api.max-retries:3}") int maxRetries) {
        
        this.maxRetries = maxRetries;
        this.timeout = Duration.ofSeconds(timeoutSeconds);
        this.apiKey = apiKey;
        
        WebClient.Builder builder = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Accept", "application/json");
        
        // Add API key headers if provided (for RapidAPI)
        if (apiKey != null && !apiKey.isBlank()) {
            builder.defaultHeader("X-RapidAPI-Key", apiKey)
                   .defaultHeader("X-RapidAPI-Host", "exercisedb.p.rapidapi.com");
            log.info("ExerciseDbApiService initialized with base URL: {} (with API key)", baseUrl);
        } else {
            log.warn("ExerciseDbApiService initialized without API key. API calls may fail.");
            log.warn("Get a free API key from: https://rapidapi.com/justin-WFnsXH_t6/api/exercisedb");
        }
        
        this.webClient = builder.build();
    }

    @Override
    public List<ExerciseApiResponseDto> fetchAllExercises() {
        log.info("Fetching all exercises from ExerciseDB API");
        
        try {
            List<ExerciseApiResponseDto> exercises = webClient.get()
                    .uri("/exercises")
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<ExerciseApiResponseDto>>() {})
                    .timeout(timeout)
                    .retryWhen(Retry.backoff(maxRetries, Duration.ofSeconds(1))
                            .doBeforeRetry(retrySignal -> 
                                log.warn("Retrying fetchAllExercises, attempt: {}", retrySignal.totalRetries() + 1)))
                    .onErrorResume(this::handleError)
                    .block();
            
            log.info("Successfully fetched {} exercises", exercises != null ? exercises.size() : 0);
            return exercises != null ? exercises : new ArrayList<>();
            
        } catch (Exception e) {
            log.error("Error fetching all exercises: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public Optional<ExerciseApiResponseDto> fetchExerciseById(String externalId) {
        log.info("Fetching exercise with ID: {}", externalId);
        
        try {
            ExerciseApiResponseDto exercise = webClient.get()
                    .uri("/exercises/exercise/{id}", externalId)
                    .retrieve()
                    .bodyToMono(ExerciseApiResponseDto.class)
                    .timeout(timeout)
                    .retryWhen(Retry.backoff(maxRetries, Duration.ofSeconds(1))
                            .doBeforeRetry(retrySignal ->
                                log.warn("Retrying fetchExerciseById for ID: {}, attempt: {}",
                                    externalId, retrySignal.totalRetries() + 1)))
                    .onErrorResume(error -> {
                        if (error instanceof WebClientResponseException.NotFound) {
                            log.warn("Exercise with ID {} not found", externalId);
                            return Mono.empty();
                        }
                        return handleError(error);
                    })
                    .block();
            
            if (exercise != null) {
                log.info("Successfully fetched exercise: {}", exercise.getName());
            }
            return Optional.ofNullable(exercise);
            
        } catch (Exception e) {
            log.error("Error fetching exercise with ID {}: {}", externalId, e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public List<ExerciseApiResponseDto> fetchExercisesByBodyPart(String bodyPart) {
        log.info("Fetching exercises for body part: {}", bodyPart);
        
        try {
            List<ExerciseApiResponseDto> exercises = webClient.get()
                    .uri("/exercises/bodyPart/{bodyPart}", bodyPart)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<ExerciseApiResponseDto>>() {})
                    .timeout(timeout)
                    .retryWhen(Retry.backoff(maxRetries, Duration.ofSeconds(1))
                            .doBeforeRetry(retrySignal -> 
                                log.warn("Retrying fetchExercisesByBodyPart for {}, attempt: {}", 
                                    bodyPart, retrySignal.totalRetries() + 1)))
                    .onErrorResume(this::handleError)
                    .block();
            
            log.info("Successfully fetched {} exercises for body part: {}", 
                    exercises != null ? exercises.size() : 0, bodyPart);
            return exercises != null ? exercises : new ArrayList<>();
            
        } catch (Exception e) {
            log.error("Error fetching exercises for body part {}: {}", bodyPart, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<ExerciseApiResponseDto> fetchExercisesByEquipment(String equipment) {
        log.info("Fetching exercises for equipment: {}", equipment);
        
        try {
            List<ExerciseApiResponseDto> exercises = webClient.get()
                    .uri("/exercises/equipment/{equipment}", equipment)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<ExerciseApiResponseDto>>() {})
                    .timeout(timeout)
                    .retryWhen(Retry.backoff(maxRetries, Duration.ofSeconds(1))
                            .doBeforeRetry(retrySignal -> 
                                log.warn("Retrying fetchExercisesByEquipment for {}, attempt: {}", 
                                    equipment, retrySignal.totalRetries() + 1)))
                    .onErrorResume(this::handleError)
                    .block();
            
            log.info("Successfully fetched {} exercises for equipment: {}", 
                    exercises != null ? exercises.size() : 0, equipment);
            return exercises != null ? exercises : new ArrayList<>();
            
        } catch (Exception e) {
            log.error("Error fetching exercises for equipment {}: {}", equipment, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<ExerciseApiResponseDto> fetchExercisesByTarget(String target) {
        log.info("Fetching exercises for target muscle: {}", target);
        
        try {
            List<ExerciseApiResponseDto> exercises = webClient.get()
                    .uri("/exercises/target/{target}", target)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<ExerciseApiResponseDto>>() {})
                    .timeout(timeout)
                    .retryWhen(Retry.backoff(maxRetries, Duration.ofSeconds(1))
                            .doBeforeRetry(retrySignal -> 
                                log.warn("Retrying fetchExercisesByTarget for {}, attempt: {}", 
                                    target, retrySignal.totalRetries() + 1)))
                    .onErrorResume(this::handleError)
                    .block();
            
            log.info("Successfully fetched {} exercises for target muscle: {}", 
                    exercises != null ? exercises.size() : 0, target);
            return exercises != null ? exercises : new ArrayList<>();
            
        } catch (Exception e) {
            log.error("Error fetching exercises for target {}: {}", target, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Generic error handler for WebClient errors
     */
    private <T> Mono<T> handleError(Throwable error) {
        if (error instanceof WebClientResponseException) {
            WebClientResponseException webClientException = (WebClientResponseException) error;
            log.error("HTTP error {}: {}", 
                    webClientException.getStatusCode(), 
                    webClientException.getResponseBodyAsString());
        } else {
            log.error("Error during API call: {}", error.getMessage(), error);
        }
        return Mono.empty();
    }
}