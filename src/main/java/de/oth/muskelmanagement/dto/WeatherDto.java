package de.oth.muskelmanagement.dto;

public class WeatherDto {
    private double temperature;
    private double minTemp;
    private double maxTemp;
    private double rainProbability;
    private String description;
    private String icon;
    private String time; // e.g., "14:00"
    private String date; // e.g., "Mon" or "15.12"

    public WeatherDto() {
    }

    public WeatherDto(double temperature, double rainProbability, String description, String icon) {
        this.temperature = temperature;
        this.rainProbability = rainProbability;
        this.description = description;
        this.icon = icon;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getMinTemp() {
        return minTemp;
    }

    public void setMinTemp(double minTemp) {
        this.minTemp = minTemp;
    }

    public double getMaxTemp() {
        return maxTemp;
    }

    public void setMaxTemp(double maxTemp) {
        this.maxTemp = maxTemp;
    }

    public double getRainProbability() {
        return rainProbability;
    }

    public void setRainProbability(double rainProbability) {
        this.rainProbability = rainProbability;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
