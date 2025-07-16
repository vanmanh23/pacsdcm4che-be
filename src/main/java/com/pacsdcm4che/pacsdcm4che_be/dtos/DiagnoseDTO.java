package com.pacsdcm4che.pacsdcm4che_be.dtos;
import lombok.Data;

@Data
public class DiagnoseDTO {
    private String description;

    private String studyId;

    public DiagnoseDTO() {
    }

    public DiagnoseDTO(String description, String studyId) {
        this.description = description;
        this.studyId = studyId;
    }
}
