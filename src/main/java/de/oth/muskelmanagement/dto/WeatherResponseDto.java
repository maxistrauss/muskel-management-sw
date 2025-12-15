package de.oth.muskelmanagement.dto;

import java.util.List;

public class WeatherResponseDto {
    private WeatherDto current;
    private List<WeatherDto> hourly; // Next few hours/intervals
    private List<WeatherDto> daily;  // Next few days

    public WeatherResponseDto() {
    }

    public WeatherResponseDto(WeatherDto current, List<WeatherDto> hourly, List<WeatherDto> daily) {
        this.current = current;
        this.hourly = hourly;
        this.daily = daily;
    }

    public WeatherDto getCurrent() {
        return current;
    }

    public void setCurrent(WeatherDto current) {
        this.current = current;
    }

    public List<WeatherDto> getHourly() {
        return hourly;
    }

    public void setHourly(List<WeatherDto> hourly) {
        this.hourly = hourly;
    }

    public List<WeatherDto> getDaily() {
        return daily;
    }

    public void setDaily(List<WeatherDto> daily) {
        this.daily = daily;
    }
}
