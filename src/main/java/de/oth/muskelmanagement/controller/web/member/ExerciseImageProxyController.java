package de.oth.muskelmanagement.controller.web.member;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Controller to proxy exercise GIF images from ExerciseDB API This is necessary because the API requires authentication
 * headers that browsers can't send
 */
@RestController
public class ExerciseImageProxyController {

    private static final Logger log = LoggerFactory.getLogger(ExerciseImageProxyController.class);

    private final WebClient webClient;
    private final String apiKey;

    public ExerciseImageProxyController(@Value("${exercisedb.api.key:}") String apiKey) {
        this.apiKey = apiKey;

        this.webClient = WebClient.builder().baseUrl("https://exercisedb.p.rapidapi.com")
                .defaultHeader("Accept", "image/gif").defaultHeader("X-RapidAPI-Key", apiKey)
                .defaultHeader("X-RapidAPI-Host", "exercisedb.p.rapidapi.com")
                .codecs(configurer -> configurer.defaultCodecs()
                        .maxInMemorySize(5 * 1024 * 1024)) // 5MB buffer for GIF images
                .build();
    }

    @GetMapping("/exercise-image")
    public ResponseEntity<byte[]> getExerciseImage(@RequestParam String exerciseId,
            @RequestParam(defaultValue = "720") int resolution) {

        try {
            log.debug("Proxying image for exercise ID: {} with resolution: {}", exerciseId, resolution);

            byte[] imageBytes = webClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/image").queryParam("exerciseId", exerciseId)
                            .queryParam("resolution", resolution).build()).retrieve().bodyToMono(byte[].class)
                    .timeout(Duration.ofSeconds(10)).onErrorResume(error -> {
                        log.error("Error fetching image for exercise {}: {}", exerciseId, error.getMessage());
                        return Mono.empty();
                    }).block();

            if (imageBytes != null && imageBytes.length > 0) {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.IMAGE_GIF);
                headers.setCacheControl("public, max-age=86400"); // Cache for 24 hours
                return ResponseEntity.ok().headers(headers).body(imageBytes);
            } else {
                log.warn("No image data received for exercise ID: {}", exerciseId);
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            log.error("Error proxying image for exercise {}: {}", exerciseId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
