package com.pacsdcm4che.pacsdcm4che_be.entity;

import jakarta.persistence.*;
import org.springframework.boot.autoconfigure.web.WebProperties;

import java.util.Date;

@Entity
@Table(name = "studies")
public class Study {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "study_instance_uid", unique = true, nullable = false)
    private String studyInstanceUID;

    @Column(name = "study_id")
    private String studyID;

    @Column(name = "study_date")
    private Date studyDate;

    @Column(name = "study_time")
    private Date studyTime;

    @Column(name = "accession_number")
    private String accessionNumber;

    @Column(name = "study_description")
    private String studyDescription;

    @Column(name = "referring_physician_name")
    private String referringPhysicianName;

    @Column(name = "modality")
    private String modality;

    @Column(name = "number_of_series")
    private Integer numberOfSeries;

    @Column(name = "number_of_instances")
    private Integer numberOfInstances;

    @Column(name = "patient_id")
    private String patientID;
    @OneToOne(mappedBy = "study")
    private Diagnose diagnose;
    public Study() {
    }

    public Study(Long id, String studyInstanceUID, String patientID, String studyID, Date studyDate, Date studyTime, String accessionNumber, String studyDescription, String referringPhysicianName, String modality, Integer numberOfSeries, Integer numberOfInstances) {
        this.id = id;
        this.studyInstanceUID = studyInstanceUID;
        this.studyID = studyID;
        this.studyDate = studyDate;
        this.studyTime = studyTime;
        this.accessionNumber = accessionNumber;
        this.studyDescription = studyDescription;
        this.referringPhysicianName = referringPhysicianName;
        this.modality = modality;
        this.numberOfSeries = numberOfSeries;
        this.numberOfInstances = numberOfInstances;
        this.patientID = patientID;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStudyInstanceUID() {
        return studyInstanceUID;
    }

    public void setStudyInstanceUID(String studyInstanceUID) {
        this.studyInstanceUID = studyInstanceUID;
    }

    public String getStudyID() {
        return studyID;
    }

    public void setStudyID(String studyID) {
        this.studyID = studyID;
    }

    public Date getStudyDate() {
        return studyDate;
    }

    public void setStudyDate(Date studyDate) {
        this.studyDate = studyDate;
    }

    public Date getStudyTime() {
        return studyTime;
    }

    public void setStudyTime(Date studyTime) {
        this.studyTime = studyTime;
    }

    public String getAccessionNumber() {
        return accessionNumber;
    }

    public void setAccessionNumber(String accessionNumber) {
        this.accessionNumber = accessionNumber;
    }

    public String getStudyDescription() {
        return studyDescription;
    }

    public void setStudyDescription(String studyDescription) {
        this.studyDescription = studyDescription;
    }

    public String getReferringPhysicianName() {
        return referringPhysicianName;
    }

    public void setReferringPhysicianName(String referringPhysicianName) {
        this.referringPhysicianName = referringPhysicianName;
    }

    public String getModality() {
        return modality;
    }

    public void setModality(String modality) {
        this.modality = modality;
    }

    public Integer getNumberOfSeries() {
        return numberOfSeries;
    }

    public void setNumberOfSeries(Integer numberOfSeries) {
        this.numberOfSeries = numberOfSeries;
    }

    public Integer getNumberOfInstances() {
        return numberOfInstances;
    }

    public void setNumberOfInstances(Integer numberOfInstances) {
        this.numberOfInstances = numberOfInstances;
    }

    public String getPatientID() {
        return patientID;
    }

    public void setPatientID(String patientID) {
        this.patientID = patientID;
    }

    public Diagnose getDiagnose() {
        return diagnose;
    }

    public void setDiagnose(Diagnose diagnose) {
        this.diagnose = diagnose;
    }
}
