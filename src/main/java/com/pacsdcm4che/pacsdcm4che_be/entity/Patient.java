package com.pacsdcm4che.pacsdcm4che_be.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import lombok.Getter;

public class Patient {
    @Column(name = "patient_id")
    private String patientID;
    @Column(name = "patient_name")
    private String patientName;
    @Column(name = "issuer_of_patient_id")
    private String issuerOfPatientID;
    @Column(name = "patient_age")
    private String patientAge;

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

    public String getIssuerOfPatientID() {
        return issuerOfPatientID;
    }

    public void setIssuerOfPatientID(String issuerOfPatientID) {
        this.issuerOfPatientID = issuerOfPatientID;
    }

    public String getPatientAge() {
        return patientAge;
    }

    public void setPatientAge(String patientAge) {
        this.patientAge = patientAge;
    }
    @Override
    public String toString() {
        return "Patient{" +
                "patientID=" + patientID +
                ", patientName=" + patientName +
                ", getIssuerOfPatientID=" + issuerOfPatientID +
                ", patientAge=" + patientAge +
                '}';
    }
}