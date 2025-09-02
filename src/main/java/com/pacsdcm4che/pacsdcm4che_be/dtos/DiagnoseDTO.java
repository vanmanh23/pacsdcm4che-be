package com.pacsdcm4che.pacsdcm4che_be.dtos;
import lombok.Data;

@Data
public class DiagnoseDTO {
<<<<<<< HEAD
    private Long id;
=======
>>>>>>> df72f9ae2dffacf61b7b12492d1966965f0b9850
    private String description;

    private String studyId;

    public DiagnoseDTO() {
    }

<<<<<<< HEAD
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
<<<<<<< HEAD
=======
    public DiagnoseDTO(String description, String studyId) {
        this.description = description;
        this.studyId = studyId;
    }
>>>>>>> df72f9ae2dffacf61b7b12492d1966965f0b9850
=======

>>>>>>> dev
}
