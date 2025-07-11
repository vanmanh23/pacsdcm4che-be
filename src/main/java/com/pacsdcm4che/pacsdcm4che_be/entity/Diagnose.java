package com.pacsdcm4che.pacsdcm4che_be.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "diagnose")
public class Diagnose {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String description;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "study_id", referencedColumnName = "id")
    private Study study;

    public Diagnose() {
    }

    public Diagnose(String description, Study study) {
        this.description = description;
        this.study = study;
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

    public Study getStudy() {
        return study;
    }

    public void setStudy(Study study) {
        this.study = study;
    }
}
