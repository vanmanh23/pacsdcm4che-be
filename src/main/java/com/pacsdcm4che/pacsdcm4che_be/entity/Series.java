package com.pacsdcm4che.pacsdcm4che_be.entity;

import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "series")
public class Series {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "series_instance_uid", unique = true, nullable = false)
    private String seriesInstanceUID;
    @Column(name = "series_description")
    private String seriesDescription;
    @Column(name = "modality")
    private String modality;
    @Column(name = "number_of_instances")
    private Integer numberOfInstances;
    @Column(name = "series_number")
    private String seriesNumber;
    @Column(name = "series_modified_date_time")
    private String seriesModifiedDateTime;
    @Column(name = "device_serial_number")
    private String deviceSerialNumber;
    @Column(name = "series_date")
    private Date seriesDate;

    public Series() {
    }
    public Series(String seriesInstanceUID, String seriesDescription, String modality, Integer numberOfInstances, String seriesModifiedDateTime, String seriesNumber, String deviceSerialNumber, Date seriesDate) {
        this.seriesInstanceUID = seriesInstanceUID;
        this.seriesDescription = seriesDescription;
        this.modality = modality;
        this.numberOfInstances = numberOfInstances;
        this.seriesModifiedDateTime = seriesModifiedDateTime;
        this.seriesNumber = seriesNumber;
        this.deviceSerialNumber = deviceSerialNumber;
        this.seriesDate = seriesDate;
    }

    public String getSeriesInstanceUID() {
        return seriesInstanceUID;
    }

    public void setSeriesInstanceUID(String seriesInstanceUID) {
        this.seriesInstanceUID = seriesInstanceUID;
    }

    public String getSeriesDescription() {
        return seriesDescription;
    }

    public void setSeriesDescription(String seriesDescription) {
        this.seriesDescription = seriesDescription;
    }

    public String getModality() {
        return modality;
    }

    public void setModality(String modality) {
        this.modality = modality;
    }

    public Integer getNumberOfInstances() {
        return numberOfInstances;
    }

    public void setNumberOfInstances(Integer numberOfInstances) {
        this.numberOfInstances = numberOfInstances;
    }

    public String getSeriesNumber() {
        return seriesNumber;
    }

    public void setSeriesNumber(String seriesNumber) {
        this.seriesNumber = seriesNumber;
    }

    public String getDeviceSerialNumber() {
        return deviceSerialNumber;
    }

    public void setDeviceSerialNumber(String deviceSerialNumber) {
        this.deviceSerialNumber = deviceSerialNumber;
    }

    public Date getSeriesDate() {
        return seriesDate;
    }

    public void setSeriesDate(Date seriesDate) {
        this.seriesDate = seriesDate;
    }

    public String getSeriesModifiedDateTime() {
        return seriesModifiedDateTime;
    }

    public void setSeriesModifiedDateTime(String seriesModifiedDateTime) {
        this.seriesModifiedDateTime = seriesModifiedDateTime;
    }
}
