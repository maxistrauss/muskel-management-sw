package de.oth.muskelmanagement.service;

import de.oth.muskelmanagement.dto.WeatherResponseDto;

public interface WeatherService {
    WeatherResponseDto getCurrentWeather(String location);
}
