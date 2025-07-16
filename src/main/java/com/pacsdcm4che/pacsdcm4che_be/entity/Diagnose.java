package com.pacsdcm4che.pacsdcm4che_be.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "diagnose")
public class Diagnose {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "description")
    private String description;

    @Column(name = "study_id", nullable = false)
    private String studyId;

    public Diagnose() {
    }

    public Diagnose(String description, String studyId) {
        this.description = description;
        this.studyId = studyId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
}
