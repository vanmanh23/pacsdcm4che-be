package com.pacsdcm4che.pacsdcm4che_be.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import lombok.Getter;

public class Patient {
    @Column(name = "patient_id")
//    @JsonProperty("00100020")
    private String patientID;

//    @JsonProperty("00100010")
    @Column(name = "patient_name")
    private String patientName;

//    @JsonProperty("00100021")
    @Column(name = "issuer_of_patient_id")
    private String issuerOfPatientID;

//    @JsonProperty("00101010")
    @Column(name = "patient_age")
    private String patientAge;

    // Getters and Setters
//    public String getPatientID() {
//        return patientID != null && patientID.getValue() != null && !patientID.getValue().isEmpty() ? patientID.getValue().get(0) : null;
//    }
//
//    public void setPatientID(DicomAttribute patientID) {
//        this.patientID = patientID;
//    }
//
//    public String getPatientName() {
//        return patientName != null && patientName.getValue() != null && !patientName.getValue().isEmpty()
//                ? patientName.getValue().get(0).getAlphabetic() : null;
//    }


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