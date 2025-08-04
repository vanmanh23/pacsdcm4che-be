package com.pacsdcm4che.pacsdcm4che_be.dtos;
import lombok.Data;

@Data
public class DiagnoseDTO {
    private Long id;
    private String description;

    private String studyId;

    public DiagnoseDTO() {
    }

    public DiagnoseDTO(Long id, String description, String studyId) {
        this.id = id;
        this.description = description;
        this.studyId = studyId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStudyId() {
        return studyId;
    }

    public void setStudyId(String studyId) {
        this.studyId = studyId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
