package de.oth.muskelmanagement.service.impl;

import de.oth.muskelmanagement.dto.WeatherDto;
import de.oth.muskelmanagement.dto.WeatherResponseDto;
import de.oth.muskelmanagement.service.WeatherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class WeatherServiceImpl implements WeatherService {

    private static final Logger log = LoggerFactory.getLogger(WeatherServiceImpl.class);
    private final WebClient webClient;

    public WeatherServiceImpl(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    @Override
    public WeatherResponseDto getCurrentWeather(String location) {
        try {
            // Fixed coordinates for Regensburg as requested
            double lat = 49.0134;
            double lon = 12.1016;

            return getWeatherFromOpenMeteo(lat, lon).block();
        } catch (Exception e) {
            log.error("Error fetching real weather data: {}", e.getMessage());
            return getMockWeather();
        }
    }

    private Mono<WeatherResponseDto> getWeatherFromOpenMeteo(double lat, double lon) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.scheme("https").host("api.open-meteo.com").path("/v1/forecast")
                        .queryParam("latitude", lat).queryParam("longitude", lon)
                        .queryParam("current", "temperature_2m,weather_code")
                        .queryParam("hourly", "temperature_2m,precipitation_probability,weather_code")
                        .queryParam("daily",
                                "weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max")
                        .queryParam("timezone", "Europe/Berlin").build()).retrieve().bodyToMono(Map.class)
                .map(this::mapOpenMeteoResponse);
    }

    private WeatherResponseDto mapOpenMeteoResponse(Map<String, Object> response) {
        // --- Current ---
        Map<String, Object> currentData = (Map<String, Object>) response.get("current");
        WeatherDto currentDto = new WeatherDto();
        if (currentData != null) {
            currentDto.setTemperature(((Number) currentData.get("temperature_2m")).doubleValue());
            int code = ((Number) currentData.get("weather_code")).intValue();
            currentDto.setDescription(getWmoDescription(code));
            currentDto.setIcon(getWmoIcon(code, true)); // Assume day for simplicity or calculate based on time

            // Open-Meteo 'current' doesn't strictly have rain prob, use daily max or 0
            currentDto.setRainProbability(0);
        }

        // --- Hourly ---
        List<WeatherDto> hourlyDtos = new ArrayList<>();
        Map<String, Object> hourlyData = (Map<String, Object>) response.get("hourly");
        if (hourlyData != null) {
            List<String> times = (List<String>) hourlyData.get("time");
            List<Double> temps = ((List<Number>) hourlyData.get("temperature_2m")).stream().map(Number::doubleValue)
                    .toList();
            List<Integer> probs = ((List<Number>) hourlyData.get("precipitation_probability")).stream()
                    .map(Number::intValue).toList();
            List<Integer> codes = ((List<Number>) hourlyData.get("weather_code")).stream().map(Number::intValue)
                    .toList();

            LocalDateTime now = LocalDateTime.now();
            int count = 0;
            for (int i = 0; i < times.size(); i++) {
                LocalDateTime time = LocalDateTime.parse(times.get(i), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                if (time.isAfter(now) || time.isEqual(now)) {
                    // Only take every 3rd hour roughly to match previous logic, or just next 24h
                    // Let's take next 8 entries, but ensure we don't spam. Open-Meteo gives hourly.
                    // We want 3-hour steps.
                    if (count < 8 && (time.getHour() % 3 == 0 || i == 0)) {
                        WeatherDto h = new WeatherDto();
                        h.setTime(time.format(DateTimeFormatter.ofPattern("HH:mm")));
                        h.setTemperature(temps.get(i));
                        h.setRainProbability(probs.get(i));
                        h.setIcon(getWmoIcon(codes.get(i), time.getHour() >= 6 && time.getHour() <= 20));
                        h.setDescription(getWmoDescription(codes.get(i)));
                        hourlyDtos.add(h);
                        count++;
                    }
                }
                if (count >= 8)
                    break;
            }
        }

        // --- Daily ---
        List<WeatherDto> dailyDtos = new ArrayList<>();
        Map<String, Object> dailyData = (Map<String, Object>) response.get("daily");
        if (dailyData != null) {
            List<String> dates = (List<String>) dailyData.get("time");
            List<Double> maxTemps = ((List<Number>) dailyData.get("temperature_2m_max")).stream()
                    .map(Number::doubleValue).toList();
            List<Double> minTemps = ((List<Number>) dailyData.get("temperature_2m_min")).stream()
                    .map(Number::doubleValue).toList();
            List<Integer> codes = ((List<Number>) dailyData.get("weather_code")).stream().map(Number::intValue)
                    .toList();

            for (int i = 0; i < dates.size(); i++) {
                if (i >= 5)
                    break; // Limit to 5 days
                WeatherDto d = new WeatherDto();
                LocalDateTime date = java.time.LocalDate.parse(dates.get(i)).atStartOfDay();
                d.setDate(date.format(DateTimeFormatter.ofPattern("EEE")));
                d.setMaxTemp(maxTemps.get(i));
                d.setMinTemp(minTemps.get(i));
                d.setIcon(getWmoIcon(codes.get(i), true));
                d.setDescription(getWmoDescription(codes.get(i)));
                dailyDtos.add(d);
            }
        }

        return new WeatherResponseDto(currentDto, hourlyDtos, dailyDtos);
    }

    // Helper to map WMO codes (Open-Meteo) to OpenWeatherMap icon names for frontend compatibility
    private String getWmoIcon(int code, boolean isDay) {
        String suffix = isDay ? "d" : "n";
        return switch (code) {
            case 0 -> "01" + suffix; // Clear sky
            case 1, 2, 3 -> "02" + suffix; // Mainly clear, partly cloudy, overcast
            case 45, 48 -> "50" + suffix; // Fog
            case 51, 53, 55, 56, 57 -> "09" + suffix; // Drizzle
            case 61, 63, 65, 66, 67, 80, 81, 82 -> "10" + suffix; // Rain
            case 71, 73, 75, 77, 85, 86 -> "13" + suffix; // Snow
            case 95, 96, 99 -> "11" + suffix; // Thunderstorm
            default -> "02" + suffix;
        };
    }

    private String getWmoDescription(int code) {
        return switch (code) {
            case 0 -> "Clear sky";
            case 1 -> "Mainly clear";
            case 2 -> "Partly cloudy";
            case 3 -> "Overcast";
            case 45 -> "Fog";
            case 48 -> "Depositing rime fog";
            case 51 -> "Light drizzle";
            case 53 -> "Moderate drizzle";
            case 55 -> "Dense drizzle";
            case 61 -> "Slight rain";
            case 63 -> "Moderate rain";
            case 65 -> "Heavy rain";
            case 71 -> "Slight snow fall";
            case 73 -> "Moderate snow fall";
            case 75 -> "Heavy snow fall";
            case 95 -> "Thunderstorm";
            default -> "Weather code " + code;
        };
    }

    private WeatherResponseDto getMockWeather() {
        WeatherDto current = new WeatherDto(20.0, 0.0, "Mock Data (Service Down)", "02d");
        current.setMinTemp(15.0);
        current.setMaxTemp(25.0);

        List<WeatherDto> hourly = new ArrayList<>();
        hourly.add(new WeatherDto(21.0, 0, "Sunny", "01d"));
        hourly.get(0).setTime("12:00");

        List<WeatherDto> daily = new ArrayList<>();
        daily.add(new WeatherDto(22.0, 0, "Sunny", "01d"));
        daily.get(0).setDate("Mon");
        daily.get(0).setMinTemp(15);
        daily.get(0).setMaxTemp(25);

        return new WeatherResponseDto(current, hourly, daily);
    }
}
