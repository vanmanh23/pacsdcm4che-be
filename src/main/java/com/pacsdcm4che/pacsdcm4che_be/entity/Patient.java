package com.pacsdcm4che.pacsdcm4che_be.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import lombok.Getter;

import java.util.Date;

public class Patient {
    @Column(name = "patient_id")
    private String patientID;
    @Column(name = "patient_name")
    private String patientName;
    @Column(name = "sex")
    private String sex;
    @Column(name = "patient_birthdate")
    private Date patientBirthDate;

    public String getPatientID() {
        return patientID;
    }

    public void setPatientID(String patientID) {
        this.patientID = patientID;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public Date getPatientBirthDate() {
        return patientBirthDate;
    }

    public void setPatientBirthDate(Date patientBirthDate) {
        this.patientBirthDate = patientBirthDate;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    @Override
    public String toString() {
        return "Patient{" +
                "patientID=" + patientID +
                ", patientName=" + patientName +
                ", sex=" + sex +
                ", patientBirthDate=" + patientBirthDate +
                '}';
    }
}