package de.oth.muskelmanagement.service.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO for deserializing exercise data from ExerciseDB API (RapidAPI version)
 */
public class ExerciseApiResponseDto {

    private String id;
    
    private String name;
    
    // Support both 'gifUrl' and 'gif_url' field names
    @JsonProperty("gifUrl")
    @JsonAlias({"gif_url", "imageUrl", "image"})
    private String gifUrl;
    
    @JsonProperty("bodyPart")
    @JsonAlias("body_part")
    private String bodyPart;
    
    private String equipment;
    
    private String target;
    
    @JsonProperty("secondaryMuscles")
    @JsonAlias("secondary_muscles")
    private List<String> secondaryMuscles = new ArrayList<>();
    
    private List<String> instructions = new ArrayList<>();

    public ExerciseApiResponseDto() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGifUrl() {
        return gifUrl;
    }

    public void setGifUrl(String gifUrl) {
        this.gifUrl = gifUrl;
    }

    public String getBodyPart() {
        return bodyPart;
    }

    public void setBodyPart(String bodyPart) {
        this.bodyPart = bodyPart;
    }

    public String getEquipment() {
        return equipment;
    }

    public void setEquipment(String equipment) {
        this.equipment = equipment;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public List<String> getSecondaryMuscles() {
        return secondaryMuscles;
    }

    public void setSecondaryMuscles(List<String> secondaryMuscles) {
        this.secondaryMuscles = secondaryMuscles;
    }

    public List<String> getInstructions() {
        return instructions;
    }

    public void setInstructions(List<String> instructions) {
        this.instructions = instructions;
    }
}