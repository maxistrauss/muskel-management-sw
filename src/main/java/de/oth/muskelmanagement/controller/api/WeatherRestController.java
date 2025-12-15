package de.oth.muskelmanagement.controller.api;

import de.oth.muskelmanagement.dto.WeatherResponseDto;
import de.oth.muskelmanagement.service.WeatherService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/weather")
public class WeatherRestController {

    private final WeatherService weatherService;

    public WeatherRestController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping
    public ResponseEntity<WeatherResponseDto> getWeather(@RequestParam(defaultValue = "Regensburg") String location) {
        return ResponseEntity.ok(weatherService.getCurrentWeather(location));
    }
}
