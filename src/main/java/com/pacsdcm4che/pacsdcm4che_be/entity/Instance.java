package com.pacsdcm4che.pacsdcm4che_be.entity;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "instances")
public class Instance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "referenced_sop_instance_uid", unique = true, nullable = false)
    private String referencedSopInstanceUID;
    @Column(name = "instance_number", unique = true, nullable = false)
    private String instanceNumber;
    @Column(name = "sop_classuid")
    private String sopClassUID;
    @Column(name = "sop_instance_uid", unique = true, nullable = false)
    private String sopInstanceUID;
    @Column(name = "study_instance_uid", nullable = false)
    private String studyInstanceUID;
    @Column(name = "series_instance_uid", nullable = false)
    private String seriesInstanceUID;
    @Column(name = "pixel_Data")
    private String pixelData;
    @Column(name = "instance_creation_date")
    private String instanceCreationDate;
    @Column(name = "instance_creation_time")
    private Date instanceCreationTime;

    public Instance() {
    }

    public Instance(String referencedSopInstanceUID, String instanceNumber, String sopClassUID, String sopInstanceUID, String studyInstanceUID, String seriesInstanceUID, String pixelData, String instanceCreationDate, Date instanceCreationTime) {
        this.referencedSopInstanceUID = referencedSopInstanceUID;
        this.instanceNumber = instanceNumber;
        this.sopClassUID = sopClassUID;
        this.sopInstanceUID = sopInstanceUID;
        this.studyInstanceUID = studyInstanceUID;
        this.seriesInstanceUID = seriesInstanceUID;
        this.pixelData = pixelData;
        this.instanceCreationDate = instanceCreationDate;
        this.instanceCreationTime = instanceCreationTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReferencedSopInstanceUID() {
        return referencedSopInstanceUID;
    }

    public void setReferencedSopInstanceUID(String referencedSopInstanceUID) {
        this.referencedSopInstanceUID = referencedSopInstanceUID;
    }

    public String getInstanceNumber() {
        return instanceNumber;
    }

    public void setInstanceNumber(String instanceNumber) {
        this.instanceNumber = instanceNumber;
    }

    public String getSopClassUID() {
        return sopClassUID;
    }

    public void setSopClassUID(String sopClassUID) {
        this.sopClassUID = sopClassUID;
    }

    public String getSopInstanceUID() {
        return sopInstanceUID;
    }

    public void setSopInstanceUID(String sopInstanceUID) {
        this.sopInstanceUID = sopInstanceUID;
    }

    public String getStudyInstanceUID() {
        return studyInstanceUID;
    }

    public void setStudyInstanceUID(String studyInstanceUID) {
        this.studyInstanceUID = studyInstanceUID;
    }

    public String getSeriesInstanceUID() {
        return seriesInstanceUID;
    }

    public void setSeriesInstanceUID(String seriesInstanceUID) {
        this.seriesInstanceUID = seriesInstanceUID;
    }

    public String getPixelData() {
        return pixelData;
    }

    public void setPixelData(String pixelData) {
        this.pixelData = pixelData;
    }

    public String getInstanceCreationDate() {
        return instanceCreationDate;
    }

    public void setInstanceCreationDate(String instanceCreationDate) {
        this.instanceCreationDate = instanceCreationDate;
    }

    public Date getInstanceCreationTime() {
        return instanceCreationTime;
    }

    public void setInstanceCreationTime(Date instanceCreationTime) {
        this.instanceCreationTime = instanceCreationTime;
    }
}
