package de.oth.muskelmanagement.dto;

import java.util.List;

public class ReportDataDto {
    private List<String> labels;
    private List<Number> data;
    private String label; // Dataset label

    public ReportDataDto() {
    }

    public ReportDataDto(List<String> labels, List<Number> data, String label) {
        this.labels = labels;
        this.data = data;
        this.label = label;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public List<Number> getData() {
        return data;
    }

    public void setData(List<Number> data) {
        this.data = data;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
